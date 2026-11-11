// 统一接口入口：整个小程序只维护这一个地址。
// 修改后端所在主机或端口时，只改这里；指向后端的服务根地址，不要带 /api。
export const API_BASE_URL = 'http://127.0.0.1:8888'
// export const API_BASE_URL = 'https://miaoj.cc/'
// 本地直接访问 Spring Boot 时留空；经生产网关访问时可设为 '/dev-api'。
export const API_PREFIX = ''
// export const API_PREFIX = '/dev-api'

// 上传接口（/common/upload）挂在服务根路径下，不走 /api 前缀
export const UPLOAD_PATH = '/common/upload'

/** 拼接后端完整地址 */
export function joinUrl (path = '') {
  const base = API_BASE_URL.replace(/\/+$/, '')
  if (!path) return base
  if (/^https?:\/\//.test(path)) return path
  let route = path.startsWith('/') ? path : `/${path}`
  const prefix = API_PREFIX.replace(/\/+$/, '')

  // Accept both bare API paths and paths copied from the production dev proxy.
  if (prefix && (route === prefix || route.startsWith(`${prefix}/`))) {
    route = route.slice(prefix.length) || '/'
  } else if (route === '/dev-api' || route.startsWith('/dev-api/')) {
    route = route.slice('/dev-api'.length) || '/'
  }

  // Mini endpoints are served under /api/mini; auth, system and upload endpoints stay at root.
  const apiRoute = route === '/mini' || route.startsWith('/mini/')
    ? `/api${route}`
    : route
  return `${base}${prefix}${apiRoute}`
}
