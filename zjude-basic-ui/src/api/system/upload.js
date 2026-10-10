import request from '@/utils/request'

// 查询上传设置
export function getUploadSettings () {
  return request({
    url: '/manage/upload/settings',
    method: 'get'
  })
}

// 保存上传设置
export function saveUploadSettings (data) {
  return request({
    url: '/manage/upload/settings',
    method: 'put',
    data
  })
}
