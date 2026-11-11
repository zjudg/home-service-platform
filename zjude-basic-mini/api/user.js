import { request } from '@/utils/request'

export function updateProfile (data) {
  return request({ url: '/system/user/profile', method: 'PUT', data })
}

export function updateAvatar (avatarUrl) {
  return request({ url: '/mini/user/profile/avatar', method: 'PUT', data: { avatarUrl } })
}
