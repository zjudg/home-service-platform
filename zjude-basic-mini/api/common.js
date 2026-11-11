import { upload } from '@/utils/request'

export function uploadFile (filePath, options = {}) {
  return upload(filePath, options)
}

// 登录前选择头像使用独立的匿名图片上传入口；文件仍由后台配置的 OSS 提供商保存。
export function uploadAvatarFile (filePath) {
  return upload(filePath, { url: '/common/upload/avatar', auth: false })
}
