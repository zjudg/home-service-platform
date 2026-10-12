import request from '@/utils/request'

export const listOrder = (params) => request({ url: '/zjudg/order/list', method: 'get', params })
