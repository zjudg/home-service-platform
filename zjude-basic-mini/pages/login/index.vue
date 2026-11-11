<template>
  <view class="login-page">
    <UiNavbar :back="true" title="" />

    <view class="login-main">
      <!-- 品牌区 -->
      <view class="brand">
        <view class="brand-logo-wrap">
          <image
            v-if="brandLogo"
            class="brand-logo"
            :src="brandLogo"
            mode="aspectFit"
          />
          <text v-else class="brand-name">
            {{ configStore.appTitle }}
          </text>
        </view>
        <text class="brand-subtitle">专业服务，放心到家</text>
      </view>

      <!-- 微信登录（默认方式） -->
      <view class="login-card card">
        <button class="wechat-btn" :disabled="!agreed" @click="onWechatLogin">
          <uni-icons type="weixin" size="20" color="#ffffff" />
          <text class="wechat-btn__text">微信授权登录</text>
        </button>

        <view class="agreement" @click="agreed = !agreed">
          <view class="agreement-check" :class="{ 'agreement-check--on': agreed }">
            <uni-icons v-if="agreed" type="checkmarkempty" size="11" color="#ffffff" />
          </view>
          <text class="agreement-text">我已阅读并同意</text>
          <text class="agreement-link" @click.stop="showPolicy('用户协议')">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link" @click.stop="showPolicy('隐私政策')">《隐私政策》</text>
        </view>

        <view class="other-login" @click="openPhoneLogin">
          <text class="other-login__text">其他方式登录</text>
          <uni-icons type="right" size="12" color="#a39b95" />
        </view>
      </view>

      <text class="footer-tip">登录即代表您同意我们保护您的个人信息</text>
    </view>

    <!-- 微信资料弹层 -->
    <view v-if="showWechatPanel" class="wechat-mask" @tap.self="showWechatPanel = false">
      <view class="wechat-panel" @tap.stop>
        <view class="panel-handle" />
        <text class="panel-title">完善个人信息</text>
        <text class="panel-desc">头像和昵称将用于服务预约与订单展示</text>

        <view class="avatar-section">
          <button class="avatar-picker" :disabled="avatarUploading" open-type="chooseAvatar" @tap.stop @chooseavatar="onChooseAvatar">
            <image v-if="wechat.avatarUrl" class="avatar-image" :src="wechat.avatarUrl" mode="aspectFill" />
            <view v-else class="avatar-placeholder">
              <view class="avatar-icon">
                <uni-icons type="camera" size="25" color="#c9582d" />
              </view>
            </view>
            <view class="avatar-edit">
              <uni-icons type="compose" size="13" color="#ffffff" />
            </view>
          </button>
          <text class="avatar-hint">{{ avatarUploading ? '头像上传中...' : (wechat.avatarUrl ? '点击更换头像' : '点击选择头像') }}</text>
        </view>

        <view class="nickname-field">
          <text class="field-label">昵称</text>
          <input
            v-model="wechat.nickname"
            type="nickname"
            class="field-input"
            placeholder="点击自动获取微信昵称"
            placeholder-class="field-placeholder"
            @blur="onNicknameBlur"
          />
        </view>

        <view class="panel-actions">
          <button class="ghost-button panel-cancel" @click="showWechatPanel = false">取消</button>
          <button class="primary-button panel-confirm" :disabled="avatarUploading" :loading="wechatLoading" @click="confirmWechatLogin">确定</button>
        </view>
      </view>
    </view>

    <!-- 手机号登录弹层 -->
    <view v-if="showPhonePanel" class="wechat-mask" @tap.self="closePhoneLogin">
      <view class="wechat-panel" @tap.stop>
        <view class="panel-handle" />
        <text class="panel-title">手机号登录</text>
        <text class="panel-desc">未注册的手机号将自动创建账号并登录</text>

        <view class="phone-field">
          <text class="phone-prefix">+86</text>
          <input
            v-model="phone.phone"
            class="field-input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="field-placeholder"
          />
        </view>

        <view class="phone-field">
          <text class="phone-prefix">码</text>
          <input
            v-model="phone.code"
            class="field-input"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            placeholder-class="field-placeholder"
          />
          <text
            class="code-btn"
            :class="{ 'code-btn--disabled': countdown > 0 || sending }"
            @click="sendCode"
          >{{ countdown > 0 ? `${countdown}s` : (sending ? '发送中' : '获取验证码') }}</text>
        </view>

        <text v-if="mockCode" class="mock-tip">开发环境验证码：{{ mockCode }}</text>

        <view class="panel-actions">
          <button class="ghost-button panel-cancel" @click="closePhoneLogin">取消</button>
          <button class="primary-button panel-confirm" :loading="phoneLoading" @click="submitPhoneLogin">确定</button>
        </view>
      </view>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow, onUnload } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { requestSmsCode } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useMasterStore } from '@/stores/master'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'
