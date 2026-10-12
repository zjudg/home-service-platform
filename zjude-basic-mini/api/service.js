import { request } from '@/utils/request'

export const listServices = (params = {}) => request({ url: '/api/mini/services', method: 'GET', data: params })
export const getService = id => request({ url: `/api/mini/services/${id}`, method: 'GET' })
export const listCategories = (params = {}) => request({ url: '/api/mini/categories', method: 'GET', data: params })
export const listSlots = (params = {}) => request({ url: '/api/mini/slots', method: 'GET', data: params })
export const createOrder = data => request({ url: '/zjudg/order', method: 'POST', data })
export const listOrders = (params = {}) => request({ url: '/zjudg/order/list', method: 'GET', data: params })
export const cancelOrder = id => request({ url: `/zjudg/order/${id}/cancel`, method: 'PUT' })
export const confirmOrder = id => request({ url: `/zjudg/order/${id}/confirm`, method: 'PUT' })
export const submitReview = data => request({ url: '/zjudg/review', method: 'POST', data })
