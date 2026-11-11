import { request } from '@/utils/request'

// 师傅端接口统一前缀 /mini/master，与用户端接口完全隔离。
// 服务端会校验当前登录账号是否为「审核通过且未停用」的师傅。

/** 我的师傅档案（未入驻时 applied=false） */
export const getMasterProfile = () => request({ url: '/mini/master/profile', method: 'GET' })

/** 提交入驻申请，重复提交会覆盖资料并重新进入待审核 */
export const applyMaster = data => request({ url: '/mini/master/apply', method: 'POST', data })

/** 切换接单开关，accepting: '1' 接单中 / '0' 停接 */
export const switchAccepting = accepting =>
  request({ url: '/mini/master/accepting', method: 'PUT', data: { accepting } })

/** 待接单池 */
export const listPoolOrders = (params = {}) =>
  request({ url: '/mini/master/orders/pool', method: 'GET', data: params })

/** 我的订单 */
export const listMasterOrders = (params = {}) =>
  request({ url: '/mini/master/orders', method: 'GET', data: params })

/** 我的订单详情 */
export const getMasterOrder = id => request({ url: `/mini/master/orders/${id}`, method: 'GET' })

/** 抢单 */
export const acceptOrder = id => request({ url: `/mini/master/orders/${id}/accept`, method: 'PUT' })

/** 开工 */
export const startOrder = id => request({ url: `/mini/master/orders/${id}/start`, method: 'PUT' })

/** 拒单：退回待派单池，会计入近期拒单次数并影响派单权重 */
export const rejectOrder = (id, reason = '') =>
  request({ url: `/mini/master/orders/${id}/reject`, method: 'PUT', data: { reason } })

/** 完工，可附完工凭证 */
export const finishOrder = (id, data = {}) =>
  request({ url: `/mini/master/orders/${id}/finish`, method: 'PUT', data })
