import request from '@/utils/request'

/**
 * 页面装修配置读写。
 *
 * 后端把装修配置落在 sys_config 的 decorate.* 键上，按页面白名单隔离：
 * decorate-home / decorate-profile / decorate-category / decorate-tabbar。
 */
function createDecorationApi (page) {
  const baseUrl = `/manage/settings/${page}`
  return {
    /** 读取该页面已有配置，返回 sys_config 记录列表 */
    listConfig () {
      return request({
        url: `${baseUrl}/config/list`,
        method: 'get',
        params: { pageNum: 1, pageSize: 100 }
      })
    },
    /** 保存该页面配置，key 必须是 decorate.* 前缀 */
    saveConfig (data) {
      return request({
        url: baseUrl,
        method: 'put',
        data
      })
    }
  }
}

export const homeDecorationApi = createDecorationApi('decorate-home')
export const profileDecorationApi = createDecorationApi('decorate-profile')
export const categoryDecorationApi = createDecorationApi('decorate-category')
export const tabbarDecorationApi = createDecorationApi('decorate-tabbar')
export const customerDecorationApi = {
  /** 客服信息复用网站设置里的两个字段 */
  get () {
    return request({ url: '/manage/settings/website', method: 'get' })
  },
  save (data) {
    return request({ url: '/manage/settings/website', method: 'put', data })
  }
}

/**
 * 把配置记录列表转成 { key: 原始字符串值 } 便于表单回填。
 *
 * @param {Array} rows sys_config 记录
 * @returns {Object} key 到 configValue 的映射
 */
export function toConfigMap (rows) {
  const map = {}
  ;(rows || []).forEach(item => {
    if (item?.configKey) {
      map[item.configKey] = item.configValue ?? ''
    }
  })
  return map
}

/**
 * 安全解析 JSON 配置，损坏时回退默认值，避免整页白屏。
 *
 * @param {string} raw 配置原始值
 * @param {*} fallback 解析失败时的默认值
 * @returns {*} 解析结果
 */
export function parseJsonConfig (raw, fallback) {
  if (!raw) return fallback
  try {
    const value = JSON.parse(raw)
    return value ?? fallback
  } catch (e) {
    return fallback
  }
}
