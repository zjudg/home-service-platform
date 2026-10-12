<template>
  <view v-if="!configStore.navigationReady" class="page startup-page safe-bottom">
    <view class="startup-brand"><image class="startup-logo" src="/static/logo.png" mode="aspectFit" /><text class="startup-brand-name">{{ appStore.appName }}</text></view>
    <view class="startup-panel surface"><uni-icons :type="configStore.loading ? 'spinner-cycle' : 'cloud-download'" size="32" color="#2f806d" /><text class="startup-title">{{ configStore.loading ? '正在准备首页' : '首页配置加载失败' }}</text><text class="startup-message">{{ configStore.loading ? '正在同步服务信息' : '请确认后端服务已启动' }}</text><button v-if="!configStore.loading" class="startup-retry" @click="reloadConfig">重新加载</button></view>
  </view>
  <view v-else class="page home-page safe-bottom">
    <view class="search-row">
      <view class="city-select-inline" @click="openCity">
        <text class="city-select-label">{{ location || '选择' }}</text>
        <uni-icons type="bottom" size="13" color="#91a39d" />
      </view>
      <view class="search-divider" />
      <view class="search-entry" @click="goServices">
        <uni-icons type="search" size="18" color="#a0ada8" />
        <text>{{ configStore.home?.searchPlaceholder || '请输入服务名称，如日常保洁' }}</text>
      </view>
    </view>
    <view v-if="banners.length" class="hero-banner"><swiper class="banner-swiper" autoplay circular indicator-dots><swiper-item v-for="banner in banners" :key="banner.id || banner.bannerId || banner.image"><image class="banner-image" :src="assetUrl(banner.image || banner.imageUrl || banner.url)" mode="aspectFill" /><view class="banner-copy"><text>{{ banner.title || banner.name || '' }}</text><text>{{ banner.subtitle || banner.description || '' }}</text><view v-if="banner.link || banner.serviceId" class="hero-button" @click.stop="openBanner(banner)">立即查看</view></view></swiper-item></swiper></view>
    <view v-else class="data-empty-banner">暂无首页活动</view>
    <scroll-view v-if="categories.length" scroll-x class="category-row"><view v-for="item in categories" :key="item.categoryId || item.id || item.categoryName" class="category-item" @click="openCategory(item.categoryId || item.id)"><view class="category-icon"><image v-if="item.imageUrl" :src="assetUrl(item.imageUrl)" mode="aspectFit" /><text v-else>{{ categoryLabel(item).slice(0, 1) }}</text></view><text>{{ categoryLabel(item) }}</text></view></scroll-view>
    <view v-else-if="categoryError" class="category-error">{{ categoryError }}</view>
    <view class="section-heading"><text>热门服务</text><text class="section-more" @click="goServices">更多服务 <uni-icons type="right" size="13" color="#2e806d" /></text></view>
    <view v-if="loading" class="empty-section">正在加载服务...</view>
    <scroll-view v-else-if="hotServices.length" scroll-x class="product-row"><view v-for="item in hotServices" :key="item.serviceId" class="product-card" @click="openService(item.serviceId)"><view class="product-visual"><image v-if="firstImage(item)" :src="assetUrl(firstImage(item))" mode="aspectFill" /><uni-icons v-else type="home" size="38" color="#2e806d" /></view><text class="product-title">{{ item.serviceName }}</text><text class="product-price">¥{{ item.price }}</text><text class="product-unit">/{{ item.unitName || '次' }}</text></view></scroll-view>
    <view v-else class="empty-section"><text>{{ errorMessage || '暂无热门服务' }}</text><button v-if="errorMessage" class="retry-button" @click="loadHomeData">重新加载</button></view>
    <AppTabBar current-key="home" />
  </view>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useAppStore } from '@/stores/app'
import { useConfigStore } from '@/stores/config'
import { getMiniCategories, getMiniServices } from '@/api/mini'
import { assetUrl } from '@/utils/assets'

