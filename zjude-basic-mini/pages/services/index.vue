<template>
  <view class="service-page">
    <UiNavbar
      :back="false"
      title="服务"
    />
    <!-- 主体：左侧分类 + 右侧服务列表 -->
    <view class="browser">
      <scroll-view
        class="menu"
        scroll-y
        :scroll-into-view="menuAnchor"
        :show-scrollbar="false"
        :enhanced="true"
        :bounces="false"
      >
        <view
          v-for="tab in categoryTabs"
          :id="`menu-${tab.value || 'all'}`"
          :key="tab.value || 'all'"
          class="menu-item"
          :class="{ 'menu-item--active': activeCategory === tab.value }"
          @click="selectCategory(tab.value)"
        >
          <text class="menu-label">
            {{ tab.label }}
          </text>
          <text
            v-if="tab.count"
            class="menu-count"
          >
            {{ tab.count }}
          </text>
        </view>
      </scroll-view>

      <scroll-view
        class="results"
        scroll-y
        :show-scrollbar="false"
        :enhanced="true"
        :bounces="false"
      >
        <view class="result-head">
          <view class="result-titles">
            <text class="result-title">
              {{ activeCategoryLabel }}
            </text>
            <text class="result-count">
              {{ filteredServices.length }} 项可预约
            </text>
          </view>
          <view class="sort-switch">
            <text
              v-for="item in sorts"
              :key="item.value"
              class="sort-item"
              :class="{ 'sort-item--active': sortBy === item.value }"
              @click="sortBy = item.value"
            >
              {{ item.label }}
            </text>
          </view>
        </view>

        <view
          v-if="loading"
          class="result-list"
        >
          <view
            v-for="n in 3"
            :key="`sk-${n}`"
            class="result-card card"
          >
            <view class="skeleton result-thumb-skeleton" />
            <view class="result-copy">
              <view class="skeleton skeleton-line" />
              <view class="skeleton skeleton-line skeleton-line--short" />
              <view class="skeleton skeleton-line skeleton-line--tiny" />
            </view>
          </view>
        </view>

        <view
          v-else-if="sortedServices.length"
          class="result-list"
        >
          <view
            v-for="item in sortedServices"
            :key="item.serviceId"
            class="result-card card"
            @click="openDetail(item.serviceId)"
          >
            <view class="result-thumb">
              <image
                v-if="serviceImage(item)"
                :src="assetUrl(serviceImage(item))"
                mode="aspectFit"
              />
              <view
                v-else
                class="result-thumb__fallback"
              >
                <uni-icons
                  type="home"
                  size="22"
                  color="#f9d5c5"
                />
              </view>
              <view
                v-if="Number(item.reserveCount) > 0"
                class="result-badge"
              >
                热
              </view>
            </view>
            <view class="result-copy">
              <view class="result-title-row">
                <text class="result-name">
                  {{ item.serviceName }}
                </text>
                <UiPrice
                  :value="item.price"
                  :unit="item.unitName || '次'"
                  size="sm"
                />
              </view>
              <text class="result-intro">
                {{ item.serviceIntro || '专业服务人员上门，标准流程作业' }}
              </text>
              <view class="result-foot">
                <text class="result-booked">
                  已约 {{ item.reserveCount || 0 }}
                </text>
              </view>
            </view>
          </view>
        </view>

        <view
          v-else
          class="result-empty"
        >
          <uni-icons
            type="list"
            size="28"
            color="#c4bcb6"
          />
          <text class="result-empty__title">
            {{ errorMessage || '该分类暂无服务' }}
          </text>
        </view>
      </scroll-view>
    </view>

    <AppFeedback />
    <AppTabBar current-key="category" />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import { listCategories, listServices } from '@/api/service'
import { useCityStore } from '@/stores/city'
import { assetUrl } from '@/utils/assets'

const cityStore = useCityStore()

const activeCategory = ref('')
const sortBy = ref('default')
const categories = ref([])
const services = ref([])
const loading = ref(true)
const errorMessage = ref('')

const sorts = [
  { label: '综合', value: 'default' },
  { label: '价格', value: 'price' },
  { label: '热度', value: 'hot' }
]

const categoryTabs = computed(() => [
  { label: '全部', value: '', count: services.value.length },
  ...categories.value.map(item => ({
    label: item.categoryName,
    value: String(item.categoryId),
    count: services.value.filter(service => String(service.categoryId) === String(item.categoryId)).length
  }))
])

const menuAnchor = computed(() => `menu-${activeCategory.value || 'all'}`)

const activeCategoryLabel = computed(() => {
  if (!activeCategory.value) return '全部服务'
  return categories.value.find(item => String(item.categoryId) === activeCategory.value)?.categoryName || '全部服务'
})

const filteredServices = computed(() => services.value.filter(item => !activeCategory.value || String(item.categoryId) === activeCategory.value))

const sortedServices = computed(() => {
  const list = [...filteredServices.value]
  if (sortBy.value === 'price') return list.sort((a, b) => Number(a.price || 0) - Number(b.price || 0))
  if (sortBy.value === 'hot') return list.sort((a, b) => Number(b.reserveCount || 0) - Number(a.reserveCount || 0))
  return list
})

