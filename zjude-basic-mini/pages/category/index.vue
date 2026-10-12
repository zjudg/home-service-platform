<template>
  <view class="page safe-bottom">
    <view v-if="category.bannerTitle || category.bannerSubtitle" class="category-hero surface">
      <text v-if="category.bannerTitle" class="category-title">{{ category.bannerTitle }}</text>
      <text v-if="category.bannerSubtitle" class="category-subtitle">{{ category.bannerSubtitle }}</text>
    </view>
    <view class="service-entry surface" @click="openServices"><view class="service-entry-icon"><uni-icons type="search" size="25" color="#2e806d" /></view><view><text class="service-entry-title">浏览全部家政服务</text><text class="service-entry-desc">保洁、清洗、陪护、搬家，一站预约</text></view><uni-icons type="right" size="18" color="#9aa9a5" /></view>
    <view v-if="navItems.length" class="category-list">
      <view v-for="item in navItems" :key="item.name" class="category-item surface" @click="openItem(item)">
        <image v-if="item.icon" class="category-icon" :src="assetUrl(item.icon)" mode="aspectFit" />
        <view v-else class="category-icon category-icon--fallback">{{ item.name?.slice(0, 1) || '分' }}</view>
        <view class="category-copy">
          <text class="category-name">{{ item.name }}</text>
          <text v-if="item.description" class="category-desc">{{ item.description }}</text>
        </view>
        <uni-icons type="right" size="18" color="#9aa9a5" />
      </view>
    </view>
    <view v-else class="empty-panel surface">
      <text class="empty-title">{{ category.emptyTitle || '暂无内容' }}</text>
      <text v-if="category.emptyDescription" class="empty-description">{{ category.emptyDescription }}</text>
    </view>
    <AppTabBar current-key="category" />
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'

const configStore = useConfigStore()
const category = computed(() => configStore.category || {})
const navItems = computed(() => category.value.navItems || configStore.home.navItems || [])

onMounted(async () => {
  await configStore.load().catch(() => {})
  uni.setNavigationBarTitle({ title: category.value.pageTitle || findLabel('category') || configStore.appTitle })
})

function findLabel (key) {
  return configStore.navigation.items.find(item => item.key === key)?.name
}

function openItem (item) {
  const path = item.modulePath || item.link || item.customLink
  if (path) uni.navigateTo({ url: path.startsWith('/') ? path : `/${path}` })
}
function openServices () { uni.navigateTo({ url: '/pages/services/index' }) }
</script>

<style lang="scss" scoped>
.category-hero { padding: 34rpx 30rpx; background: $app-primary; color: #fff; }
.category-title, .category-subtitle { display: block; }
.category-title { font-size: 38rpx; font-weight: 700; }
.category-subtitle { margin-top: 12rpx; opacity: .82; font-size: 25rpx; }
.category-list { margin-top: 24rpx; }
.category-item { display: flex; align-items: center; min-height: 132rpx; margin-bottom: 18rpx; padding: 22rpx 26rpx; }
.category-icon { flex: 0 0 auto; width: 64rpx; height: 64rpx; margin-right: 22rpx; }
.category-icon--fallback { display: flex; align-items: center; justify-content: center; border-radius: 18rpx; background: $app-primary-soft; color: $app-primary; font-size: 30rpx; }
.category-copy { flex: 1; min-width: 0; }
.category-name, .category-desc { display: block; }
.category-name { color: $app-text; font-size: 29rpx; font-weight: 600; }
.category-desc { margin-top: 8rpx; color: $app-muted; font-size: 24rpx; }
.empty-panel { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300rpx; margin-top: 24rpx; padding: 32rpx; text-align: center; }
.empty-title { color: $app-text; font-size: 30rpx; font-weight: 600; }
.empty-description { margin-top: 12rpx; color: $app-muted; font-size: 25rpx; }
.service-entry{display:flex;align-items:center;gap:18rpx;margin-top:24rpx;padding:24rpx}.service-entry-icon{display:flex;align-items:center;justify-content:center;width:76rpx;height:76rpx;border-radius:20rpx;background:$app-primary-soft}.service-entry-title,.service-entry-desc{display:block}.service-entry-title{color:$app-text;font-size:29rpx;font-weight:700}.service-entry-desc{margin-top:8rpx;color:$app-muted;font-size:23rpx}
</style>

