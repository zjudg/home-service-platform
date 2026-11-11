import request from '@/utils/request'

export const listRefund = (params) => request({ url: '/zjudg/refund/list', method: 'get', params })
export const approveRefund = (refundId) => request({ url: `/zjudg/refund/${refundId}/approve`, method: 'put' })
export const rejectRefund = (refundId, reason) =>
  request({ url: `/zjudg/refund/${refundId}/reject`, method: 'put', data: { reason } })
export const syncRefund = (refundId) => request({ url: `/zjudg/refund/${refundId}/sync`, method: 'get' })
