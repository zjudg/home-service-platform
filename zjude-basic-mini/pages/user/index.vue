<template>
  <view class="user-page has-tabbar">
    <UiNavbar :back="false" title="我的">
      <template #right>
        <view class="settings-btn" @click.stop="openSettings">
          <uni-icons type="gear" size="20" color="#7a726c" />
        </view>
      </template>
    </UiNavbar>

    <view class="user-body">
      <!-- 个人资料 -->
      <view class="profile-card card" @click="!loggedIn && goLogin()">
        <view
          class="avatar"
          @click.stop="editAvatar"
        >
          <image
            v-if="loggedIn && userAvatar"
            :src="userAvatar"
            mode="aspectFill"
          />
          <image
            v-else-if="!loggedIn && defaultAvatarLogo"
            class="avatar-logo"
            :src="defaultAvatarLogo"
            mode="aspectFit"
          />
        </view>
        <view class="profile-copy">
          <text class="profile-name">{{ loggedIn ? displayName : '点击登录' }}</text>
          <text class="profile-meta">{{ loggedIn ? '欢迎回来，管理您的服务' : '登录后查看预约与评价' }}</text>
        </view>
      </view>

      <!-- 订单概览 -->
      <view class="orders-card card">
        <view class="orders-head">
          <text class="orders-title">预约订单</text>
          <view class="orders-all" @click="openOrders()">
            <text>全部</text>
            <uni-icons type="right" size="11" color="#a39b95" />
          </view>
        </view>
        <view class="orders-grid">
          <view v-for="item in orderStats" :key="item.status" class="orders-item" @click="openOrders(item.status)">
            <view class="orders-value-wrap">
              <text class="orders-value">{{ item.value }}</text>
              <view v-if="item.value > 0 && item.status === PAYMENT_PENDING" class="orders-badge" />
            </view>
            <text class="orders-label">{{ item.title }}</text>
          </view>
        </view>
      </view>

      <!-- 最近订单：把「有几单」变成「是哪一单」 -->
      <view v-if="loggedIn && latestOrder" class="latest-card card" @click="openOrderDetail(latestOrder)">
        <view class="latest-head">
          <text class="latest-title">最近订单</text>
          <view class="status-pill" :class="canPayOrder(latestOrder) ? 'status-pill--payment-pending' : `status-pill--${latestOrder.orderStatus}`">
            <view class="status-dot" />
            <text>{{ userOrderStatusLabel(latestOrder) }}</text>
          </view>
        </view>
        <view class="latest-main">
          <view class="latest-thumb">
            <image
              v-if="latestOrder.categoryImage && !latestOrder.categoryIconFailed"
              class="latest-thumb__image"
              :src="assetUrl(latestOrder.categoryImage)"
              mode="aspectFit"
              @error="latestOrder.categoryIconFailed = true"
            />
            <uni-icons
              v-else
              :type="orderServiceIcon(latestOrder)"
              size="22"
              color="#fff4ed"
            />
          </view>
          <view class="latest-copy">
            <text class="latest-name">{{ latestOrder.serviceName || '家政服务' }}</text>
            <view class="latest-meta">
              <uni-icons type="calendar" size="12" color="#a39b95" />
              <text>{{ appointmentText(latestOrder) }}</text>
            </view>
          </view>
          <uni-icons type="right" size="14" color="#c4bcb6" />
        </view>
      </view>

      <!-- 待评价提醒：完工但未评价的订单，直接给入口 -->
      <view v-if="loggedIn && pendingReviews.length" class="review-tip card" @click="openReview(pendingReviews[0])">
        <view class="review-icon">
          <uni-icons type="star-filled" size="16" color="#d98a1f" />
        </view>
        <view class="review-copy">
          <text class="review-title">有 {{ pendingReviews.length }} 单待评价</text>
          <text class="review-desc">评价后帮助更多人选到合适的师傅</text>
        </view>
        <text class="review-action">去评价</text>
      </view>

      <!-- 服务入口 -->
      <UiSection title="我的服务">
        <view class="menu-card card">
          <view v-for="(item, index) in serviceActions" :key="item.title" class="menu-row" :class="{ 'menu-row--last': index === serviceActions.length - 1 }" @click="item.action">
            <view class="menu-icon" :class="`menu-icon--${item.tone}`">
              <uni-icons :type="item.icon" size="18" :color="item.color" />
            </view>
            <view class="menu-copy">
              <text class="menu-title">{{ item.title }}</text>
              <text class="menu-desc">{{ item.desc }}</text>
            </view>
            <uni-icons type="right" size="14" color="#c4bcb6" />
          </view>
        </view>
      </UiSection>

      <!-- 师傅入口：未入驻显示招募，已入驻显示师傅中心；未登录也展示以承接招募 -->
      <view class="master-entry card" @click="openMasterCenter">
        <view class="master-icon" :class="{ 'master-icon--active': canEnterMaster }">
          <uni-icons :type="canEnterMaster ? 'staff-filled' : 'personadd'" size="18" :color="canEnterMaster ? '#e06a3d' : '#7a726c'" />
        </view>
        <view class="master-copy">
          <text class="master-title">{{ canEnterMaster ? '师傅工作台' : '成为师傅' }}</text>
          <text class="master-desc">{{ masterEntryDesc }}</text>
        </view>
        <uni-icons type="right" size="14" color="#c4bcb6" />
      </view>

      <button v-if="loggedIn" class="logout" @click="logout">退出登录</button>
      <text v-else class="login-hint">登录后可管理预约、地址与评价</text>
    </view>

    <AppFeedback />
    <ContactSheet
      v-model:visible="contactVisible"
      :phone="supportPhone"
      :hours="supportHours"
      :qr-code="supportQrCode"
    />
    <AppTabBar current-key="profile" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import ContactSheet from '@/components/ContactSheet.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiSection from '@/components/ui/UiSection.vue'
