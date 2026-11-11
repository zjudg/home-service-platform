/**
 * 数据看板的日期区间与金额格式化。
 *
 * <p>这些函数决定请求参数与界面展示，容易被改坏且不易察觉，
 * 因此独立成模块便于单测；页面只负责拼装与渲染。</p>
 */

/**
 * 格式化为 yyyy-MM-dd。
 *
 * <p>刻意用本地时间拼接而不走 toISOString：后者按 UTC 输出，
 * 东八区凌晨会少一天，导致统计区间错位。</p>
 *
 * @param {Date} date 日期
 * @returns {string} yyyy-MM-dd
 */
export function formatDate (date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

/**
 * 生成「近 N 天」的起止日期。
 *
 * @param {number} days 天数，含今天
 * @param {Date} [today] 参考日期，便于测试注入
 * @returns {[string, string]} [开始日期, 结束日期]
 */
export function recentRange (days, today = new Date()) {
  const safeDays = Math.max(1, Number(days) || 1)
  const end = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const begin = new Date(end.getFullYear(), end.getMonth(), end.getDate())
  begin.setDate(end.getDate() - (safeDays - 1))
  return [formatDate(begin), formatDate(end)]
}

/**
 * 金额展示：固定两位小数并加千分位。
 *
 * @param {number|string} value 金额
 * @returns {string} 展示文本
 */
export function formatMoney (value) {
  const number = Number(value ?? 0)
  if (!Number.isFinite(number)) return '0.00'
  return number.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 折线图横轴标签：把 yyyy-MM-dd 截成 MM-dd。
 *
 * @param {string[]} days 日期数组
 * @returns {string[]} 标签数组
 */
export function axisLabels (days) {
  return (days || []).map(day => String(day).slice(5))
}
