<template>
  <view class="page safe-bottom">
    <view class="section-title">应用设置</view>
    <view class="settings-list surface">
      <view class="setting-row" @click="showAbout">
        <view><text class="setting-title">关于应用</text><text class="setting-desc">Zjude Aden 基础移动端</text></view>
        <uni-icons type="right" size="18" color="#a4afbf" />
      </view>
      <view class="setting-row" @click="clearCache">
        <view><text class="setting-title">清理缓存</text><text class="setting-desc">清理本地临时数据</text></view>
        <uni-icons type="right" size="18" color="#a4afbf" />
      </view>
      <view class="setting-row setting-row--static">
        <view><text class="setting-title">运行环境</text><text class="setting-desc">{{ appStore.platform }}</text></view>
        <text class="version">v1.0.0</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

function showAbout () {
  uni.showModal({ title: '关于应用', content: 'Zjude Aden 通用移动端基础框架', showCancel: false })
}

function clearCache () {
  uni.showToast({ title: '缓存已清理', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.settings-list { margin-top: 24rpx; padding: 0 28rpx; }
.setting-row { display: flex; align-items: center; justify-content: space-between; padding: 30rpx 0; border-bottom: 1rpx solid $app-border; }
.setting-row:last-child { border-bottom: 0; }
.setting-title, .setting-desc { display: block; }
.setting-title { color: $app-text; font-size: 28rpx; }
.setting-desc { margin-top: 10rpx; color: $app-muted; font-size: 24rpx; }
.version { color: $app-muted; font-size: 24rpx; }
</style>