import { listOrders } from '@/api/service'
import { assetUrl } from '@/utils/assets'
import { ORDER_STATUS, PAYMENT_PENDING, canPayOrder, orderServiceIcon, userOrderStatusLabel } from '@/constants/order'
import { useAuthStore } from '@/stores/auth'
import { useConfigStore } from '@/stores/config'
import { useMasterStore, AUDIT_PENDING, AUDIT_REJECTED } from '@/stores/master'
import { useAddressStore } from '@/stores/address'
import { countFavorites } from '@/api/favorite'
import { useIdentity } from '@/composables/useIdentity'
import { saveUserAvatar } from '@/utils/user-avatar'
import { goLogin as guardGoLogin, isAuthenticated, requireLogin as guardRequireLogin } from '@/utils/guard'
import { showDialog, showToast } from '@/utils/feedback'
import { requestSubscribe } from '@/composables/usePayment'
import { MINI_PROGRAM_MODE_MASTER, setMiniProgramMode } from '@/utils/mini-program-mode'

const authStore = useAuthStore()
const configStore = useConfigStore()
const userAvatar = computed(() => assetUrl(authStore.user?.avatar || authStore.user?.avatarUrl || ''))
const defaultAvatarLogo = computed(() => assetUrl(
  configStore.site?.mobileLogo || configStore.site?.mobileLoginLogo || configStore.site?.memberDefaultAvatar || ''
))
const avatarUploading = ref(false)
const masterStore = useMasterStore()
const addressStore = useAddressStore()
const { canEnterMaster, roles } = useIdentity()
const isAdmin = computed(() => roles.value.includes('admin') || roles.value.includes('mini_admin'))
const loggedIn = computed(() => isAuthenticated())
const displayName = computed(() => authStore.user?.nickName || authStore.user?.userName || '用户')

const orderStats = ref([
  { title: '待支付', status: PAYMENT_PENDING, value: 0 },
  { title: '待接单', status: ORDER_STATUS.PENDING, value: 0 },
  { title: '服务中', status: ORDER_STATUS.SERVING, value: 0 },
  { title: '已完成', status: ORDER_STATUS.FINISHED, value: 0 },
  { title: '已取消', status: ORDER_STATUS.CANCELLED, value: 0 }
])

// 客服配置来自后台「网站设置」，未配置时给兜底文案，避免出现空白说明
const supportPhone = computed(() => String(configStore.site?.servicePhone || '').trim())
const supportHours = computed(() => String(configStore.site?.serviceHours || '').trim())
const supportQrCode = computed(() => assetUrl(configStore.site?.serviceQrCode || ''))
const contactVisible = ref(false)

const serviceActions = computed(() => {
  const items = [
    { title: '服务地址', desc: addressHint.value, icon: 'location', tone: 'brand', color: '#e06a3d', action: () => requireLogin(openAddresses) },
    { title: '技能证书', desc: '查看并购买技能证书', icon: 'medal', tone: 'warning', color: '#d98a1f', action: () => requireLogin(() => uni.navigateTo({ url: '/pages/certificates/index' })) },
    { title: '我的收藏', desc: favoriteHint.value, icon: 'heart', tone: 'danger', color: '#d9503f', action: () => requireLogin(openFavorites) },
    { title: '服务评价', desc: '查看已完成的评价', icon: 'star', tone: 'warning', color: '#d98a1f', action: () => requireLogin(() => openOrders('2')) },
    { title: '联系客服', desc: supportHours.value || '客服电话将在后台配置后展示', icon: 'phone', tone: 'info', color: '#4a7fb5', action: contactSupport }
  ]
  if (isAdmin.value) {
    items.unshift({ title: '订单消息提醒', desc: '授权接收新订单和师傅接单通知', icon: 'notification', tone: 'warning', color: '#d98a1f', action: requestAdminOrderNotifications })
  }
  return items
})

