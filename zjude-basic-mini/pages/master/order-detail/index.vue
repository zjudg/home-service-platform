<template>
  <view class="detail-page">
    <UiNavbar
      :back="true"
      title="订单详情"
    />

    <view class="detail-body">
      <AppStatus
        v-if="loading"
        loading
        loading-text="正在加载订单"
      />
      <AppStatus
        v-else-if="error"
        :error="error"
        @retry="load"
      />

      <template v-else-if="order.orderId">
        <view
          class="status-card card"
          :class="`status-card--${order.orderStatus}`"
        >
          <text class="status-title">
            {{ orderStatusLabel(order.orderStatus) }}
          </text>
          <text class="status-desc">
            {{ orderStatusDesc(order.orderStatus) }}
          </text>

          <view class="steps">
            <view
              v-for="(step, index) in steps"
              :key="step.key"
              class="step"
              :class="{ 'step--done': step.done }"
            >
              <view class="step-dot" />
              <text class="step-label">
                {{ step.label }}
              </text>
              <view
                v-if="index < steps.length - 1"
                class="step-line"
                :class="{ 'step-line--done': steps[index + 1].done }"
              />
            </view>
          </view>
        </view>

        <view class="card section-card">
          <text class="section-title">
            服务信息
          </text>
          <view class="info-row">
            <text class="info-label">
              服务项目
            </text>
            <text class="info-value">
              {{ order.serviceName || '-' }}
            </text>
          </view>
          <view class="info-row">
            <text class="info-label">
              订单编号
            </text>
            <text class="info-value">
              {{ order.orderNo || order.orderId }}
            </text>
          </view>
          <view class="info-row">
            <text class="info-label">
              预约时间
            </text>
            <text class="info-value">
              {{ formatDate(order.appointmentDate) }} {{ order.slotName || '' }}
            </text>
          </view>
          <view class="info-row">
            <text class="info-label">
              订单金额
            </text>
            <text class="info-value info-value--amount">
              ¥{{ order.amount || 0 }}
            </text>
          </view>
        </view>

        <view class="card section-card">
          <text class="section-title">
            上门信息
          </text>
          <view class="info-row info-row--block">
            <text class="info-label">
              服务地址
            </text>
            <text class="info-value info-value--wrap">
              {{ order.address || '待确认' }}
            </text>
          </view>
          <view class="info-row">
            <text class="info-label">
              联系电话
            </text>
            <text class="info-value">
              {{ order.phone || '待确认' }}
            </text>
          </view>
          <view
            v-if="order.remark"
            class="info-row info-row--block"
          >
            <text class="info-label">
              用户备注
            </text>
            <text class="info-value info-value--wrap">
              {{ order.remark }}
            </text>
          </view>
        </view>

        <view
          v-if="hasTimeline"
          class="card section-card"
        >
          <text class="section-title">
            服务记录
          </text>
          <view
            v-for="item in timeline"
            :key="item.label"
            class="info-row"
          >
            <text class="info-label">
              {{ item.label }}
            </text>
            <text class="info-value">
              {{ item.value }}
            </text>
          </view>
        </view>

        <!-- 完工凭证：仅服务中可上传，完工后只展示 -->
        <view
          v-if="canUploadPhotos"
          class="card section-card"
        >
          <text class="section-title">
            完工凭证
          </text>
          <view class="photo-grid">
            <view
              v-for="(photo, index) in photos"
              :key="photo"
              class="photo-item"
            >
              <image
                class="photo-img"
                :src="assetUrl(photo)"
                mode="aspectFill"
                @click="previewPhoto(index)"
              />
              <view
                class="photo-remove"
                @click.stop="removePhoto(index)"
              >
                <uni-icons
                  type="closeempty"
                  size="14"
                  color="#fff"
                />
              </view>
            </view>
            <view
              v-if="photos.length < MAX_PHOTOS"
              class="photo-add"
              @click="choosePhotos"
            >
              <uni-icons
                type="camera"
                size="22"
                color="#a39b95"
              />
              <text class="photo-add-text">
                上传
              </text>
            </view>
          </view>
          <text class="photo-tip">
            最多 {{ MAX_PHOTOS }} 张，建议拍摄服务完成后的现场照片
          </text>
        </view>

        <view
          v-else-if="finishedPhotos.length"
          class="card section-card"
        >
          <text class="section-title">
            完工凭证
          </text>
          <view class="photo-grid">
            <image
              v-for="(photo, index) in finishedPhotos"
              :key="photo"
              class="photo-img photo-img--readonly"
              :src="assetUrl(photo)"
              mode="aspectFill"
              @click="previewFinished(index)"
            />
          </view>
        </view>

        <view class="action-bar">
          <button
            v-if="order.orderStatus === ORDER_STATUS.ACCEPTED"
            class="action-btn"
            :disabled="submitting"
            @click="start"
          >
            确认开工
          </button>
          <button
            v-else-if="order.orderStatus === ORDER_STATUS.SERVING"
            class="action-btn action-btn--primary"
            :disabled="submitting"
            @click="finish"
          >
            确认完工
          </button>
          <button
            v-else
            class="action-btn action-btn--ghost"
            @click="goBack"
          >
            返回列表
          </button>
        </view>
      </template>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import AppStatus from '@/components/AppStatus.vue'
