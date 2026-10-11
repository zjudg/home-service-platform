import { request } from '@/utils/request'

export function updateProfile (data) {
  return request({ url: '/system/user/profile', method: 'PUT', data })
}
