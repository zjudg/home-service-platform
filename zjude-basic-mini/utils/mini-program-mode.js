const MODE_STORAGE_KEY = 'mini:appMode'

export const MINI_PROGRAM_MODE_MASTER = 'master'
export const MINI_PROGRAM_MODE_CUSTOMER = 'customer'

export function getMiniProgramMode () {
  try {
    return uni.getStorageSync(MODE_STORAGE_KEY) || ''
  } catch (error) {
    return ''
  }
}

export function setMiniProgramMode (mode) {
  try {
    uni.setStorageSync(MODE_STORAGE_KEY, mode)
  } catch (error) {
    // 页面仍可跳转；存储失败时只是不记住本次端切换偏好。
  }
}

/** Explicit customer choice wins; otherwise approved masters enter the master app. */
export function resolveMiniProgramMode (savedMode, isApprovedMaster) {
  if (savedMode === MINI_PROGRAM_MODE_CUSTOMER) return MINI_PROGRAM_MODE_CUSTOMER
  return isApprovedMaster ? MINI_PROGRAM_MODE_MASTER : ''
}
