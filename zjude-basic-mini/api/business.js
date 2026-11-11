import { request } from '@/utils/request'

export const listCertificateProducts = () => request({ url: '/mini/business/certificates', method: 'GET' })
export const createCertificateApplication = data => request({ url: '/mini/business/certificates/applications', method: 'POST', data })
export const getCertificateApplication = id => request({ url: `/mini/business/certificates/applications/${id}`, method: 'GET' })
export const createBusinessPayment = data => request({ url: '/mini/business/payments', method: 'POST', data })
export const syncBusinessPayment = (bizType, bizId) => request({ url: `/mini/business/payments/${bizType}/${bizId}/status`, method: 'GET' })