async function requestAdminOrderNotifications () {
  const templates = configStore.subscribeTemplates || {}
  if (!templates.enabled) return showToast('后台尚未启用订阅消息')
  const ids = [templates.adminCreated, templates.adminAccepted].filter(Boolean)
  if (!ids.length) return showToast('管理员订单通知模板尚未配置')
  const accepted = await requestSubscribe(ids)
  showToast(accepted ? '授权成功，可接收已授权的订单提醒' : '未授权，无法接收管理员订单提醒')
}

const favoriteHint = computed(() => {
  if (!loggedIn.value || favoriteCount.value === null) return '收藏的服务项目'
  return favoriteCount.value ? `已收藏 ${favoriteCount.value} 项服务` : '暂无收藏，去服务页看看'
})

const addressHint = computed(() => {
  if (!loggedIn.value) return '管理常用上门地址'
  if (!addressStore.loaded) return '管理常用上门地址'
  return addressStore.list.length ? `已保存 ${addressStore.list.length} 条地址` : '暂无地址，点击添加'
})

// 最近订单、待评价各自按状态单独查询，避免历史订单超过一页时漏掉进行中的单
const activeOrders = ref([])
const finishedOrders = ref([])
const favoriteCount = ref(null)

const ORDER_STATUS_ACTIVE = [ORDER_STATUS.PENDING, ORDER_STATUS.ACCEPTED, ORDER_STATUS.SERVING]

function sortByAppointment (list) {
  return [...list].sort((a, b) =>
    String(a.appointmentDate || '').localeCompare(String(b.appointmentDate || '')))
}

// 最近一单：优先展示进行中的订单（按预约时间由近到远），没有进行中再取最新完成/取消的一单
const latestOrder = computed(() => {
  const active = sortByAppointment(activeOrders.value)[0]
  if (active) return active
  return finishedOrders.value[0] || null
})

// 已完工但未评价的订单
const pendingReviews = computed(() =>
  finishedOrders.value.filter(item => Number(item.reviewed || 0) === 0)
)

// 「我的」是 tabBar 页面，用 onShow 而非 onMounted：登录/退出后返回本页才能刷新状态
onShow(async () => {
  await configStore.load().catch(() => {})
  if (isAuthenticated()) {
    loadOrders()
    masterStore.loadProfile().catch(() => {})
    // 地址条数用于菜单说明文案，失败不影响页面
    addressStore.load().catch(() => {})
    loadFavoriteCount()
  } else {
    masterStore.reset()
    addressStore.reset()
    favoriteCount.value = null
    activeOrders.value = []
    finishedOrders.value = []
    orderStats.value = orderStats.value.map(item => ({ ...item, value: 0 }))
  }
})

function loadFavoriteCount () {
  countFavorites()
    .then(response => {
      const value = response?.data
      favoriteCount.value = typeof value === 'number' ? value : Number(value || 0)
    })
    .catch(() => {
      // 收藏数只用于文案，失败时回落到默认提示
      favoriteCount.value = null
    })
}

// 师傅入口的一句话说明，按审核进度区分
const masterEntryDesc = computed(() => {
  if (!loggedIn.value) return '认证后接单上门，时间自由'
  if (!masterStore.applied) return '认证后接单上门，时间自由'
  if (masterStore.auditStatus === AUDIT_PENDING) return '入驻审核中，请耐心等待'
  if (masterStore.auditStatus === AUDIT_REJECTED) return '审核未通过，点击查看原因'
  return '查看接单、开工与完工记录'
})

async function openMasterCenter () {
  if (!isAuthenticated()) return goLogin('/pages/master/apply/index')
  // 用户可能在后台审核期间一直停留在小程序，点击入口时再次校验最新审核状态。
  await masterStore.loadProfile().catch(() => {})
  if (canEnterMaster.value) {
    setMiniProgramMode(MINI_PROGRAM_MODE_MASTER)
    uni.reLaunch({ url: '/pages/master/home/index' })
    return
  }
  uni.navigateTo({ url: '/pages/master/apply/index' })
}

