import request from '@/utils/request'

/**
 * 后台用户管理：仅包含小程序注册用户。
 *
 * @param {Object} params keyword / source / beginTime / endTime / pageNum / pageSize
 */
export const listZjudgUsers = (params) => request({ url: '/zjudg/user/list', method: 'get', params })

export const setZjudgMiniAdmin = (userId, enabled) => request({
  url: `/zjudg/user/${userId}/mini-admin`,
  method: 'put',
  data: { enabled }
})
