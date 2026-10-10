import { createPageSettingsApi } from './index'

import request from '@/utils/request'

export const { listConfig, saveConfig } = createPageSettingsApi('payment')

export function validatePayment (provider) {
  return request({
    url: '/manage/settings/payment/validate',
    method: 'put',
    data: { provider }
  })
}
