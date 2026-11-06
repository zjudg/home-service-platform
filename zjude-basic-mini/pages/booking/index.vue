<template>
  <view class="booking-page">
    <UiNavbar
      :back="true"
      title="预约服务"
    />
    <view
      v-if="loading"
      class="booking-skeleton"
    >
      <view class="summary card booking-skeleton__summary">
        <view class="skeleton booking-skeleton__thumb" />
        <view class="booking-skeleton__summary-copy">
          <view class="skeleton booking-skeleton__line booking-skeleton__line--name" />
          <view class="skeleton booking-skeleton__line booking-skeleton__line--price" />
        </view>
      </view>
      <view class="block card booking-skeleton__date">
        <view class="booking-skeleton__head">
          <view class="skeleton booking-skeleton__line booking-skeleton__line--title" />
          <view class="skeleton booking-skeleton__date-entry" />
        </view>
        <view class="booking-skeleton__chips">
          <view
            v-for="index in 5"
            :key="`date-${index}`"
            class="skeleton booking-skeleton__chip"
          />
        </view>
      </view>
      <view class="block card">
        <view class="booking-skeleton__head">
          <view class="skeleton booking-skeleton__line booking-skeleton__line--title" />
          <view class="skeleton booking-skeleton__line booking-skeleton__line--hint" />
        </view>
        <view class="booking-skeleton__slots">
          <view
            v-for="index in 6"
            :key="`slot-${index}`"
            class="skeleton booking-skeleton__slot"
          />
        </view>
      </view>
      <view class="block card booking-skeleton__contact">
        <view class="skeleton booking-skeleton__line booking-skeleton__line--title" />
        <view
          v-for="index in 3"
          :key="`field-${index}`"
          class="booking-skeleton__field"
        >
          <view class="skeleton booking-skeleton__line booking-skeleton__line--label" />
          <view class="skeleton booking-skeleton__line booking-skeleton__line--value" />
        </view>
      </view>
      <view class="booking-skeleton__bar">
        <view class="skeleton booking-skeleton__price" />
        <view class="skeleton booking-skeleton__button" />
      </view>
    </view>

    <template v-else-if="service.serviceId">
      <!-- 服务摘要 -->
      <view class="summary card">
        <view class="summary-thumb">
          <image
            v-if="serviceImage"
            :src="assetUrl(serviceImage)"
            mode="aspectFit"
          />
          <view
            v-else
            class="summary-thumb__fallback"
          >
            <uni-icons
              type="home"
              size="24"
              color="#f9d5c5"
            />
          </view>
        </view>
        <view class="summary-copy">
          <text class="summary-name">
            {{ service.serviceName }}
          </text>
          <view class="summary-price">
            <UiPrice
              :value="service.price"
              :unit="service.unitName || '次'"
              size="sm"
            />
          </view>
        </view>
      </view>

      <!-- 日期 -->
      <view class="block card">
        <view class="block-head">
          <text class="block-title">
            服务日期
          </text>
          <view
            class="date-picker-entry"
            @click="calendarOpen = !calendarOpen"
          >
            <uni-icons
              type="calendar"
              size="16"
              color="#c9582d"
            />
            <view class="date-picker-copy">
              <text class="date-picker-caption">已选日期</text>
              <text class="date-picker-value">{{ selectedDateLabel }}</text>
            </view>
            <uni-icons
              :type="calendarOpen ? 'top' : 'bottom'"
              size="13"
              color="#c9582d"
            />
          </view>
        </view>
        <view v-if="calendarOpen" class="date-calendar">
          <view
            v-for="month in calendarMonths"
            :key="month.key"
            class="calendar-month"
          >
            <view class="calendar-month-title">
              <text>{{ month.year }}年{{ month.month }}月</text>
              <text class="calendar-month-range">可预约</text>
            </view>
            <view class="calendar-weekdays">
              <text v-for="weekday in calendarWeekdays" :key="weekday">{{ weekday }}</text>
            </view>
            <view class="calendar-grid">
              <view
                v-for="(day, index) in month.days"
                :key="day ? day.value : `empty-${month.key}-${index}`"
                class="calendar-day"
                :class="{ 'calendar-day--active': day && form.date === day.value, 'calendar-day--today': day && day.value === minDate, 'calendar-day--empty': !day }"
                @click="day && selectDate(day.value)"
              >
                <text v-if="day" class="calendar-day-number">{{ day.dateNumber }}</text>
                <text v-if="day && day.value === minDate" class="calendar-day-label">今天</text>
              </view>
            </view>
          </view>
          <text class="calendar-hint">可选今天至未来两个月，点击日期后自动查询时段</text>
        </view>
        <scroll-view
          scroll-x
          class="date-scroll"
          :show-scrollbar="false"
        >
          <view class="date-track">
            <view
              v-for="day in quickDateOptions"
              :key="day.value"
              class="date-chip"
              :class="{ 'date-chip--active': form.date === day.value }"
              @click="selectDate(day.value)"
            >
              <text class="date-week">
                {{ day.week }}
              </text>
              <text class="date-day">
                {{ day.label }}
              </text>
            </view>
          </view>
        </scroll-view>
        <text class="date-selected">已选择：{{ selectedDateFull }}</text>
      </view>

      <!-- 时段 -->
      <view class="block card">
        <view class="block-head">
          <text class="block-title">
            服务时段
          </text>
          <text class="block-hint">
            余量实时更新
          </text>
        </view>
        <view
          v-if="slots.length"
          class="slot-grid"
        >
          <view
            v-for="slot in slots"
            :key="slot.slotId"
            class="slot"
            :class="{ 'slot--active': form.slotId === slot.slotId, 'slot--disabled': isSlotFull(slot) }"
            @click="selectSlot(slot)"
          >
            <text class="slot-name">
              {{ slot.slotName }}
            </text>
            <text class="slot-left">
              {{ isSlotFull(slot) ? '已约满' : `余 ${availableOf(slot)}` }}
            </text>
          </view>
        </view>
        <text
          v-else
          class="block-empty"
        >
          该日期暂无可预约时段
        </text>
      </view>

      <!-- 联系信息 -->
      <view class="block card">
        <text class="block-title">
          联系信息
        </text>

        <view
          class="field"
          @click="openAddressBook"
        >
          <text class="field-label">
            服务地址
          </text>
          <view class="field-value">
            <text :class="['field-text', { 'field-text--placeholder': !form.address }]">
              {{ form.address || '请选择地址簿中的服务地址' }}
            </text>
            <uni-icons
              type="right"
              size="16"
              color="#c4bcb6"
            />
          </view>
        </view>

        <view
          v-if="requiresQuantity"
          class="field"
        >
          <text class="field-label">
            {{ quantityLabel }}
          </text>
          <view class="duration-field-content">
            <view class="duration-input">
              <input
                v-model.number="form.quantity"
                class="field-input duration-input__control"
                type="number"
                :min="quantityMin"
                :step="quantityStep"
                maxlength="8"
                :placeholder="quantityPlaceholder"
                placeholder-class="field-placeholder"
                @blur="normalizeQuantity"
              >
              <text class="duration-input__unit">{{ quantityUnit }}</text>
            </view>
            <text class="duration-hint">{{ quantityHint }}</text>
          </view>
        </view>

        <view class="field">
          <text class="field-label">
            联系电话
          </text>
          <input
            v-model="form.phone"
            class="field-input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="field-placeholder"
          >
        </view>

        <view class="field field--area">
          <text class="field-label">
            备注
          </text>
          <textarea
            v-model="form.remark"
            class="field-textarea"
            maxlength="120"
            auto-height
            placeholder="如：有宠物、需要自带清洁剂（选填）"
            placeholder-class="field-placeholder"
          />
        </view>
      </view>

      <view class="booking-notice card--sunken">
        <uni-icons
          type="info"
          size="14"
          color="#b26b4b"
        />
        <text>保洁服务时长为预估时间，如实际服务超时，将按实际超出时长补收超时费用。</text>
      </view>

      <view class="tip">
        <uni-icons
          type="info"
          size="13"
          color="#a39b95"
        />
        <text>预约开始前 2 小时可免费取消</text>
      </view>

      <!-- 底部提交 -->
      <view class="submit-bar">
        <view class="submit-price">
          <text class="submit-label">
            应付
          </text>
          <UiPrice
            :value="totalPrice"
            size="md"
          />
        </view>
        <button
          class="primary-button submit-button"
          :loading="submitting"
          @click="submit"
        >
          提交预约
        </button>
      </view>
    </template>

    <view
      v-else
      class="booking-empty"
    >
      <UiEmpty
        icon="info"
        :title="errorMessage || '服务不存在或已下架'"
        description="换个服务看看吧"
      />
    </view>
  </view>

  <AppFeedback />
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { createOrder, getService, listSlots } from '@/api/service'
import { useAddressStore } from '@/stores/address'
import { useCityStore } from '@/stores/city'
import { requestSubscribe } from '@/composables/usePayment'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { clearDraft, readDraft, saveDraft } from '@/utils/draft'
import { toastSuccess, showToast } from '@/utils/feedback'
import { payOrder } from '@/composables/usePayment'

