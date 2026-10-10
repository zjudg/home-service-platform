import { createPageSettingsApi } from './index'
import request from '@/utils/request'

export const { listConfig, saveConfig } = createPageSettingsApi('app')

export function getAppRuntimeConfig () {
  return request({
    url: '/integrated/app/config',
    method: 'get'
  })
}
