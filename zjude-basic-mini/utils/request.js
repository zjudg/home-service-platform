import { API_TIMEOUT } from '@/constants/app'
import { getStorage } from '@/utils/storage'
import { TOKEN_KEY } from '@/constants/app'

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
let unauthorizedHandler = null

export function setUnauthorizedHandler (handler) {
  unauthorizedHandler = handler
}

function buildUrl (url) {
  if (/^https?:\/\//.test(url)) return url
  return `${baseUrl.replace(/\/$/, '')}/${url.replace(/^\//, '')}`
}

function getErrorMessage (response) {
  return response?.msg || response?.message || '请求失败，请稍后重试'
}

export function request (options = {}) {
  const token = getStorage(TOKEN_KEY, '')
  return new Promise((resolve, reject) => {
    uni.request({
      url: buildUrl(options.url || ''),
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      timeout: options.timeout || API_TIMEOUT,
      success: ({ statusCode, data }) => {
        if (statusCode === 401 || data?.code === 401) {
          unauthorizedHandler?.()
          reject(new Error('登录状态已失效'))
          return
        }
        if (statusCode < 200 || statusCode >= 300) {
          reject(new Error(getErrorMessage(data)))
          return
        }
        if (data && data.code !== undefined && data.code !== 200) {
          reject(new Error(getErrorMessage(data)))
          return
        }
        resolve(data)
      },
      fail: error => reject(new Error(error.errMsg || '网络连接失败'))
    })
  })
}

export function upload (filePath, options = {}) {
  const token = getStorage(TOKEN_KEY, '')
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: buildUrl(options.url || '/common/upload'),
      filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: ({ statusCode, data }) => {
        let response = data
        try { response = typeof data === 'string' ? JSON.parse(data) : data } catch (error) { /* keep raw response */ }
        if (statusCode === 401 || response?.code === 401) {
          unauthorizedHandler?.()
          reject(new Error('登录状态已失效'))
          return
        }
        if (response?.code !== undefined && response.code !== 200) {
          reject(new Error(getErrorMessage(response)))
          return
        }
        resolve(response)
      },
      fail: error => reject(new Error(error.errMsg || '文件上传失败'))
    })
  })
}