async function loadOrders () {
  const [overview, active, finished] = await Promise.allSettled([
    listOrders({ pageNum: 1, pageSize: 100 }),
    Promise.all(ORDER_STATUS_ACTIVE.map(status => listOrders({ pageNum: 1, pageSize: 10, status }))),
    listOrders({ pageNum: 1, pageSize: 20, status: ORDER_STATUS.FINISHED })
  ])

  // 概览数字失败时保留上一次结果，不阻塞下方卡片
  if (overview.status === 'fulfilled') {
    const orders = overview.value?.rows || overview.value?.data?.rows || []
    orderStats.value = orderStats.value.map(item => ({
      ...item,
      value: item.status === PAYMENT_PENDING
        ? orders.filter(canPayOrder).length
        : orders.filter(order => String(order.orderStatus) === item.status &&
          (item.status !== ORDER_STATUS.PENDING || String(order.paymentStatus) === '1')).length
    }))
  }

  if (active.status === 'fulfilled') {
    activeOrders.value = active.value
      .map(response => response?.rows || response?.data?.rows || [])
      .flat()
  }

  if (finished.status === 'fulfilled') {
    const value = finished.value
    finishedOrders.value = value?.rows || value?.data?.rows || []
  }
}

function goLogin () { guardGoLogin('/pages/user/index') }
function requireLogin (callback) { return guardRequireLogin(callback) }

async function editAvatar () {
  if (!loggedIn.value) return goLogin()
  if (avatarUploading.value) return

  try {
    const result = await new Promise((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: resolve,
        fail: reject
      })
    })
    const filePath = result?.tempFilePaths?.[0]
    if (!filePath) return

    avatarUploading.value = true
    await saveUserAvatar(filePath, authStore)
    showToast('头像已更新')
  } catch (error) {
    if (!String(error?.errMsg || error?.message || '').includes('cancel')) {
      showToast(error.message || '头像上传失败，请重试')
    }
  } finally {
    avatarUploading.value = false
  }
}

// 预约时间：日期 + 时段，缺日期时给可读提示
function appointmentText (order) {
  if (!order?.appointmentDate) return '待确认时间'
  return `${order.appointmentDate} ${order.slotName || ''}`.trim()
}

function openOrderDetail (order) {
  const id = order?.orderId || order?.orderNo
  if (!id) return
  uni.navigateTo({ url: `/pages/order-detail/index?id=${encodeURIComponent(id)}` })
}

function openReview (order) {
  const id = order?.orderId || order?.orderNo
  if (!id) return
  uni.navigateTo({ url: `/pages/review/index?id=${encodeURIComponent(id)}` })
}

// 客服：有配置号码直接拨号，没有则提示联系方式未配置，避免弹出「暂未开放」
// 客服：优先展示弹层，内含拨号与二维码；两者都未配置时给出明确提示
function contactSupport () {
  if (!supportPhone.value && !supportQrCode.value) {
    showDialog({
      title: '联系客服',
      content: '客服联系方式暂未配置，可在「设置 - 关于应用」查看最新信息。',
      showCancel: false
    })
    return
  }
  contactVisible.value = true
}

function openAddresses () {
  uni.navigateTo({ url: '/pages/address/index' })
}

function openFavorites () {
  uni.navigateTo({ url: '/pages/favorite/index' })
}

function openOrders (status = '') {
  if (!isAuthenticated()) return goLogin()
  uni.reLaunch({ url: `/pages/orders/index${status ? `?status=${status}` : ''}` })
}
function openSettings () { uni.navigateTo({ url: '/pages/settings/index' }) }

async function logout () {
  const confirmed = await showDialog({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmText: '退出登录',
    danger: true
  })
  if (!confirmed) return
  await authStore.signOut()
  uni.reLaunch({ url: '/pages/user/index' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.user-page { min-height: 100vh; background: $bg; }

/* ---------- 个人资料 ---------- */
.profile-card {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-4;
}
.profile-card:active { opacity: 0.85; }

.avatar {
  display: flex;
  flex: 0 0 120rpx;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 120rpx;
  overflow: hidden;
  border-radius: 50%;
  border: 4rpx solid $surface;
  background: $brand-100;
  box-shadow: $shadow-card;
  color: $brand-600;
  font-size: 44rpx;
  font-weight: $weight-bold;
}
.avatar image { width: 100%; height: 100%; }

.profile-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }

.profile-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 38rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
}

.profile-meta { margin-top: 6rpx; font-size: $font-micro; color: $ink-500; }

.settings-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: $radius-pill;
  background: rgba(255, 255, 255, 0.86);
}

