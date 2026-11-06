<template>
  <view class="category-page has-tabbar">
    <UiNavbar :back="false" title="分类" />
    <view v-if="loading" class="loading-wrap">
      <view v-for="n in 3" :key="`sk-${n}`" class="skeleton skeleton-row" />
    </view>

    <view v-else-if="categories.length" class="browser">
      <scroll-view class="menu" scroll-y :show-scrollbar="false" :enhanced="true" :bounces="false">
        <view
          v-for="item in categories"
          :key="item.categoryId"
          class="menu-item"
          :class="{ 'menu-item--active': activeCategory === String(item.categoryId) }"
          @click="activeCategory = String(item.categoryId)"
        >
          <text class="menu-label">{{ item.categoryName }}</text>
          <text class="menu-count">{{ serviceCount(item.categoryId) }}</text>
        </view>
      </scroll-view>

      <scroll-view class="results" scroll-y :show-scrollbar="false" :enhanced="true" :bounces="false">
        <view class="result-head">
          <text class="result-title">{{ selectedCategory?.categoryName || '全部服务' }}</text>
          <text class="result-count">{{ selectedServices.length }} 项</text>
        </view>

        <view v-if="selectedServices.length" class="result-list">
          <view
            v-for="item in selectedServices"
            :key="item.serviceId"
            class="result-card card"
            @click="openService(item.serviceId)"
          >
            <view class="result-cover">
              <image v-if="serviceImage(item)" :src="assetUrl(serviceImage(item))" mode="widthFix" />
              <view v-else class="result-cover__fallback">
                <uni-icons type="home" size="22" color="#f9d5c5" />
              </view>
            </view>
            <view class="result-copy">
              <view class="result-title-row">
                <text class="result-name">{{ item.serviceName }}</text>
                <UiPrice :value="item.price" :unit="item.unitName || '次'" size="sm" />
              </view>
              <text class="result-intro">{{ item.serviceIntro || '标准上门服务流程' }}</text>
            </view>
          </view>
        </view>

        <text v-else class="result-empty">该分类暂无可预约服务</text>
      </scroll-view>
    </view>

    <UiEmpty
      v-else
      icon="list"
      :title="errorMessage || category.emptyTitle || '暂无服务分类'"
      description="可以先浏览全部服务"
    >
      <button class="primary-button empty-action" @click="openServices">查看全部服务</button>
    </UiEmpty>

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
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { listCategories, listServices } from '@/api/service'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'

const configStore = useConfigStore()
const category = computed(() => configStore.category || {})
const categories = ref([])
const services = ref([])
const activeCategory = ref('')
const loading = ref(false)
const errorMessage = ref('')

const selectedCategory = computed(() => categories.value.find(item => String(item.categoryId) === activeCategory.value) || categories.value[0])
const selectedServices = computed(() => {
  const id = selectedCategory.value?.categoryId
  return services.value.filter(item => !id || String(item.categoryId) === String(id))
})

function serviceCount (categoryId) {
  return services.value.filter(item => String(item.categoryId) === String(categoryId)).length
}

function responseList (response) { return response?.rows || response?.data?.rows || (Array.isArray(response?.data) ? response.data : Array.isArray(response) ? response : []) }
function serviceImage (item) { const images = item?.carouselImages || item?.imageUrl || ''; return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || '' }

onMounted(async () => {
  await configStore.load().catch(() => {})
  loading.value = true
  const [categoryResult, serviceResult] = await Promise.allSettled([listCategories(), listServices()])
  if (categoryResult.status === 'fulfilled') {
    categories.value = responseList(categoryResult.value)
    activeCategory.value = categories.value[0] ? String(categories.value[0].categoryId) : ''
  } else {
    errorMessage.value = categoryResult.reason?.message || '服务分类加载失败'
  }
  if (serviceResult.status === 'fulfilled') services.value = responseList(serviceResult.value)
  else errorMessage.value = serviceResult.reason?.message || '服务列表加载失败'
  loading.value = false
})

function openService (id) { uni.navigateTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(id)}` }) }
function openServices () { uni.navigateTo({ url: '/pages/services/index' }) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.category-page {
  display: flex;
  height: 100vh;
  flex-direction: column;
  overflow: hidden;
  background: $bg;
}

/* ---------- 左右分栏：撑满剩余高度 ---------- */
.browser {
  display: flex;
  flex: 1;
  min-height: 0;
  padding-bottom: calc(108rpx + env(safe-area-inset-bottom));
}

.menu {
  flex: 0 0 200rpx;
  width: 200rpx;
  height: 100%;
  padding: 0 0 $space-6;
  background: rgba(245, 241, 238, 0.6);
}

.menu-item {
  position: relative;
  display: flex;
  min-height: 112rpx;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2rpx;
  padding: $space-2 $space-1;
  color: $ink-500;
  transition: color 0.2s ease, background 0.2s ease;
}

.menu-item--active {
  color: $brand-600;
  background: $bg;
}

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

.menu-label {
  font-size: $font-caption;
  line-height: 1.3;
  text-align: center;
}

.menu-item--active .menu-label { font-weight: $weight-semibold; }

.menu-count { font-size: 18rpx; color: $ink-300; }
.menu-item--active .menu-count { color: $brand-400; }

.results {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: $space-2 $page-gutter $space-6 $space-3;
}

.result-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 0 0 $space-2;
}

.result-title { font-size: $font-body; font-weight: $weight-semibold; color: $ink-900; }
.result-count { font-size: $font-micro; color: $ink-400; }

.result-list { display: flex; flex-direction: column; gap: $space-2; }

.result-card { display: block; padding: $space-2; }
.result-card:active { opacity: 0.85; }

.result-cover {
  width: 100%;
  border-radius: $radius-md;
  overflow: hidden;
  background: $brand-50;
}
.result-cover image { display: block; width: 100%; height: auto; }

.result-cover__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 112rpx;
  background: linear-gradient(135deg, $brand-50, $brand-100);
}

.result-copy {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 6rpx;
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

.result-empty {
  display: block;
  padding: $space-8 0;
  text-align: center;
  font-size: $font-caption;
  color: $ink-400;
}

/* ---------- 加载与空态 ---------- */
.loading-wrap {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: $space-2;
  padding: $space-4 $page-gutter;
}

.skeleton-row { height: 160rpx; border-radius: $radius-lg; }

.empty-action { width: 280rpx; margin-top: $space-4; line-height: 80rpx; }
</style>
