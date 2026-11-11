import { readonly, ref } from 'vue'

/**
 * 全局提示服务：统一 Toast / Dialog 的调用入口。
 * 组件层通过 <AppFeedback /> 渲染，页面只调用这里的方法。
 */

const toastState = ref({
  visible: false,
  message: '',
  type: 'info'
})

const dialogState = ref({
  visible: false,
  title: '',
  content: '',
  confirmText: '确定',
  cancelText: '取消',
  showCancel: true,
  danger: false,
  resolve: null
})

let toastTimer = null

export const toast = readonly(toastState)
export const dialog = readonly(dialogState)

/**
 * 轻提示
 * @param {string} message 文案
 * @param {{ type?: 'info'|'success'|'warning'|'error', duration?: number }} options
 */
export function showToast (message, options = {}) {
  const { type = 'info', duration = 2000 } = options
  if (!message) return
  if (toastTimer) clearTimeout(toastTimer)
  Object.assign(toastState.value, { visible: true, message, type })
  toastTimer = setTimeout(() => { toastState.value.visible = false }, duration)
}

export function hideToast () {
  if (toastTimer) clearTimeout(toastTimer)
  toastState.value.visible = false
}

/**
 * 确认弹窗，返回 Promise<boolean>
 * @param {{ title?: string, content?: string, confirmText?: string, cancelText?: string, showCancel?: boolean, danger?: boolean }} options
 * @returns {Promise<boolean>}
 */
export function showDialog (options = {}) {
  const {
    title = '提示',
    content = '',
    confirmText = '确定',
    cancelText = '取消',
    showCancel = true,
    danger = false
  } = options

  // 已有弹窗时先结束上一个，避免 Promise 悬挂
  if (dialogState.value.visible) dialogState.value.resolve?.(false)

  return new Promise(resolve => {
    Object.assign(dialogState.value, {
      visible: true,
      title,
      content,
      confirmText,
      cancelText,
      showCancel,
      danger,
      resolve
    })
  })
}

export function resolveDialog (confirmed) {
  const resolve = dialogState.value.resolve
  dialogState.value.visible = false
  dialogState.value.resolve = null
  resolve?.(confirmed)
}

/** 成功提示 */
export function toastSuccess (message) { showToast(message, { type: 'success' }) }
/** 错误提示 */
export function toastError (message) { showToast(message, { type: 'error' }) }
/** 普通提示 */
export function toastInfo (message) { showToast(message, { type: 'info' }) }
/** 警告提示 */
export function toastWarning (message) { showToast(message, { type: 'warning' }) }