const service = ref({})
const slots = ref([])
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

const cityStore = useCityStore()
const addressStore = useAddressStore()
const configStore = useConfigStore()

const form = reactive({ date: '', slotId: '', address: '', phone: '', remark: '', quantity: '' })
const draftKey = computed(() => (service.value.serviceId ? `booking:${service.value.serviceId}` : ''))

const totalPrice = computed(() => {
  const price = Number(service.value.price || 0)
  const quantity = Number(form.quantity)
  if (measureType.value === 'NONE') return price
  if (!requiresQuantity.value) return 0
  return Number.isFinite(quantity) && quantity > 0 ? price * quantity : 0
})

const measureType = computed(() => service.value?.measureType || '')
const requiresQuantity = computed(() => ['HOUR', 'AREA', 'COUNT'].includes(measureType.value))
const measurementConfigured = computed(() => measureType.value === 'NONE' || requiresQuantity.value)
const quantityUnit = computed(() => service.value?.unitName || '')
const quantityLabel = computed(() => ({ HOUR: '预约时长', AREA: '服务面积', COUNT: '服务数量' }[measureType.value] || '服务数量'))
const quantityMin = computed(() => measureType.value === 'HOUR' ? 2 : measureType.value === 'COUNT' ? 1 : 0.01)
const quantityStep = computed(() => measureType.value === 'AREA' ? 0.01 : 1)
const quantityPlaceholder = computed(() => ({ HOUR: '至少2小时', AREA: '请输入服务面积', COUNT: '请输入服务数量' }[measureType.value] || '请输入数量'))
const quantityHint = computed(() => ({
  HOUR: '最少 2 小时起，必须填写整数',
  AREA: `按每${quantityUnit.value || '面积'}单价计算，支持填写小数`,
  COUNT: '按服务数量计算预估费用，必须填写整数'
}[measureType.value] || '请填写服务数量'))

