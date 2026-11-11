import request from '@/utils/request'

export const listCertificateApplications = (params) => request({ url: '/zjudg/business/certificate/applications', method: 'get', params })
export const updateCertificateApplication = (id, data) => request({ url: `/zjudg/business/certificate/applications/${id}`, method: 'put', data })
