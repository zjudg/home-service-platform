import { TOKEN_KEY, USER_KEY } from '@/constants/app'

/**
 * 清理本地临时数据。
 *
 * <p>刻意保留登录态（token / 用户信息）与用户选择的城市：
 * 「清理缓存」的目的是清掉表单草稿与临时图片等脏数据，
 * 把登录态一起清掉会让用户莫名其妙退出登录。</p>
 *
 * @returns {Promise<{cleared: number, kept: number}>} 清理与保留的键数量
 */
export function clearCache () {
  const keep = [TOKEN_KEY, USER_KEY, 'serviceCity', 'serviceCityId']
  return new Promise(resolve => {
    let info = {}
    try {
      info = uni.getStorageInfoSync() || {}
    } catch (error) {
      console.warn('[cache] failed to read storage info', error)
    }
    const keys = Array.isArray(info.keys) ? info.keys : []
    let cleared = 0
    let kept = 0
    keys.forEach(key => {
      if (keep.includes(key)) {
        kept += 1
        return
      }
      try {
        uni.removeStorageSync(key)
        cleared += 1
      } catch (error) {
        console.warn(`[cache] failed to remove ${key}`, error)
      }
    })
    resolve({ cleared, kept })
  })
}