const appStore = useAppStore(); const configStore = useConfigStore(); const location = ref('汉中市')
const categories = ref([]); const services = ref([]); const loading = ref(false); const errorMessage = ref(''); const categoryError = ref('')
const banners = computed(() => configStore.home?.banners || [])
const hotServices = computed(() => services.value.slice(0, 4))
function responseList (response) { const payload = response?.data ?? response; if (Array.isArray(payload)) return payload; return payload?.rows || payload?.list || response?.rows || [] }
function categoryLabel (item) { return item.categoryName || item.name || item.title || '家政' }
function firstImage (item) { const value = item?.carouselImages || item?.imageUrl || ''; return Array.isArray(value) ? value[0] : String(value).split(',')[0] }
onMounted(async () => { await configStore.load().catch(() => {}); await loadHomeData(); uni.$on('city-changed', value => { location.value = value }); uni.setNavigationBarTitle({ title: '' }) })
async function loadHomeData () {
  loading.value = true
  errorMessage.value = ''
  categoryError.value = ''
  const [categoryResult, serviceResult] = await Promise.allSettled([
    withTimeout(getMiniCategories(), '分类加载超时'),
    withTimeout(getMiniServices(), '服务加载超时')
  ])
  if (categoryResult.status === 'fulfilled') categories.value = responseList(categoryResult.value)
  else categoryError.value = categoryResult.reason?.message || '分类加载失败，请检查网络后重试'
  if (serviceResult.status === 'fulfilled') services.value = responseList(serviceResult.value)
  else errorMessage.value = serviceResult.reason?.message || '服务加载失败，请检查网络后重试'
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
function goServices () { uni.navigateTo({ url: '/pages/services/index' }) }
function openCategory (id) { uni.navigateTo({ url: `/pages/services/index?categoryId=${encodeURIComponent(id)}` }) }
function openService (id) { if (id) uni.navigateTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(id)}` }) }
function openBanner (banner) { if (banner.serviceId) return openService(banner.serviceId); if (banner.link) uni.navigateTo({ url: banner.link }) }
function openCity () { uni.navigateTo({ url: `/pages/city/index?current=${encodeURIComponent(location.value)}` }) }
</script>

<style lang="scss" scoped>
.home-page{background:#f5f7f6;padding-bottom:24rpx}.topbar{display:flex;align-items:center;justify-content:space-between;padding:8rpx 4rpx 18rpx}.city-select,.top-actions{display:flex;align-items:center}.city-select{gap:6rpx;color:#325f56;font-size:25rpx}.top-actions{gap:24rpx}.search-bar{display:flex;align-items:center;gap:12rpx;height:72rpx;padding:0 22rpx;border-radius:38rpx;background:#fff;color:#a0ada8;font-size:24rpx;box-shadow:0 5rpx 18rpx rgba(35,90,73,.04)}.hero-banner{height:248rpx;margin-top:18rpx;border-radius:22rpx;overflow:hidden;background:#e8f0ec}.banner-swiper,.banner-image{width:100%;height:100%}.banner-swiper{position:relative}.banner-copy{position:absolute;left:30rpx;top:42rpx;color:#245e53}.banner-copy>text{display:block}.banner-copy>text:first-child{font-size:38rpx;font-weight:800}.banner-copy>text:nth-child(2){margin-top:10rpx;color:#6f8f86;font-size:22rpx}.hero-button{display:inline-block;margin-top:24rpx;padding:12rpx 24rpx;border-radius:26rpx;background:#2e806d;color:#fff;font-size:22rpx}.data-empty-banner{display:flex;align-items:center;justify-content:center;height:180rpx;margin-top:18rpx;border-radius:22rpx;background:#e8f0ec;color:#6f8f86;font-size:25rpx}.category-row,.product-row{white-space:nowrap;margin:18rpx -32rpx 0;padding:0 32rpx}.category-item{display:inline-flex;flex-direction:column;align-items:center;width:126rpx;color:#3c514d;font-size:22rpx}.category-icon{display:flex;align-items:center;justify-content:center;width:68rpx;height:68rpx;margin-bottom:9rpx;border-radius:21rpx;background:#e1f3ec;color:#2e806d;overflow:hidden}.category-icon image{width:100%;height:100%}.section-heading{display:flex;align-items:center;justify-content:space-between;margin:30rpx 2rpx 14rpx;color:#254d45;font-size:30rpx;font-weight:800}.section-more{display:flex;align-items:center;color:#2e806d;font-size:22rpx;font-weight:400}.product-card{display:inline-flex;flex-direction:column;width:166rpx;margin-right:14rpx;padding-bottom:10rpx;border-radius:16rpx;background:#fff;overflow:hidden;box-shadow:0 5rpx 16rpx rgba(35,90,73,.05)}.product-visual{display:flex;align-items:center;justify-content:center;height:116rpx;background:#e1f3ec;overflow:hidden}.product-visual image{width:100%;height:100%}.product-title{margin:12rpx 12rpx 0;color:#334743;font-size:24rpx}.product-price{margin:8rpx 12rpx 0;color:#e7604b;font-size:24rpx;font-weight:700}.product-unit{margin:2rpx 12rpx 0;color:#9aa7a2;font-size:18rpx}.install-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12rpx}.install-card{display:flex;flex-direction:column;padding:12rpx;border-radius:16rpx;background:#fff;box-shadow:0 5rpx 16rpx rgba(35,90,73,.05)}.install-visual{display:flex;align-items:center;justify-content:center;height:130rpx;border-radius:12rpx;background:#e1f3ec;overflow:hidden}.install-visual image{width:100%;height:100%}.install-card>text{margin-top:10rpx;color:#3b4e49;font-size:23rpx}.install-price{color:#e7604b!important;font-size:19rpx!important}.appliance-card{display:flex;align-items:center;gap:16rpx;padding:20rpx;border-radius:18rpx;background:#fff;box-shadow:0 5rpx 16rpx rgba(35,90,73,.05)}.appliance-icon{display:flex;align-items:center;justify-content:center;width:88rpx;height:88rpx;border-radius:20rpx;background:#e0f1eb;overflow:hidden}.appliance-icon image{width:100%;height:100%}.appliance-card>view:nth-child(2){display:flex;flex:1;flex-direction:column}.appliance-title{color:#2d4641;font-size:27rpx;font-weight:700}.appliance-desc{margin-top:7rpx;color:#94a19d;font-size:21rpx}.appliance-price{margin-top:7rpx;color:#e7604b;font-size:25rpx;font-weight:700}.appliance-price text{color:#98a49f;font-size:19rpx;font-weight:400}.empty-section,.loading-line{padding:48rpx 0;color:#8b9b96;text-align:center;font-size:24rpx}.startup-page{display:flex;align-items:center;justify-content:center;min-height:100vh;padding:80rpx 32rpx;background:#f5f7f6}.startup-brand{display:flex;align-items:center;margin-bottom:56rpx}.startup-logo{width:64rpx;height:64rpx}.startup-brand-name{margin-left:18rpx;color:#245d55;font-size:30rpx;font-weight:700}.startup-panel{display:flex;width:100%;flex-direction:column;align-items:center;padding:58rpx 38rpx;text-align:center}.startup-title{margin-top:24rpx;color:#2c4943;font-size:34rpx;font-weight:700}.startup-message{margin-top:14rpx;color:#91a09b;font-size:25rpx}.startup-retry{margin-top:30rpx;color:#fff;background:#2e806d}
.city-bar{display:flex;align-items:center;justify-content:space-between;height:72rpx;margin-bottom:12rpx;padding:0 22rpx;border-radius:18rpx;background:#fff;color:#55756c;font-size:23rpx;box-shadow:0 5rpx 18rpx rgba(35,90,73,.04)}.city-bar-left{display:flex;align-items:center;gap:8rpx}.city-name{margin-left:6rpx;color:#245d55;font-weight:700}.search-row{display:flex;align-items:center;height:72rpx;padding:0 18rpx;border-radius:38rpx;background:#fff;box-shadow:0 5rpx 18rpx rgba(35,90,73,.04)}.city-select-inline{display:flex;flex:0 0 auto;align-items:center;gap:5rpx;max-width:180rpx;color:#3d5b53;font-size:23rpx}.city-select-label{max-width:132rpx;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.search-divider{width:1rpx;height:34rpx;margin:0 16rpx;background:#e2ebe7}.search-entry{display:flex;flex:1;align-items:center;min-width:0;gap:10rpx;color:#a0ada8;font-size:23rpx}.search-entry text{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.category-error{margin-top:18rpx;color:#a0ada8;text-align:center;font-size:22rpx}.retry-button{margin-top:16rpx;padding:0 28rpx;color:#2e806d;background:transparent;font-size:22rpx;line-height:60rpx}
</style>
