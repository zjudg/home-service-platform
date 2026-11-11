import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 路由切换进度条：更细更慢，主色与小程序品牌橙一致
NProgress.configure({
  showSpinner: false,
  minimum: 0.12,
  trickleSpeed: 160,
  speed: 420,
  easing: 'ease',
  parent: 'body'
})
