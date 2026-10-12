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

export function getMiniServices (params = {}) {
  return request({ url: '/api/mini/services', method: 'GET', data: params })
}

export function getMiniService (id) {
  return request({ url: `/api/mini/services/${id}`, method: 'GET' })
}

export function getMiniCategories (params = {}) {
  return request({ url: '/api/mini/categories', method: 'GET', data: params })
}

export function getMiniSlots (params = {}) {
  return request({ url: '/api/mini/slots', method: 'GET', data: params })
}

export function getMiniCities (params = {}) {
  return request({ url: '/api/mini/cities', method: 'GET', data: params })
}

export function getServices (params = {}) {
  return request({ url: '/zjudg/service/list', method: 'GET', data: params })
}

export function getService (id) {
  return request({ url: `/zjudg/service/${id}`, method: 'GET' })
}

export function createOrder (data) {
  return request({ url: '/zjudg/order', method: 'POST', data })
}

export function getOrders (params = {}) {
  return request({ url: '/zjudg/order/list', method: 'GET', data: params })
}
