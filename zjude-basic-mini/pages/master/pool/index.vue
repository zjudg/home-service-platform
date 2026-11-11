<template>
  <view class="pool-page has-tabbar">
    <UiNavbar
      :back="false"
      title="接单大厅"
    >
      <template #right>
        <view
          class="refresh-btn"
          @click="load"
        >
          <uni-icons
            type="refreshempty"
            size="18"
            color="#7a726c"
          />
        </view>
      </template>
    </UiNavbar>

    <view class="pool-body">
      <view class="filter-bar">
        <view class="filter-copy">
          <text class="filter-title">
            仅看我擅长的
          </text>
          <text class="filter-desc">
            {{ onlyMatched ? '按我的技能筛选订单' : '显示全部待接单' }}
          </text>
        </view>
        <switch
          :checked="onlyMatched"
          color="#e06a3d"
          @change="onFilterChange"
        />
      </view>

      <view class="hint-card">
        <uni-icons
          type="info"
          size="15"
          color="#4a7fb5"
        />
        <text class="hint-text">
          仅展示未接单的预约，接单后请按时上门。
        </text>
      </view>

      <AppStatus
        v-if="error"
        :error="error"
        @retry="load"
      />
      <AppStatus
        v-else-if="!canOperate"
        :error="denyReason"
      />

      <template v-else-if="orders.length">
        <view
          v-for="order in orders"
          :key="order.orderId"
          class="order-card card"
        >
          <view class="order-head">
            <text class="order-no">
              NO.{{ order.orderNo || order.orderId }}
            </text>
            <view class="amount">
              ¥{{ order.amount || 0 }}
            </view>
          </view>

          <text class="order-name">
            {{ order.serviceName || '家政服务' }}
          </text>

          <view class="meta-list">
            <view class="meta-row">
              <uni-icons
                type="calendar"
                size="14"
                color="#a39b95"
              />
              <text class="meta-text">
                {{ formatDate(order.appointmentDate) }} {{ order.slotName || '' }}
              </text>
            </view>
            <view class="meta-row">
              <uni-icons
                type="location"
                size="14"
                color="#a39b95"
              />
              <text class="meta-text">
                {{ order.address || '地址待确认' }}
              </text>
            </view>
          </view>

          <button
            class="accept-btn"
            :disabled="submittingId === order.orderId"
            @click="accept(order)"
          >
            {{ submittingId === order.orderId ? '接单中...' : '立即接单' }}
          </button>
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
      current-key="master-pool"
      :items="tabItems"
    />
    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import AppStatus from '@/components/AppStatus.vue'
import { acceptOrder, listPoolOrders } from '@/api/master'
import { useMasterStore } from '@/stores/master'
import { MASTER_TAB_ITEMS } from '@/constants/master-navigation'
import { showToast } from '@/utils/feedback'

const masterStore = useMasterStore()
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')
const orders = ref([])
const submittingId = ref('')
const pageNum = ref(1)
const total = ref(0)
const finished = ref(false)
// 是否只看与本人技能匹配的订单；用户选择会本地持久化
const onlyMatched = ref(false)

const FILTER_STORAGE_KEY = 'master:pool:onlyMatched'

const PAGE_SIZE = 10

const tabItems = MASTER_TAB_ITEMS

const canOperate = computed(() => masterStore.isApproved)

const denyReason = computed(() =>
  masterStore.isPending
    ? '入驻申请审核中，通过后即可接单'
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
  // 已有数据时静默刷新，避免切回页面时出现整页 loading 闪烁
  if (!orders.value.length) {
    loading.value = true
  }
  error.value = ''
  pageNum.value = 1
  finished.value = false
  try {
    const response = await listPoolOrders({ pageNum: 1, pageSize: PAGE_SIZE, onlyMatched: onlyMatched.value })
    const rows = response?.rows || []
    orders.value = rows
    total.value = response?.total ?? rows.length
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

// 触底加载更多待接单
async function loadMore () {
  if (loadingMore.value || finished.value || !masterStore.isApproved) return
  if (orders.value.length >= total.value) {
    finished.value = true
    return
  }
  loadingMore.value = true
  try {
    const next = pageNum.value + 1
    const response = await listPoolOrders({ pageNum: next, pageSize: PAGE_SIZE, onlyMatched: onlyMatched.value })
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

async function accept (order) {
  if (submittingId.value) return
  submittingId.value = order.orderId
  try {
    await acceptOrder(order.orderId)
    showToast('接单成功', { type: 'success' })
    orders.value = orders.value.filter(item => item.orderId !== order.orderId)
    uni.navigateTo({ url: `/pages/master/order-detail/index?id=${order.orderId}` })
  } catch (e) {
    // 并发抢单失败时提示并刷新列表，避免展示已被接走的订单
    showToast(e.message || '接单失败')
    load()
  } finally {
    submittingId.value = ''
  }
}

/**
 * 切换「只看擅长」开关：持久化选择并立即重新查询。
 *
 * 首次加载不做整页 loading，避免切换时出现白屏。
 *
 * @param {Object} event switch 事件
 */
function onFilterChange (event) {
  const next = Boolean(event?.detail?.value)
  if (next === onlyMatched.value) return
  onlyMatched.value = next
  try {
    uni.setStorageSync(FILTER_STORAGE_KEY, next ? '1' : '0')
  } catch (e) {
    // 存储失败不影响本次筛选
  }
  load()
}

// 恢复上次的筛选偏好
try {
  onlyMatched.value = uni.getStorageSync(FILTER_STORAGE_KEY) === '1'
} catch (e) {
  onlyMatched.value = false
}

// 接单大厅的订单变化很快，每次进入都重新拉取
onShow(async () => {
  await masterStore.loadProfile()
  await load()
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.pool-page { display: flex; min-height: 100vh; flex-direction: column; background: $bg; }

.pool-body {
  display: flex;
  flex: 1;
  min-height: calc(100vh - 320rpx);
  flex-direction: column;
  padding: 0 $page-gutter $space-6;
}

.refresh-btn { padding: 8rpx; }

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-3 $space-4;
  margin-bottom: $space-2;
  background: $surface;
  border-radius: $radius-md;
}

.filter-copy {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.filter-title {
  font-size: $font-body;
  font-weight: $weight-medium;
  color: $ink-900;
}

.filter-desc {
  font-size: $font-micro;
  color: $ink-400;
}

.hint-card {
  display: flex; align-items: center; gap: $space-2;
  padding: $space-3; margin-bottom: $space-3;
  background: $info-bg; border-radius: $radius-md;
}

.hint-text { font-size: $font-caption; color: $ink-500; }

.order-card { padding: $space-4; margin-bottom: $space-3; }

.order-head { display: flex; align-items: center; justify-content: space-between; }

.order-no { font-size: $font-micro; color: $ink-400; }

.amount { font-size: $font-title; font-weight: $weight-bold; color: $brand-600; }

.order-name {
  display: block; margin-top: $space-2;
  font-size: $font-body; font-weight: $weight-semibold; color: $ink-900;
}

.meta-list { margin-top: $space-3; display: flex; flex-direction: column; gap: $space-2; }

.meta-row { display: flex; align-items: center; gap: 10rpx; }

.meta-text { font-size: $font-caption; color: $ink-500; flex: 1; }

.accept-btn {
  margin-top: $space-4; height: 80rpx; border-radius: $radius-md;
  background: $brand-500; color: #fff;
  font-size: $font-button; font-weight: $weight-semibold;
  &[disabled] { opacity: 0.6; }
}
</style>
