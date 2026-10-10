import request from '@/utils/request'
import {
  mockListBanner,
  mockGetBanner,
  mockAddBanner,
  mockUpdateBanner,
  mockDelBanner
} from './banner.mock'

const useMock = import.meta.env.DEV

// 查询轮播图列表
export function listBanner (query) {
  if (useMock) {
    return mockListBanner(query)
  }
  return request({
    url: '/mall/banner/list',
    method: 'get',
    params: query
  })
}

// 查询轮播图详细
export function getBanner (bannerId) {
  if (useMock) {
    return mockGetBanner(bannerId)
  }
  return request({
    url: '/mall/banner/' + bannerId,
    method: 'get'
  })
}

// 新增轮播图
export function addBanner (data) {
  if (useMock) {
    return mockAddBanner(data)
  }
  return request({
    url: '/mall/banner',
    method: 'post',
    data
  })
}

// 修改轮播图
export function updateBanner (data) {
  if (useMock) {
    return mockUpdateBanner(data)
  }
  return request({
    url: '/mall/banner',
    method: 'put',
    data
  })
}

// 删除轮播图
export function delBanner (bannerId) {
  if (useMock) {
    return mockDelBanner(bannerId)
  }
  return request({
    url: '/mall/banner/' + bannerId,
    method: 'delete'
  })
}
