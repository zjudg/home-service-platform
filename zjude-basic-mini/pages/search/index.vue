<template>
  <view class="page search-page safe-bottom">
    <UiNavbar :back="true">
      <template #title>
        <view class="nav-search">
          <uni-icons type="search" size="16" color="#a39b95" />
          <input
            v-model="keyword"
            class="nav-search__input"
            focus
            confirm-type="search"
            placeholder="搜索保洁、家电清洗、陪护"
            @confirm="submitSearch"
          />
          <uni-icons v-if="keyword" type="clear" size="16" color="#c4bcb6" @click="keyword = ''" />
        </view>
      </template>
      <template #right>
        <text class="nav-action" @click="submitSearch">搜索</text>
      </template>
    </UiNavbar>

    <view v-if="!keyword && recentSearches.length" class="history-section">
      <view class="section-heading">
        <text>最近搜索</text>
        <uni-icons type="trash" size="18" color="#969696" @click="clearHistory" />
      </view>
      <view class="keyword-list">
        <text v-for="term in recentSearches" :key="term" class="keyword-chip" @click="searchTerm(term)">{{ term }}</text>
      </view>
    </view>

    <view v-if="!keyword" class="history-section">
      <view class="section-heading"><text>服务分类</text></view>
      <view class="keyword-list">
        <text v-for="category in categories" :key="category.categoryId" class="keyword-chip" @click="searchTerm(category.categoryName)">{{ category.categoryName }}</text>
      </view>
    </view>

    <view class="section-heading result-heading">
      <text>{{ keyword ? '搜索结果' : '推荐服务' }}</text>
      <text class="result-count">{{ filteredServices.length }} 项服务</text>
    </view>
    <view v-if="loading" class="empty-state surface">正在加载服务...</view>
    <view v-else-if="filteredServices.length" class="service-list">
      <view v-for="item in filteredServices" :key="item.serviceId" class="service-card surface" @click="openDetail(item.serviceId)">
        <view class="service-cover">
          <image v-if="serviceImage(item)" :src="assetUrl(serviceImage(item))" mode="aspectFit" />
          <uni-icons v-else type="home" size="30" color="#ff6b3d" />
        </view>
        <view class="service-copy">
          <text class="service-name">{{ item.serviceName }}</text>
          <text class="service-intro">{{ item.serviceIntro || item.categoryName || '查看服务内容与可预约时间' }}</text>
          <text class="service-price">¥{{ item.price }}<text> / {{ item.unitName || '次' }}</text></text>
        </view>
      </view>
    </view>
    <view v-else class="empty-state surface">
      <uni-icons type="search" size="30" color="#ff6b3d" />
      <text>{{ errorMessage || (keyword ? '没有找到相关服务，试试其他关键词' : '暂无可推荐服务') }}</text>
    </view>
  </view>

  <AppFeedback />
</template>

<script setup>
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { listCategories, listServices } from '@/api/service'
import { assetUrl } from '@/utils/assets'

const keyword = ref('')
const services = ref([])
const categories = ref([])
const storedSearches = uni.getStorageSync('miniRecentServiceSearches')
const recentSearches = ref(Array.isArray(storedSearches) ? storedSearches : [])
const loading = ref(false)
const errorMessage = ref('')
const filteredServices = computed(() => {
  const query = keyword.value.trim().toLocaleLowerCase()
  if (!query) return services.value.slice(0, 8)
  return services.value.filter(item => `${item.serviceName || ''}${item.serviceIntro || ''}${item.categoryName || ''}`.toLocaleLowerCase().includes(query))
})

function responseList (response) {
  const value = response?.data ?? response
  if (Array.isArray(value)) return value
  return value?.rows || value?.list || response?.rows || []
}

function serviceImage (item) {
  const images = item?.productImage || item?.carouselImages || item?.imageUrl || ''
  return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || ''
}

onMounted(async () => {
  const options = getCurrentPages().at(-1)?.options || {}
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : ''
  loading.value = true
  try {
    const [serviceResponse, categoryResponse] = await Promise.all([listServices(), listCategories()])
    services.value = responseList(serviceResponse)
    categories.value = responseList(categoryResponse)
  } catch (error) {
    errorMessage.value = error.message || '服务加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
})

function submitSearch () {
  const term = keyword.value.trim()
  if (!term) return
  recentSearches.value = [term, ...recentSearches.value.filter(item => item !== term)].slice(0, 8)
  uni.setStorageSync('miniRecentServiceSearches', recentSearches.value)
}

function searchTerm (term) {
  keyword.value = term
  submitSearch()
}

function clearHistory () {
  recentSearches.value = []
  uni.removeStorageSync('miniRecentServiceSearches')
}

function openDetail (id) {
  if (id) uni.navigateTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(id)}` })
}

</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.search-page { padding-top: $space-2; }

/* 导航栏内的搜索输入 */
.nav-search {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: center;
  gap: 8rpx;
  height: 64rpx;
  padding: 0 $space-2;
  border-radius: $radius-pill;
  background: $bg-sunken;
}

.nav-search__input {
  flex: 1;
  min-width: 0;
  font-size: $font-micro;
  color: $ink-900;
}

.nav-action {
  padding: 0 $space-1;
  color: $brand-600;
  font-size: $font-caption;
  font-weight: $weight-semibold;
}
.history-section { margin-top: 34rpx; }
.section-heading { display: flex; align-items: center; justify-content: space-between; color: $app-text; font-size: 28rpx; font-weight: 700; }
.keyword-list { display: flex; flex-wrap: wrap; gap: 14rpx; margin-top: 18rpx; }
.keyword-chip { padding: 12rpx 20rpx; color: $app-text-soft; background: #fff; border-radius: 999rpx; font-size: 22rpx; }
.result-heading { margin: 38rpx 2rpx 16rpx; }
.result-count { color: $app-muted; font-size: 21rpx; font-weight: 400; }
.service-list { display: flex; flex-direction: column; gap: 14rpx; }
.service-card { display: flex; align-items: center; padding: 18rpx; }
.service-cover { display: flex; flex: 0 0 140rpx; align-items: center; justify-content: center; width: 140rpx; height: 140rpx; overflow: hidden; border-radius: 18rpx; background: #f4f4f4; }
.service-cover image { width: 100%; height: 100%; }
.service-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; margin-left: 18rpx; }
.service-name { color: $app-text; font-size: 27rpx; font-weight: 700; }
.service-intro { display: -webkit-box; margin-top: 8rpx; color: $app-muted; font-size: 21rpx; line-height: 1.4; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.service-price { margin-top: 12rpx; color: $app-primary; font-size: 29rpx; font-weight: 800; }
.service-price text { color: $app-muted; font-size: 19rpx; font-weight: 400; }
.empty-state { display: flex; min-height: 280rpx; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; margin-top: 12rpx; padding: 28rpx; color: $app-muted; font-size: 23rpx; text-align: center; }
</style>
