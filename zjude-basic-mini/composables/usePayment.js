import { createPayment, syncPaymentStatus } from '@/api/pay'
import { createBusinessPayment, syncBusinessPayment } from '@/api/business'

/**
 * 判定支付是否真正完成。
 *
 * <p>只认服务端查单结果：微信前端的 success 回调只代表「用户走完了支付流程」，
 * 弱网或用户中途退出时并不等于钱已到账，因此必须回查订单。
 * 抽成纯函数便于单测，避免判定逻辑散落在 Promise 链里无法覆盖。</p>
 *
 * @param {Object|Object} response 查单接口返回
 * @returns {{paid: boolean, message: string}} paid 为 true 表示服务端确认已支付
 */
export function resolvePayResult (response) {
  const order = response?.data || response
  const paid = String(order?.paymentStatus) === '1'
  return { paid, message: paid ? '' : '支付结果确认中，请稍后刷新订单' }
}

/**
 * 小程序订阅消息模板 ID。
 *
 * 必须以微信公众平台后台申请到的真实模板 ID 替换，且与后台
 * 「通知设置 → 订阅消息」里填写的保持一致。留空则跳过授权请求，
 * 不阻断下单与支付。
 */
/**
 * 请求订阅消息授权。
 *
 * 微信规则：用户每次授权只能推送一条对应模板的消息，因此需要在业务节点
 * 逐一请求，不能一次请求多次推送。用户拒绝或平台不支持时静默返回。
 *
 * @param {string[]} templateIds 需要申请的模板 ID 列表
 * @returns {Promise<boolean>} 是否至少授权了一个模板
 */
export function requestSubscribe (templateIds) {
  // 微信一次调用最多支持 3 个模板；去重可避免后台把同一模板配置到多个事件时调用失败。
  const ids = [...new Set((templateIds || []).filter(Boolean))].slice(0, 3)
  if (!ids.length) return Promise.resolve(false)
  return new Promise(resolve => {
    // #ifdef MP-WEIXIN
    uni.requestSubscribeMessage({
      tmplIds: ids,
      success: result => {
        const accepted = ids.some(id => result[id] === 'accept')
        resolve(accepted)
      },
      fail: () => resolve(false)
    })
    // #endif
    // #ifndef MP-WEIXIN
    resolve(false)
    // #endif
  })
}

/**
 * 发起订单支付。
 *
 * 流程：服务端创建支付单 → 唤起微信支付 → 主动同步支付结果。
 * 支付结果以服务端同步查询为准，不信任前端回调，避免弱网下误判。
 *
 * @param {string} orderId 订单主键
 * @returns {Promise<{paid: boolean, message?: string}>} 支付结果
 */
export function payOrder (orderId) {
  return createPayment(orderId)
    .then(response => {
      const params = response?.data || response
      if (!params || !params.package || !params.paySign) {
        throw new Error('支付参数不完整，请检查后台支付配置')
      }
      return new Promise((resolve, reject) => {
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: params.timeStamp,
          nonceStr: params.nonceStr,
          package: params.package,
          signType: params.signType || 'RSA',
          paySign: params.paySign,
          success: () => resolve(true),
          fail: error => {
            const message = error?.errMsg || ''
            if (message.indexOf('cancel') >= 0) {
              reject(new Error('已取消支付'))
            } else {
              reject(new Error(message || '支付失败'))
            }
          }
        })
      })
    })
    .then(() => syncPaymentStatus(orderId))
    .then(response => resolvePayResult(response))
}

export function payBusiness (bizType, bizId) {
  return createBusinessPayment({ bizType, bizId })
    .then(response => {
      const params = response?.data || response
      if (!params || !params.package || !params.paySign) throw new Error('支付参数不完整，请检查后台支付配置')
      return new Promise((resolve, reject) => {
        uni.requestPayment({ provider: 'wxpay', timeStamp: params.timeStamp, nonceStr: params.nonceStr, package: params.package, signType: params.signType || 'RSA', paySign: params.paySign, success: () => resolve(), fail: error => reject(new Error(error?.errMsg || '支付失败')) })
      })
    })
    .then(() => syncBusinessPayment(bizType, bizId))
    .then(response => {
      const paid = Boolean(response?.data ?? response)
      if (!paid) throw new Error('支付结果确认中，请稍后刷新账单')
      return { paid }
    })
}
