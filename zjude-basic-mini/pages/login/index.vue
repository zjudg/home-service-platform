<template>
  <view class="login-page">
    <view class="login-brand">
      <image class="brand-logo" src="/static/logo.png" mode="aspectFit" />
      <text class="brand-name">Zjude Aden</text>
      <text class="brand-subtitle">通用移动端工作台</text>
    </view>
    <view class="login-panel surface">
      <text class="panel-title">欢迎登录</text>
      <text class="panel-subtitle">使用管理系统账号继续</text>
      <view class="field">
        <text class="field-label">账号</text>
        <input v-model="form.username" class="field-input" placeholder="请输入账号" maxlength="64" />
      </view>
      <view class="field">
        <text class="field-label">密码</text>
        <input v-model="form.password" class="field-input" password placeholder="请输入密码" maxlength="64" @confirm="submit" />
      </view>
      <view v-if="errorMessage" class="error-message">{{ errorMessage }}</view>
      <button class="primary-button login-button" :loading="loading" @click="submit">登录</button>
    </view>
    <text class="login-footer">安全、统一、跨端的基础应用框架</text>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({ username: '', password: '' })

onShow(() => {
  if (authStore.isLoggedIn) uni.switchTab({ url: '/pages/home/index' })
})

async function submit () {
  if (!form.username.trim() || !form.password) {
    errorMessage.value = '请输入账号和密码'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    await authStore.signIn({ username: form.username.trim(), password: form.password, code: '', uuid: '' })
    uni.switchTab({ url: '/pages/home/index' })
  } catch (error) {
    errorMessage.value = error.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page { min-height: 100vh; padding: 120rpx 40rpx 48rpx; background: linear-gradient(155deg, #edf4ff 0%, #f8faff 46%, #f5f7fb 100%); }
.login-brand { display: flex; flex-direction: column; align-items: center; }
.brand-logo { width: 112rpx; height: 112rpx; margin-bottom: 26rpx; border-radius: 24rpx; background: #fff; }
.brand-name { color: $app-text; font-size: 42rpx; font-weight: 800; }
.brand-subtitle { margin-top: 12rpx; color: $app-muted; font-size: 26rpx; }
.login-panel { margin-top: 72rpx; padding: 44rpx 36rpx 40rpx; box-shadow: 0 18rpx 50rpx rgba(38, 74, 135, 0.08); }
.panel-title { display: block; color: $app-text; font-size: 38rpx; font-weight: 700; }
.panel-subtitle { display: block; margin-top: 12rpx; color: $app-muted; font-size: 25rpx; }
.field { margin-top: 38rpx; padding-bottom: 18rpx; border-bottom: 1rpx solid $app-border; }
.field-label { display: block; margin-bottom: 14rpx; color: $app-muted; font-size: 24rpx; }
.field-input { width: 100%; height: 54rpx; color: $app-text; font-size: 30rpx; }
.error-message { margin-top: 20rpx; color: #e45656; font-size: 24rpx; }
.login-button { margin-top: 42rpx; width: 100%; }
.login-footer { display: block; margin-top: 48rpx; color: #a5afbf; font-size: 23rpx; text-align: center; }
</style>
