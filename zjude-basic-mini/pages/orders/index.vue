<template>
  <view class="orders-page has-tabbar">
    <UiNavbar
      :back="false"
      title="我的订单"
    >
      <template #right>
        <view
          class="search-btn"
          @click="searchOpen = !searchOpen"
        >
          <uni-icons
            type="search"
            size="18"
            color="#7a726c"
          />
        </view>
      </template>
    </UiNavbar>

    <view
      v-if="searchOpen"
      class="search-field"
    >
      <uni-icons
        type="search"
        size="16"
        color="#a39b95"
      />
      <input
        v-model="keyword"
        class="search-input"
        placeholder="搜索服务名称或订单号"
        confirm-type="search"
      >
      <view
        v-if="keyword"
        @click="keyword = ''"
      >
        <uni-icons
          type="clear"
          size="16"
          color="#c4bcb6"
        />
      </view>
    </view>

    <view class="tabs-wrap">
      <UiTabs
        v-model="active"
        :items="tabs"
      />
    </view>

    <view
      class="orders-body"
      :class="{ 'orders-body--guest': !loggedIn }"
    >
      <UiEmpty
        v-if="!loggedIn"
        icon="person"
        title="登录后查看订单"
        description="登录即可查看预约进度、服务记录与评价"
      >
        <button
          class="primary-button empty-action"
          @click="goLoginWithRedirect"
        >
          立即登录
        </button>
      </UiEmpty>

      <view
        v-else-if="filtered.length"
        class="order-list"
      >
        <view
          v-for="order in filtered"
          :key="order.orderId || order.orderNo"
          class="order-card card"
          @click="openDetail(order)"
        >
          <view class="order-head">
            <text class="order-no">
              NO.{{ order.orderNo || order.orderId }}
            </text>
            <view
              class="status-pill"
              :class="canPayOrder(order) ? 'status-pill--payment-pending' : `status-pill--${order.orderStatus}`"
            >
              <view class="status-dot" />
              <text>{{ statusText(order) }}</text>
            </view>
          </view>

          <view class="order-cover">
            <image
              v-if="orderServiceCover(order) && !order.serviceCoverFailed"
              class="order-cover__image"
              :src="assetUrl(orderServiceCover(order))"
              mode="widthFix"
              @error="order.serviceCoverFailed = true"
            />
            <view v-else class="order-cover__fallback">
              <uni-icons
                :type="orderServiceIcon(order)"
                size="27"
                color="#d95f37"
              />
            </view>
          </view>

          <view class="order-main">
            <view class="order-copy">
              <text class="order-name">
                {{ order.serviceName || '家政服务' }}
              </text>
              <view class="order-meta">
                <uni-icons
                  type="calendar"
                  size="13"
                  color="#9a918a"
                />
                <text>{{ appointmentText(order) }}</text>
              </view>
              <text v-if="order.quantity" class="order-quantity">服务数量：{{ order.quantity }}{{ order.quantityUnit || '' }}</text>
            </view>
            <UiPrice
              :value="order.amount || 0"
              size="sm"
            />
          </view>

          <view
            v-if="hasOrderActions(order)"
            class="order-actions"
          >
            <button
              v-if="canCancelOrder(order)"
              class="order-action-button order-action-button--secondary"
              @click.stop="cancel(order)"
            >
              取消预约
            </button>
            <button
              v-if="canPayOrder(order)"
              class="order-action-button order-action-button--primary"
              @click.stop="pay(order)"
            >
              去支付
            </button>
            <button
              v-if="canRefundOrder(order)"
              class="order-action-button order-action-button--secondary"
              @click.stop="refund(order)"
            >
              申请退款
            </button>
            <button
              v-if="isReviewPending(order)"
              class="order-action-button order-action-button--primary"
              @click.stop="review(order)"
            >
              评价服务
            </button>
            <text
              v-else-if="String(order.orderStatus) === '2'"
              class="reviewed-hint"
            >
              已评价
            </text>
          </view>
          <view
            v-if="String(order.orderStatus) === ORDER_STATUS.CANCELLED"
            class="order-delete-action"
            @click.stop="removeOrder(order)"
          >
            <uni-icons
              type="trash"
              size="19"
              color="#a39b95"
            />
          </view>
        </view>
      </view>

      <UiEmpty
        v-else
        icon="calendar"
        :title="keyword ? '没有匹配的订单' : '还没有预约记录'"
        :description="keyword ? '换个关键词试试' : '挑一项服务，让家更轻松'"
      >
        <button
          v-if="!keyword"
          class="primary-button empty-action"
          @click="goServices"
        >
          去预约服务
        </button>
      </UiEmpty>
    </view>

    <AppFeedback />
    <AppTabBar current-key="cart" />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiTabs from '@/components/ui/UiTabs.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { cancelOrder, deleteOrder, listOrders } from '@/api/service'
