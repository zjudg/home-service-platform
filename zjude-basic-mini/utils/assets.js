import { API_BASE_URL } from '@/config'

// 后端返回的相对图片地址补全为可访问地址；已是完整 URL 时原样返回
export function assetUrl (value) {
  if (!value) return ''
  if (/^(https?:)?\/\//.test(value) || value.startsWith('data:')) return value
  const base = API_BASE_URL.replace(/\/+$/, '')
  return `${base}${value.startsWith('/') ? value : `/${value}`}`
}