const serviceImage = computed(() => {
  const images = service.value?.carouselImages || service.value?.imageUrl || ''
  return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || ''
})

const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

const calendarOpen = ref(false)
const calendarWeekdays = ['一', '二', '三', '四', '五', '六', '日']
const minDate = computed(() => formatDate(new Date()))

// 从今天起可预约未来两个月，按月份分组展示日历卡片
const dateOptions = computed(() => {
  const list = []
  const now = new Date()
  const base = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const end = new Date(base.getFullYear(), base.getMonth() + 2, base.getDate())
  for (let day = new Date(base); day <= end; day.setDate(day.getDate() + 1)) {
    const value = formatDate(day)
    const offset = Math.round((day.getTime() - base.getTime()) / 86400000)
    list.push({
      value,
      week: offset === 0 ? '今天' : offset === 1 ? '明天' : WEEK[day.getDay()],
      label: `${day.getMonth() + 1}/${day.getDate()}`,
      dateNumber: day.getDate(),
      weekdayIndex: day.getDay()
    })
  }
  return list
})

const quickDateOptions = computed(() => dateOptions.value.slice(0, 7))
const calendarMonths = computed(() => {
  const groups = new Map()
  dateOptions.value.forEach(day => {
    const [year, month] = day.value.split('-').map(Number)
    const key = `${year}-${month}`
    if (!groups.has(key)) groups.set(key, { key, year, month, days: [] })
    groups.get(key).days.push(day)
  })
  return [...groups.values()].map(group => {
    const first = new Date(group.year, group.month - 1, 1).getDay()
    const leading = (first + 6) % 7
    return { ...group, days: [...Array(leading).fill(null), ...group.days] }
  })
})
const selectedDateLabel = computed(() => {
  const selected = dateOptions.value.find(day => day.value === form.date)
  return selected ? `${selected.label.replace('/', '月')}日` : '选择日期'
})
const selectedDateFull = computed(() => {
  const selected = dateOptions.value.find(day => day.value === form.date)
  return selected ? `${form.date.slice(0, 4)}年${selected.label.replace('/', '月')}日` : '请选择日期'
})

