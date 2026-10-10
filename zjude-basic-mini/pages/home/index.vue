<template>
  <view class="page home-page safe-bottom">
    <view class="hero surface" :style="heroStyle">
      <view>
        <text class="eyebrow">{{ configStore.appTitle }}</text>
        <text class="hero-title">{{ configStore.home.seasonTitle || (authStore.isLoggedIn ? `你好，${displayName}` : configStore.appTitle) }}</text>
        <text v-if="configStore.home.seasonSubtitle" class="hero-subtitle">{{ configStore.home.seasonSubtitle }}</text>
      </view>
      <image class="hero-logo" :src="assetUrl(configStore.site.mobileLogo) || '/static/logo.png'" mode="aspectFit" />
    </view>

    <template v-if="configStore.home.navItems.length">
      <view class="section-head">
        <text class="section-title">{{ configStore.home.navTitle || '快捷入口' }}</text>
      </view>
      <view class="quick-grid">
        <view v-for="item in configStore.home.navItems" :key="item.name" class="quick-item surface" @click="openNavItem(item)">
          <image v-if="item.icon" class="quick-icon" :src="assetUrl(item.icon)" mode="aspectFit" />
          <uni-icons v-else type="forward" size="26" color="#2f6fed" />
          <text>{{ item.name }}</text>
        </view>
      </view>
    </template>

    <view class="section-head section-head--recent">
      <text class="section-title">系统状态</text>
    </view>
    <view class="status-card surface">
      <view class="status-row"><text>当前环境</text><text class="status-value">{{ appStore.platform }}</text></view>
      <view class="status-row"><text>登录状态</text><text class="status-value" :class="{ 'status-value--ok': authStore.isLoggedIn }">{{ authStore.isLoggedIn ? '已登录' : '游客访问' }}</text></view>
      <view class="status-row"><text>框架版本</text><text class="status-value">Vue 3 + uni-app</text></view>
    </view>
    <button v-if="!authStore.isLoggedIn" class="login-entry" @click="goLogin">登录后使用账户功能</button>
    <AppTabBar current-key="home" />
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'

const appStore = useAppStore()
const authStore = useAuthStore()
const configStore = useConfigStore()
const displayName = computed(() => authStore.user?.nickName || authStore.user?.userName || '用户')
const heroStyle = computed(() => {
  const background = assetUrl(configStore.home.topBackground)
  return background
    ? { backgroundImage: `url(${background})`, backgroundSize: 'cover', backgroundPosition: 'center' }
    : {}
})

onMounted(async () => {
  await configStore.load().catch(() => {})
  uni.setNavigationBarTitle({ title: configStore.appTitle })
})

function openNavItem (item) {
  const path = item.modulePath || item.link || item.customLink
  if (path) {
    uni.navigateTo({ url: path.startsWith('/') ? path : `/${path}` })
    return
  }
  uni.showToast({ title: item.name || '暂未配置', icon: 'none' })
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
.quick-icon { width: 52rpx; height: 52rpx; }
.status-card { padding: 8rpx 28rpx; }
.status-row { display: flex; align-items: center; justify-content: space-between; padding: 28rpx 0; color: $app-muted; font-size: 27rpx; border-bottom: 1rpx solid $app-border; }
.status-row:last-child { border-bottom: 0; }
.status-value { color: $app-text; }
.status-value--ok { color: #23a26d; }
.login-entry { margin-top: 36rpx; color: $app-primary; background: transparent; border: 1rpx solid #c9d9f7; font-size: 28rpx; line-height: 82rpx; border-radius: 16rpx; }
</style>
