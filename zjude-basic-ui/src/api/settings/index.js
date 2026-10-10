import request from '@/utils/request'

/**
 * 创建页面专属配置接口。
 *
 * @param {string} page 页面标识
 */
export function createPageSettingsApi (page) {
  const baseUrl = `/manage/settings/${page}`
  return {
    listConfig (query) {
      return request({
        url: `${baseUrl}/config/list`,
        method: 'get',
        params: query
      })
    },
    saveConfig (data) {
      return request({
        url: baseUrl,
        method: 'put',
        data
      })
    }
  }
}
