<template>
  <view v-if="loading" class="app-status app-status--loading">
    <view class="status-spinner" />
    <text>{{ loadingText }}</text>
  </view>
  <view v-else-if="error" class="app-status">
    <text class="status-title">{{ errorTitle }}</text>
    <text class="status-message">{{ error }}</text>
    <button class="status-button" @click="$emit('retry')">重新加载</button>
  </view>
</template>

<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  loadingText: { type: String, default: '加载中...' },
  errorTitle: { type: String, default: '加载失败' }
})

defineEmits(['retry'])
</script>

<style lang="scss" scoped>
.app-status { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 100rpx 40rpx; color: $uni-text-color-grey; }
.status-title { color: $uni-text-color; font-size: 32rpx; font-weight: 600; }
.status-message { margin-top: 14rpx; font-size: 26rpx; text-align: center; }
.status-button { margin-top: 28rpx; padding: 0 32rpx; background: $uni-color-primary; color: #fff; font-size: 26rpx; line-height: 72rpx; border-radius: 36rpx; }
.status-spinner { width: 42rpx; height: 42rpx; margin-bottom: 24rpx; border: 5rpx solid #dfe8f5; border-top-color: $uni-color-primary; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
