<template>
  <view class="master-orders has-tabbar">
    <UiNavbar
      :back="false"
      title="我的订单"
    />

    <view class="tabs-wrap">
      <UiTabs
        v-model="active"
        :items="tabs"
        centered
      />
    </view>

    <view class="orders-body">
      <AppStatus
        v-if="error"
        :error="error"
        @retry="load"
      />
      <AppStatus
        v-else-if="!masterStore.isApproved"
        :error="denyReason"
      />

      <template v-else-if="orders.length">
        <view
          v-for="order in orders"
          :key="order.orderId"
          class="order-card card"
          @click="openDetail(order)"
        >
          <view class="order-head">
            <text class="order-no">
              NO.{{ order.orderNo || order.orderId }}
            </text>
            <view
              class="status-pill"
              :class="`status-pill--${order.orderStatus}`"
            >
              <view class="status-dot" />
              <text>{{ orderStatusLabel(order.orderStatus) }}</text>
            </view>
          </view>

          <text class="order-name">
            {{ order.serviceName || '家政服务' }}
          </text>

          <view class="meta-list">
            <view class="meta-row">
              <uni-icons
                type="calendar"
                size="13"
                color="#a39b95"
              />
              <text class="meta-text">
                {{ formatDate(order.appointmentDate) }} {{ order.slotName || '' }}
              </text>
            </view>
            <view class="meta-row">
              <uni-icons
                type="location"
                size="13"
                color="#a39b95"
              />
              <text class="meta-text">
                {{ order.address || '地址待确认' }}
              </text>
            </view>
          </view>

          <view class="order-foot">
            <text class="order-amount">
              ¥{{ order.amount || 0 }}
            </text>
            <button
              v-if="order.orderStatus === ORDER_STATUS.ACCEPTED"
              class="foot-btn foot-btn--ghost"
              @click.stop="reject(order)"
            >
              拒单
            </button>
            <button
              v-if="order.orderStatus === ORDER_STATUS.ACCEPTED"
              class="foot-btn"
              @click.stop="start(order)"
            >
              开工
            </button>
            <button
              v-else-if="order.orderStatus === ORDER_STATUS.SERVING"
              class="foot-btn foot-btn--primary"
              @click.stop="finish(order)"
            >
              完工
            </button>
          </view>
        </view>
      </template>

      <UiEmpty
        v-else
        image="/static/nodata.jpg"
        :image-size="520"
        centered
        title="暂无数据"
      />
    </view>

    <AppTabBar
      current-key="master-orders"
      :items="tabItems"
    />
    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { onLoad, onReachBottom, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiTabs from '@/components/ui/UiTabs.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import AppStatus from '@/components/AppStatus.vue'
import { listMasterOrders, startOrder, finishOrder, rejectOrder } from '@/api/master'
import { useMasterStore } from '@/stores/master'
import { MASTER_TAB_ITEMS } from '@/constants/master-navigation'
import { ORDER_STATUS, MASTER_ORDER_TABS, orderStatusLabel } from '@/constants/order'
import { showDialog, showToast } from '@/utils/feedback'

const masterStore = useMasterStore()
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const orders = ref([])
const active = ref(ORDER_STATUS.ACCEPTED)
const pageNum = ref(1)
const total = ref(0)
const finished = ref(false)

const PAGE_SIZE = 10

const tabs = MASTER_ORDER_TABS

const tabItems = MASTER_TAB_ITEMS

const denyReason = computed(() =>
  masterStore.isPending
    ? '入驻申请审核中，通过后即可查看订单'
    : masterStore.isRejected
      ? '入驻申请未通过，请修改资料后重新提交'
      : '请先完成师傅入驻申请'
)

function formatDate (value) {
  if (!value) return '待确认'
  return String(value).slice(0, 10)
}

async function load () {
  if (!masterStore.isApproved) return
  loading.value = true
  error.value = ''
  pageNum.value = 1
  finished.value = false
  try {
    const params = { pageNum: 1, pageSize: PAGE_SIZE, ...(active.value ? { status: active.value } : {}) }
    const response = await listMasterOrders(params)
    const rows = response?.rows || []
    orders.value = rows
    total.value = response?.total ?? rows.length
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

// 触底加载下一页，保持页面高度不跳动
async function loadMore () {
  if (loadingMore.value || finished.value || !masterStore.isApproved) return
  if (orders.value.length >= total.value) {
    finished.value = true
    return
  }
  loadingMore.value = true
  try {
    const next = pageNum.value + 1
    const params = { pageNum: next, pageSize: PAGE_SIZE, ...(active.value ? { status: active.value } : {}) }
    const response = await listMasterOrders(params)
    const rows = response?.rows || []
    if (!rows.length) {
      finished.value = true
    } else {
      orders.value = orders.value.concat(rows)
      pageNum.value = next
    }
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loadingMore.value = false
  }
}

onReachBottom(() => { loadMore() })
onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

async function start (order) {
  const confirmed = await showDialog({
    title: '确认开工',
    content: '确认已到达服务地址并开始服务？',
    confirmText: '已到达，开工'
  })
  if (!confirmed) return
  try {
    await startOrder(order.orderId)
    showToast('已开始服务', { type: 'success' })
    load()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function reject (order) {
  const confirmed = await showDialog({
    title: '确认拒单',
    content: '拒单后订单将重新分配，近期拒单次数会影响后续自动派单的优先顺序。',
    confirmText: '确认拒单',
    danger: true
  })
  if (!confirmed) return
  try {
    await rejectOrder(order.orderId)
    showToast('已拒单', { type: 'success' })
    load()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function finish (order) {
  const confirmed = await showDialog({
    title: '确认完工',
    content: '确认本次服务已完成？完成后用户将进行确认与评价。',
    confirmText: '确认完工'
  })
  if (!confirmed) return
  try {
    await finishOrder(order.orderId)
    showToast('服务已完成', { type: 'success' })
    load()
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

function openDetail (order) {
  uni.navigateTo({ url: `/pages/master/order-detail/index?id=${order.orderId}` })
}

// 首次进入读取路由上的 status 过滤，之后每次回到前台都重新拉取
onLoad(options => {
  const allowedStatuses = [ORDER_STATUS.ACCEPTED, ORDER_STATUS.SERVING, ORDER_STATUS.FINISHED]
  if (allowedStatuses.includes(String(options?.status || ''))) {
    active.value = String(options.status)
  }
})

onShow(async () => {
  await masterStore.loadProfile()
  await load()
})

// 切换页签重新查询，避免只在前端过滤导致分页数据不全
watch(active, () => { load() })
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.master-orders { display: flex; min-height: 100vh; flex-direction: column; background: $bg; }

.tabs-wrap { padding: 0 $page-gutter; }

.orders-body {
  display: flex;
  flex: 1;
  min-height: calc(100vh - 420rpx);
  flex-direction: column;
  padding: $space-3 $page-gutter $space-6;
}

.order-card { padding: $space-4; margin-bottom: $space-3; }

.order-head { display: flex; align-items: center; justify-content: space-between; }

.order-no { font-size: $font-micro; color: $ink-400; }

.status-pill {
  display: flex; align-items: center; gap: 8rpx;
  padding: 4rpx 16rpx; border-radius: $radius-pill;
  background: $bg-sunken; font-size: $font-micro; color: $ink-500;
  &--1 { background: $info-bg; color: $info; }
  &--2 { background: $success-bg; color: $success; }
  &--4 { background: $warning-bg; color: $warning; }
  &--3 { background: $bg-sunken; color: $ink-400; }
}

.status-dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: currentColor; }

.order-name {
  display: block; margin-top: $space-2;
  font-size: $font-body; font-weight: $weight-semibold; color: $ink-900;
}

.meta-list { margin-top: $space-3; display: flex; flex-direction: column; gap: 10rpx; }

.meta-row { display: flex; align-items: center; gap: 10rpx; }

.meta-text { font-size: $font-caption; color: $ink-500; flex: 1; }

.order-foot {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: $space-3; padding-top: $space-3; border-top: 2rpx solid $line-soft;
}

.order-amount { font-size: $font-title; font-weight: $weight-bold; color: $brand-600; }

.foot-btn {
  margin: 0; padding: 0 32rpx; height: 64rpx; line-height: 64rpx;
  border-radius: $radius-pill; background: $bg-sunken; color: $ink-700;
  font-size: $font-caption;
  &--primary { background: $brand-500; color: #fff; }
  &--ghost { background: transparent; color: $ink-400; padding: 0 20rpx; }
}
</style>
