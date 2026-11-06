import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import { TOKEN_KEY, USER_KEY } from '@/constants/app'
import { getStorage, removeStorage, setStorage } from '@/utils/storage'
import { setUnauthorizedHandler } from '@/utils/request'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getStorage(TOKEN_KEY, ''))
  const user = ref(getStorage(USER_KEY, null))
  const loading = ref(false)
  const isLoggedIn = computed(() => Boolean(token.value))
  const restored = ref(false)
  let restorePromise = null

  function clearSession () {
    token.value = ''
    user.value = null
    removeStorage(TOKEN_KEY)
    removeStorage(USER_KEY)
  }

  async function signIn (credentials) {
    loading.value = true
    try {
      const response = await authApi.login(credentials)
      const nextToken = response.token || response.data?.token
      if (!nextToken) throw new Error('登录响应缺少令牌')
      token.value = nextToken
      setStorage(TOKEN_KEY, nextToken)
      await loadUser()
      return response
    } finally {
      loading.value = false
    }
  }

  async function signInWithWechat (profile) {
    loading.value = true
    try {
      const response = await authApi.wechatLogin(profile)
      const nextToken = response.token || response.data?.token
      if (!nextToken) throw new Error('微信登录响应缺少令牌')
      token.value = nextToken
      setStorage(TOKEN_KEY, nextToken)
      await loadUser()
      return response
    } finally {
      loading.value = false
    }
  }

  async function silentWechatSignIn (code) {
    const response = await authApi.silentWechatLogin(code)
    if (response.registered === false || response.data?.registered === false) {
      clearSession()
      return { registered: false }
    }
    const nextToken = response.token || response.data?.token
    if (!nextToken) throw new Error('微信静默登录响应缺少令牌')
    token.value = nextToken
    setStorage(TOKEN_KEY, nextToken)
    await loadUser()
    return { ...response, registered: true }
  }

  async function signInWithSms (payload) {
    loading.value = true
    try {
      const response = await authApi.smsLogin(payload)
      const nextToken = response.token || response.data?.token
      if (!nextToken) throw new Error('手机登录响应缺少令牌')
      token.value = nextToken
      setStorage(TOKEN_KEY, nextToken)
      await loadUser()
      return response
    } finally {
      loading.value = false
    }
  }
  async function loadUser () {
    const response = await authApi.getInfo()
    user.value = response.user || response.data?.user || null
    setStorage(USER_KEY, user.value)
    return response
  }

  async function signOut () {
    try {
      if (token.value) await authApi.logout()
    } finally {
      clearSession()
    }
  }

  async function restore () {
    if (restored.value) return Boolean(token.value)
    if (restorePromise) return restorePromise
    if (!token.value) {
      restored.value = true
      return false
    }

    restorePromise = (async () => {
      try {
        await loadUser()
        return true
      } catch {
        // A 401 clears the session through the request layer. Keep the saved
        // token for network/server errors so a temporary outage doesn't log out the user.
        return false
      } finally {
        restored.value = true
        restorePromise = null
      }
    })()
    return restorePromise
  }

  setUnauthorizedHandler(() => {
    clearSession()
  })

  return { token, user, loading, isLoggedIn, signIn, signInWithWechat, silentWechatSignIn, signInWithSms, loadUser, signOut, restore, clearSession }
})
