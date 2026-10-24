<template>
  <view
    v-if="!configStore.navigationReady"
    class="page startup-page"
  >
    <view class="startup-brand">
      <image
        class="startup-logo"
        src="/static/logo.png"
        mode="aspectFit"
      />
      <text class="startup-brand-name">
        {{ appStore.appName }}
      </text>
    </view>
    <view class="startup-panel card">
      <view class="startup-icon">
        <uni-icons
          :type="configStore.loading ? 'spinner-cycle' : 'cloud-download'"
          size="30"
          color="#e06a3d"
        />
      </view>
      <text class="startup-title">
        {{ configStore.loading ? '正在准备首页' : '首页配置加载失败' }}
      </text>
      <text class="startup-message">
        {{ configStore.loading ? '正在同步服务信息' : '请确认后端服务已启动' }}
      </text>
      <button
        v-if="!configStore.loading"
        class="primary-button startup-retry"
        @click="reloadConfig"
      >
        重新加载
      </button>
    </view>
  </view>

  <view
    v-else
    class="home-page has-tabbar"
  >
    <UiNavbar
      :back="false"
      :title="pageTitle"
    />

    <view class="home-body">
      <!-- 城市 + 搜索 -->
      <view class="search-bar">
        <view
          class="city-select"
          @click="openCity"
        >
          <uni-icons
            type="location-filled"
            size="14"
            color="#e06a3d"
          />
          <text class="city-name">
            {{ location || '选择城市' }}
          </text>
          <uni-icons
            type="bottom"
            size="10"
            color="#c9582d"
          />
        </view>
        <view class="search-divider" />
        <view
          class="search-entry"
          @click="openSearchPage"
        >
          <uni-icons
            type="search"
            size="16"
            color="#a39b95"
          />
          <text class="search-placeholder">
            {{ configStore.home?.searchPlaceholder || '搜索保洁、家电清洗、陪护' }}
          </text>
        </view>
      </view>

      <!-- 轮播 -->
      <view
        v-if="banners.length"
        class="banner-wrap card card--flush"
      >
        <swiper
          class="banner-swiper"
          autoplay
          circular
          :interval="4500"
          :duration="450"
          @change="onBannerChange"
        >
          <swiper-item
            v-for="banner in banners"
            :key="banner.id || banner.bannerId || banner.image"
          >
            <view
              class="banner-slide"
              @click="openBanner(banner)"
            >
              <image
                v-if="bannerImage(banner)"
                class="banner-image"
                :src="assetUrl(bannerImage(banner))"
                mode="aspectFill"
              />
              <view
                v-else
                class="banner-fallback"
              />
              <view class="banner-mask" />
              <view class="banner-copy">
                <text class="banner-title">
                  {{ banner.title || banner.name || '' }}
                </text>
                <text
                  v-if="banner.subtitle || banner.description"
                  class="banner-sub"
                >
                  {{ banner.subtitle || banner.description }}
                </text>
                <view
                  v-if="banner.link || banner.serviceId"
                  class="banner-cta"
                >
                  了解服务
                </view>
              </view>
            </view>
          </swiper-item>
        </swiper>
        <view
          v-if="banners.length > 1"
          class="banner-dots"
        >
          <view
            v-for="(banner, index) in banners"
            :key="`dot-${index}`"
            class="banner-dot"
            :class="{ 'banner-dot--active': index === bannerIndex }"
          />
        </view>
      </view>

      <!-- 分类 -->
      <view
        v-if="categories.length"
        class="category-panel card"
      >
        <view
          v-for="item in visibleCategories"
          :key="item.categoryId || item.id || item.categoryName"
          class="category-item"
          @click="openCategory(item.categoryId || item.id)"
        >
          <view class="category-icon">
            <image
              v-if="item.imageUrl"
              :src="assetUrl(item.imageUrl)"
              mode="aspectFit"
            />
            <text v-else>
              {{ categoryLabel(item).slice(0, 1) }}
            </text>
          </view>
          <text class="category-name">
            {{ categoryLabel(item) }}
          </text>
        </view>
      </view>
      <view
        v-else-if="categoryError"
        class="inline-error"
      >
        {{ categoryError }}
      </view>

      <!-- 信任条 -->
      <view class="trust-strip">
        <view
          v-for="item in trusts"
          :key="item.title"
          class="trust-item"
        >
          <text class="trust-icon">
            {{ item.icon }}
          </text>
          <text class="trust-text">
            {{ item.title }}
          </text>
        </view>
      </view>

      <!-- 推荐服务 -->
      <UiSection
        title="推荐服务"
        subtitle="本周热门"
        more="更多服务"
        @more="goServices"
      >
        <view
          v-if="loading"
          class="service-grid"
        >
          <view
            v-for="n in 2"
            :key="`sk-${n}`"
            class="service-tile card card--flush"
          >
            <view class="skeleton service-cover-skeleton" />
            <view class="service-tile__body">
              <view class="skeleton skeleton-line" />
              <view class="skeleton skeleton-line skeleton-line--short" />
            </view>
          </view>
        </view>
        <view
          v-else-if="hotServices.length"
          class="service-grid"
        >
          <view
            v-for="item in hotServices"
            :key="item.serviceId"
            class="service-tile card card--flush"
            @click="openService(item.serviceId)"
          >
            <view class="service-cover">
              <image
                v-if="firstImage(item)"
                :src="assetUrl(firstImage(item))"
                mode="aspectFill"
              />
              <view
                v-else
                class="service-cover-fallback"
              >
                <uni-icons
                  type="home"
                  size="30"
                  color="#f9d5c5"
                />
              </view>
            </view>
            <view class="service-tile__body">
              <text class="service-tile__title">
                {{ item.serviceName }}
              </text>
              <text class="service-tile__intro">
                {{ item.serviceIntro || '查看服务内容与可预约时间' }}
              </text>
              <view class="service-tile__foot">
                <UiPrice
                  :value="item.price"
                  :unit="item.unitName || '次'"
                  size="sm"
                />
                <text class="service-booked">
                  已约 {{ item.reserveCount || 0 }}
                </text>
              </view>
            </view>
          </view>
        </view>
        <view
          v-else
          class="empty-placeholder card"
        >
          <text class="empty-text">
            {{ errorMessage || '暂无热门服务' }}
          </text>
          <button
            v-if="errorMessage"
            class="ghost-button"
            @click="loadHomeData"
          >
            重新加载
          </button>
        </view>
      </UiSection>
    </view>

    <AppFeedback />
    <AppTabBar current-key="home" />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiSection from '@/components/ui/UiSection.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import { useAppStore } from '@/stores/app'
