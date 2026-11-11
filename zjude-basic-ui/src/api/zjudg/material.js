import request from '@/utils/request'

export const listMaterial = (params) => request({ url: '/zjudg/material/list', method: 'get', params })
export const getMaterial = (materialId) => request({ url: `/zjudg/material/${materialId}`, method: 'get' })
export const addMaterial = (data) => request({ url: '/zjudg/material', method: 'post', data })
export const updateMaterial = (data) => request({ url: '/zjudg/material', method: 'put', data })
export const removeMaterial = (materialIds) =>
  request({ url: `/zjudg/material/${materialIds.join(',')}`, method: 'delete' })
export const moveMaterial = (materialIds, groupId) =>
  request({ url: '/zjudg/material/move', method: 'put', data: { materialIds, groupId } })

export const listMaterialGroups = () => request({ url: '/zjudg/material/groups', method: 'get' })
export const addMaterialGroup = (groupName) =>
  request({ url: '/zjudg/material/groups', method: 'post', data: { groupName } })
export const renameMaterialGroup = (groupId, groupName) =>
  request({ url: `/zjudg/material/groups/${groupId}`, method: 'put', data: { groupName } })
export const removeMaterialGroup = (groupId) =>
  request({ url: `/zjudg/material/groups/${groupId}`, method: 'delete' })