import { applyRefund } from '@/api/refund'
import {
  USER_ORDER_TABS,
  canCancelOrder,
  canPayOrder,
  canRefundOrder,
  hasOrderActions,
  isReviewPending,
  matchesTab,
  ORDER_STATUS,
  PAYMENT_PENDING,
  REVIEW_PENDING,
  orderServiceCover,
  orderServiceIcon,
  userOrderStatusLabel
} from '@/constants/order'
import { payOrder, requestSubscribe } from '@/composables/usePayment'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'
import { goLogin, isAuthenticated, requireLogin } from '@/utils/guard'
import { showDialog, showToast, toastSuccess } from '@/utils/feedback'

const loggedIn = computed(() => isAuthenticated())
const configStore = useConfigStore()
const tabs = USER_ORDER_TABS
const active = ref('')
const orders = ref([])
const searchOpen = ref(false)
const keyword = ref('')
let requestSerial = 0

const queryStatus = computed(() => {
  return active.value
})

const filtered = computed(() => orders.value.filter(item => {
  const query = keyword.value.trim().toLowerCase()
  return matchesTab(item, active.value) &&
    (!query || `${item.orderNo || item.orderId || ''}${item.serviceName || ''}`.toLowerCase().includes(query))
}))

async function loadOrders () {
  // 未登录不跳转，由用户在空态里手动点击登录
  if (!isAuthenticated()) return
  const serial = ++requestSerial
  try {
    const request = status => listOrders({
      pageNum: 1,
      pageSize: 20,
      ...(status ? { status } : {})
    })
    const response = active.value === PAYMENT_PENDING
      ? await Promise.all([request(ORDER_STATUS.PENDING), request(ORDER_STATUS.ACCEPTED)])
      : await request(queryStatus.value)
    if (serial !== requestSerial) return
    const rows = result => result?.rows || result?.data?.rows || []
    orders.value = Array.isArray(response) ? response.flatMap(rows) : rows(response)
  } catch (e) {
    showToast(e.message || '订单加载失败')
  }
}

onLoad(options => {
  const status = options?.status || ''
  if (status === REVIEW_PENDING) {
    active.value = ORDER_STATUS.FINISHED
  } else if (tabs.some(tab => tab.value === status)) {
    active.value = status
  }
})

// 从详情页取消/确认/评价后返回，列表需要反映最新状态
onShow(() => { loadOrders() })
watch(active, () => { loadOrders() })

function goLoginWithRedirect () { goLogin('/pages/orders/index') }
function statusText (order) { return userOrderStatusLabel(order) }
function appointmentText (order) {
  const date = order?.appointmentDate || '待确认日期'
  return `${date}${order?.slotName ? ` · ${order.slotName}` : ''}`
}
function openDetail (order) { uni.navigateTo({ url: `/pages/order-detail/index?id=${order.orderId || order.orderNo}` }) }
function goServices () { uni.reLaunch({ url: '/pages/services/index' }) }

async function cancel (order) {
  requireLogin(async () => {
    const confirmed = await showDialog({ title: '取消预约', content: '确定取消这笔预约吗？', confirmText: '确定取消', danger: true })
    if (!confirmed) return
    try {
      await cancelOrder(order.orderId)
      order.orderStatus = '3'
      toastSuccess('已取消')
    } catch (e) {
      showToast(e.message || '操作失败')
    }
  })
}

async function removeOrder (order) {
  if (!isAuthenticated()) return goLogin()
  const confirmed = await showDialog({
    title: '删除订单',
    content: '确定删除这笔已取消的订单吗？删除后无法恢复。',
    confirmText: '删除',
    danger: true
  })
  if (!confirmed) return
  try {
    await deleteOrder(order.orderId)
    orders.value = orders.value.filter(item => item.orderId !== order.orderId)
    toastSuccess('订单已删除')
  } catch (e) {
    showToast(e.message || '删除失败')
  }
}