import { finishOrder, getMasterOrder, startOrder } from '@/api/master'
import { uploadFile } from '@/api/common'
import { assetUrl } from '@/utils/assets'
import {
  MASTER_ORDER_STEPS,
  ORDER_STATUS,
  masterStepIndex,
  orderStatusDesc,
  orderStatusLabel
} from '@/constants/order'
import { showDialog, showToast } from '@/utils/feedback'

const order = reactive({})
const loading = ref(false)
const error = ref('')
const submitting = ref(false)
const orderId = ref('')
const photos = ref([])
const uploading = ref(false)

// 完工凭证上限，与后端 finish_photos 字段长度预算一致
const MAX_PHOTOS = 6

// 仅「服务中」允许上传，完工后只读展示
const canUploadPhotos = computed(() => order.orderStatus === ORDER_STATUS.SERVING)

// 已提交的凭证，切分为可预览的数组
const finishedPhotos = computed(() => {
  const value = order.finishPhotos
  if (!value) return []
  return String(value).split(',').map(item => item.trim()).filter(Boolean)
})

const steps = computed(() => {
  const reached = masterStepIndex(order.orderStatus)
  return MASTER_ORDER_STEPS.map((step, index) => ({ ...step, done: reached >= 0 && index <= reached }))
})

const timeline = computed(() => {
  const items = []
  if (order.acceptTime) items.push({ label: '接单时间', value: formatTime(order.acceptTime) })
  if (order.startTime) items.push({ label: '开工时间', value: formatTime(order.startTime) })
  if (order.finishTime) items.push({ label: '完工时间', value: formatTime(order.finishTime) })
  return items
})

const hasTimeline = computed(() => timeline.value.length > 0)

function formatDate (value) {
  if (!value) return '待确认'
  return String(value).slice(0, 10)
}

