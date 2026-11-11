import { request } from '@/utils/request'

// 用户端退款：申请与进度查询
export const applyRefund = (orderId, reason) =>
  request({ url: `/mini/refund/orders/${orderId}`, method: 'POST', data: { reason } })

export const getRefund = orderId =>
  request({ url: `/mini/refund/orders/${orderId}`, method: 'GET' })
