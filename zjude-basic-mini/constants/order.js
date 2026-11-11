// 订单状态唯一口径：用户端与师傅端共用，避免出现两套文案。
// 与后端 com.zjude.common.enums.ZjudgOrderStatus 保持一致。
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

export const ORDER_STATUS_DESC = {
  0: '等待师傅接单，请耐心等待',
  1: '师傅正在为您服务',
  2: '服务已完成，可在订单详情查看并评价',
  3: '订单已取消',
  4: '师傅已接单，将按预约时间上门'
}

/**
 * 「待评价」不是订单状态，而是「已完成 + 未评价」的组合条件。
 * 这里用一个不会与真实状态冲突的标记值，避免在页签里塞复合逻辑。
 */
export const REVIEW_PENDING = 'review_pending'
export const PAYMENT_PENDING = 'payment_pending'

/**
 * 判断订单是否处于待评价。
 *
 * @param {Object} order 订单
 * @returns {boolean} 已完成且没有评价记录
 */
export function isReviewPending (order) {
  if (!order) return false
  const finished = String(order.orderStatus) === ORDER_STATUS.FINISHED
  return finished && Number(order.reviewed || 0) === 0
}

/**
 * 判断订单是否命中某个页签。
 *
 * @param {Object} order 订单
 * @param {string} tabValue 页签值，空串表示全部
 * @returns {boolean} 是否命中
 */
export function matchesTab (order, tabValue) {
  if (!tabValue) return true
  if (tabValue === PAYMENT_PENDING) return canPayOrder(order)
  if (tabValue === REVIEW_PENDING) return isReviewPending(order)
  // 可支付订单统一归入「待支付」，不要同时出现在已接单等生命周期页签。
  if (canPayOrder(order)) return false
  if (tabValue === ORDER_STATUS.PENDING) {
    return String(order?.orderStatus) === ORDER_STATUS.PENDING && String(order?.paymentStatus) === '1'
  }
  return String(order?.orderStatus) === tabValue
}

/**
 * 用户端是否可以发起支付。
 * 待接单和已接单都可能处于未支付状态，服务中及终态订单不再开放支付。
 */
export function canPayOrder (order) {
  const status = String(order?.orderStatus)
  return (status === ORDER_STATUS.PENDING || status === ORDER_STATUS.ACCEPTED) &&
    String(order?.paymentStatus) === '0'
}

/** 用户端是否可以取消订单。 */
export function canCancelOrder (order) {
  return [ORDER_STATUS.PENDING, ORDER_STATUS.ACCEPTED, ORDER_STATUS.SERVING]
    .includes(String(order?.orderStatus))
}

/** 已支付且未进入终态的订单可以申请退款。 */
export function canRefundOrder (order) {
  const status = String(order?.orderStatus)
  return String(order?.paymentStatus) === '1' &&
    status !== ORDER_STATUS.FINISHED && status !== ORDER_STATUS.CANCELLED
}

/** 是否需要渲染订单操作区。 */
export function hasOrderActions (order) {
  const completed = String(order?.orderStatus) === ORDER_STATUS.FINISHED
  return canCancelOrder(order) || canPayOrder(order) || canRefundOrder(order) ||
    isReviewPending(order) || (completed && !isReviewPending(order))
}

// 用户端「我的订单」筛选页签
export const USER_ORDER_TABS = [
  { label: '全部', value: '' },
  { label: '待支付', value: PAYMENT_PENDING },
  { label: '待接单', value: ORDER_STATUS.PENDING },
  { label: '已接单', value: ORDER_STATUS.ACCEPTED },
  { label: '服务中', value: ORDER_STATUS.SERVING },
  { label: '已完成', value: ORDER_STATUS.FINISHED }
]

// 师傅端「我的订单」筛选页签
export const MASTER_ORDER_TABS = [
  { label: '待开工', value: ORDER_STATUS.ACCEPTED },
  { label: '服务中', value: ORDER_STATUS.SERVING },
  { label: '已完成', value: ORDER_STATUS.FINISHED }
]

// 用户端订单进度：下单 → 接单 → 服务中 → 完成
export const USER_ORDER_STEPS = [
  { key: 'created', label: '已下单' },
  { key: 'accepted', label: '师傅接单' },
  { key: 'serving', label: '服务中' },
  { key: 'done', label: '已完成' }
]

// 师傅端订单进度：接单 → 开工 → 完工
export const MASTER_ORDER_STEPS = [
  { key: 'accepted', label: '已接单' },
  { key: 'serving', label: '服务中' },
  { key: 'done', label: '已完成' }
]

export function orderStatusLabel (value) {
  return ORDER_STATUS_LABEL[String(value)] || '处理中'
}

export function orderStatusDesc (value) {
  return ORDER_STATUS_DESC[String(value)] || ''
}

/** Customer-facing status combines the order lifecycle and payment state. */
export function userOrderStatusLabel (order) {
  return canPayOrder(order) ? '待支付' : orderStatusLabel(order?.orderStatus)
}

/** Actionable guidance for a submitted order whose payment is incomplete. */
export function userOrderStatusDesc (order) {
  return canPayOrder(order)
    ? '订单已提交，请完成支付后等待师傅接单'
    : orderStatusDesc(order?.orderStatus)
}

/**
 * 用户端订单进度完成到的步骤下标，已取消时返回 -1。
 * @param {string|number} status 订单状态
 * @returns {number} 已完成步骤下标
 */
export function userStepIndex (status) {
  const value = String(status ?? '')
  if (value === ORDER_STATUS.CANCELLED) return -1
  if (value === ORDER_STATUS.PENDING) return 0
  if (value === ORDER_STATUS.ACCEPTED) return 1
  if (value === ORDER_STATUS.SERVING) return 2
  if (value === ORDER_STATUS.FINISHED) return 3
  return 0
}

/**
 * 师傅端订单进度完成到的步骤下标，已取消时返回 -1。
 * @param {string|number} status 订单状态
 * @returns {number} 已完成步骤下标
 */
export function masterStepIndex (status) {
  const value = String(status ?? '')
  if (value === ORDER_STATUS.CANCELLED) return -1
  if (value === ORDER_STATUS.ACCEPTED) return 0
  if (value === ORDER_STATUS.SERVING) return 1
  if (value === ORDER_STATUS.FINISHED) return 2
  return 0
}

export const PAYMENT_STATUS_LABEL = {
  0: '未支付',
  1: '已支付',
  2: '已退款'
}

export function paymentStatusLabel (value) {
  return PAYMENT_STATUS_LABEL[String(value)] || '-'
}

/** Service cover is fetched from the current service row for order-card display. */
export function orderServiceCover (order) {
  const images = order?.serviceImage || ''
  const values = Array.isArray(images) ? images : String(images).split(',')
  return values.map(value => String(value || '').trim()).find(Boolean) || ''
}

/** Give each order card a recognizable fallback icon when no cover exists. */
export function orderServiceIcon (order) {
  const name = `${order?.serviceName || ''} ${order?.categoryName || ''}`
  if (/月嫂|育儿|保姆|母婴|护工|陪护|陪诊|照护/.test(name)) return 'staff-filled'
  if (/维修|安装|家电|电器/.test(name)) return 'settings-filled'
  return 'home-filled'
}
