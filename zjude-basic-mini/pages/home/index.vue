<template>
  <view
    v-if="!configStore.navigationReady"
    class="page startup-page"
  >
    <view class="startup-glow startup-glow--top" />
    <view class="startup-glow startup-glow--bottom" />

    <view class="startup-header">
      <view class="startup-logo-frame">
        <image
          class="startup-logo"
          src="/static/logo.png"
          mode="aspectFit"
        />
      </view>
      <text class="startup-brand-name">
        {{ appStore.appName }}
      </text>
    </view>

    <view class="startup-main">
      <view class="startup-illustration">
        <view class="startup-orbit startup-orbit--outer" />
        <view class="startup-orbit startup-orbit--inner" />
        <view class="startup-home-icon">
          <uni-icons
            type="home"
            size="66"
            color="#e06a3d"
          />
        </view>
        <view class="startup-spark startup-spark--one">
          ✦
        </view>
        <view class="startup-spark startup-spark--two">
          ✦
        </view>
        <view class="startup-art-dot startup-art-dot--one" />
        <view class="startup-art-dot startup-art-dot--two" />
      </view>

      <view class="startup-copy">
        <text class="startup-title">
          首页加载中，请稍后
        </text>
        <text class="startup-message">
          {{ configStore.loading ? '正在为您准备服务信息' : configStore.error }}
        </text>
      </view>
    </view>

    <view class="startup-bottom">
      <view
        v-if="configStore.loading"
        class="startup-progress"
        aria-label="Loading"
      >
        <view class="startup-progress-bar" />
      </view>
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
          :style="bannerHeightStyle"
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
              :style="bannerHeightStyle"
              @click="openBanner(banner)"
            >
              <image
                class="banner-image"
                :src="assetUrl(bannerImage(banner))"
                mode="widthFix"
                @load="onBannerImageLoad($event, banner)"
              />
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
                mode="aspectFit"
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
import { computed, onMounted, reactive, ref } from 'vue'
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
const categories = ref([]); const services = ref([]); const loading = ref(true); const errorMessage = ref(''); const categoryError = ref('')
const bannerIndex = ref(0)
const bannerImageRatios = reactive({})
const bannerContentWidth = uni.getSystemInfoSync().windowWidth || 375
const tableBanners = ref([])
// 轮播优先用后台「轮播图管理」(zjudg_banner) 的数据；
// 未配置轮播图时回退到装修配置里的 banners，保证首页不会因此变空
const banners = computed(() => {
  const source = tableBanners.value.length ? tableBanners.value : (configStore.home?.banners || [])
  return source.filter(banner => banner?.image || banner?.imageUrl || banner?.url)
})
const bannerHeight = computed(() => {
  const image = bannerImage(banners.value[bannerIndex.value])
  const ratio = bannerImageRatios[image] || (320 / 686)
  return Math.round(bannerContentWidth * ratio)
})
const bannerHeightStyle = computed(() => ({ height: `${bannerHeight.value}px` }))
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
function onBannerImageLoad (event, banner) {
  const { width, height } = event.detail || {}
  const image = bannerImage(banner)
  if (image && width > 0 && height > 0) bannerImageRatios[image] = height / width
}

