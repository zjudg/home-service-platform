import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getMiniBootstrap } from '@/api/mini'

function payload (response) {
  return response?.data || response || {}
}

export const useConfigStore = defineStore('config', () => {
  const site = ref({})
  const navigation = ref({ items: [] })
  const home = ref({ navItems: [], banners: [] })
  const category = ref({})
  const profile = ref({ navItems: [], orderItems: [] })
  const loading = ref(false)
  const loaded = ref(false)
  const error = ref('')
  const appTitle = computed(() => site.value.shopName || '应用')
  const navigationReady = computed(() => loaded.value && navigation.value.items.length > 0)

  async function load () {
    if (loading.value || loaded.value) return
    loading.value = true
    error.value = ''
    try {
      const bootstrap = payload(await getMiniBootstrap())
      site.value = bootstrap.site || {}
      navigation.value = normalizeNavigation(bootstrap.navigation || {})
      home.value = normalizePage(bootstrap.home || {})
      category.value = bootstrap.category || {}
      profile.value = normalizePage(bootstrap.profile || {})
      if (!navigation.value.items.length) {
        throw new Error('后台暂未配置底部导航')
      }
      loaded.value = true
    } catch (requestError) {
      error.value = requestError.message || '页面配置加载失败'
      throw requestError
    } finally {
      loading.value = false
    }
  }

  function normalizeNavigation (value) {
    return {
      ...value,
      items: Array.isArray(value.items) ? value.items.filter(item => String(item.isVisible ?? '1') !== '0') : []
    }
  }

  function normalizePage (value) {
    return {
      ...value,
      navItems: Array.isArray(value.navItems) ? value.navItems.filter(item => String(item.isVisible ?? '1') !== '0') : [],
      banners: Array.isArray(value.banners) ? value.banners.filter(Boolean) : [],
      orderItems: Array.isArray(value.orderItems) ? value.orderItems.filter(item => String(item.isVisible ?? '1') !== '0') : []
    }
  }

  return { site, navigation, home, category, profile, loading, loaded, error, appTitle, navigationReady, load }
})
