export const isH5 = () => process.env.UNI_PLATFORM === 'h5'
export const isWeixin = () => process.env.UNI_PLATFORM === 'mp-weixin'
export const isApp = () => process.env.UNI_PLATFORM === 'app-plus'

export function getPlatform () {
  if (isWeixin()) return 'mp-weixin'
  if (isApp()) return 'app'
  return 'h5'
}
