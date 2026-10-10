import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  getMiniCategory,
  getMiniHome,
  getMiniNavigation,
  getMiniProfile,
  getMiniSite
} from '@/api/mini'

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

  async function load () {
    if (loading.value || loaded.value) return
    loading.value = true
    error.value = ''
    try {
      const [siteResponse, navigationResponse, homeResponse, categoryResponse, profileResponse] = await Promise.all([
        getMiniSite(),
        getMiniNavigation(),
        getMiniHome(),
        getMiniCategory(),
        getMiniProfile()
      ])
      site.value = payload(siteResponse)
      navigation.value = normalizeNavigation(payload(navigationResponse))
      home.value = normalizePage(payload(homeResponse))
      category.value = payload(categoryResponse)
      profile.value = normalizePage(payload(profileResponse))
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

  return { site, navigation, home, category, profile, loading, loaded, error, appTitle, load }
})
