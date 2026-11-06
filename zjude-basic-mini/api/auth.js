import { request } from '@/utils/request'

export function login (data) {
  return request({ url: '/login', method: 'POST', data })
}

export function wechatLogin (data) {
  return request({ url: '/mini/auth/login', method: 'POST', data, auth: false })
}

export function silentWechatLogin (code) {
  return wechatLogin({ code, silent: true })
}

/** 下发手机验证码 */
export function requestSmsCode (phone) {
  return request({ url: '/mini/auth/sms-code', method: 'POST', data: { phone } })
}

/** 手机号 + 验证码登录，未注册由服务端自动创建账号 */
export function smsLogin (data) {
  return request({ url: '/mini/auth/sms-login', method: 'POST', data })
}

export function getInfo () {
  return request({ url: '/get/info', method: 'GET' })
}

export function logout () {
  return request({ url: '/logout', method: 'POST' })
}
