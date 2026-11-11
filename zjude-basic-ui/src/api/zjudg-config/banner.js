import request from '@/utils/request'

// 查询轮播图列表
export function listBanner (query) {
  return request({
    url: '/zjudg/banner/list',
    method: 'get',
    params: query
  })
}

// 查询轮播图详细
export function getBanner (bannerId) {
  return request({
    url: '/zjudg/banner/' + bannerId,
    method: 'get'
  })
}

// 新增轮播图
export function addBanner (data) {
  return request({
    url: '/zjudg/banner',
    method: 'post',
    data
  })
}

// 修改轮播图
export function updateBanner (data) {
  return request({
    url: '/zjudg/banner',
    method: 'put',
    data
  })
}

// 删除轮播图
export function delBanner (bannerId) {
  return request({
    url: '/zjudg/banner/' + bannerId,
    method: 'delete'
  })
}
