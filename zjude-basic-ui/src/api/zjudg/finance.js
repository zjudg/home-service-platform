import request from '@/utils/request'

/**
 * 数据看板汇总。
 *
 * @param {Object} params beginDate / endDate，格式 yyyy-MM-dd，省略时后端取近 15 天
 * @returns {Promise} summary / sales / orders / newUsers / activeUsers / cashflow / refunds / days
 */
export const getFinanceDashboard = (params) =>
  request({ url: '/zjudg/finance/dashboard', method: 'get', params })