import { uploadAvatarToStorage } from '@/utils/user-avatar'
import { HOME_PATH, sanitizeRedirect } from '@/utils/guard'
import {
  getMiniProgramMode,
  MINI_PROGRAM_MODE_CUSTOMER,
  MINI_PROGRAM_MODE_MASTER,
  setMiniProgramMode
} from '@/utils/mini-program-mode'
import { showToast, toastSuccess } from '@/utils/feedback'

const authStore = useAuthStore()
const masterStore = useMasterStore()
const configStore = useConfigStore()
const brandLogo = computed(() => assetUrl(configStore.site?.mobileLoginLogo || configStore.site?.mobileLogo || ''))

const redirect = ref('')
const isWechat = ref(false)
const agreed = ref(false)

const showWechatPanel = ref(false)
const wechatLoading = ref(false)
const avatarUploading = ref(false)
const wechat = reactive({ avatarUrl: '', nickname: '' })

const showPhonePanel = ref(false)
const phoneLoading = ref(false)
const sending = ref(false)
const countdown = ref(0)
const mockCode = ref('')
const phone = reactive({ phone: '', code: '' })

let ticker = null

onShow(async () => {
  await configStore.load().catch(() => {})
  const options = getCurrentPages().at(-1)?.options || {}
  redirect.value = options.redirect ? decodeURIComponent(options.redirect) : ''
  isWechat.value = process.env.UNI_PLATFORM === 'mp-weixin'
  if (authStore.isLoggedIn) redirectAfterLogin()
})

onUnload(() => stopCountdown())

/* ---------- 协议 ---------- */
function requireAgreement () {
  if (agreed.value) return true
  showToast('请先阅读并同意用户协议和隐私政策')
  return false
}

function showPolicy (name) {
  const type = name === '隐私政策' ? 'privacy' : 'agreement'
  uni.navigateTo({
    url: `/pages/legal/index?type=${type}`,
    fail: () => showToast(`${name}暂时无法打开，请稍后重试`)
  })
}

/* ---------- 微信登录 ---------- */
function onWechatLogin () {
  if (!requireAgreement()) return
  if (!isWechat.value) {
    showToast('请在微信小程序中进行授权登录')
    return
  }
  wechat.avatarUrl = ''
  wechat.nickname = ''
  showWechatPanel.value = true
}

async function onChooseAvatar (event) {
  // 微信只提供临时文件路径；它只作为上传输入，不绑定到 image，也不写入用户资料。
  const filePath = event.detail?.avatarUrl
  if (!filePath || avatarUploading.value) return
  avatarUploading.value = true
  try {
    // 选择后立即上传，渲染状态只使用后台存储服务返回的永久 URL。
    wechat.avatarUrl = await uploadAvatarToStorage(filePath)
  } catch (error) {
    wechat.avatarUrl = ''
    showToast(error.message || '头像上传失败，请重试')
  } finally {
    avatarUploading.value = false
  }
}

function onNicknameBlur (event) {
  if (event.detail?.value) wechat.nickname = event.detail.value
}

async function confirmWechatLogin () {
  if (!wechat.nickname.trim()) return showToast('请填写昵称')
  wechatLoading.value = true
  try {
    const loginResult = await new Promise((resolve, reject) => uni.login({ provider: 'weixin', success: resolve, fail: reject }))
    await authStore.signInWithWechat({
      code: loginResult.code,
      nickname: wechat.nickname.trim(),
      // This is either empty or the permanent URL returned by the configured
      // storage provider; a wxfile:// temporary path never reaches the API.
      avatarUrl: wechat.avatarUrl
    })
    showWechatPanel.value = false
    redirectAfterLogin()
  } catch (error) {
    showToast(error.message || '微信登录失败，请稍后重试')
  } finally {
    wechatLoading.value = false
  }
}

/* ---------- 手机号登录 ---------- */
function openPhoneLogin () {
  if (!requireAgreement()) return
  phone.code = ''
  mockCode.value = ''
  showPhonePanel.value = true
}

function closePhoneLogin () {
  showPhonePanel.value = false
  stopCountdown()
}

