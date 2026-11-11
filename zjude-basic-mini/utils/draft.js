import { getStorage, removeStorage, setStorage } from '@/utils/storage'

const DRAFT_PREFIX = 'zjude-mini-draft:'

function draftKey (key) {
  return `${DRAFT_PREFIX}${key}`
}

// 本地暂存页面草稿（如预约表单），登录回跳后恢复
export function saveDraft (key, value) {
  setStorage(draftKey(key), value)
}

export function readDraft (key, fallback = null) {
  return getStorage(draftKey(key), fallback)
}

export function clearDraft (key) {
  removeStorage(draftKey(key))
}