function responseList (response) { return response?.rows || response?.data?.rows || (Array.isArray(response?.data) ? response.data : Array.isArray(response) ? response : []) }
function serviceImage (item) { const images = item?.productImage || item?.carouselImages || item?.imageUrl || ''; return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || '' }

onMounted(async () => {
  loading.value = true
  await cityStore.initialize()
  const options = getCurrentPages().at(-1)?.options || {}
  activeCategory.value = options.categoryId ? String(options.categoryId) : ''
  try {
    const [c, s] = await Promise.all([listCategories(), listServices({ cityId: cityStore.currentCityId() })])
    categories.value = responseList(c)
    services.value = responseList(s)
  } catch (e) {
    errorMessage.value = e.message || '服务数据加载失败'
  } finally {
    loading.value = false
  }
  // 城市切换后重新拉取，避免展示其他城市的服务
  uni.$on('city-changed', () => {
    loadServices()
  })
})

/**
 * 按当前城市重新加载服务列表。
 */
async function loadServices () {
  if (!cityStore.currentCityId() && !cityStore.cityName) return
  loading.value = true
  try {
    const s = await listServices({ cityId: cityStore.currentCityId() })
    services.value = responseList(s)
  } catch (e) {
    errorMessage.value = e.message || '服务数据加载失败'
  } finally {
    loading.value = false
  }
}

function selectCategory (value) {
  activeCategory.value = value
}
function openDetail (id) { uni.navigateTo({ url: `/pages/service-detail/index?id=${id}` }) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.service-page {
  display: flex;
  height: 100vh;
  flex-direction: column;
  overflow: hidden;
  background: $bg;
}

/* ---------- 左右分栏 ---------- */
.browser {
  display: flex;
  flex: 1;
  min-height: 0;
  padding-top: $space-2;
}

.menu {
  flex: 0 0 188rpx;
  width: 188rpx;
  height: 100%;
  padding: $space-1 0 calc(108rpx + env(safe-area-inset-bottom));
  background: rgba(245, 241, 238, 0.6);
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  min-height: 104rpx;
  padding: $space-2 $space-1;
  font-size: $font-caption;
  line-height: 1.3;
  text-align: center;
  color: $ink-500;
  transition: color 0.2s ease;
}

.menu-item--active { color: $brand-600; font-weight: $weight-semibold; }
.menu-item--active .menu-label { font-weight: $weight-semibold; }

.menu-item--active::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 6rpx;
  height: 36rpx;
  transform: translateY(-50%);
  border-radius: 0 $radius-pill $radius-pill 0;
  background: $brand-500;
}

.menu-label { min-width: 0; }

.menu-count {
  flex: 0 0 auto;
  font-size: 18rpx;
  color: $ink-300;
}
.menu-item--active .menu-count { color: $brand-400; }

.results {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 $page-gutter $space-6 $space-3;
}

.result-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: $space-2;
  padding: $space-1 0 $space-2;
}

.result-titles { display: flex; min-width: 0; flex-direction: column; }

.result-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.result-count { margin-top: 4rpx; font-size: $font-micro; color: $ink-400; }

.sort-switch { display: flex; flex: 0 0 auto; align-items: center; gap: $space-2; }

.sort-item { font-size: $font-micro; color: $ink-400; }
.sort-item--active { color: $brand-600; font-weight: $weight-semibold; }

/* ---------- 结果列表 ---------- */
.result-list {
  display: flex;
  flex-direction: column;
  gap: $space-2;
  padding-bottom: calc(108rpx + env(safe-area-inset-bottom));
}

.result-card {
  display: block;
  padding: $space-2;
  transition: transform 0.15s ease;
}
.result-card:active { transform: scale(0.99); }

.result-thumb {
  position: relative;
  width: 100%;
  height: 128rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  overflow: hidden;
  background: transparent;
}
.result-thumb image { display: block; width: 320rpx; height: 128rpx; }
.result-thumb-skeleton { width: 100%; height: 128rpx; border-radius: $radius-md; }

.result-thumb__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 128rpx;
  background: linear-gradient(135deg, $brand-50, $brand-100);
}

.result-badge {
  position: absolute;
  top: 0;
  left: 0;
  padding: 0 8rpx;
  border-radius: $radius-md 0 $radius-md 0;
  background: $brand-500;
  color: #fff;
  font-size: 18rpx;
  line-height: 30rpx;
}

.result-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
  padding: $space-2 4rpx 2rpx;
}

.result-title-row { display: flex; align-items: center; justify-content: space-between; gap: $space-2; }

.result-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.result-intro {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  font-size: $font-micro;
  line-height: 30rpx;
  color: $ink-400;
}

.result-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24rpx;
}

.result-booked { font-size: 18rpx; color: $ink-300; }

/* ---------- 状态 ---------- */
.result-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;
  padding: $space-8 $space-3;
  text-align: center;
}

.result-empty__title { font-size: $font-caption; color: $ink-400; }
.result-empty__link { font-size: $font-micro; color: $brand-600; }

.skeleton-line { height: 26rpx; }
.skeleton-line--short { width: 70%; }
.skeleton-line--tiny { width: 40%; }
</style>
