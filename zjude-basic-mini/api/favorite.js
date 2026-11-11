import { request } from '@/utils/request'

// 用户收藏：身份由 token 解析，不接受客户端传 userId
export const listFavorites = () => request({ url: '/mini/user/favorites', method: 'GET' })

export const countFavorites = () => request({ url: '/mini/user/favorites/count', method: 'GET' })

export const checkFavorite = serviceId =>
  request({ url: `/mini/user/favorites/check/${serviceId}`, method: 'GET' })

export const addFavorite = serviceId =>
  request({ url: `/mini/user/favorites/${serviceId}`, method: 'POST' })

export const removeFavorite = serviceId =>
  request({ url: `/mini/user/favorites/${serviceId}`, method: 'DELETE' })
