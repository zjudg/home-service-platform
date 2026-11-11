import request from '@/utils/request'

export const listCertificateProducts = (params) => request({ url: '/zjudg/certificate/product/list', method: 'get', params })
export const addCertificateProduct = (data) => request({ url: '/zjudg/certificate/product', method: 'post', data })
export const updateCertificateProduct = (data) => request({ url: '/zjudg/certificate/product', method: 'put', data })
export const deleteCertificateProduct = (ids) => request({ url: `/zjudg/certificate/product/${ids}`, method: 'delete' })