import { useCityStore } from '@/stores/city'
import { useConfigStore } from '@/stores/config'
import { getMiniBanners } from '@/api/mini'
import { listCategories, listServices } from '@/api/service'
import { assetUrl } from '@/utils/assets'
import { toastInfo } from '@/utils/feedback'

const appStore = useAppStore(); const configStore = useConfigStore(); const cityStore = useCityStore()
const location = ref('')
const pageTitle = computed(() => configStore.site?.shopName || appStore.appName)
const categories = ref([]); const services = ref([]); const loading = ref(false); const errorMessage = ref(''); const categoryError = ref('')
const bannerIndex = ref(0)
const tableBanners = ref([])
// 轮播优先用后台「轮播图管理」(zjudg_banner) 的数据；
// 未配置轮播图时回退到装修配置里的 banners，保证首页不会因此变空
const banners = computed(() => (tableBanners.value.length ? tableBanners.value : (configStore.home?.banners || [])))
const visibleCategories = computed(() => categories.value.slice(0, 8))
const hotServices = computed(() => services.value.slice(0, 4))
const trusts = [
  { icon: '✓', title: '持证上岗' },
  { icon: '✓', title: '先行赔付' },
  { icon: '✓', title: '准时到达' },
  { icon: '✓', title: '免费取消' }
]

function responseList (response) { const payload = response?.data ?? response; if (Array.isArray(payload)) return payload; return payload?.rows || payload?.list || response?.rows || [] }
function categoryLabel (item) { return item.categoryName || item.name || item.title || '家政' }
function firstImage (item) { const value = item?.carouselImages || item?.imageUrl || ''; return Array.isArray(value) ? value[0] : String(value).split(',')[0] }
function bannerImage (banner) { return banner?.image || banner?.imageUrl || banner?.url || '' }
function onBannerChange (event) { bannerIndex.value = event.detail?.current || 0 }

