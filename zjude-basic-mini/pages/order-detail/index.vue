<template>
  <view class="order-detail-page">
    <UiNavbar :back="true" title="订单详情" />
    <template v-if="order.orderId || order.orderNo">
      <!-- 状态头 -->
      <view class="status-block" :class="`status-block--${order.orderStatus}`">
        <text class="status-title">{{ statusText(order) }}</text>
        <text class="status-desc">{{ statusDesc(order) }}</text>
      </view>

      <!-- 进度时间轴 -->
      <view class="progress card">
        <view v-for="(step, index) in progressSteps" :key="step.key" class="progress-step" :class="{ 'progress-step--done': step.done }">
          <view class="progress-rail">
            <view class="progress-node" />
            <view v-if="index < progressSteps.length - 1" class="progress-line" />
          </view>
          <text class="progress-label">{{ step.label }}</text>
        </view>
      </view>

      <!-- 订单信息 -->
      <UiSection title="订单信息">
        <view class="info-card card">
          <view class="info-row">
            <text class="info-label">服务项目</text>
            <text class="info-value">{{ order.serviceName || '家政服务' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">预约时间</text>
            <text class="info-value">{{ order.appointmentDate || '-' }} {{ order.slotName || '' }}</text>
          </view>
          <view v-if="order.quantity" class="info-row">
            <text class="info-label">服务数量</text>
            <text class="info-value">{{ order.quantity }}{{ order.quantityUnit || '' }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">服务地址</text>
            <text class="info-value">{{ order.address || '-' }}</text>
          </view>
          <view v-if="order.phone" class="info-row">
            <text class="info-label">联系电话</text>
            <text class="info-value">{{ order.phone }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">订单编号</text>
            <text class="info-value info-value--mono">{{ order.orderNo || order.orderId }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">支付状态</text>
            <text class="info-value">{{ String(order.paymentStatus) === '1' ? '已支付' : String(order.paymentStatus) === '2' ? '已退款' : '待支付' }}</text>
          </view>
          <view v-if="order.createTime" class="info-row">
            <text class="info-label">下单时间</text>
            <text class="info-value">{{ order.createTime }}</text>
          </view>
          <view v-if="order.remark" class="info-row info-row--remark">
            <text class="info-label">订单备注</text>
            <text class="info-value">{{ order.remark }}</text>
          </view>
        </view>
      </UiSection>

      <UiSection v-if="progressRecords.length" title="服务进度记录">
        <view class="record-card card">
          <view v-for="record in progressRecords" :key="record.key" class="record-row">
            <view class="record-dot" />
            <text class="record-label">{{ record.label }}</text>
            <text class="record-time">{{ record.time }}</text>
          </view>
        </view>
      </UiSection>

      <UiSection v-if="finishPhotoList.length" title="服务完成凭证">
        <scroll-view class="finish-photos" scroll-x :show-scrollbar="false">
          <image
            v-for="(photo, index) in finishPhotoList"
            :key="photo"
            class="finish-photo"
            :src="assetUrl(photo)"
            mode="aspectFill"
            @click="previewFinishPhoto(index)"
          />
        </scroll-view>
      </UiSection>

      <!-- 退款进度：只要存在退款单就展示，状态取自后端 refund 字段 -->
      <UiSection
        v-if="order.refund"
        title="退款进度"
      >
        <view
          class="refund-card"
          :class="`refund-card--${order.refund.refundStatus}`"
        >
          <view class="refund-head">
            <text class="refund-status">
              {{ order.refund.refundStatusLabel || '处理中' }}
            </text>
            <text class="refund-amount">
              ¥{{ amountText(order.refund.refundAmount) }}
            </text>
          </view>
          <text class="refund-desc">
            {{ refundDesc(order.refund.refundStatus) }}
          </text>
          <view
            v-if="order.refund.createTime"
            class="refund-meta"
          >
            <text>申请时间</text>
            <text>{{ order.refund.createTime }}</text>
          </view>
          <view
            v-if="order.refund.refundTime"
            class="refund-meta"
          >
            <text>处理时间</text>
            <text>{{ order.refund.refundTime }}</text>
          </view>
        </view>
      </UiSection>

      <!-- 金额 -->
      <view class="amount-card card">
        <text class="amount-label">订单金额</text>
        <UiPrice :value="order.amount || 0" size="lg" />
      </view>

      <!-- 操作 -->
      <view class="actions">
        <button v-if="canPayOrder(order)" class="primary-button action-btn" @click="pay">立即支付</button>
        <button v-if="String(order.orderStatus) === '1'" class="primary-button action-btn" @click="confirm">确认服务完成</button>
        <button v-if="String(order.orderStatus) === '2'" class="primary-button action-btn" @click="review">评价服务</button>
        <button v-if="String(order.orderStatus) === '0' || String(order.orderStatus) === '1'" class="ghost-button action-btn action-btn--danger" @click="cancel">取消预约</button>
      </view>
    </template>

    <view v-else class="detail-empty">
      <UiEmpty icon="info" :title="errorMessage || '订单不存在'" description="可以返回订单列表查看其他订单" />
    </view>
  </view>
  <AppFeedback />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiSection from '@/components/ui/UiSection.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { cancelOrder, confirmOrder, getOrder } from '@/api/service'
import { canPayOrder, USER_ORDER_STEPS, userOrderStatusDesc, userOrderStatusLabel, userStepIndex } from '@/constants/order'
import { payOrder, requestSubscribe } from '@/composables/usePayment'
import { useConfigStore } from '@/stores/config'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { toastSuccess, showToast } from '@/utils/feedback'
import { assetUrl } from '@/utils/assets'

const order = reactive({})
const configStore = useConfigStore()
const errorMessage = ref('')

// 状态推进顺序：下单 → 师傅接单 → 服务中 → 完成
const STEPS = USER_ORDER_STEPS

const progressSteps = computed(() => {
  const reached = userStepIndex(order.orderStatus)
  return STEPS.map((step, index) => ({ ...step, done: reached >= 0 && index <= reached }))
})

const progressRecords = computed(() => [
  { key: 'created', label: '订单提交', time: order.createTime },
  { key: 'accepted', label: '师傅接单', time: order.acceptTime },
  { key: 'started', label: '开始服务', time: order.startTime },
  { key: 'finished', label: '服务完成', time: order.finishTime }
].filter(record => record.time))

const finishPhotoList = computed(() => {
  const photos = order.finishPhotos || ''
  const list = Array.isArray(photos) ? photos : String(photos).split(',')
  return list.map(photo => String(photo || '').trim()).filter(Boolean)
})

onMounted(async () => {
  if (!ensureLoggedIn(currentPageUrl())) return
  const id = getCurrentPages().at(-1)?.options?.id
  try {
    const response = await getOrder(id)
    const item = response?.data || response
    if (item) Object.assign(order, item)
  } catch (e) {
    errorMessage.value = e.message || '订单加载失败'
  }
})

function statusText (value) { return userOrderStatusLabel(value) }

// 退款状态说明：让用户知道还要不要继续等、能不能联系客服
function refundDesc (status) {
  const value = String(status ?? '')
  if (value === '0') return '退款申请已提交，平台正在审核'
  if (value === '1') return '审核通过，退款正在原路返回，通常 1-3 个工作日到账'
  if (value === '2') return '退款已完成，请留意微信支付到账通知'
  if (value === '3') return '退款处理失败，请联系客服协助处理'
  if (value === '4') return '退款申请未通过审核，如有疑问请联系客服'
  return '退款处理中'
}

function amountText (value) {
  const number = Number(value ?? 0)
  return number.toFixed(2)
}

function previewFinishPhoto (index) {
  const urls = finishPhotoList.value.map(assetUrl)
  uni.previewImage({ current: urls[index], urls })
}

function statusDesc (value) {
  return userOrderStatusDesc(value) || ''
}

async function cancel () {
  try {
    await cancelOrder(order.orderId)
    order.orderStatus = '3'
    toastSuccess('已取消')
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function confirm () {
  try {
    await confirmOrder(order.orderId)
    order.orderStatus = '2'
    toastSuccess('已确认完成')
  } catch (e) {
    showToast(e.message || '操作失败')
  }
}

async function pay () {
  try {
    const templates = configStore.subscribeTemplates || {}
    if (templates.enabled && templates.paid) await requestSubscribe([templates.paid])
    const result = await payOrder(order.orderId)
    if (result.paid) {
      order.paymentStatus = '1'
      toastSuccess('支付成功')
    } else {
      showToast(result.message || '支付结果确认中')
    }
  } catch (e) {
    showToast(e.message || '支付失败')
  }
}

function review () { uni.navigateTo({ url: `/pages/review/index?id=${order.orderId}` }) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.order-detail-page {
  min-height: 100vh;
  padding: 0 $page-gutter calc(#{$space-6} + env(safe-area-inset-bottom));
  background: $bg;
}

/* ---------- 状态头 ---------- */
.status-block {
  margin: 0 (-$page-gutter);
  padding: $space-6 $page-gutter $space-8;
  background: linear-gradient(180deg, $brand-100 0%, $brand-50 100%);
}

.status-block--1 { background: linear-gradient(180deg, $info-bg, #f7fafd); }
.status-block--2 { background: linear-gradient(180deg, $success-bg, #f4fbf7); }
.status-block--3 { background: linear-gradient(180deg, $bg-sunken, $bg); }

.status-title {
  display: block;
  font-size: 42rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: -0.5rpx;
}

.status-desc { display: block; margin-top: $space-1; font-size: $font-caption; color: $ink-500; }

/* ---------- 时间轴 ---------- */
.progress {
  display: flex;
  align-items: flex-start;
  margin-top: $space-3;
  padding: $space-4 $space-2;
}

.progress-step {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  gap: $space-1;
}

.progress-rail {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: center;
}

.progress-node {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: $line;
  z-index: 1;
}

.progress-line {
  position: absolute;
  top: 8rpx;
  left: 50%;
  width: 100%;
  height: 2rpx;
  background: $line;
}

.progress-step--done .progress-node { background: $brand-500; }
.progress-step--done .progress-line { background: $brand-200; }

.progress-label { font-size: $font-micro; color: $ink-400; }
.progress-step--done .progress-label { color: $ink-700; font-weight: $weight-medium; }

/* ---------- 信息 ---------- */
.info-card { padding: $space-2 $space-4; }

.info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: $space-4;
  padding: $space-3 0;
  border-bottom: 1rpx solid $line-soft;
}
.info-row:last-child { border-bottom: 0; }
.info-row--remark .info-value { text-align: left; white-space: pre-wrap; }

.info-label { flex: 0 0 auto; font-size: $font-caption; color: $ink-500; }

.info-value {
  flex: 1;
  min-width: 0;
  text-align: right;
  font-size: $font-caption;
  line-height: 1.5;
  color: $ink-900;
  word-break: break-all;
}

.info-value--mono { color: $ink-500; font-size: $font-micro; }

.record-card { padding: 0 $space-4; }
.record-row { display: flex; align-items: center; gap: $space-2; padding: $space-3 0; border-bottom: 1rpx solid $line-soft; }
.record-row:last-child { border-bottom: 0; }
.record-dot { flex: 0 0 12rpx; width: 12rpx; height: 12rpx; border-radius: 50%; background: $brand-400; }
.record-label { flex: 1; font-size: $font-caption; color: $ink-700; }
.record-time { font-size: $font-micro; color: $ink-400; }

.finish-photos { white-space: nowrap; }
.finish-photo { display: inline-block; width: 200rpx; height: 200rpx; margin-right: $space-2; border-radius: $radius-md; background: $bg-sunken; }

/* ---------- 金额 ---------- */
/* ---------- 退款进度 ---------- */
.refund-card {
  padding: $space-3 $space-4;
  border-radius: $radius-md;
  background: $bg-sunken;
}

.refund-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.refund-status {
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.refund-amount {
  font-size: $font-caption;
  color: $ink-500;
}

.refund-desc {
  display: block;
  margin-top: $space-2;
  font-size: $font-micro;
  color: $ink-500;
  line-height: 1.6;
}

.refund-meta {
  display: flex;
  justify-content: space-between;
  margin-top: $space-2;
  font-size: $font-micro;
  color: $ink-400;
}

.refund-card--0,
.refund-card--1 {
  background: $warning-bg;
}

.refund-card--2 {
  background: $success-bg;
}

.refund-card--3,
.refund-card--4 {
  background: $danger-bg;
}

.amount-card {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: $space-2;
  padding: $space-4;
}

.amount-label { font-size: $font-caption; color: $ink-500; }

/* ---------- 操作 ---------- */
.actions { display: flex; flex-direction: column; gap: $space-2; margin-top: $space-4; }

.action-btn { width: 100%; height: 88rpx; line-height: 88rpx; margin: 0; }

.action-btn--danger {
  color: $danger;
  border-color: #f3d7d4;
  background: $surface;
  line-height: 84rpx;
  border-radius: $radius-pill;
}

.detail-empty { padding-top: $space-8; }
</style>
