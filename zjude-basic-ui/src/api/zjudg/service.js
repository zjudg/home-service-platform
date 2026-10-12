import request from '@/utils/request'

export const listService = (params) => request({ url: '/zjudg/service/list', method: 'get', params })
export const getService = (id) => request({ url: `/zjudg/service/${id}`, method: 'get' })
export const addService = (data) => request({ url: '/zjudg/service', method: 'post', data })
export const updateService = (data) => request({ url: '/zjudg/service', method: 'put', data })
export const deleteService = (ids) => request({ url: `/zjudg/service/${ids}`, method: 'delete' })
