<script setup>
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useMasterStore } from '@/stores/master'
import { useConfigStore } from '@/stores/config'
import { useCityStore } from '@/stores/city'
import { HOME_PATH, isAuthenticated, sanitizeRedirect } from '@/utils/guard'
import { isWeixin } from '@/utils/platform'
import { bootstrapAuthSession } from '@/utils/auth-bootstrap'
import {
  getMiniProgramMode,
  MINI_PROGRAM_MODE_CUSTOMER,
  MINI_PROGRAM_MODE_MASTER,
  resolveMiniProgramMode,
  setMiniProgramMode
} from '@/utils/mini-program-mode'

const appStore = useAppStore()
const authStore = useAuthStore()
const configStore = useConfigStore()
const cityStore = useCityStore()
const masterStore = useMasterStore()
let authBootstrapPromise = null

function bootstrapAuth () {
  if (authBootstrapPromise) return authBootstrapPromise

  authBootstrapPromise = bootstrapAuthSession({
    authStore,
    isWeixin: isWeixin(),
    login: () => new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
  })

  return authBootstrapPromise
}

onLaunch(async () => {
  // 保留用户上次主动选择的端模式；访客可以直接浏览公开服务内容。
  // 已注册的微信用户通过静默 code 换取 token，无需再次手动登录。
  bootstrapAuth().catch(() => {})
  await Promise.all([
    cityStore.initialize(),
    configStore.load().catch(() => {})
  ])
  appStore.markReady()
})

// 已通过审核的师傅默认进入师傅端；手动切换到用户端后会跨启动保留该选择。
onShow(async () => {
  await bootstrapAuth()
  const savedMode = getMiniProgramMode()
  if (!isAuthenticated()) return

  const currentPage = getCurrentPages().at(-1)
  if (savedMode !== MINI_PROGRAM_MODE_CUSTOMER) {
    try {
      await authStore.restore()
      if (!authStore.isLoggedIn) return

      await masterStore.loadProfile()
      const nextMode = resolveMiniProgramMode(savedMode, masterStore.applied && masterStore.isApproved)
      if (nextMode !== savedMode) setMiniProgramMode(nextMode)
      if (nextMode === MINI_PROGRAM_MODE_MASTER) {
        if (!currentPage?.route?.startsWith('pages/master/')) {
          uni.reLaunch({ url: '/pages/master/home/index' })
        }
        return
      }
    } catch (error) {
      // 网络异常时保留当前页，不阻止访客/用户继续使用小程序。
    }
  }

  // A silent login may finish after the login page's own onShow hook has run.
  // Leave that page once the existing account has been authenticated.
  if (currentPage?.route === 'pages/login/index') {
    let target = currentPage.options?.redirect || ''
    try {
      if (target) target = decodeURIComponent(target)
    } catch (error) {
      target = ''
    }
    uni.reLaunch({ url: sanitizeRedirect(target) || HOME_PATH })
  }
})

</script>

<style lang="scss">
@import './styles/common.scss';
</style>
