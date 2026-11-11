import request from '@/utils/request'

export const listCity = (params) => request({ url: '/zjudg/city/list', method: 'get', params })
export const getCity = (cityId) => request({ url: `/zjudg/city/${cityId}`, method: 'get' })
export const addCity = (data) => request({ url: '/zjudg/city', method: 'post', data })
export const updateCity = (data) => request({ url: '/zjudg/city', method: 'put', data })
export const deleteCity = (cityIds) => request({ url: `/zjudg/city/${cityIds}`, method: 'delete' })
