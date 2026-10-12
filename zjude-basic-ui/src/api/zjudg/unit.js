import request from '@/utils/request'

export const listUnit = (params) => request({ url: '/zjudg/unit/list', method: 'get', params })
export const addUnit = (data) => request({ url: '/zjudg/unit', method: 'post', data })
export const updateUnit = (data) => request({ url: '/zjudg/unit', method: 'put', data })
export const deleteUnit = (ids) => request({ url: `/zjudg/unit/${ids}`, method: 'delete' })