function formatDate (date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function responseList (response) { return response?.rows || response?.data?.rows || (Array.isArray(response?.data) ? response.data : Array.isArray(response) ? response : []) }
function availableOf (slot) { return slot.availableCount ?? slot.maxReserve ?? 0 }
function isSlotFull (slot) { return Number(availableOf(slot)) <= 0 }

function restoreDraft () {
  if (!draftKey.value) return
  const draft = readDraft(draftKey.value)
  if (!draft) {
    form.quantity = ''
    return
  }
  Object.assign(form, {
    address: draft.address || '',
    phone: draft.phone || '',
    remark: draft.remark || '',
    date: draft.date || form.date,
    quantity: Number(draft.quantity) || ''
  })
  if (draft.slotId) form.slotId = draft.slotId
}

async function loadSlots () {
  const id = service.value.serviceId
  if (!id) return
  try {
    const response = await listSlots({ serviceId: id, appointmentDate: form.date, cityId: cityStore.currentCityId() })
    slots.value = responseList(response)
    if (!slots.value.some(slot => slot.slotId === form.slotId && !isSlotFull(slot))) {
      form.slotId = slots.value.find(slot => !isSlotFull(slot))?.slotId || ''
    }
  } catch (e) {
    slots.value = []
  }
}

onMounted(async () => {
  const id = getCurrentPages().at(-1)?.options?.id
  if (!ensureLoggedIn(currentPageUrl())) return
  form.date = formatDate(new Date())
  uni.$on('address-selected', onAddressSelected)
  try {
    await cityStore.initialize()
    if (!id) {
      errorMessage.value = '预约服务信息缺失'
    } else {
      const [serviceResult, slotResult] = await Promise.allSettled([
        getService(id),
        listSlots({ serviceId: id, cityId: cityStore.currentCityId() })
      ])
      if (serviceResult.status === 'rejected') throw serviceResult.reason
      const serviceResponse = serviceResult.value
      const serviceData = serviceResponse?.data || serviceResponse
      service.value = serviceData && typeof serviceData === 'object' ? serviceData : {}
      if (!service.value.serviceId) {
        errorMessage.value = '服务不存在或已下架'
      } else {
        slots.value = slotResult.status === 'fulfilled' ? responseList(slotResult.value) : []

        const initialDate = form.date
        restoreDraft()
        if (!dateOptions.value.some(day => day.value === form.date)) {
          form.date = initialDate
        }
        if (form.date !== initialDate) {
          await loadSlots()
        } else if (!slots.value.some(slot => slot.slotId === form.slotId && !isSlotFull(slot))) {
          form.slotId = slots.value.find(slot => !isSlotFull(slot))?.slotId || ''
        }

      }
    }
  } catch (e) {
    errorMessage.value = e.message || '预约信息加载失败'
  } finally {
    loading.value = false
  }
  // 地址列表用于展示地址簿状态，并带出默认地址。
  addressStore.load().then(() => {
    if (!form.address) applyDefaultAddress()
  }).catch(() => {})
})

function onAddressSelected (address) {
  if (!address) return
  form.address = `${address.region || ''}${address.detail || ''}`
  if (address.contactPhone) form.phone = address.contactPhone
}

onUnmounted(() => {
  uni.$off('address-selected', onAddressSelected)
})

function applyDefaultAddress () {
  const address = addressStore.defaultAddress
  if (!address || form.address) return
  form.address = `${address.region || ''}${address.detail || ''}`
  if (!form.phone && address.contactPhone) form.phone = address.contactPhone
}

// 地址簿里没有记录时，回退到微信地址选择，两条路都能填上地址
function openAddressBook () {
  uni.navigateTo({ url: '/pages/address/index' })
}

watch(form, value => {
  if (draftKey.value) saveDraft(draftKey.value, { ...value })
}, { deep: true })

function selectDate (value) {
  calendarOpen.value = false
  if (form.date === value) return
  form.date = value
  form.slotId = ''
  loadSlots()
}

function selectSlot (slot) {
  if (isSlotFull(slot)) return showToast('该时段已约满')
  form.slotId = slot.slotId
}

function normalizeQuantity () {
  const value = Number(form.quantity)
  if (measureType.value === 'AREA') form.quantity = Number.isFinite(value) && value > 0 ? Math.round(value * 100) / 100 : ''
  if (measureType.value === 'HOUR') form.quantity = Number.isInteger(value) && value >= 2 ? value : 2
  if (measureType.value === 'COUNT') form.quantity = Number.isInteger(value) && value >= 1 ? value : 1
}

async function submit () {
  if (!ensureLoggedIn(currentPageUrl())) return
  if (!measurementConfigured.value) return showToast('服务单位计量方式未配置，请联系客服')
  if (!form.slotId) return showToast('请选择服务时段')
  if (!form.address) return showToast('请选择服务地址')
  if (measureType.value === 'HOUR' && (!Number.isInteger(Number(form.quantity)) || Number(form.quantity) < 2)) return showToast('预约时长必须是整数且不少于2小时')
  if (measureType.value === 'AREA' && (!Number.isFinite(Number(form.quantity)) || Number(form.quantity) <= 0)) return showToast('请填写有效的服务面积')
  if (measureType.value === 'COUNT' && (!Number.isInteger(Number(form.quantity)) || Number(form.quantity) < 1)) return showToast('请填写有效的服务数量')
  if (!/^1\d{10}$/.test(form.phone)) return showToast('请填写正确的手机号')
  submitting.value = true
  let orderId = ''
  try {
    // 授权操作必须由用户刚才的预约点击触发；不授权不影响继续下单。
    const templates = configStore.subscribeTemplates || {}
    if (templates.enabled) await requestSubscribe([templates.paid, templates.accepted, templates.finished])
    const response = await createOrder({
      serviceId: service.value.serviceId,
      appointmentDate: form.date,
      slotId: form.slotId,
      address: form.address,
      phone: form.phone,
      quantity: requiresQuantity.value ? Number(form.quantity) : null,
      cityId: cityStore.currentCityId(),
      remark: form.remark
    })
    orderId = response?.data?.orderId || response?.orderId || 'latest'
  } catch (e) {
    showToast(e.message || '提交失败')
    submitting.value = false
    return
  }

  clearDraft(draftKey.value)
  try {
    const payment = await payOrder(orderId)
    toastSuccess(payment.paid ? '预约并支付成功' : '订单已创建，支付结果确认中')
  } catch (e) {
    showToast(e.message === '已取消支付' ? '订单已创建，请完成线上支付' : (e.message || '支付失败，请在订单详情中继续支付'))
  } finally {
    submitting.value = false
    setTimeout(() => uni.redirectTo({ url: `/pages/order-detail/index?id=${orderId}` }), 700)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.booking-page {
  min-height: 100vh;
  padding: $space-3 $page-gutter calc(180rpx + env(safe-area-inset-bottom));
  background: $bg;
}

/* ---------- 服务摘要 ---------- */
.summary {
  display: flex;
  align-items: center;
  padding: $space-2;
}

.summary-thumb {
  flex: 0 0 128rpx;
  width: 128rpx;
  height: 128rpx;
  border-radius: $radius-md;
  overflow: hidden;
  background: $bg-sunken;
}
.summary-thumb image { width: 100%; height: 100%; }

.summary-thumb__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, $brand-50, $brand-100);
}

.summary-copy { display: flex; flex: 1; min-width: 0; flex-direction: column; margin-left: $space-3; }

.summary-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.summary-price { margin-top: $space-1; }

/* ---------- 通用区块 ---------- */
.block { margin-top: $space-2; padding: $space-4; }

.block-head { display: flex; align-items: center; justify-content: space-between; }

.block-title {
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.block-hint { font-size: $font-micro; color: $ink-400; }

.date-picker-entry {
  display: flex;
  align-items: center;
  gap: 4rpx;
  min-width: 164rpx;
  padding: 8rpx 12rpx;
  border: 1rpx solid $brand-200;
  border-radius: $radius-md;
  background: linear-gradient(135deg, $brand-50, #fff);
  color: $brand-700;
}

.date-picker-copy {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  margin-left: 4rpx;
}

.date-picker-caption {
  color: $ink-400;
  font-size: 17rpx;
  line-height: 1.2;
}

.date-picker-value {
  margin-top: 2rpx;
  color: $brand-700;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  line-height: 1.25;
}

.date-calendar {
  margin-top: $space-3;
  padding-top: $space-3;
  border-top: 1rpx solid $line-soft;
}

.calendar-month + .calendar-month {
  margin-top: $space-4;
  padding-top: $space-4;
  border-top: 1rpx solid $line-soft;
}

.calendar-month-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-3;
  color: $ink-900;
  font-size: $font-caption;
  font-weight: $weight-semibold;
}

.calendar-month-range {
  color: $ink-400;
  font-size: $font-micro;
  font-weight: $weight-regular;
}

.calendar-weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8rpx;
}

.calendar-weekdays {
  margin-bottom: 8rpx;
  text-align: center;
  color: $ink-400;
  font-size: $font-micro;
}

.calendar-day {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 76rpx;
  border-radius: $radius-md;
  color: $ink-700;
  font-size: $font-caption;
}

.calendar-day-number { line-height: 1.2; }
.calendar-day-label { margin-top: 3rpx; color: $brand-600; font-size: 17rpx; }

.calendar-day--today { background: $brand-50; color: $brand-600; }

.calendar-day--active {
  background: $brand-500;
  color: #fff;
  font-weight: $weight-semibold;
}

.calendar-day--active .calendar-day-label { color: #fff; }

.calendar-day--empty { pointer-events: none; }

.calendar-hint {
  display: block;
  margin-top: $space-2;
  color: $ink-400;
  font-size: $font-micro;
  text-align: center;
}

.block-empty {
  display: block;
  margin-top: $space-3;
  font-size: $font-caption;
  color: $ink-400;
}

/* ---------- 日期 ---------- */
.date-scroll { margin: $space-3 (-$space-4) 0; }

.date-track { display: inline-flex; gap: $space-1; padding: 0 $space-4; }

.date-chip {
  display: flex;
  width: 108rpx;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  padding: $space-2 0;
  border-radius: $radius-md;
  background: $bg-sunken;
  transition: all 0.18s ease;
}

.date-chip--active {
  background: $brand-500;
  box-shadow: 0 6rpx 18rpx rgba(224, 106, 61, 0.22);
}

.date-week { font-size: $font-micro; color: $ink-500; }
.date-day { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-900; }

.date-chip--active .date-week,
.date-chip--active .date-day { color: #fff; }

/* ---------- 时段 ---------- */
.slot-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: $space-2;
  margin-top: $space-3;
}

.slot {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  padding: $space-2 $space-3;
  border-radius: $radius-md;
  background: $bg-sunken;
  border: 1rpx solid transparent;
  transition: all 0.18s ease;
}

.slot--active {
  background: $brand-50;
  border-color: $brand-400;
}

.slot--disabled { opacity: 0.45; }

.slot-name { font-size: $font-caption; font-weight: $weight-semibold; color: $ink-900; }
.slot-left { font-size: $font-micro; color: $ink-400; }

.slot--active .slot-name { color: $brand-700; }
.slot--active .slot-left { color: $brand-600; }

/* ---------- 表单字段 ---------- */
.field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 1rpx solid $line-soft;
}
.field:last-child { border-bottom: 0; padding-bottom: 0; }

.field--area { flex-direction: column; align-items: stretch; gap: $space-2; }

.field-label {
  flex: 0 0 auto;
  font-size: $font-caption;
  color: $ink-500;
}

.field-value { display: flex; flex: 1; min-width: 0; align-items: center; justify-content: flex-end; gap: 6rpx; }

.field-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: right;
  font-size: $font-caption;
  color: $ink-900;
}
.field-text--placeholder { color: $ink-400; }

