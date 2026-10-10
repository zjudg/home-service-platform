import request from '@/utils/request'

export function listConfig (query) {
  return request({
    url: '/manage/settings/notification/config/list',
    method: 'get',
    params: query
  })
}

export function saveConfig (data) {
  return request({
    url: '/manage/settings/notification',
    method: 'put',
    data
  })
}
