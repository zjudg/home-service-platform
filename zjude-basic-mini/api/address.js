import { request } from '@/utils/request'

// 用户地址簿：登录态由 token 解析，不接受客户端传 userId
export const listAddresses = () => request({ url: '/mini/user/addresses', method: 'GET' })

export const getDefaultAddress = () => request({ url: '/mini/user/addresses/default', method: 'GET' })

export const getAddress = addressId => request({ url: `/mini/user/addresses/${addressId}`, method: 'GET' })

export const createAddress = data => request({ url: '/mini/user/addresses', method: 'POST', data })

export const updateAddress = (addressId, data) =>
  request({ url: `/mini/user/addresses/${addressId}`, method: 'PUT', data })

export const setDefaultAddress = addressId =>
  request({ url: `/mini/user/addresses/${addressId}/default`, method: 'PUT' })

export const deleteAddress = addressId =>
  request({ url: `/mini/user/addresses/${addressId}`, method: 'DELETE' })