.field-input {
  flex: 1;
  min-width: 0;
  text-align: right;
  font-size: $font-caption;
  color: $ink-900;
}

.duration-input { display: flex; flex: 1; align-items: center; justify-content: flex-end; }
.duration-field-content { flex: 1; min-width: 0; }
.duration-input__control { flex: 0 0 140rpx; }
.duration-input__unit { margin-left: 8rpx; font-size: $font-caption; color: $ink-500; }
.duration-hint { display: block; margin-top: 6rpx; text-align: right; font-size: $font-micro; color: $ink-400; line-height: 1.4; }

.field-placeholder { color: $ink-400; }

.field-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: $space-2;
  border-radius: $radius-md;
  background: $bg-sunken;
  font-size: $font-caption;
  line-height: 1.6;
  color: $ink-900;
}

.booking-notice {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  margin-top: $space-2;
  padding: $space-3 $space-4;
  font-size: $font-micro;
  line-height: 1.6;
  color: $ink-500;
}

/* ---------- 提示 ---------- */
.tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  margin-top: $space-3;
  font-size: $font-micro;
  color: $ink-400;
}

/* ---------- 底栏 ---------- */
.submit-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-2 $page-gutter calc(#{$space-2} + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  box-shadow: $shadow-bar;
}

.submit-price { display: flex; flex-direction: column; }

