import request from '@/utils/request'

export const listWithdraw = (params) => request({ url: '/zjudg/withdraw/list', method: 'get', params })
export const approveWithdraw = (id, remark) =>
  request({ url: `/zjudg/withdraw/${id}/approve`, method: 'put', data: { remark } })
export const rejectWithdraw = (id, remark) =>
  request({ url: `/zjudg/withdraw/${id}/reject`, method: 'put', data: { remark } })
export const markWithdrawPaid = (id, transferNo) =>
  request({ url: `/zjudg/withdraw/${id}/paid`, method: 'put', data: { transferNo } })
export const markWithdrawFailed = (id, remark) =>
  request({ url: `/zjudg/withdraw/${id}/failed`, method: 'put', data: { remark } })