function stopCountdown () {
  if (ticker) clearInterval(ticker)
  ticker = null
  countdown.value = 0
}

function startCountdown (seconds) {
  stopCountdown()
  countdown.value = seconds
  ticker = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) stopCountdown()
  }, 1000)
}

async function sendCode () {
  if (countdown.value > 0 || sending.value) return
  if (!/^1\d{10}$/.test(phone.phone)) return showToast('请输入正确的手机号')
  sending.value = true
  try {
    const response = await requestSmsCode(phone.phone)
    const data = response?.data || response || {}
    mockCode.value = data.mockCode || ''
    showToast(mockCode.value ? `验证码：${mockCode.value}` : '验证码已发送', { type: 'success' })
    startCountdown(Number(data.resendIn) || 60)
  } catch (error) {
    showToast(error.message || '验证码发送失败')
  } finally {
    sending.value = false
  }
}

async function submitPhoneLogin () {
  if (!/^1\d{10}$/.test(phone.phone)) return showToast('请输入正确的手机号')
  if (!/^\d{4,6}$/.test(phone.code)) return showToast('请输入验证码')
  phoneLoading.value = true
  try {
    await authStore.signInWithSms({ phone: phone.phone, code: phone.code })
    showPhonePanel.value = false
    stopCountdown()
    toastSuccess('登录成功')
    redirectAfterLogin()
  } catch (error) {
    showToast(error.message || '登录失败，请稍后重试')
  } finally {
    phoneLoading.value = false
  }
}

/* ---------- 登录后跳转 ---------- */
async function redirectAfterLogin () {
  const target = sanitizeRedirect(redirect.value) || HOME_PATH
  const path = target.split('?')[0]
  const isMasterDestination = path.startsWith('/pages/master/')
  const shouldPreferMaster = isMasterDestination || getMiniProgramMode() !== MINI_PROGRAM_MODE_CUSTOMER

  if (shouldPreferMaster) {
    try {
      await masterStore.loadProfile()
      if (masterStore.applied && masterStore.isApproved) {
        setMiniProgramMode(MINI_PROGRAM_MODE_MASTER)
        return uni.reLaunch({ url: '/pages/master/home/index' })
      }
    } catch (error) {
      // 审核状态暂时不可用时仍按用户传入的安全回跳地址继续。
    }
  }

  const tabPages = ['/pages/home/index', '/pages/category/index', '/pages/services/index', '/pages/orders/index', '/pages/user/index']
  if (tabPages.includes(path)) return uni.reLaunch({ url: target })
  if (getCurrentPages().length > 1) return uni.redirectTo({ url: target })
  uni.reLaunch({ url: HOME_PATH })
  setTimeout(() => uni.navigateTo({ url: target, fail: () => uni.reLaunch({ url: HOME_PATH }) }), 0)
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 0 $page-gutter calc(#{$space-6} + env(safe-area-inset-bottom));
  background: $bg;
}

.login-main {
  display: flex;
  flex: 1;
  flex-direction: column;
  justify-content: center;
  padding-bottom: $space-6;
}

/* ---------- 品牌 ---------- */
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-6 0;
}

.brand-logo-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 520rpx;
  height: 240rpx;
}

.brand-logo { width: 100%; height: 100%; }

.brand-name {
  font-size: 36rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: 1rpx;
}

.brand-subtitle {
  margin-top: -8rpx;
  font-size: $font-micro;
  color: $ink-400;
  letter-spacing: 2rpx;
}

/* ---------- 登录卡片 ---------- */
.login-card { padding: $space-4; }

/* 协议 */
.agreement {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4rpx;
  margin-top: 40rpx;
}

.agreement-check {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 30rpx;
  height: 30rpx;
  margin-right: 4rpx;
  border: 1rpx solid $ink-300;
  border-radius: 50%;
  line-height: 1;
}
.agreement-check--on {
  border-color: $brand-500;
  background: $brand-500;
}
.agreement-check :deep(.uni-icons) {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.agreement-text { font-size: $font-micro; color: $ink-400; }
.agreement-link { font-size: $font-micro; color: $brand-600; }

/* 微信按钮 */
.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $space-1;
  width: 100%;
  height: 92rpx;
  padding: 0;
  border: 0;
  border-radius: $radius-pill;
  background: $brand-500;
  box-shadow: 0 6rpx 18rpx rgba(224, 106, 61, 0.24);
}
.wechat-btn::after { border: 0; }
.wechat-btn:active { background: $brand-600; transform: scale(0.98); }
/* 未勾选协议：保留品牌色背景，仅降低透明度并禁止点击 */
.wechat-btn[disabled] {
  background: $brand-500;
  color: #fff;
  box-shadow: none;
  opacity: 0.45;
  pointer-events: none;
}