.user-body { padding: $space-3 $page-gutter $space-4; }

/* ---------- 订单概览 ---------- */
.orders-card { margin-top: $space-2; padding: $space-4; }

.orders-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: $space-3;
}

.orders-title { font-size: $font-body; font-weight: $weight-semibold; color: $ink-900; }

.orders-all {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: $font-micro;
  color: $ink-400;
}

.orders-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); }

.orders-item { display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.orders-item:active { opacity: 0.7; }

.orders-value-wrap { position: relative; }

.orders-value { font-size: 38rpx; font-weight: $weight-heavy; color: $ink-900; }

.orders-badge {
  position: absolute;
  top: 2rpx;
  right: -14rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: $danger;
}

.orders-label { font-size: $font-micro; color: $ink-400; }

/* ---------- 最近订单 ---------- */
.latest-card { margin-top: $space-3; padding: $space-3 $space-4; }
.latest-card:active { opacity: 0.85; }

.latest-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.latest-title { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-500; }

.status-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 4rpx 16rpx;
  border-radius: $radius-pill;
  background: $bg-sunken;
  font-size: $font-micro;
  color: $ink-500;
}
.status-pill--0 { color: $warning; background: $warning-bg; }
.status-pill--payment-pending { color: $warning; background: $warning-bg; }
.status-pill--1 { color: $info; background: $info-bg; }
.status-pill--2 { color: $success; background: $success-bg; }
.status-pill--4 { color: $brand-600; background: $brand-50; }

.status-dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: currentColor; }

.latest-main {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-top: $space-3;
}

.latest-thumb {
  display: flex;
  flex: 0 0 88rpx;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border-radius: $radius-md;
  background: $brand-500;
}
.latest-thumb__image { width: 58rpx; height: 58rpx; }

.latest-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }

.latest-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.latest-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
  font-size: $font-micro;
  color: $ink-400;
}

/* ---------- 待评价提醒 ---------- */
.review-tip {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin-top: $space-3;
  padding: $space-3 $space-4;
  background: $warning-bg;
  border: 1rpx solid rgba(217, 138, 31, 0.16);
}
.review-tip:active { opacity: 0.85; }

.review-icon {
  display: flex;
  flex: 0 0 56rpx;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: $radius-pill;
  background: $surface;
}

.review-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }

.review-title { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-900; }
.review-desc { margin-top: 4rpx; font-size: $font-micro; color: $ink-500; }

.review-action {
  flex: 0 0 auto;
  padding: 10rpx 24rpx;
  border-radius: $radius-pill;
  background: $warning;
  color: $surface;
  font-size: $font-micro;
  font-weight: $weight-semibold;
}

/* ---------- 菜单 ---------- */
.menu-card { padding: 0 $space-4; }

.menu-row {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 1rpx solid $line-soft;
}
.menu-row--last { border-bottom: 0; }
.menu-row:active { opacity: 0.75; }

.menu-icon {
  display: flex;
  flex: 0 0 68rpx;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: $radius-md;
  background: $brand-50;
}

.menu-icon--warning { background: $warning-bg; }
.menu-icon--danger { background: $danger-bg; }
.menu-icon--info { background: $info-bg; }

.menu-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }

.menu-title { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-900; }
.menu-desc { margin-top: 4rpx; font-size: $font-micro; color: $ink-400; }

/* ---------- 师傅入口 ---------- */
.master-entry {
  display: flex;
  align-items: center;
  margin-top: $space-3;
  padding: $space-3 $space-4;
}

.master-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-md;
  background: $bg-sunken;
  display: flex;
  align-items: center;
  justify-content: center;
}

.master-icon--active {
  background: $brand-tint;
}

.master-copy {
  flex: 1;
  margin-left: $space-3;
}

.master-title {
  display: block;
  font-size: $font-body;
  font-weight: $weight-medium;
  color: $ink-900;
}

.master-desc {
  display: block;
  margin-top: 6rpx;
  font-size: $font-caption;
  color: $ink-400;
}

/* ---------- 底部 ---------- */
.logout {
  width: 100%;
  margin-top: $space-6;
  color: $brand-600;
  background: $surface;
  border: 2rpx solid $brand-200;
  border-radius: $radius-pill;
  box-shadow: $shadow-card;
  font-size: $font-body;
  font-weight: $weight-semibold;
  line-height: 80rpx;
}
.logout:active { opacity: 0.82; }
.logout::after { border: 0; }

.login-hint {
  display: block;
  margin-top: $space-6;
  text-align: center;
  font-size: $font-micro;
  color: $ink-400;
}
</style>
