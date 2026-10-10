import request from '@/utils/request'
import { createPageSettingsApi } from './index'

export const { listConfig, saveConfig } = createPageSettingsApi('website')

export function getWebsiteSettings () {
  return request({
    url: '/manage/settings/website',
    method: 'get'
  })
}

export function saveWebsiteSettings (data) {
  return request({
    url: '/manage/settings/website',
    method: 'put',
    data
  })
}
