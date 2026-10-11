import { isApp, isH5, isWeixin } from '@/utils/platform'

export function chooseImage (options = {}) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({ count: options.count || 1, sourceType: options.sourceType || ['album', 'camera'], success: resolve, fail: reject })
  })
}

export function copyText (text) {
  return new Promise((resolve, reject) => {
    uni.setClipboardData({ data: text, success: resolve, fail: reject })
  })
}

export function getCapabilities () {
  return { h5: isH5(), weixin: isWeixin(), app: isApp() }
}
