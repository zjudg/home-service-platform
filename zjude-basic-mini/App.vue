<script setup>
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useConfigStore } from '@/stores/config'
import { useCityStore } from '@/stores/city'

const appStore = useAppStore()
const authStore = useAuthStore()
const configStore = useConfigStore()
const cityStore = useCityStore()

async function silentWechatLogin () {
  if (process.env.UNI_PLATFORM !== 'mp-weixin') return authStore.restore()
  try {
    const result = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    if (!result?.code) return authStore.restore()
    const response = await authStore.silentWechatSignIn(result.code)
    return response.registered ? 'authenticated' : 'login-required'
  } catch {
    // 网络异常时继续尝试使用上次会话；无可用会话则保留当前页面供用户重试。
    return authStore.restore()
  }
}

onLaunch(async () => {
  await Promise.all([
    cityStore.initialize(),
    configStore.load().catch(() => {})
  ])
  appStore.markReady()
})

onShow(async () => {
  const authResult = await silentWechatLogin()
  if (authResult !== 'login-required') return

  const pages = getCurrentPages()
  const currentRoute = pages[pages.length - 1]?.route
  // 通过服务费/合同分享链接进入时先展示业务内容，支付或签署动作再引导登录。
  if (['pages/service-fee/index', 'pages/contract-sign/index'].includes(currentRoute)) return
  if (currentRoute !== 'pages/login/index') uni.reLaunch({ url: '/pages/login/index' })
})
</script>

<style lang="scss">
@import './styles/common.scss';
</style>
