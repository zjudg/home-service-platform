import request from '@/utils/request'

export const listSettleLog = (params) => request({ url: '/zjudg/settle/list', method: 'get', params })
