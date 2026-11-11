import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { reverseGeocodeLocation } from '@/api/service'
import { getCurrentCoordinates } from '@/utils/location'

// 城市选择结果同时保存 ID 与名称：ID 用于接口过滤，名称用于界面展示
const STORAGE_KEY = 'serviceCity'
const STORAGE_ID_KEY = 'serviceCityId'

export const useCityStore = defineStore('city', () => {
  const cityId = ref('')
  const cityName = ref('')
  const initialized = ref(false)
  let initialization = null

  const hasCity = computed(() => Boolean(cityId.value || cityName.value))

  /**
   * 从本地存储恢复上次选择的城市。
   */
  function restore () {
    try {
      cityName.value = uni.getStorageSync(STORAGE_KEY) || ''
      cityId.value = uni.getStorageSync(STORAGE_ID_KEY) || ''
    } catch (e) {
      cityName.value = ''
      cityId.value = ''
    }
  }

  /** 启动时定位并锁定本次使用的服务城市；定位失败时保留上次选择。 */
  function initialize () {
    if (initialized.value) return Promise.resolve()
    if (initialization) return initialization
    restore()
    if (process.env.UNI_PLATFORM !== 'mp-weixin') {
      initialized.value = true
      return Promise.resolve()
    }
    initialization = (async () => {
      try {
        const coordinates = await getCurrentCoordinates()
        const response = await reverseGeocodeLocation(coordinates)
        const city = response?.data || response
        if (city?.cityId && city?.cityName) select(city)
      } catch {
        // 定位或逆地理编码不可用时保留已持久化城市，不阻断小程序使用。
      } finally {
        initialized.value = true
        initialization = null
      }
    })()
    return initialization
  }

  /**
   * 选择城市并持久化。
   *
   * @param {Object|string} city 城市对象 {cityId, cityName} 或仅城市名
   */
  function select (city) {
    const id = typeof city === 'string' ? '' : city?.cityId || ''
    const name = typeof city === 'string' ? city : city?.cityName || ''
    cityId.value = id
    cityName.value = name
    try {
      uni.setStorageSync(STORAGE_KEY, name)
      uni.setStorageSync(STORAGE_ID_KEY, id)
    } catch (e) {
      // 存储失败不影响本次会话
    }
    // 广播给正在监听的页面
    uni.$emit('city-changed', { cityId: id, cityName: name })
  }

  /**
   * 当前城市 ID，未选择时返回空串（后端按不限制处理）。
   *
   * @returns {string} 城市 ID
   */
  function currentCityId () {
    return cityId.value || ''
  }

  return { cityId, cityName, hasCity, initialized, restore, initialize, select, currentCityId }
})
