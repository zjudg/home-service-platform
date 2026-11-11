import request from '@/utils/request'

export const listOrder = (params) => request({ url: '/zjudg/order/list', method: 'get', params })
export const deleteOrder = (id) => request({ url: `/zjudg/order/${id}`, method: 'delete' })
export const createOfflinePaidOrder = (data) => request({ url: '/zjudg/order/offline', method: 'post', data })
export const listOrderCities = () => request({ url: '/api/mini/cities', method: 'get' })
export const listOrderServices = (params) => request({ url: '/api/mini/services', method: 'get', params })
export const listOrderSlots = (params) => request({ url: '/api/mini/slots', method: 'get', params })
