import { TOKEN_KEY } from '@/constants/app'
import { getStorage } from '@/utils/storage'
import { showDialog } from '@/utils/feedback'

// 页面路由白名单：登录后只允许回跳到这些站内页面
export const LOGIN_REDIRECT_ALLOWLIST = [
  '/pages/home/index',
  '/pages/city/index',
  '/pages/category/index',
  '/pages/search/index',
  '/pages/services/index',
  '/pages/service-detail/index',
  '/pages/booking/index',
  '/pages/orders/index',
  '/pages/order-detail/index',
  '/pages/review/index',
  '/pages/cart/index',
  '/pages/user/index',
  '/pages/address/index',
  '/pages/address/edit/index',
  '/pages/favorite/index',
  '/pages/settings/index',
  // 师傅端分包：与用户端同一个小程序，登录后可直接回跳
  '/pages/master/home/index',
  '/pages/master/pool/index',
  '/pages/master/orders/index',
  '/pages/master/order-detail/index',
  '/pages/master/apply/index',
  '/pages/master/profile/index',
  '/pages/master/wallet/index',
]

// 无页面栈可回退时，reLaunch 的兜底页面
export const HOME_PATH = '/pages/home/index'

// 前端登录态判定：与 auth store 保持同一数据源（本地 token）
export function isAuthenticated () {
  return Boolean(getStorage(TOKEN_KEY, ''))
}

// 当前页面完整地址（含 query），用于登录后回跳
export function currentPageUrl () {
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  const page = pages[pages.length - 1]
  if (!page || !page.route) return ''
  const query = Object.entries(page.options || {})
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join('&')
  return `/${page.route}${query ? `?${query}` : ''}`
}

// 只接受站内白名单路由，避免用 redirect 参数把用户带到任意页面
export function sanitizeRedirect (target) {
  if (!target) return ''
  const path = String(target).split('?')[0]
  return LOGIN_REDIRECT_ALLOWLIST.includes(path) ? target : ''
}

export function buildLoginUrl (redirect) {
  const safeRedirect = sanitizeRedirect(redirect)
  return safeRedirect ? `/pages/login/index?redirect=${encodeURIComponent(safeRedirect)}` : '/pages/login/index'
}

export function goLogin (redirect) {
  uni.navigateTo({ url: buildLoginUrl(redirect) })
}

// 需要登录的页面入口守卫：未登录时跳转登录页并返回 false
export function ensureLoggedIn (redirect) {
  if (isAuthenticated()) return true
  goLogin(redirect)
  return false
}

// 页面内动作级守卫：未登录时弹窗确认后去登录
export async function requireLogin (callback) {
  if (isAuthenticated()) return callback?.()
  const confirmed = await showDialog({
    title: '登录后继续',
    content: '登录后可管理预约和服务信息',
    confirmText: '去登录'
  })
  if (confirmed) goLogin(currentPageUrl())
  return undefined
}
