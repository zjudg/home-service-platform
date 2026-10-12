import request from '@/utils/request'

export const listReview = (params) => request({ url: '/zjudg/review/list', method: 'get', params })
export const replyReview = (data) => request({ url: '/zjudg/review/reply', method: 'put', data })
export const updateReviewStatus = (data) => request({ url: '/zjudg/review/status', method: 'put', data })
