import { uploadAvatarFile, uploadFile } from '@/api/common'
import { updateAvatar } from '@/api/user'

function getAvatarUrl (response) {
  const avatarUrl = response?.url || response?.data?.url || ''
  if (!/^https?:\/\/\S+$/.test(avatarUrl)) throw new Error('头像上传未返回 OSS 地址')
  return avatarUrl
}

/** Upload an image through the configured object-storage provider. */
export async function uploadAvatarToStorage (filePath) {
  return getAvatarUrl(await uploadAvatarFile(filePath))
}

/** Upload an image through the configured object-storage provider and save its URL. */
export async function saveUserAvatar (filePath, authStore) {
  const avatarUrl = getAvatarUrl(await uploadFile(filePath))

  await updateAvatar(avatarUrl)
  await authStore.loadUser()
  return avatarUrl
}
