<template>
  <view class="login-page">
    <view class="login-brand"><image class="brand-logo" src="/static/logo.png" mode="aspectFit" /><text class="brand-name">妙洁家政</text><text class="brand-subtitle">专业服务，放心到家</text></view>
    <view class="login-panel surface">
      <text class="panel-title">欢迎登录</text><text class="panel-subtitle">登录后即可预约并管理家政服务</text>
      <button v-if="isWechat" class="wechat-button" open-type="chooseAvatar" @chooseavatar="onChooseAvatar"><uni-icons type="weixin" size="24" color="#fff" />微信授权登录</button>
      <button v-else class="wechat-button" @click="startWechatAuth"><uni-icons type="weixin" size="24" color="#fff" />微信授权登录</button>
      <view class="divider"><text>或使用账号登录</text></view>
      <view class="field"><text class="field-label">账号</text><input v-model="form.username" class="field-input" placeholder="请输入账号" maxlength="64" /></view>
      <view class="field"><text class="field-label">密码</text><input v-model="form.password" class="field-input" password placeholder="请输入密码" maxlength="64" @confirm="submit" /></view>
      <view v-if="errorMessage" class="error-message">{{ errorMessage }}</view><button class="primary-button login-button" :loading="loading" @click="submit">账号登录</button>
    </view>
    <view v-if="showWechatPanel" class="wechat-mask"><view class="wechat-panel surface"><text class="wechat-title">完善微信资料</text><text class="wechat-desc">授权后用于展示头像和称呼</text><button class="avatar-picker" open-type="chooseAvatar" @chooseavatar="onChooseAvatar"><image v-if="wechat.avatarUrl" :src="wechat.avatarUrl" /><uni-icons v-else type="person" size="38" color="#7b9b91" /></button><input v-model="wechat.nickname" type="nickname" class="nickname-input" placeholder="请输入昵称" @blur="onNicknameBlur" /><view class="wechat-actions"><button class="cancel-button" @click="showWechatPanel = false">取消</button><button class="confirm-button" :loading="wechatLoading" @click="confirmWechatLogin">确认授权</button></view></view></view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore(); const loading = ref(false); const wechatLoading = ref(false); const errorMessage = ref(''); const showWechatPanel = ref(false); const redirect = ref(''); const isWechat = ref(false)
const form = reactive({ username: '', password: '' }); const wechat = reactive({ avatarUrl: '', nickname: '' })
onShow(() => { const options = getCurrentPages().at(-1)?.options || {}; redirect.value = options.redirect ? decodeURIComponent(options.redirect) : ''; isWechat.value = process.env.UNI_PLATFORM === 'mp-weixin'; if (authStore.isLoggedIn) redirectAfterLogin() })
function onChooseAvatar (event) { wechat.avatarUrl = event.detail?.avatarUrl || ''; showWechatPanel.value = true }
function onNicknameBlur (event) { if (event.detail?.value) wechat.nickname = event.detail.value }
function startWechatAuth () { uni.showToast({ title: '请在微信小程序中完成授权', icon: 'none' }) }
async function confirmWechatLogin () { if (!wechat.nickname.trim()) return uni.showToast({ title: '请输入昵称', icon: 'none' }); wechatLoading.value = true; errorMessage.value = ''; try { const loginResult = await new Promise((resolve, reject) => uni.login({ provider: 'weixin', success: resolve, fail: reject })); await authStore.signInWithWechat({ code: loginResult.code, nickname: wechat.nickname.trim(), avatarUrl: wechat.avatarUrl }); redirectAfterLogin() } catch (error) { errorMessage.value = error.message || '微信登录失败，请稍后重试' } finally { wechatLoading.value = false } }
async function submit () { if (!form.username.trim() || !form.password) return (errorMessage.value = '请输入账号和密码'); loading.value = true; errorMessage.value = ''; try { await authStore.signIn({ username: form.username.trim(), password: form.password, code: '', uuid: '' }); redirectAfterLogin() } catch (error) { errorMessage.value = error.message || '登录失败，请稍后重试' } finally { loading.value = false } }
function redirectAfterLogin () { const target = redirect.value || '/pages/home/index'; const tabPages = ['/pages/home/index', '/pages/category/index', '/pages/orders/index', '/pages/user/index']; if (tabPages.includes(target)) uni.reLaunch({ url: target }); else uni.redirectTo({ url: target }) }
</script>

<style lang="scss" scoped>
.login-page{min-height:100vh;padding:120rpx 40rpx 48rpx;background:linear-gradient(155deg,#edf7f2 0%,#f8fbf9 50%,#f4f7f5 100%)}.login-brand{display:flex;flex-direction:column;align-items:center}.brand-logo{width:112rpx;height:112rpx;margin-bottom:26rpx;border-radius:24rpx;background:#fff}.brand-name{color:#245d55;font-size:42rpx;font-weight:800}.brand-subtitle{margin-top:12rpx;color:#7f9a91;font-size:26rpx}.login-panel{margin-top:72rpx;padding:44rpx 36rpx 40rpx;box-shadow:0 18rpx 50rpx rgba(38,90,72,.08)}.panel-title{display:block;color:$app-text;font-size:38rpx;font-weight:700}.panel-subtitle{display:block;margin-top:12rpx;color:$app-muted;font-size:25rpx}.wechat-button{display:flex;align-items:center;justify-content:center;gap:12rpx;width:100%;margin-top:38rpx;color:#fff;background:#2e806d;border-radius:14rpx;line-height:88rpx}.divider{display:flex;align-items:center;gap:16rpx;margin:34rpx 0;color:#a3b1ac;font-size:22rpx}.divider:before,.divider:after{content:'';height:1rpx;flex:1;background:#e4ebe7}.field{margin-top:28rpx;padding-bottom:18rpx;border-bottom:1rpx solid $app-border}.field-label{display:block;margin-bottom:14rpx;color:$app-muted;font-size:24rpx}.field-input{width:100%;height:54rpx;color:$app-text;font-size:30rpx}.error-message{margin-top:20rpx;color:#e45656;font-size:24rpx}.login-button{width:100%;margin-top:36rpx}.wechat-mask{position:fixed;inset:0;z-index:20;display:flex;align-items:flex-end;background:rgba(24,48,39,.42)}.wechat-panel{width:100%;padding:42rpx 38rpx calc(34rpx + env(safe-area-inset-bottom));border-radius:28rpx 28rpx 0 0;text-align:center}.wechat-title{display:block;color:$app-text;font-size:34rpx;font-weight:700}.wechat-desc{display:block;margin-top:12rpx;color:$app-muted;font-size:23rpx}.avatar-picker{display:flex;align-items:center;justify-content:center;width:136rpx;height:136rpx;margin:30rpx auto 22rpx;padding:0;border-radius:50%;background:#e1f3ec;overflow:hidden}.avatar-picker image{width:100%;height:100%}.nickname-input{height:78rpx;padding:0 20rpx;border:1rpx solid $app-border;border-radius:12rpx;color:$app-text;text-align:left}.wechat-actions{display:flex;gap:18rpx;margin-top:28rpx}.cancel-button,.confirm-button{flex:1;line-height:78rpx;border-radius:12rpx;font-size:26rpx}.cancel-button{color:$app-muted;background:#f4f7f5}.confirm-button{color:#fff;background:#2e806d}
</style>
