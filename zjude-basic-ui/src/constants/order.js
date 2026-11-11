// 订单状态唯一口径，与后端 com.zjude.common.enums.ZjudgOrderStatus 保持一致。
// 小程序侧对应 constants/order.js，两端改动需同步。

export const ORDER_STATUS = {
  PENDING: '0',
  SERVING: '1',
  FINISHED: '2',
  CANCELLED: '3',
  ACCEPTED: '4'
}

export const ORDER_STATUS_LABEL = {
  0: '待接单',
  1: '服务中',
  2: '已完成',
  3: '已取消',
  4: '已接单'
}

export const ORDER_STATUS_TAG_TYPE = {
  0: 'warning',
  1: 'primary',
  2: 'success',
  3: 'info',
  4: 'warning'
}

/** 后台筛选下拉选项，顺序按业务推进顺序 */
export const ORDER_STATUS_OPTIONS = [
  { label: '待接单', value: ORDER_STATUS.PENDING },
  { label: '已接单', value: ORDER_STATUS.ACCEPTED },
  { label: '服务中', value: ORDER_STATUS.SERVING },
  { label: '已完成', value: ORDER_STATUS.FINISHED },
  { label: '已取消', value: ORDER_STATUS.CANCELLED }
]

export const PAYMENT_STATUS_LABEL = {
  0: '未支付',
  1: '已支付',
  2: '已退款'
}

export const PAYMENT_SOURCE_LABEL = {
  WECHAT: '微信支付',
  OFFLINE: '线下收款'
}

export function orderStatusLabel (value) {
  return ORDER_STATUS_LABEL[String(value)] || String(value ?? '-')
}

export function orderStatusTagType (value) {
  return ORDER_STATUS_TAG_TYPE[String(value)] || 'info'
}

export function paymentStatusLabel (value) {
  return PAYMENT_STATUS_LABEL[String(value)] || String(value ?? '-')
}

export function paymentSourceLabel (value) {
  return PAYMENT_SOURCE_LABEL[String(value)] || String(value ?? '-')
}

// 师傅审核状态，与后端 ZjudgMaster 保持一致
export const MASTER_AUDIT_LABEL = {
  0: '待审核',
  1: '已通过',
  2: '已驳回'
}

export const MASTER_AUDIT_TAG_TYPE = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

export const MASTER_AUDIT_OPTIONS = [
  { label: '待审核', value: '0' },
  { label: '已通过', value: '1' },
  { label: '已驳回', value: '2' }
]

export function masterAuditLabel (value) {
  return MASTER_AUDIT_LABEL[String(value)] || '待审核'
}

export function masterAuditTagType (value) {
  return MASTER_AUDIT_TAG_TYPE[String(value)] || 'info'
}