async function pay (order) {
  if (!isAuthenticated()) return goLogin()
  try {
    const templates = configStore.subscribeTemplates || {}
    if (templates.enabled && templates.paid) await requestSubscribe([templates.paid])
    const result = await payOrder(order.orderId)
    if (result.paid) {
      toastSuccess('支付成功')
    } else {
      showToast(result.message || '支付结果确认中')
    }
    loadOrders()
  } catch (e) {
    showToast(e.message || '支付失败')
  }
}
async function refund (order) {
  if (!isAuthenticated()) return goLogin()
  const confirmed = await showDialog({
    title: '申请退款',
    content: '退款申请提交后需平台审核，审核通过将原路退回。确认提交？',
    confirmText: '提交申请'
  })
  if (!confirmed) return
  try {
    await applyRefund(order.orderId, '用户主动申请')
    toastSuccess('退款申请已提交')
    loadOrders()
  } catch (e) {
    showToast(e.message || '申请失败')
  }
}

function review (order) { requireLogin(() => uni.navigateTo({ url: `/pages/review/index?id=${order.orderId}` })) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.orders-page {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  background: $bg;
}

.orders-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-3 $page-gutter $space-3;
  background: linear-gradient(180deg, #fdece4 0%, #fff7f3 70%, $bg 100%);
}

.head-copy { display: flex; flex-direction: column; }

.head-title {
  font-size: 44rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: -1rpx;
}

.head-sub { margin-top: 6rpx; font-size: $font-micro; color: $ink-500; }

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: $radius-pill;
  background: rgba(255, 255, 255, 0.86);
}

.search-field {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin: 0 $page-gutter $space-2;
  padding: 0 $space-3;
  height: 76rpx;
  border-radius: $radius-pill;
  background: $surface;
  box-shadow: $shadow-card;
}

.search-input { flex: 1; min-width: 0; font-size: $font-caption; color: $ink-900; }

/* 与自定义 TopBar 保持 20px 间距 */
.tabs-wrap { padding: 20px $page-gutter $space-3; }

.orders-body {
  display: flex;
  flex-direction: column;
  padding: 0 $page-gutter $space-4;
}

.orders-body::before {
  content: '';
  display: block;
  height: 1rpx;
}

/* 未登录提示占满剩余高度并垂直居中 */
.orders-body--guest {
  flex: 1;
  justify-content: center;
  padding-bottom: calc(#{$space-4} + 140rpx);
}

.order-list { display: flex; flex-direction: column; gap: $space-2; }

.order-card { padding: $space-2; }

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: $space-1;
}

.order-no { font-size: $font-micro; color: $ink-400; }

.status-pill {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 14rpx;
  border-radius: $radius-pill;
  background: $warning-bg;
  font-size: $font-micro;
  font-weight: $weight-medium;
  color: $warning;
}

.status-dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: currentColor; }

.status-pill--payment-pending { color: $warning; }
.status-pill--1 { color: $info; }
.status-pill--2 { color: $success; }
.status-pill--3 { color: $ink-400; }
.status-pill--4 { color: $warning; }

.order-cover {
  width: 100%;
  overflow: hidden;
  border-radius: 18rpx;
  background: $brand-50;
}

.order-cover__image { display: block; width: 100%; height: auto; }

.order-cover__fallback {
  display: flex;
  width: 100%;
  height: 176rpx;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $brand-50, $brand-100);
}

.order-main {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-2 0;
}

.order-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; }

.order-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-top: 6rpx;
  font-size: $font-micro;
  color: $ink-400;
}

.order-quantity {
  display: block;
  margin-top: 6rpx;
  font-size: $font-micro;
  color: $ink-400;
}

.order-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  padding-top: $space-2;
  border-top: 1rpx solid $line-soft;
}

.order-delete-action {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  min-height: 48rpx;
  padding-top: $space-1;
}

.order-action-button {
  width: 100%;
  min-width: 0;
  height: 76rpx;
  margin: 0;
  padding: 0 $space-2;
  border: 1rpx solid transparent;
  border-radius: 18rpx;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  line-height: 74rpx;
  transition: transform 0.15s ease, background 0.15s ease;
}
.order-action-button::after { border: 0; }
.order-action-button:active { transform: scale(0.98); }

.order-action-button--primary {
  background: $brand-500;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba(224, 106, 61, 0.16);
}
.order-action-button--primary:active { background: $brand-600; }

.order-action-button--secondary {
  border-color: $line;
  background: $surface-muted;
  color: $ink-700;
}
.order-action-button--secondary:active { background: $bg-sunken; }

.reviewed-hint {
  padding: 0 $space-2;
  font-size: $font-micro;
  color: $ink-400;
}

.empty-action { width: 280rpx; margin-top: $space-4; line-height: 80rpx; }
</style>
