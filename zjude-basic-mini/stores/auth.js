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
    if (!token.value) return false
    try {
      await loadUser()
      return true
    } catch (error) {
      clearSession()
      return false
    }
  }

  setUnauthorizedHandler(() => {
    clearSession()
  })

  return { token, user, loading, isLoggedIn, signIn, loadUser, signOut, restore, clearSession }
})
