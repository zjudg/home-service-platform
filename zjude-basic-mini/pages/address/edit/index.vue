<template>
  <view class="address-edit-page">
    <UiNavbar :back="true" :title="isEdit ? '编辑地址' : '新增地址'" />

    <view class="address-body">
      <view class="card form-card">
        <view class="form-item">
          <text class="form-label">
            联系人<text class="required">*</text>
          </text>
          <input
            v-model="form.contactName"
            class="form-input"
            placeholder="请填写联系人姓名"
            maxlength="64"
          >
        </view>

        <view class="form-item">
          <text class="form-label">
            联系电话<text class="required">*</text>
          </text>
          <input
            v-model="form.contactPhone"
            class="form-input"
            type="number"
            placeholder="11 位手机号"
            maxlength="11"
          >
        </view>

        <view class="form-item">
          <text class="form-label">
            所在区域
          </text>
          <view class="region-field-row">
            <input
              v-model="form.region"
              class="form-input"
              placeholder="如 陕西省汉中市汉台区"
              maxlength="255"
            >
            <button
              class="region-map-button"
              :disabled="locationBusy"
              @click="chooseFromMap"
            >
              <uni-icons type="map" size="15" color="#c9582d" />
              <text>{{ locationBusy ? '选择中...' : '地图选择' }}</text>
            </button>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">
            详细地址<text class="required">*</text>
          </text>
          <textarea
            v-model="form.detail"
            class="form-textarea"
            placeholder="街道、门牌号、楼层等"
            maxlength="255"
            :auto-height="true"
          />
        </view>

        <view class="form-item">
          <text class="form-label">
            标签
          </text>
          <view class="label-row">
            <view
              v-for="tag in labelOptions"
              :key="tag"
              class="label-chip"
              :class="{ 'label-chip--active': form.label === tag }"
              @click="toggleLabel(tag)"
            >
              {{ tag }}
            </view>
          </view>
        </view>

        <view class="form-item form-item--last form-item--switch">
          <text class="form-label form-label--inline">
            设为默认地址
          </text>
          <switch
            :checked="form.isDefault === '1'"
            color="#e06a3d"
            @change="onDefaultChange"
          />
        </view>
      </view>

      <button
        class="primary-button submit-button"
        :disabled="submitting"
        @click="submit"
      >
        {{ submitting ? '保存中...' : '保存' }}
      </button>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { useAddressStore } from '@/stores/address'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { showToast, toastSuccess } from '@/utils/feedback'
import { reverseGeocodeLocation } from '@/api/service'

const addressStore = useAddressStore()
const labelOptions = ['家', '公司', '父母家']
const addressId = ref('')
const submitting = ref(false)
const locationBusy = ref(false)

const form = reactive({
  contactName: '',
  contactPhone: '',
  region: '',
  detail: '',
  label: '',
  isDefault: '0'
})

const isEdit = computed(() => Boolean(addressId.value))

onLoad(async options => {
  if (!ensureLoggedIn(currentPageUrl())) return
  addressId.value = options?.id || ''
  if (!addressId.value) {
    // 新增时若还没有任何地址，默认勾选「设为默认」，与后端首条自动默认保持一致
    await addressStore.load().catch(() => {})
    form.isDefault = addressStore.list.length === 0 ? '1' : '0'
    return
  }
  const target = addressStore.list.find(item => item.addressId === addressId.value)
  if (target) {
    fillForm(target)
    return
  }
  // 列表还没加载过（例如从深链进入）时补一次加载
  await addressStore.load(true).catch(() => {})
  const loaded = addressStore.list.find(item => item.addressId === addressId.value)
  if (loaded) fillForm(loaded)
  else showToast('地址不存在')
})

function fillForm (item) {
  form.contactName = item.contactName || ''
  form.contactPhone = item.contactPhone || ''
  form.region = item.region || ''
  form.detail = item.detail || ''
  form.label = item.label || ''
  form.isDefault = item.isDefault === '1' ? '1' : '0'
}

function toggleLabel (tag) {
  form.label = form.label === tag ? '' : tag
}

function onDefaultChange (event) {
  form.isDefault = event.detail.value ? '1' : '0'
}

function invokeUni (method, options = {}) {
  return new Promise((resolve, reject) => {
    if (typeof uni[method] !== 'function') return reject(new Error('当前平台暂不支持地图选址'))
    uni[method]({ ...options, success: resolve, fail: reject })
  })
}

function applyLocationRegion (location) {
  const region = [location?.provinceName, location?.cityName, location?.districtName].filter(Boolean).join('')
  if (region) form.region = region
}

async function chooseFromMap () {
  if (locationBusy.value) return
  locationBusy.value = true
  try {
    const picked = await invokeUni('chooseLocation')
    if (picked.latitude && picked.longitude) {
      const response = await reverseGeocodeLocation({ longitude: picked.longitude, latitude: picked.latitude })
      applyLocationRegion(response?.data)
    } else {
      throw new Error('地图未返回位置坐标，请重试')
    }
    if (!form.region) showToast('未能识别所在区域，请手动填写')
  } catch (error) {
    if (!String(error?.errMsg || error?.message || '').includes('cancel')) {
      showToast(error?.message || error?.errMsg || '地图选址失败，请手动填写地址')
    }
  } finally {
    locationBusy.value = false
  }
}

function validate () {
  if (!form.contactName.trim()) return '请填写联系人'
  if (!/^1\d{10}$/.test(form.contactPhone.trim())) return '联系电话格式不正确'
  if (!form.detail.trim()) return '请填写详细地址'
  return ''
}

async function submit () {
  const message = validate()
  if (message) {
    showToast(message)
    return
  }
  submitting.value = true
  try {
    const data = {
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      region: form.region.trim(),
      detail: form.detail.trim(),
      label: form.label,
      isDefault: form.isDefault
    }
    if (isEdit.value) {
      await addressStore.update(addressId.value, data)
    } else {
      await addressStore.create(data)
    }
    toastSuccess('已保存')
    setTimeout(() => uni.navigateBack(), 600)
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.address-edit-page { min-height: 100vh; background: $bg; }

.address-body { padding: 0 $page-gutter $space-8; }

.form-card { padding: 0 $space-4; }

.region-field-row { display: flex; align-items: center; gap: $space-2; }
.region-field-row .form-input { flex: 1; min-width: 0; }
.region-map-button { display: flex; flex: 0 0 auto; align-items: center; gap: 6rpx; height: 48rpx; margin: 0; padding: 0 10rpx; border: 0; border-radius: $radius-sm; background: $brand-50; color: $brand-600; font-size: $font-micro; line-height: 1; }
.region-map-button::after { border: 0; }
.region-map-button[disabled] { opacity: 0.55; }

.form-item {
  padding: $space-3 0;
  border-bottom: 2rpx solid $line-soft;
  &--last { border-bottom: none; }
  &--switch {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.form-label { display: block; margin-bottom: 12rpx; font-size: $font-caption; color: $ink-500; }
.form-label--inline { margin-bottom: 0; }

.required { margin-left: 4rpx; color: $danger; }

.form-input { width: 100%; height: 68rpx; font-size: $font-body; color: $ink-900; }

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  font-size: $font-body;
  color: $ink-900;
  line-height: 1.6;
}

.label-row { display: flex; gap: $space-2; }

.label-chip {
  padding: 10rpx 28rpx;
  border-radius: $radius-pill;
  background: $bg-sunken;
  color: $ink-500;
  font-size: $font-caption;
}
.label-chip--active { background: $brand-50; color: $brand-600; font-weight: $weight-semibold; }

.submit-button { margin-top: $space-4; }
</style>
