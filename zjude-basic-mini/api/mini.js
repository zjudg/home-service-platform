import { request } from '@/utils/request'

export function getMiniBootstrap () {
  return request({ url: '/api/mini/config/bootstrap', method: 'GET' })
}

export function getMiniSite () {
  return request({ url: '/api/mini/config/site', method: 'GET' })
}

export function getMiniNavigation () {
  return request({ url: '/api/mini/config/navigation', method: 'GET' })
}

export function getMiniHome () {
  return request({ url: '/api/mini/config/home', method: 'GET' })
}

export function getMiniCategory () {
  return request({ url: '/api/mini/config/category', method: 'GET' })
}

export function getMiniProfile () {
  return request({ url: '/api/mini/config/profile', method: 'GET' })
}
