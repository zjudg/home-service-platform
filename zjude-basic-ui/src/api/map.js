import request from '@/utils/request'

export function getMapRuntimeConfig () {
  return request({
    url: '/integrated/map/config',
    method: 'get'
  })
}
