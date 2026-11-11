// 首屏加载遮罩控制
// index.html 里的进度条约 2 秒走满，应用挂载后再淡出，避免一闪而过。
const MIN_VISIBLE = 2000
const loadedAt = Date.now()

export function hideAppLoader () {
  if (typeof document === 'undefined') return
  const wrapper = document.getElementById('loader-wrapper')
  if (!wrapper) return
  const remain = Math.max(0, MIN_VISIBLE - (Date.now() - loadedAt))
  window.setTimeout(() => {
    document.body.classList.add('loaded')
    window.setTimeout(() => wrapper.remove(), 600)
  }, remain)
}
