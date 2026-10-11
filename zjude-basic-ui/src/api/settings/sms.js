import { createPageSettingsApi } from './index'
import request from '@/utils/request'

export const { listConfig, saveConfig } = createPageSettingsApi('sms')

export function validateSms () {
  return request({
    url: '/manage/settings/sms/validate',
    method: 'put'
  })
}

export function testSms (data) {
  return request({
    url: '/manage/settings/sms/test',
    method: 'put',
    data
  })
}
