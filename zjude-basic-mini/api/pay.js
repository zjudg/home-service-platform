import { request } from '@/utils/request'

// 订单支付：创建支付单后由前端唤起微信支付
export const createPayment = orderId =>
  request({ url: `/mini/pay/orders/${orderId}`, method: 'POST' })

// 主动同步支付结果，用于支付完成后确认，避免仅依赖异步回调
export const syncPaymentStatus = orderId =>
  request({ url: `/mini/pay/orders/${orderId}/status`, method: 'GET' })
