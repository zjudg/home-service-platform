/**
 * 城市管理的纯函数。
 *
 * <p>首字母归一与表单校验规则决定提交给后端的数据，
 * 出错时用户只会看到「系统异常」，因此独立成模块便于单测。</p>
 */

/**
 * 首字母统一转大写并去掉空白。
 *
 * <p>列表按 `initial_letter, city_name` 排序，大小写混排会让同一字母的城市
 * 分裂成两组，所以归一必须在提交前完成，而不是靠后端兜底。</p>
 *
 * @param {string} value 原始首字母
 * @returns {string} 单个大写字母；输入非法时返回空串
 */
export function normalizeInitialLetter (value) {
  const letter = String(value == null ? '' : value).trim().toUpperCase()
  return /^[A-Z]$/.test(letter) ? letter : ''
}

/**
 * 城市ID 去掉首尾空白。
 *
 * <p>城市ID 是主键，带空格的 ID 在 `=` 查询里查不到，会造成「新增报已存在、
 * 详情查不到」的自相矛盾，因此入口处先清洗。</p>
 *
 * @param {string} value 原始ID
 * @returns {string} 清洗后的ID
 */
export function normalizeCityId (value) {
  return String(value == null ? '' : value).trim()
}

/**
 * 把表单数据整理成提交给后端的载荷。
 *
 * @param {Object} form 表单对象
 * @returns {Object} 只保留后端声明的字段，并完成归一
 */
export function buildCityPayload (form) {
  const source = form || {}
  return {
    cityId: normalizeCityId(source.cityId),
    cityName: String(source.cityName == null ? '' : source.cityName).trim(),
    provinceName: String(source.provinceName == null ? '' : source.provinceName).trim(),
    initialLetter: normalizeInitialLetter(source.initialLetter),
    isHot: source.isHot === 'Y' ? 'Y' : 'N',
    status: source.status === '1' ? '1' : '0'
  }
}

/**
 * 提交前校验，返回第一条错误文案。
 *
 * <p>后端已有 `@NotBlank`/`@Size`，但前端不校验会导致请求打出去才报错，
 * 用户看到的是「系统异常」而不是具体字段问题。</p>
 *
 * @param {Object} payload 已归一的载荷
 * @returns {string|null} 错误文案；无错误返回 null
 */
export function validateCityPayload (payload) {
  if (!payload.cityId) return '城市ID不能为空'
  if (payload.cityId.length > 16) return '城市ID长度不能超过16个字符'
  if (!payload.cityName) return '城市名称不能为空'
  if (payload.cityName.length > 64) return '城市名称长度不能超过64个字符'
  if (!payload.provinceName) return '所属省份不能为空'
  if (payload.provinceName.length > 64) return '所属省份长度不能超过64个字符'
  if (!payload.initialLetter) return '首字母必须是单个英文字母'
  return null
}

/**
 * 批量删除时把ID数组拼成后端接受的逗号分隔串。
 *
 * @param {string[]} ids 城市ID数组
 * @returns {string} 逗号分隔的城市ID；空数组返回空串
 */
export function joinCityIds (ids) {
  if (!Array.isArray(ids)) return ''
  return ids
    .map(id => normalizeCityId(id))
    .filter(Boolean)
    .join(',')
}