onMounted(async () => {
  cityStore.restore()
  location.value = cityStore.cityName || location.value
  await configStore.load().catch(() => {})
  await loadHomeData()
  // 城市切换后重新拉取服务，保证列表与所选城市一致
  uni.$on('city-changed', value => {
    location.value = value?.cityName || ''
    loadHomeData()
  })
})

// 从城市选择页返回时同步展示；页面被 redirectTo 重建时事件可能丢失，这里兜底
onShow(() => {
  const name = cityStore.cityName
  if (name && name !== location.value) {
    location.value = name
    loadHomeData()
  }
})

async function loadHomeData () {
  loading.value = true
  errorMessage.value = ''
  categoryError.value = ''
  const [categoryResult, serviceResult, bannerResult] = await Promise.allSettled([
    withTimeout(listCategories(), '分类加载超时'),
    withTimeout(listServices({ cityId: cityStore.currentCityId() }), '服务加载超时'),
    withTimeout(getMiniBanners('index'), '轮播加载超时')
  ])
  if (categoryResult.status === 'fulfilled') categories.value = responseList(categoryResult.value)
  else categoryError.value = categoryResult.reason?.message || '分类加载失败，请检查网络后重试'
  if (serviceResult.status === 'fulfilled') services.value = responseList(serviceResult.value)
  else errorMessage.value = serviceResult.reason?.message || '服务加载失败，请检查网络后重试'
  // 轮播失败不影响首页其余内容，保留上一次结果并回退装修配置
  if (bannerResult.status === 'fulfilled') tableBanners.value = responseList(bannerResult.value)
  bannerIndex.value = 0
  loading.value = false
}

function withTimeout (promise, message) {
  let timer
  return Promise.race([
    promise,
    new Promise((resolve, reject) => { timer = setTimeout(() => reject(new Error(`${message}，请检查服务地址和网络`)), 8000) })
  ]).finally(() => clearTimeout(timer))
}

