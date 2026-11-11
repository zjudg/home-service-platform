import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import * as masterApi from '@/api/master'
import { ORDER_STATUS } from '@/constants/order'

// 师傅档案状态：0待审核 1已通过 2已驳回（与后端 ZjudgMaster 一致）
export const AUDIT_PENDING = '0'
export const AUDIT_APPROVED = '1'
export const AUDIT_REJECTED = '2'

export const useMasterStore = defineStore('master', () => {
  const profile = ref(null)
  const applied = ref(false)
  const auditStatus = ref('')
  const canAcceptOrders = ref(false)
  const loading = ref(false)
  const loaded = ref(false)
  let profileRequest = null
  let profileRequestVersion = 0

  const isApproved = computed(() => auditStatus.value === AUDIT_APPROVED)
  const isPending = computed(() => auditStatus.value === AUDIT_PENDING)
  const isRejected = computed(() => auditStatus.value === AUDIT_REJECTED)
  const accepting = computed(() => profile.value?.accepting === '1')

  /**
   * 审核状态文案，未入驻时给出引导语。
   */
  const statusText = computed(() => {
    if (!applied.value) return '未入驻'
    if (isApproved.value) return '审核通过'
    if (isRejected.value) return '审核未通过'
    return '审核中'
  })

  function applyState (payload) {
    applied.value = Boolean(payload?.applied)
    profile.value = payload?.master || null
    auditStatus.value = payload?.auditStatus || ''
    canAcceptOrders.value = Boolean(payload?.canAcceptOrders)
    loaded.value = true
  }

  function reset () {
    profileRequestVersion += 1
    profileRequest = null
    loading.value = false
    profile.value = null
    applied.value = false
    auditStatus.value = ''
    canAcceptOrders.value = false
    loaded.value = false
  }

  function loadProfile () {
    if (profileRequest) return profileRequest

    const requestVersion = profileRequestVersion
    loading.value = true
    const request = (async () => {
      try {
        const response = await masterApi.getMasterProfile()
        if (requestVersion === profileRequestVersion) {
          applyState(response?.data || response)
        }
        return profile.value
      } finally {
        if (profileRequest === request) {
          profileRequest = null
          loading.value = false
        }
      }
    })()
    profileRequest = request
    return request
  }

  async function apply (form) {
    const response = await masterApi.applyMaster(form)
    await loadProfile()
    return response
  }

  async function toggleAccepting (next) {
    await masterApi.switchAccepting(next)
    if (profile.value) {
      profile.value = { ...profile.value, accepting: next }
      canAcceptOrders.value = next === '1' && isApproved.value
    }
  }

  /**
   * 师傅端动作可用性：只有审核通过的师傅才能接单/开工/完工。
   */
  function canOperateOrder (order) {
    if (!isApproved.value) return false
    const status = String(order?.orderStatus ?? '')
    return status === ORDER_STATUS.ACCEPTED || status === ORDER_STATUS.SERVING
  }

  return {
    profile,
    applied,
    auditStatus,
    canAcceptOrders,
    loading,
    loaded,
    isApproved,
    isPending,
    isRejected,
    accepting,
    statusText,
    loadProfile,
    apply,
    toggleAccepting,
    canOperateOrder,
    reset
  }
})