onMounted(async () => {
  await cityStore.initialize()
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
  margin-bottom: $space-2;
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

.home-body { padding: 0 $page-gutter 0; }

/* ---------- 轮播 ---------- */
.banner-wrap {
  position: relative;
  margin-right: -$page-gutter;
  margin-left: -$page-gutter;
  overflow: hidden;
  border-radius: $radius-lg;
}

.banner-swiper, .banner-slide { width: 100%; }
.banner-slide { position: relative; display: block; background: $bg; }
.banner-image { display: block; width: 100%; }

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

.service-cover { position: relative; width: 100%; height: 140rpx; overflow: hidden; background: #fff5ef; }
.service-cover image { width: 100%; height: 100%; }
.service-cover-skeleton { width: 100%; height: 140rpx; }

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
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  padding: calc(48rpx + env(safe-area-inset-top)) 40rpx calc(40rpx + env(safe-area-inset-bottom));
  background: linear-gradient(155deg, #fffaf7 0%, #faf7f5 54%, #f5f8f4 100%);
}

.startup-glow {
  position: absolute;
  z-index: 0;
  width: 500rpx;
  height: 500rpx;
  border-radius: 50%;
  pointer-events: none;
}

.startup-glow--top {
  top: -250rpx;
  right: -220rpx;
  background: radial-gradient(circle, rgba(245, 138, 95, 0.18) 0%, rgba(245, 138, 95, 0.05) 52%, rgba(245, 138, 95, 0) 74%);
}

.startup-glow--bottom {
  bottom: -300rpx;
  left: -270rpx;
  background: radial-gradient(circle, rgba(47, 158, 107, 0.11) 0%, rgba(47, 158, 107, 0.035) 52%, rgba(47, 158, 107, 0) 74%);
}

.startup-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  min-height: 76rpx;
}

.startup-logo-frame {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.9);
  border-radius: 22rpx;
  background: #fff;
  box-shadow: 0 10rpx 28rpx rgba(31, 28, 26, 0.08);
}

.startup-logo { width: 58rpx; height: 58rpx; border-radius: 14rpx; }
.startup-brand-name {
  margin-left: 18rpx;
  font-size: 32rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: 0.5rpx;
}

.startup-main {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 680rpx;
  padding: 24rpx 0 48rpx;
}

.startup-illustration {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 390rpx;
  height: 360rpx;
  margin-bottom: 26rpx;
}

.startup-orbit {
  position: absolute;
  display: block;
  border: 1rpx solid rgba(224, 106, 61, 0.12);
  border-radius: 50%;
}

.startup-orbit--outer {
  width: 350rpx;
  height: 350rpx;
  background: rgba(255, 255, 255, 0.24);
  animation: startup-orbit-pulse 3.2s ease-in-out infinite;
}

.startup-orbit--inner {
  width: 272rpx;
  height: 272rpx;
  border-color: rgba(224, 106, 61, 0.08);
  background: rgba(255, 255, 255, 0.36);
  animation: startup-orbit-pulse 3.2s ease-in-out -1.6s infinite;
}

.startup-home-icon {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 172rpx;
  height: 172rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.95);
  border-radius: 52rpx;
  background: linear-gradient(145deg, #fff 12%, #fff6f0 100%);
  box-shadow: 0 22rpx 54rpx rgba(180, 88, 50, 0.15), inset 0 1rpx 0 #fff;
  animation: startup-home-float 2.8s ease-in-out infinite;
}

.startup-spark {
  position: absolute;
  z-index: 2;
  color: $brand-400;
  font-size: 36rpx;
  line-height: 1;
  animation: startup-sparkle 1.8s ease-in-out infinite;
}

.startup-spark--one { top: 48rpx; right: 76rpx; }
.startup-spark--two { bottom: 66rpx; left: 62rpx; color: #83b998; font-size: 24rpx; }

.startup-art-dot {
  position: absolute;
  z-index: 2;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #f5a27f;
  box-shadow: 0 0 0 9rpx rgba(245, 162, 127, 0.13);
  animation: startup-dot-pulse 1.8s ease-in-out infinite;
}

.startup-art-dot--one { top: 106rpx; left: 40rpx; }
.startup-art-dot--two { right: 40rpx; bottom: 104rpx; width: 10rpx; height: 10rpx; background: #7bb58f; box-shadow: 0 0 0 8rpx rgba(123, 181, 143, 0.14); }

.startup-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 100%;
  text-align: center;
}

.startup-title {
  font-size: 38rpx;
  font-weight: $weight-bold;
  letter-spacing: 0.5rpx;
  color: $ink-900;
}

.startup-message {
  max-width: 620rpx;
  min-height: 36rpx;
  margin-top: 14rpx;
  font-size: $font-caption;
  line-height: 1.6;
  color: $ink-400;
}

.startup-bottom {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.92);
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18rpx 54rpx rgba(31, 28, 26, 0.07);
}

.startup-progress {
  position: relative;
  width: 100%;
  height: 10rpx;
  overflow: hidden;
  border-radius: 999rpx;
  background: #f3eeea;
}

.startup-progress-bar {
  width: 42%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #f6b18f, $brand-500);
  animation: startup-progress 1.6s ease-in-out infinite;
}

.startup-retry {
  width: 100%;
  margin: 0;
  line-height: 82rpx;
}

.startup-retry:active { transform: scale(0.99); }

@keyframes startup-progress {
  0% { transform: translateX(-105%); }
  100% { transform: translateX(245%); }
}

@keyframes startup-orbit-pulse {
  0%, 100% { opacity: 0.6; transform: scale(0.96); }
  50% { opacity: 1; transform: scale(1.04); }
}

@keyframes startup-home-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10rpx); }
}

@keyframes startup-sparkle {
  0%, 100% { opacity: 0.5; transform: scale(0.84) rotate(-8deg); }
  50% { opacity: 1; transform: scale(1.12) rotate(8deg); }
}

@keyframes startup-dot-pulse {
  0%, 100% { opacity: 0.65; transform: scale(0.85); }
  50% { opacity: 1; transform: scale(1.18); }
}

@media (prefers-reduced-motion: reduce) {
  .startup-progress-bar,
  .startup-orbit--outer,
  .startup-orbit--inner,
  .startup-home-icon,
  .startup-spark,
  .startup-art-dot {
    animation-duration: 4s;
  }
}
</style>
