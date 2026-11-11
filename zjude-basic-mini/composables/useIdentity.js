import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useMasterStore } from '@/stores/master'

// 师傅角色 key，与后端 sys_role.role_key = 'master' 一致。
export const MASTER_ROLE = 'master'

/**
 * 身份判定唯一入口。
 *
 * 用户端与师傅端共用一个小程序，页面通过这里判断是否展示师傅入口，
 * 避免在各页面散落 `roles.includes('master')`。
 *
 * 师傅身份以已提交的师傅档案为准；真正的订单权限始终由后端审核状态校验。
 */
export function useIdentity () {
  const authStore = useAuthStore()
  const masterStore = useMasterStore()

  const roles = computed(() => {
    const value = authStore.user?.roles
    if (!value) return []
    return Array.isArray(value) ? value : Array.from(value)
  })

  const isLoggedIn = computed(() => authStore.isLoggedIn)
  const isMaster = computed(() => roles.value.includes(MASTER_ROLE) || masterStore.applied)

  // 审核流程只更新师傅档案，不依赖额外的 sys_user_role 变更。
  const canEnterMaster = computed(() => masterStore.applied && masterStore.isApproved)

  // 已提交申请但未通过审核，展示「审核中 / 已驳回」状态
  const masterPending = computed(() => masterStore.applied && !masterStore.isApproved)

  return { roles, isLoggedIn, isMaster, canEnterMaster, masterPending }
}
