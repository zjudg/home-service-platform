import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as addressApi from '@/api/address'

function payload (response) {
  return response?.data ?? response ?? null
}

export const useAddressStore = defineStore('address', () => {
  const list = ref([])
  const loading = ref(false)
  const loaded = ref(false)

  const defaultAddress = computed(() => list.value.find(item => item.isDefault === '1') || list.value[0] || null)
  const isEmpty = computed(() => loaded.value && list.value.length === 0)

  function applyList (value) {
    list.value = Array.isArray(value) ? value : []
    loaded.value = true
  }

  async function load (force = false) {
    if (loading.value) return list.value
    if (loaded.value && !force) return list.value
    loading.value = true
    try {
      const response = await addressApi.listAddresses()
      applyList(payload(response))
      return list.value
    } finally {
      loading.value = false
    }
  }

  // 失败时也要把 loaded 置回 true，否则页面会一直停在骨架屏
  async function refresh () {
    loading.value = false
    loaded.value = false
    try {
      return await load(true)
    } catch (e) {
      loaded.value = true
      throw e
    }
  }

  async function create (form) {
    await addressApi.createAddress(form)
    return refresh()
  }

  async function update (addressId, form) {
    await addressApi.updateAddress(addressId, form)
    return refresh()
  }

  async function setDefault (addressId) {
    await addressApi.setDefaultAddress(addressId)
    return refresh()
  }

  async function remove (addressId) {
    await addressApi.deleteAddress(addressId)
    return refresh()
  }

  function reset () {
    list.value = []
    loaded.value = false
  }

  return {
    list,
    loading,
    loaded,
    defaultAddress,
    isEmpty,
    load,
    refresh,
    create,
    update,
    setDefault,
    remove,
    reset
  }
})
