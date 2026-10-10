import { upload } from '@/utils/request'

export function uploadFile (filePath, options = {}) {
  return upload(filePath, options)
}
