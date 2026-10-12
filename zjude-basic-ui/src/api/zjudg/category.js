import request from '@/utils/request'

export const listCategory = (params) => request({ url: '/zjudg/category/list', method: 'get', params })
export const addCategory = (data) => request({ url: '/zjudg/category', method: 'post', data })
export const updateCategory = (data) => request({ url: '/zjudg/category', method: 'put', data })
export const deleteCategory = (ids) => request({ url: `/zjudg/category/${ids}`, method: 'delete' })
