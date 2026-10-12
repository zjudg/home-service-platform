import request from '@/utils/request'

export const listBooking = (params) => request({ url: '/zjudg/booking/list', method: 'get', params })
export const addBooking = (data) => request({ url: '/zjudg/booking', method: 'post', data })
export const updateBooking = (data) => request({ url: '/zjudg/booking', method: 'put', data })
export const deleteBooking = (ids) => request({ url: `/zjudg/booking/${ids}`, method: 'delete' })