.submit-label { font-size: 18rpx; color: $ink-400; }

.submit-button {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  margin: 0;
}

.booking-empty { padding-top: $space-8; }

.booking-skeleton { padding-bottom: 180rpx; }
.booking-skeleton__summary { display: flex; align-items: center; padding: $space-2; }
.booking-skeleton__thumb { flex: 0 0 128rpx; width: 128rpx; height: 128rpx; border-radius: $radius-md; }
.booking-skeleton__summary-copy { flex: 1; margin-left: $space-3; }
.booking-skeleton__line { height: 26rpx; }
.booking-skeleton__line--name { width: 64%; }
.booking-skeleton__line--price { width: 38%; margin-top: $space-2; }
.booking-skeleton__head { display: flex; align-items: center; justify-content: space-between; }
.booking-skeleton__line--title { width: 150rpx; }
.booking-skeleton__line--hint { width: 120rpx; height: 22rpx; }
.booking-skeleton__date-entry { width: 164rpx; height: 62rpx; border-radius: $radius-md; }
.booking-skeleton__chips { display: flex; gap: $space-1; margin: $space-3 (-$space-4) 0; padding: 0 $space-4; overflow: hidden; }
.booking-skeleton__chip { flex: 0 0 108rpx; height: 76rpx; border-radius: $radius-md; }
.booking-skeleton__slots { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: $space-2; margin-top: $space-3; }
.booking-skeleton__slot { height: 76rpx; border-radius: $radius-md; }
.booking-skeleton__field { display: flex; justify-content: space-between; margin-top: $space-4; padding-bottom: $space-3; border-bottom: 1rpx solid $line-soft; }
.booking-skeleton__line--label { width: 110rpx; height: 22rpx; }
.booking-skeleton__line--value { width: 220rpx; height: 22rpx; }
.booking-skeleton__bar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 10; display: flex; align-items: center; gap: $space-3; padding: $space-2 $page-gutter calc(#{$space-2} + env(safe-area-inset-bottom)); background: rgba(255, 255, 255, 0.96); box-shadow: $shadow-bar; }
.booking-skeleton__price { width: 120rpx; height: 34rpx; }
.booking-skeleton__button { flex: 1; height: 88rpx; border-radius: $radius-pill; }
</style>