function formatTime (value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

async function load () {
  loading.value = true
  error.value = ''
  try {
    const response = await getMasterOrder(orderId.value)
    Object.assign(order, response?.data || {})
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function start () {
  const confirmed = await showDialog({
    title: '确认开工',
    content: '确认已到达服务地址并开始服务？',
    confirmText: '已到达，开工'
  })
  if (!confirmed) return
  submitting.value = true
  try {
    await startOrder(orderId.value)
    showToast('已开始服务', { type: 'success' })
    await load()
  } catch (e) {
    showToast(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function finish () {
  const confirmed = await showDialog({
    title: '确认完工',
    content: photos.value.length
      ? '确认本次服务已完成？完成后用户将进行确认与评价。'
      : '尚未上传完工凭证，确认仍然提交完工？',
    confirmText: '确认完工'
  })
  if (!confirmed) return
  submitting.value = true
  try {
    await finishOrder(orderId.value, photos.value.length ? { finishPhotos: photos.value.join(',') } : {})
    showToast('服务已完成', { type: 'success' })
    photos.value = []
    await load()
  } catch (e) {
    showToast(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 选图并逐张上传，任一失败只跳过该张，不阻断其余图片。
 */
async function choosePhotos () {
  if (uploading.value) return
  const remain = MAX_PHOTOS - photos.value.length
  if (remain <= 0) {
    showToast(`最多上传 ${MAX_PHOTOS} 张`)
    return
  }
  try {
    const res = await uni.chooseImage({ count: remain, sizeType: ['compressed'] })
    const paths = res?.tempFilePaths || []
    if (!paths.length) return
    uploading.value = true
    for (const path of paths) {
      try {
        const response = await uploadFile(path)
        const url = response?.url || response?.data?.url
        if (url) photos.value.push(url)
      } catch (e) {
        showToast(e.message || '图片上传失败')
      }
    }
  } catch (e) {
    // 用户取消选图不提示
  } finally {
    uploading.value = false
  }
}

function removePhoto (index) {
  photos.value.splice(index, 1)
}

function previewPhoto (index) {
  uni.previewImage({ urls: photos.value.map(item => assetUrl(item)), current: index })
}

function previewFinished (index) {
  uni.previewImage({ urls: finishedPhotos.value.map(item => assetUrl(item)), current: index })
}

function goBack () {
  uni.navigateBack()
}

onMounted(() => {
  const id = getCurrentPages().at(-1)?.options?.id
  if (!id) {
    error.value = '缺少订单参数'
    return
  }
  orderId.value = id
  load()
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.detail-page { min-height: 100vh; background: $bg; }

.detail-body { padding: $space-3 $page-gutter $space-8; }

.status-card {
  padding: $space-4; margin-bottom: $space-3;
  &--1 { background: $info-bg; }
  &--2 { background: $success-bg; }
  &--4 { background: $warning-bg; }
}

.status-title { display: block; font-size: $font-title; font-weight: $weight-bold; color: $ink-900; }

.status-desc { display: block; margin-top: 8rpx; font-size: $font-caption; color: $ink-500; }

.steps { display: flex; align-items: flex-start; margin-top: $space-4; }

.step { flex: 1; display: flex; flex-direction: column; align-items: center; position: relative; }

.step-dot {
  width: 18rpx; height: 18rpx; border-radius: 50%;
  background: $ink-300;
}

.step--done .step-dot { background: $brand-500; }

.step-label { margin-top: 10rpx; font-size: $font-micro; color: $ink-400; }

.step--done .step-label { color: $ink-700; }

.step-line {
  position: absolute; top: 8rpx; left: 50%; width: 100%; height: 2rpx;
  background: $line;
}

.step-line--done { background: $brand-400; }

.section-card { padding: $space-4; margin-bottom: $space-3; }

.section-title {
  display: block; margin-bottom: $space-3;
  font-size: $font-body; font-weight: $weight-semibold; color: $ink-900;
}

.info-row { display: flex; align-items: flex-start; padding: 10rpx 0; }

.info-label { width: 160rpx; flex-shrink: 0; font-size: $font-caption; color: $ink-400; }

.info-value { flex: 1; font-size: $font-caption; color: $ink-700; text-align: right; }

.info-value--amount { color: $brand-600; font-weight: $weight-semibold; }

.info-value--wrap { text-align: left; line-height: 1.6; }

.info-row--block { flex-direction: column; gap: 6rpx; }

.info-row--block .info-value--wrap { text-align: left; }

.action-bar { margin-top: $space-4; }

.photo-grid { display: flex; flex-wrap: wrap; gap: $space-2; }

.photo-item { position: relative; width: 160rpx; height: 160rpx; }

.photo-img { width: 160rpx; height: 160rpx; border-radius: $radius-sm; background: $bg-sunken; }

.photo-remove {
  position: absolute; top: -10rpx; right: -10rpx;
  width: 36rpx; height: 36rpx; border-radius: 50%;
  background: rgba(31, 28, 26, 0.65);
  display: flex; align-items: center; justify-content: center;
}

.photo-add {
  width: 160rpx; height: 160rpx; border-radius: $radius-sm;
  border: 2rpx dashed $line; background: $surface-muted;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}

.photo-add-text { margin-top: 6rpx; font-size: $font-micro; color: $ink-400; }

.photo-tip { display: block; margin-top: $space-2; font-size: $font-micro; color: $ink-400; }

.action-btn {
  height: 88rpx; line-height: 88rpx; border-radius: $radius-md;
  background: $bg-sunken; color: $ink-700; font-size: $font-button;
  &--primary { background: $brand-500; color: #fff; }
  &--ghost { background: transparent; color: $ink-500; border: 2rpx solid $line; }
  &[disabled] { opacity: 0.6; }
}
</style>
