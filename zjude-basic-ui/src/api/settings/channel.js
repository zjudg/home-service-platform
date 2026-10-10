import request from '@/utils/request'
import { createPageSettingsApi } from './index'

export const wechatConfigApi = createPageSettingsApi('channel-wechat')
export const miniConfigApi = createPageSettingsApi('channel-mini')
export const openConfigApi = createPageSettingsApi('channel-open')
export const h5ConfigApi = createPageSettingsApi('channel-h5')
export const pcConfigApi = createPageSettingsApi('channel-pc')

export const channelApi = {
  listMenus: params => request({ url: '/manage/channel/menus/list', method: 'get', params }),
  saveMenu: data => request({ url: '/manage/channel/menus', method: data.menuId ? 'put' : 'post', data }),
  removeMenu: id => request({ url: `/manage/channel/menus/${id}`, method: 'delete' }),
  listReplies: params => request({ url: '/manage/channel/replies/list', method: 'get', params }),
  saveReply: data => request({ url: '/manage/channel/replies', method: data.replyId ? 'put' : 'post', data }),
  removeReply: id => request({ url: `/manage/channel/replies/${id}`, method: 'delete' }),
  listUploads: params => request({ url: '/manage/channel/uploads/list', method: 'get', params }),
  addUpload: data => request({ url: '/manage/channel/uploads', method: 'post', data })
}
