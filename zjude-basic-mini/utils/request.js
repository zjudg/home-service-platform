import { API_TIMEOUT, TOKEN_KEY } from '@/constants/app'
import { UPLOAD_PATH, joinUrl } from '@/config'
import { getStorage } from '@/utils/storage'

let unauthorizedHandler = null

export function setUnauthorizedHandler (handler) {
  unauthorizedHandler = handler
}

function getErrorMessage (response) {
  return response?.msg || response?.message || response?.errMsg || ''
}

export function request (options = {}) {
  const token = getStorage(TOKEN_KEY, '')
  return new Promise((resolve, reject) => {
    uni.request({
      url: joinUrl(options.url || ''),
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(token && options.auth !== false ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      timeout: options.timeout || API_TIMEOUT,
      success: ({ statusCode, data }) => {
        if (statusCode === 401 || data?.code === 401) {
          unauthorizedHandler?.()
          reject(new Error(getErrorMessage(data)))
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
      fail: reject
    })
  })
}

export function upload (filePath, options = {}) {
  const token = getStorage(TOKEN_KEY, '')
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: joinUrl(options.url || UPLOAD_PATH),
      filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: ({ statusCode, data }) => {
        let response = data
        try { response = typeof data === 'string' ? JSON.parse(data) : data } catch (error) { /* keep raw response */ }
        if (statusCode === 401 || response?.code === 401) {
          unauthorizedHandler?.()
          reject(new Error(getErrorMessage(response)))
          return
        }
        if (response?.code !== undefined && response.code !== 200) {
          reject(new Error(getErrorMessage(response)))
          return
        }
        resolve(response)
      },
      fail: reject
    })
  })
}
