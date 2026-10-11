const IMAGE_POOL = [
  'https://picsum.photos/seed/fruit/banner/1/750/360',
  'https://picsum.photos/seed/fruit/banner/2/750/360',
  'https://picsum.photos/seed/fruit/banner/3/750/360',
  'https://picsum.photos/seed/fruit/banner/4/750/360'
]

let autoIncrement = 0

const rows = [
  {
    bannerId: 'B_20260920100000001',
    bannerTitle: '中秋水果礼盒预售',
    imageUrl: IMAGE_POOL[0],
    linkType: '1',
    linkUrl: '/pages/activity/midautumn',
    bannerPosition: 'index',
    bannerSort: 1,
    status: '0',
    startTime: '2026-09-01 00:00:00',
    endTime: '2026-10-07 23:59:59',
    createBy: 'admin',
    createTime: '2026-09-20 10:12:33',
    updateBy: '',
    updateTime: null,
    remark: '首页主推位'
  },
  {
    bannerId: 'B_20260920100000002',
    bannerTitle: '智利车厘子空运直达',
    imageUrl: IMAGE_POOL[1],
    linkType: '2',
    linkUrl: 'https://example.com/cherry',
    bannerPosition: 'index',
    bannerSort: 2,
    status: '0',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-09-18 09:41:02',
    updateBy: '',
    updateTime: null,
    remark: '长期有效'
  },
  {
    bannerId: 'B_20260920100000003',
    bannerTitle: '热带水果专区上新',
    imageUrl: IMAGE_POOL[2],
    linkType: '1',
    linkUrl: '/pages/category/list?id=tropical',
    bannerPosition: 'category',
    bannerSort: 1,
    status: '0',
    startTime: '2026-09-10 00:00:00',
    endTime: '2026-12-31 23:59:59',
    createBy: 'admin',
    createTime: '2026-09-10 15:20:11',
    updateBy: 'admin',
    updateTime: '2026-09-15 11:03:47',
    remark: ''
  },
  {
    bannerId: 'B_20260920100000004',
    bannerTitle: '国庆满减活动',
    imageUrl: IMAGE_POOL[3],
    linkType: '0',
    linkUrl: '',
    bannerPosition: 'activity',
    bannerSort: 1,
    status: '1',
    startTime: '2026-09-28 00:00:00',
    endTime: '2026-10-08 23:59:59',
    createBy: 'admin',
    createTime: '2026-09-08 08:05:19',
    updateBy: '',
    updateTime: null,
    remark: '待运营确认后启用'
  },
  {
    bannerId: 'B_20260920100000005',
    bannerTitle: '新人首单立减 20 元',
    imageUrl: IMAGE_POOL[0],
    linkType: '1',
    linkUrl: '/pages/coupon/center',
    bannerPosition: 'activity',
    bannerSort: 2,
    status: '0',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-09-05 19:33:50',
    updateBy: '',
    updateTime: null,
    remark: ''
  },
  {
    bannerId: 'B_20260920100000006',
    bannerTitle: '当季柑橘尝鲜价',
    imageUrl: IMAGE_POOL[1],
    linkType: '1',
    linkUrl: '/pages/goods/detail?id=10086',
    bannerPosition: 'index',
    bannerSort: 3,
    status: '0',
    startTime: '2026-09-25 00:00:00',
    endTime: '2026-11-30 23:59:59',
    createBy: 'admin',
    createTime: '2026-09-01 14:27:06',
    updateBy: '',
    updateTime: null,
    remark: ''
  },
  {
    bannerId: 'B_20260920100000007',
    bannerTitle: '进口蓝莓买二送一',
    imageUrl: IMAGE_POOL[2],
    linkType: '0',
    linkUrl: '',
    bannerPosition: 'category',
    bannerSort: 2,
    status: '0',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-08-28 10:11:12',
    updateBy: '',
    updateTime: null,
    remark: ''
  },
  {
    bannerId: 'B_20260920100000008',
    bannerTitle: '会员日双倍积分',
    imageUrl: IMAGE_POOL[3],
    linkType: '2',
    linkUrl: 'https://example.com/member/day',
    bannerPosition: 'activity',
    bannerSort: 3,
    status: '1',
    startTime: '2026-08-01 00:00:00',
    endTime: '2026-08-31 23:59:59',
    createBy: 'admin',
    createTime: '2026-08-20 16:45:30',
    updateBy: 'admin',
    updateTime: '2026-09-02 09:14:22',
    remark: '活动已结束'
  },
  {
    bannerId: 'B_20260920100000009',
    bannerTitle: '有机蔬菜专区',
    imageUrl: IMAGE_POOL[0],
    linkType: '1',
    linkUrl: '/pages/category/list?id=organic',
    bannerPosition: 'category',
    bannerSort: 3,
    status: '0',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-08-15 11:52:08',
    updateBy: '',
    updateTime: null,
    remark: ''
  },
  {
    bannerId: 'B_20260920100000010',
    bannerTitle: '冷链配送全程保鲜',
    imageUrl: IMAGE_POOL[1],
    linkType: '0',
    linkUrl: '',
    bannerPosition: 'index',
    bannerSort: 4,
    status: '0',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-08-10 09:03:41',
    updateBy: '',
    updateTime: null,
    remark: '品牌宣传位'
  },
  {
    bannerId: 'B_20260920100000011',
    bannerTitle: '榴莲季限时秒杀',
    imageUrl: IMAGE_POOL[2],
    linkType: '1',
    linkUrl: '/pages/seckill/index',
    bannerPosition: 'activity',
    bannerSort: 4,
    status: '0',
    startTime: '2026-09-30 10:00:00',
    endTime: '2026-10-15 22:00:00',
    createBy: 'admin',
    createTime: '2026-08-06 20:18:55',
    updateBy: '',
    updateTime: null,
    remark: ''
  },
  {
    bannerId: 'B_20260920100000012',
    bannerTitle: '企业团购通道',
    imageUrl: IMAGE_POOL[3],
    linkType: '2',
    linkUrl: 'https://example.com/b2b',
    bannerPosition: 'index',
    bannerSort: 5,
    status: '1',
    startTime: null,
    endTime: null,
    createBy: 'admin',
    createTime: '2026-08-01 08:30:00',
    updateBy: '',
    updateTime: null,
    remark: ''
  }
]

