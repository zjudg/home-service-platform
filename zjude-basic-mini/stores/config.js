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
  const navigationReady = computed(() => loaded.value && navigation.value.items.length > 0)

  async function load () {
    if (loading.value || loaded.value) return
    loading.value = true
    error.value = ''
    try {
      const responses = await Promise.allSettled([
        getMiniSite(),
        getMiniNavigation(),
        getMiniHome(),
        getMiniCategory(),
        getMiniProfile()
      ])
      const [siteResponse, navigationResponse, homeResponse, categoryResponse, profileResponse] = responses
      site.value = valueOr(siteResponse, site.value)
      navigation.value = normalizeNavigation(valueOr(navigationResponse, navigation.value))
      home.value = normalizePage(valueOr(homeResponse, home.value))
      category.value = valueOr(categoryResponse, category.value)
      profile.value = normalizePage(valueOr(profileResponse, profile.value))
      const failed = responses.find(response => response.status === 'rejected')
      if (failed) error.value = failed.reason?.message || '部分页面配置加载失败'
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

  function valueOr (response, fallback) {
    return response?.status === 'fulfilled' ? payload(response.value) : fallback
  }

  function normalizeNavigation (value) {
    return {
      ...value,
      items: Array.isArray(value.items)
        ? value.items
            .filter(item => String(item.isVisible ?? '1') !== '0')
            .map(item => ({ ...item, key: item.key || item.page, name: item.name || item.label }))
        : []
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
