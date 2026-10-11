export const channels = [
  {
    key: 'system',
    name: '系统通知',
    description: 'APP及站内系统通知'
  },
  {
    key: 'sms',
    name: '短信通知',
    description: '阿里云短信通知'
  },
  {
    key: 'wechat',
    name: '微信模板消息',
    description: '微信公众号模板消息'
  },
  {
    key: 'mini',
    name: '微信小程序提醒',
    description: '微信小程序订阅消息'
  }
]

const verifyVariables = [
  ['手机号', '{mobile}'],
  ['验证码', '{code}']
]

export const notificationScenes = [
  {
    key: 'systemAnnouncement',
    name: '系统通知',
    description: '向站内或客户端发送系统通知',
    channels: ['system', 'wechat', 'mini'],
    variables: [['标题', '{title}'], ['内容', '{content}'], ['时间', '{time}']]
  },
  {
    key: 'register',
    name: '注册验证码',
    description: '用户注册时发送验证码',
    channels: ['sms'],
    variables: verifyVariables
  },
  {
    key: 'changeMobile',
    name: '变更手机号短信',
    description: '会员变更手机号通知',
    channels: ['sms'],
    variables: verifyVariables
  },
  {
    key: 'resetPassword',
    name: '重置密码短信',
    description: '会员找回密码通知',
    channels: ['sms'],
    variables: verifyVariables
  },
  {
    key: 'loginCode',
    name: '验证码登录',
    description: '会员验证码登录',
    channels: ['sms'],
    variables: verifyVariables
  },
  {
    key: 'bindMobile',
    name: '绑定手机号',
    description: '会员绑定手机号',
    channels: ['sms'],
    variables: verifyVariables
  }
]