function delay (payload) {
  return new Promise(resolve => setTimeout(() => resolve(payload), 200))
}

function now () {
  const current = new Date()
  const pad = value => String(value).padStart(2, '0')
  return `${current.getFullYear()}-${pad(current.getMonth() + 1)}-${pad(current.getDate())} ` +
    `${pad(current.getHours())}:${pad(current.getMinutes())}:${pad(current.getSeconds())}`
}

function matchesKeyword (row, keyword) {
  return !keyword || row.bannerTitle.includes(keyword)
}

function matchesDateRange (row, params) {
  const beginTime = params?.beginTime
  const endTime = params?.endTime
  const created = row.createTime.slice(0, 10)
  if (beginTime && created < beginTime) {
    return false
  }
  return !endTime || created <= endTime
}

export function mockListBanner (query = {}) {
  const { pageNum = 1, pageSize = 10, bannerTitle, bannerPosition, linkType, status, params } = query
  const filtered = rows.filter(row =>
    matchesKeyword(row, bannerTitle) &&
    (!bannerPosition || row.bannerPosition === bannerPosition) &&
    (!linkType || row.linkType === linkType) &&
    (!status || row.status === status) &&
    matchesDateRange(row, params)
  )
  const start = (Number(pageNum) - 1) * Number(pageSize)
  return delay({
    code: 200,
    msg: '查询成功',
    rows: filtered.slice(start, start + Number(pageSize)).map(row => ({ ...row })),
    total: filtered.length
  })
}

export function mockGetBanner (bannerId) {
  const target = rows.find(row => row.bannerId === String(bannerId))
  return delay({
    code: 200,
    msg: '操作成功',
    data: target ? { ...target } : null
  })
}

export function mockAddBanner (data) {
  autoIncrement += 1
  rows.unshift({
    ...data,
    bannerId: `B_${now().slice(0, 10).replaceAll('-', '')}9000000${autoIncrement}`,
    createBy: 'admin',
    createTime: now(),
    updateBy: '',
    updateTime: null
  })
  return delay({ code: 200, msg: '操作成功' })
}

export function mockUpdateBanner (data) {
  const index = rows.findIndex(row => row.bannerId === data.bannerId)
  if (index !== -1) {
    rows[index] = { ...rows[index], ...data, updateBy: 'admin', updateTime: now() }
  }
  return delay({ code: 200, msg: '操作成功' })
}

export function mockDelBanner (bannerIds) {
  const ids = (Array.isArray(bannerIds) ? bannerIds : String(bannerIds).split(',')).map(String)
  for (let i = rows.length - 1; i >= 0; i -= 1) {
    if (ids.includes(rows[i].bannerId)) {
      rows.splice(i, 1)
    }
  }
  return delay({ code: 200, msg: '操作成功' })
}
