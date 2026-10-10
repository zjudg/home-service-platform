<template>
  <view class="page home-page safe-bottom">
    <view class="hero surface">
      <view>
        <text class="eyebrow">Zjude Aden</text>
        <text class="hero-title">{{ authStore.isLoggedIn ? `你好，${displayName}` : '欢迎使用' }}</text>
        <text class="hero-subtitle">{{ authStore.isLoggedIn ? '欢迎回到你的移动工作台' : '这是一个无需登录即可访问的基础工作台' }}</text>
      </view>
      <image class="hero-logo" src="/static/logo.png" mode="aspectFit" />
    </view>

    <view class="section-head">
      <text class="section-title">快捷入口</text>
      <text class="muted">基础能力</text>
    </view>
    <view class="quick-grid">
      <view v-for="item in shortcuts" :key="item.title" class="quick-item surface" @click="showComingSoon(item.title)">
        <uni-icons :type="item.icon" size="26" color="#2f6fed" />
        <text>{{ item.title }}</text>
      </view>
    </view>

    <view class="section-head section-head--recent">
      <text class="section-title">系统状态</text>
    </view>
    <view class="status-card surface">
      <view class="status-row"><text>当前环境</text><text class="status-value">{{ appStore.platform }}</text></view>
      <view class="status-row"><text>登录状态</text><text class="status-value" :class="{ 'status-value--ok': authStore.isLoggedIn }">{{ authStore.isLoggedIn ? '已登录' : '游客访问' }}</text></view>
      <view class="status-row"><text>框架版本</text><text class="status-value">Vue 3 + uni-app</text></view>
    </view>
    <button v-if="!authStore.isLoggedIn" class="login-entry" @click="goLogin">登录后使用账户功能</button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const appStore = useAppStore()
const authStore = useAuthStore()
const displayName = computed(() => authStore.user?.nickName || authStore.user?.userName || '用户')
const shortcuts = [
  { title: '个人资料', icon: 'person' },
  { title: '消息中心', icon: 'chat' },
  { title: '帮助中心', icon: 'help' },
  { title: '关于应用', icon: 'info' }
]

function showComingSoon (title) {
  uni.showToast({ title: `${title}即将开放`, icon: 'none' })
}

function goLogin () {
  uni.navigateTo({ url: '/pages/login/index' })
}
</script>

<style lang="scss" scoped>
.hero { display: flex; align-items: center; justify-content: space-between; padding: 42rpx 36rpx; background: linear-gradient(135deg, #2f6fed, #5b8df4); border: 0; box-shadow: 0 18rpx 40rpx rgba(47, 111, 237, 0.22); }
.eyebrow, .hero-title, .hero-subtitle { display: block; color: #fff; }
.eyebrow { opacity: .75; font-size: 24rpx; }
.hero-title { margin-top: 12rpx; font-size: 40rpx; font-weight: 700; }
.hero-subtitle { margin-top: 14rpx; opacity: .82; font-size: 25rpx; }
.hero-logo { width: 104rpx; height: 104rpx; padding: 14rpx; border-radius: 24rpx; background: rgba(255, 255, 255, .16); }
.section-head { display: flex; align-items: baseline; justify-content: space-between; margin: 48rpx 4rpx 22rpx; }
.section-head--recent { margin-top: 48rpx; }
.quick-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20rpx; }
.quick-item { display: flex; align-items: center; gap: 20rpx; min-height: 126rpx; padding: 0 28rpx; color: $app-text; font-size: 28rpx; }
.status-card { padding: 8rpx 28rpx; }
.status-row { display: flex; align-items: center; justify-content: space-between; padding: 28rpx 0; color: $app-muted; font-size: 27rpx; border-bottom: 1rpx solid $app-border; }
.status-row:last-child { border-bottom: 0; }
.status-value { color: $app-text; }
.status-value--ok { color: #23a26d; }
.login-entry { margin-top: 36rpx; color: $app-primary; background: transparent; border: 1rpx solid #c9d9f7; font-size: 28rpx; line-height: 82rpx; border-radius: 16rpx; }
</style>
