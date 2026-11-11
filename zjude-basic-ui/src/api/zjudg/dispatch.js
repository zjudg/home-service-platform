import request from '@/utils/request'

export const listDispatch = (params) => request({ url: '/zjudg/dispatch/list', method: 'get', params })
export const listCandidates = (orderId) => request({ url: `/zjudg/dispatch/candidates/${orderId}`, method: 'get' })
export const autoDispatch = (orderId) => request({ url: `/zjudg/dispatch/auto/${orderId}`, method: 'put' })
export const manualDispatch = (data) => request({ url: '/zjudg/dispatch/manual', method: 'put', data })
