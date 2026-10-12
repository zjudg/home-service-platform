<template>
  <view class="page safe-bottom">
    <view class="cart-hero surface">
      <text class="cart-title">{{ pageTitle }}</text>
      <text v-if="cartSubtitle" class="cart-subtitle">{{ cartSubtitle }}</text>
    </view>
    <view class="empty-panel surface">
      <view class="empty-icon">{{ pageTitle.slice(0, 1) || '购' }}</view>
      <text class="empty-title">{{ emptyTitle }}</text>
      <text v-if="emptyDescription" class="empty-description">{{ emptyDescription }}</text>
    </view>
    <AppTabBar current-key="cart" />
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
const cartConfig = computed(() => configStore.category?.cart || {})
const pageTitle = computed(() => findLabel('cart') || configStore.appTitle)
const cartSubtitle = computed(() => cartConfig.value.subtitle || '')
const emptyTitle = computed(() => cartConfig.value.emptyTitle || '')
const emptyDescription = computed(() => cartConfig.value.emptyDescription || '')

onMounted(async () => {
  await configStore.load().catch(() => {})
  uni.setNavigationBarTitle({ title: pageTitle.value })
})

function findLabel (key) {
  return configStore.navigation.items.find(item => item.key === key)?.name
}
</script>

<style lang="scss" scoped>
.cart-hero { padding: 34rpx 30rpx; background: $app-primary; color: #fff; }
.cart-title, .cart-subtitle { display: block; }
.cart-title { font-size: 38rpx; font-weight: 700; }
.cart-subtitle { margin-top: 12rpx; opacity: .82; font-size: 25rpx; }
.empty-panel { display: flex; flex-direction: column; align-items: center; margin-top: 24rpx; padding: 80rpx 30rpx; text-align: center; }
.empty-icon { display: flex; align-items: center; justify-content: center; width: 100rpx; height: 100rpx; border-radius: 30rpx; background: $app-primary-soft; color: $app-primary; font-size: 42rpx; font-weight: 700; }
.empty-title { margin-top: 28rpx; color: $app-text; font-size: 30rpx; font-weight: 600; }
.empty-description { margin-top: 12rpx; color: $app-muted; font-size: 25rpx; }
</style>
