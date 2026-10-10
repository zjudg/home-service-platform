<template>
  <view class="page safe-bottom">
    <view class="profile surface">
      <view class="avatar"><text>{{ initials }}</text></view>
      <view class="profile-copy">
        <text class="profile-name">{{ displayName }}</text>
        <text class="profile-meta">{{ authStore.user?.email || '已登录用户' }}</text>
      </view>
    </view>
    <view class="section-title user-title">账户信息</view>
    <view class="info-list surface">
      <view class="info-row"><text>用户名</text><text class="info-value">{{ authStore.user?.userName || '-' }}</text></view>
      <view class="info-row"><text>昵称</text><text class="info-value">{{ authStore.user?.nickName || '-' }}</text></view>
      <view class="info-row"><text>手机号</text><text class="info-value">{{ authStore.user?.phonenumber || '-' }}</text></view>
      <view class="info-row"><text>邮箱</text><text class="info-value">{{ authStore.user?.email || '-' }}</text></view>
    </view>
    <button class="primary-button logout-button" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const displayName = computed(() => authStore.user?.nickName || authStore.user?.userName || '用户')
const initials = computed(() => displayName.value.slice(0, 1).toUpperCase())

async function logout () {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    success: async ({ confirm }) => {
      if (!confirm) return
      await authStore.signOut()
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}
</script>

<style lang="scss" scoped>
.profile { display: flex; align-items: center; padding: 36rpx 32rpx; }
.avatar { display: flex; align-items: center; justify-content: center; width: 112rpx; height: 112rpx; border-radius: 50%; background: #e6efff; color: $app-primary; font-size: 44rpx; font-weight: 700; }
.profile-copy { display: flex; flex-direction: column; margin-left: 26rpx; }
.profile-name { color: $app-text; font-size: 36rpx; font-weight: 700; }
.profile-meta { margin-top: 10rpx; color: $app-muted; font-size: 25rpx; }
.user-title { margin: 48rpx 4rpx 20rpx; }
.info-list { padding: 0 28rpx; }
.info-row { display: flex; justify-content: space-between; padding: 30rpx 0; color: $app-muted; font-size: 27rpx; border-bottom: 1rpx solid $app-border; }
.info-row:last-child { border-bottom: 0; }
.info-value { max-width: 60%; overflow: hidden; color: $app-text; text-overflow: ellipsis; white-space: nowrap; }
.logout-button { width: 100%; margin-top: 54rpx; background: #fff; color: #e45656; border: 1rpx solid #f2cccc; }
</style>
