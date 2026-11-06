import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  getMiniCategory,
  getMiniHome,
  getMiniNavigation,
  getMiniProfile,
  getMiniSite,
  getMiniSubscriptionTemplates
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
  const subscribeTemplates = ref({ enabled: false, accepted: '', finished: '', paid: '', adminCreated: '', adminAccepted: '' })
  const loading = ref(false)
  const loaded = ref(false)
  const error = ref('')
  let loadPromise = null
  const appTitle = computed(() => site.value.shopName || '应用')
  const navigationReady = computed(() => loaded.value && navigation.value.items.length > 0)

  function load () {
    if (loaded.value) return Promise.resolve()
    if (loadPromise) return loadPromise
    loading.value = true
    error.value = ''
    loadPromise = (async () => {
      try {
        const responses = await Promise.allSettled([
          getMiniSite(),
          getMiniNavigation(),
          getMiniHome(),
          getMiniCategory(),
          getMiniProfile(),
          getMiniSubscriptionTemplates()
        ])
        const [siteResponse, navigationResponse, homeResponse, categoryResponse, profileResponse, subscribeResponse] = responses
        site.value = valueOr(siteResponse, site.value)
        navigation.value = normalizeNavigation(valueOr(navigationResponse, navigation.value))
        home.value = normalizePage(valueOr(homeResponse, home.value))
        category.value = valueOr(categoryResponse, category.value)
        profile.value = normalizePage(valueOr(profileResponse, profile.value))
        subscribeTemplates.value = valueOr(subscribeResponse, subscribeTemplates.value)
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
        loadPromise = null
      }
    })()
    return loadPromise
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

  async function refreshSubscriptionTemplates () {
    const response = await getMiniSubscriptionTemplates()
    subscribeTemplates.value = response?.data || response || subscribeTemplates.value
    return subscribeTemplates.value
  }

  return { site, navigation, home, category, profile, subscribeTemplates, loading, loaded, error, appTitle, navigationReady, load, refreshSubscriptionTemplates }
})
