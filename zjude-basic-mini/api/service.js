import { request } from '@/utils/request'

// 公开数据：无需登录
export const listServices = (params = {}) => request({ url: '/mini/services', method: 'GET', data: params })
export const getService = id => request({ url: `/mini/services/${id}`, method: 'GET' })
export const listCategories = (params = {}) => request({ url: '/mini/categories', method: 'GET', data: params })
export const listSlots = (params = {}) => request({ url: '/mini/slots', method: 'GET', data: params })
export const listCities = (params = {}) => request({ url: '/mini/cities', method: 'GET', data: params })
export const reverseGeocodeLocation = data => request({ url: '/mini/config/location/reverse', method: 'POST', data })

// 订单与评价：需要登录态，身份由服务端 token 解析
export const createOrder = data => request({ url: '/mini/user/orders', method: 'POST', data })
export const listOrders = (params = {}) => request({ url: '/mini/user/orders', method: 'GET', data: params })
export const getOrder = id => request({ url: `/mini/user/orders/${id}`, method: 'GET' })
export const cancelOrder = id => request({ url: `/mini/user/orders/${id}/cancel`, method: 'PUT' })
export const confirmOrder = id => request({ url: `/mini/user/orders/${id}/confirm`, method: 'PUT' })
export const submitReview = data => request({ url: '/mini/user/reviews', method: 'POST', data })
