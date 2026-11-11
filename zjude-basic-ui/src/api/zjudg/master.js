import request from '@/utils/request'

export const listMaster = (params) => request({ url: '/zjudg/master/list', method: 'get', params })
export const getMaster = (id) => request({ url: `/zjudg/master/${id}`, method: 'get' })
export const addMaster = (data) => request({ url: '/zjudg/master', method: 'post', data })
export const updateMaster = (data) => request({ url: '/zjudg/master', method: 'put', data })
export const auditMaster = (data) => request({ url: '/zjudg/master/audit', method: 'put', data })
export const deleteMaster = (ids) => request({ url: `/zjudg/master/${ids}`, method: 'delete' })