async function reloadConfig () { configStore.loaded = false; await configStore.load().catch(() => {}) }
function openSearchPage () { uni.navigateTo({ url: '/pages/services/index?focus=1' }) }
function goServices () { uni.navigateTo({ url: '/pages/services/index' }) }
function openCategory (id) { uni.navigateTo({ url: `/pages/services/index?categoryId=${encodeURIComponent(id)}` }) }
function openService (id) { if (id) uni.navigateTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(id)}` }) }
function openBanner (banner) {
  if (!banner) return
  if (banner.serviceId) return openService(banner.serviceId)
  const link = banner.link || banner.linkUrl
  if (!link) return
  // 外部链接复制到剪贴板，站内路径才做页面跳转
  if (/^https?:\/\//.test(link)) {
    uni.setClipboardData({ data: link })
    return
  }
  uni.navigateTo({
    url: link,
    fail: () => toastInfo('页面暂不可用')
  })
}
function openCity () { uni.navigateTo({ url: `/pages/city/index?current=${encodeURIComponent(location.value)}` }) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.home-page {
  min-height: 100vh;
  background: $bg;
}

/* ---------- 城市 + 搜索（页面内容区首行） ---------- */
.search-bar {
  display: flex;
  align-items: center;
  height: 88rpx;
  margin-bottom: $space-3;
  padding: 0 $space-2;
  border-radius: $radius-pill;
  background: $surface;
  box-shadow: 0 6rpx 22rpx rgba(31, 28, 26, 0.07);
}

.city-select {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 4rpx;
  max-width: 210rpx;
  padding: 0 $space-1;
}

.city-name {
  max-width: 160rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  color: $brand-700;
}

.search-divider {
  flex: 0 0 1rpx;
  width: 1rpx;
  height: 36rpx;
  margin: 0 $space-2 0 $space-1;
  background: $line;
}

.search-entry {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: center;
  gap: 8rpx;
}

.search-placeholder {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-micro;
  color: $ink-400;
}

.home-body { padding: $space-3 $page-gutter 0; }

/* ---------- 轮播 ---------- */
.banner-wrap {
  position: relative;
  overflow: hidden;
  border-radius: $radius-lg;
}

.banner-swiper, .banner-image, .banner-slide { width: 100%; height: 320rpx; }
.banner-slide { position: relative; display: block; }

.banner-fallback { width: 100%; height: 100%; background: linear-gradient(135deg, #fdece4, #f9d5c5); }

.banner-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(100deg, rgba(31, 28, 26, 0.62) 0%, rgba(31, 28, 26, 0.18) 58%, rgba(31, 28, 26, 0) 100%);
}

.banner-copy {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 $space-4;
  color: #fff;
}

.banner-title { font-size: 40rpx; font-weight: $weight-heavy; letter-spacing: 0.5rpx; }
.banner-sub { margin-top: $space-1; font-size: $font-caption; color: rgba(255, 255, 255, 0.88); }

.banner-cta {
  align-self: flex-start;
  margin-top: $space-3;
  padding: 0 $space-3;
  height: 60rpx;
  line-height: 60rpx;
  border-radius: $radius-pill;
  background: rgba(255, 255, 255, 0.92);
  color: $brand-600;
  font-size: $font-micro;
  font-weight: $weight-semibold;
}

.banner-dots {
  position: absolute;
  right: $space-3;
  bottom: $space-2;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.banner-dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: $radius-pill;
  background: rgba(255, 255, 255, 0.55);
  transition: all 0.25s ease;
}

.banner-dot--active { width: 32rpx; background: #fff; }

/* ---------- 分类 ---------- */
.category-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  row-gap: $space-4;
  margin-top: $space-3;
  padding: $space-4 $space-2;
}

.category-item {
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: center;
  gap: $space-1;
}
.category-item:active { opacity: 0.7; }

.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border-radius: $radius-md;
  background: $brand-50;
  color: $brand-500;
  font-size: 30rpx;
  font-weight: $weight-bold;
  overflow: hidden;
}

.category-icon image { width: 100%; height: 100%; }

.category-name {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-micro;
  font-weight: $weight-medium;
  color: $ink-700;
}

/* ---------- 信任条 ---------- */
.trust-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $space-3;
  padding: $space-2 $space-3;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.7);
}

.trust-item { display: flex; align-items: center; gap: 6rpx; }

.trust-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  background: $brand-100;
  color: $brand-600;
  font-size: 16rpx;
  font-weight: $weight-bold;
}

.trust-text { font-size: $font-micro; color: $ink-500; }

/* ---------- 服务卡（两列） ---------- */
.service-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $space-2;
}

.service-tile {
  overflow: hidden;
  transition: transform 0.15s ease;
}
.service-tile:active { transform: scale(0.98); }

.service-cover { position: relative; width: 100%; height: 240rpx; overflow: hidden; background: $bg-sunken; }
.service-cover image { width: 100%; height: 100%; }
.service-cover-skeleton { width: 100%; height: 240rpx; }

.service-cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, $brand-50, $brand-100);
}

.service-tile__body { display: flex; flex-direction: column; padding: $space-3; }

.service-tile__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.service-tile__intro {
  display: -webkit-box;
  margin-top: 6rpx;
  height: 64rpx;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  font-size: $font-micro;
  line-height: 32rpx;
  color: $ink-400;
}

.service-tile__foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: $space-2;
}

.service-booked { font-size: $font-micro; color: $ink-300; }

.skeleton-line { height: 28rpx; margin-top: $space-2; }
.skeleton-line--short { width: 60%; }

/* ---------- 状态 ---------- */
.inline-error {
  margin-top: $space-3;
  padding: $space-3;
  border-radius: $radius-md;
  background: $surface;
  color: $ink-400;
  font-size: $font-micro;
  text-align: center;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;
  padding: $space-6;
}

.empty-text { font-size: $font-caption; color: $ink-400; }

/* ---------- 启动页 ---------- */
.startup-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $space-8 $page-gutter;
}

.startup-brand { display: flex; align-items: center; margin-bottom: $space-8; }
.startup-logo { width: 76rpx; height: 76rpx; border-radius: $radius-md; }
.startup-brand-name {
  margin-left: $space-2;
  font-size: 36rpx;
  font-weight: $weight-heavy;
  color: $brand-600;
  letter-spacing: 1rpx;
}

.startup-panel {
  display: flex;
  width: 100%;
  flex-direction: column;
  align-items: center;
  padding: $space-8 $space-4;
  text-align: center;
}

.startup-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: $brand-50;
}

.startup-title { margin-top: $space-3; font-size: $font-title; font-weight: $weight-bold; color: $ink-900; }
.startup-message { margin-top: $space-1; font-size: $font-caption; color: $ink-400; }
.startup-retry { width: 280rpx; margin-top: $space-4; line-height: 80rpx; }
</style>
