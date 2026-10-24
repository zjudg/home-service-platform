import { request } from '@/utils/request'

// 小程序启动与页面装修配置（只读，后台维护）
export function getMiniBootstrap () {
  return request({ url: '/mini/config/bootstrap', method: 'GET' })
}

export function getMiniSite () {
  return request({ url: '/mini/config/site', method: 'GET' })
}

export function getMiniNavigation () {
  return request({ url: '/mini/config/navigation', method: 'GET' })
}

export function getMiniHome () {
  return request({ url: '/mini/config/home', method: 'GET' })
}

export function getMiniCategory () {
  return request({ url: '/mini/config/category', method: 'GET' })
}

export function getMiniProfile () {
  return request({ url: '/mini/config/profile', method: 'GET' })
}

// 轮播图来自后台「轮播图管理」(zjudg_banner)，只返回启用中且在有效期内的数据
export function getMiniBanners (position = 'index') {
  return request({ url: '/mini/config/banners', method: 'GET', data: { position } })
}