.wechat-btn__text {
  color: #fff;
  font-size: $font-button;
  font-weight: $weight-semibold;
}

/* 其他方式 */
.other-login {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  margin-top: $space-4;
  padding-top: $space-3;
  border-top: 1rpx solid $line-soft;
}

.other-login__text {
  font-size: $font-caption;
  color: $ink-500;
}

/* 页脚 */
.footer-tip {
  display: block;
  margin-top: $space-6;
  text-align: center;
  font-size: 18rpx;
  color: $ink-300;
}

/* ---------- 底部弹层 ---------- */
.wechat-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: flex-end;
  padding-top: 72rpx;
  background: $overlay;
}

.wechat-panel {
  position: relative;
  width: 100%;
  padding: $space-3 $space-4 calc(#{$space-6} + env(safe-area-inset-bottom));
  border-radius: 36rpx 36rpx 0 0;
  background: $surface;
  text-align: center;
  box-shadow: 0 -12rpx 48rpx rgba(31, 28, 26, 0.12);
  animation: panel-up 0.25s cubic-bezier(0.2, 0.9, 0.3, 1);
}

@keyframes panel-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.panel-handle {
  width: 72rpx;
  height: 7rpx;
  margin: 0 auto $space-3;
  border-radius: $radius-pill;
  background: #e6dfda;
}

.panel-title {
  display: block;
  font-size: $font-title;
  font-weight: $weight-bold;
  color: $ink-900;
}

.panel-desc {
  display: block;
  margin-top: 10rpx;
  font-size: $font-micro;
  color: $ink-400;
}

/* 头像 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: $space-4 0 $space-3;
}

.avatar-picker {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 144rpx;
  height: 144rpx;
  padding: 0;
  overflow: hidden;
  border: 6rpx solid #fff;
  border-radius: 50%;
  background: #fff7f3;
  box-shadow: 0 0 0 2rpx $brand-100, 0 8rpx 24rpx rgba(31, 28, 26, 0.08);
}
.avatar-picker::after { border: 0; }
.avatar-picker:active { transform: scale(0.97); }
.avatar-image { width: 100%; height: 100%; border-radius: 50%; }

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.avatar-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: #fff;
}

.avatar-edit {
  position: absolute;
  right: 0;
  bottom: 2rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid $surface;
  border-radius: 50%;
  background: $brand-500;
}

.avatar-hint {
  margin-top: $space-1;
  font-size: $font-micro;
  color: $ink-400;
}

/* 昵称 */
.nickname-field {
  padding: $space-3;
  border: 1rpx solid $line-soft;
  border-radius: 24rpx;
  background: $bg-sunken;
  text-align: left;
}

.field-label {
  display: block;
  margin-bottom: 10rpx;
  font-size: $font-micro;
  color: $ink-500;
  font-weight: $weight-medium;
}

.field-input {
  flex: 1;
  min-width: 0;
  width: 100%;
  height: 56rpx;
  font-size: $font-body;
  color: $ink-900;
}

.field-placeholder { color: $ink-300; }

/* 手机号 */
.phone-field {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-top: $space-3;
  padding: 0 $space-3;
  height: 96rpx;
  border-radius: $radius-md;
  background: $bg-sunken;
  text-align: left;
}

.phone-prefix {
  flex: 0 0 auto;
  font-size: $font-caption;
  color: $ink-500;
  font-weight: $weight-medium;
}

.code-btn {
  flex: 0 0 auto;
  padding-left: $space-2;
  border-left: 1rpx solid $line;
  font-size: $font-micro;
  color: $brand-600;
  font-weight: $weight-semibold;
}

.code-btn--disabled { color: $ink-300; }

.mock-tip {
  display: block;
  margin-top: $space-2;
  font-size: 18rpx;
  color: $warning;
}

/* 操作 */
.panel-actions {
  display: flex;
  gap: $space-3;
  margin-top: $space-4;
}

.panel-cancel {
  flex: 1;
  height: 88rpx;
  border-radius: $radius-pill;
  border-color: $line;
  background: $surface-muted;
  color: $ink-700;
}

.panel-confirm {
  flex: 1.4;
  height: 88rpx;
  line-height: 88rpx;
  margin: 0;
  box-shadow: 0 8rpx 20rpx rgba(224, 106, 61, 0.2);
}
</style>
