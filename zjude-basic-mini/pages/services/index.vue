<template>
  <view class="page safe-bottom">
    <view class="service-head"><view><text class="eyebrow">HOME SERVICE</text><text class="title">找服务</text></view><view class="location" @click="chooseLocation"><uni-icons type="location" size="18" color="#2e806d" />{{ location }}</view></view>
    <view class="search surface"><uni-icons type="search" size="20" color="#8b98aa" /><input v-model="keyword" placeholder="搜索保洁、家电清洗、陪护" confirm-type="search" /></view>
    <scroll-view scroll-x class="category-scroll"><view v-for="item in categories" :key="item.categoryId" class="category-chip" :class="{ active: activeCategory === item.categoryId }" @click="activeCategory = item.categoryId">{{ item.categoryName }}</view></scroll-view>
    <view class="section-head"><text class="section-title">推荐服务</text><text class="muted">{{ filteredServices.length }} 项服务</text></view>
    <view v-if="loading" class="empty-panel surface">正在加载服务...</view>
    <view v-else-if="filteredServices.length" class="service-list"><view v-for="item in filteredServices" :key="item.serviceId" class="service-card surface" @click="openDetail(item.serviceId)"><view class="service-cover"><image v-if="item.imageUrl" :src="item.imageUrl" mode="aspectFill" /><uni-icons v-else type="home" size="30" color="#2e806d" /></view><view class="service-copy"><text class="service-name">{{ item.serviceName }}</text><text class="service-intro">{{ item.serviceIntro || '暂无服务说明' }}</text><view class="service-bottom"><text class="price">¥{{ item.price }}<text class="unit">/{{ item.unitName || '次' }}</text></text><text class="sold">已预约 {{ item.reserveCount || 0 }} 人</text></view></view></view></view>
    <view v-else class="empty-panel surface">{{ errorMessage || '没有找到匹配的服务' }}</view>
    <AppTabBar current-key="category" />
  </view>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { listCategories, listServices } from '@/api/service'
const keyword = ref(''); const activeCategory = ref(''); const location = ref('杭州'); const categories = ref([]); const services = ref([]); const loading = ref(false); const errorMessage = ref('')
const filteredServices = computed(() => services.value.filter(item => (!activeCategory.value || item.categoryId === activeCategory.value) && (!keyword.value || `${item.serviceName}${item.serviceIntro}`.includes(keyword.value))))
function responseList (response) { return response?.rows || response?.data?.rows || (Array.isArray(response?.data) ? response.data : Array.isArray(response) ? response : []) }
onMounted(async () => { loading.value = true; const options = getCurrentPages().at(-1)?.options || {}; activeCategory.value = options.categoryId || ''; try { const [c, s] = await Promise.all([listCategories(), listServices()]); categories.value = responseList(c); services.value = responseList(s) } catch (e) { errorMessage.value = e.message || '服务数据加载失败' } finally { loading.value = false } uni.setNavigationBarTitle({ title: '服务' }) })
function openDetail (id) { uni.navigateTo({ url: `/pages/service-detail/index?id=${id}` }) }
function chooseLocation () { uni.chooseLocation({ success: result => { location.value = result.name || result.address || location.value }, fail: () => uni.showToast({ title: '暂无法获取位置', icon: 'none' }) }) }
</script>
<style lang="scss" scoped>
.service-head{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:26rpx}.eyebrow{display:block;color:$app-primary;font-size:20rpx;letter-spacing:3rpx}.title{display:block;margin-top:8rpx;color:$app-text;font-size:44rpx;font-weight:800}.location{display:flex;align-items:center;gap:6rpx;color:$app-muted;font-size:24rpx}.search{display:flex;align-items:center;gap:16rpx;padding:0 24rpx;height:84rpx}.search input{flex:1;color:$app-text;font-size:28rpx}.category-scroll{white-space:nowrap;margin:26rpx -32rpx 0;padding:0 32rpx}.category-chip{display:inline-block;margin-right:16rpx;padding:16rpx 26rpx;border-radius:40rpx;background:#fff;color:$app-muted;font-size:25rpx}.category-chip.active{background:$app-primary-soft;color:$app-primary;font-weight:600}.section-head{display:flex;justify-content:space-between;align-items:center;margin:38rpx 4rpx 20rpx}.service-card{display:flex;margin-bottom:18rpx;padding:22rpx}.service-cover{display:flex;align-items:center;justify-content:center;width:150rpx;height:150rpx;border-radius:16rpx;background:$app-primary-tint}.service-copy{display:flex;flex:1;min-width:0;flex-direction:column;margin-left:22rpx}.service-name{color:$app-text;font-size:30rpx;font-weight:700}.service-intro{display:-webkit-box;margin-top:10rpx;color:$app-muted;font-size:24rpx;line-height:1.5;overflow:hidden;-webkit-box-orient:vertical;-webkit-line-clamp:2}.service-bottom{display:flex;align-items:baseline;justify-content:space-between;margin-top:auto}.price{color:$app-accent;font-size:34rpx;font-weight:800}.unit{font-size:23rpx;font-weight:400}.sold{color:#9aa7a2;font-size:22rpx}.empty-panel{padding:72rpx 30rpx;text-align:center;color:$app-muted}
</style>
