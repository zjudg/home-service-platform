export function assetUrl (value) {
  if (!value) return ''
  if (/^https?:\/\//.test(value)) return value

  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  if (value.startsWith('/api/')) return value
  const prefix = baseUrl.replace(/\/$/, '')
  return `${prefix}${value.startsWith('/') ? value : `/${value}`}`
}
