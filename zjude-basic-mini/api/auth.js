import { request } from '@/utils/request'

export function login (data) {
  return request({ url: '/login', method: 'POST', data })
}

export function getInfo () {
  return request({ url: '/get/info', method: 'GET' })
}

export function getRouters () {
  return request({ url: '/get/routers', method: 'GET' })
}

export function logout () {
  return request({ url: '/logout', method: 'POST' })
}
