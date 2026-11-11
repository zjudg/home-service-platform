<template>
  <view class="apply-page">
    <UiNavbar
      :back="true"
      title="师傅入驻"
    />

    <view class="apply-body">
      <view
        v-if="readonly"
        class="notice-card"
      >
        <uni-icons
          type="info"
          size="15"
          color="#4a7fb5"
        />
        <text class="notice-text">
          {{ noticeText }}
        </text>
      </view>

      <view class="card form-card">
        <view class="form-item">
          <text class="form-label">
            姓名<text class="required">
              *
            </text>
          </text>
          <input
            v-model="form.name"
            class="form-input"
            placeholder="请输入姓名"
            maxlength="64"
          >
        </view>

        <view class="form-item">
          <text class="form-label">
            联系电话<text class="required">
              *
            </text>
          </text>
          <input
            v-model="form.phone"
            class="form-input"
            type="number"
            placeholder="11 位手机号"
            maxlength="11"
          >
        </view>

        <view class="form-item">
          <text class="form-label">
            服务城市
          </text>
          <view class="select-field" @click="chooseCity">
            <text :class="['select-value', { 'select-value--placeholder': !form.cityName }]">
              {{ form.cityName || '请选择服务城市' }}
            </text>
            <uni-icons type="right" size="16" color="#c4bcb6" />
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">
            健康证<text class="required">*</text>
          </text>
          <view class="certificate-picker" @click="chooseCertificate('health')">
            <image v-if="form.healthCertUrl" class="certificate-image" :src="assetUrl(form.healthCertUrl)" mode="aspectFill" />
            <view v-else class="certificate-placeholder">
              <uni-icons type="camera" size="22" color="#a39b95" />
              <text>上传健康证图片</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">
            技能证书（选填）
          </text>
          <view class="certificate-picker" @click="chooseCertificate('skill')">
            <image v-if="form.skillCertUrl" class="certificate-image" :src="assetUrl(form.skillCertUrl)" mode="aspectFill" />
            <view v-else class="certificate-placeholder">
              <uni-icons type="camera" size="22" color="#a39b95" />
              <text>上传技能证书图片（选填）</text>
            </view>
          </view>
        </view>

        <view class="form-item form-item--last">
          <text class="form-label">
            擅长服务
          </text>
          <view class="skill-options">
            <view
              v-for="option in skillOptions"
              :key="option.key"
              class="skill-chip"
              :class="{ 'skill-chip--active': selectedSkills.includes(option.key) }"
              @click="toggleSkill(option.key)"
            >
              {{ option.label }}
            </view>
          </view>
          <text class="field-hint">可多选，请选择您实际可以承接的服务类型</text>
        </view>
      </view>

      <text class="tip-text">
        提交后平台将核验资料，审核期间不影响您正常下单服务。
      </text>

      <button
        class="submit-btn"
        :disabled="submitting || readonly || uploading"
        @click="submit"
      >
        {{ submitting ? '提交中...' : submitText }}
      </button>
    </view>

    <AppFeedback />
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { useMasterStore } from '@/stores/master'
import { uploadFile } from '@/api/common'
import { assetUrl } from '@/utils/assets'
import { showToast } from '@/utils/feedback'
import { useCityStore } from '@/stores/city'

const masterStore = useMasterStore()
const cityStore = useCityStore()
const submitting = ref(false)
const uploading = ref(false)

const skillOptions = [
  { key: 'clean', value: 'ZC_CLEAN_HOME', label: '家庭保洁' },
  { key: 'deep-clean', value: 'ZC_CLEAN_DEEP', label: '深度保洁' },
  { key: 'new-house-clean', value: 'ZC_CLEAN_NEW', label: '开荒保洁' },
  { key: 'appliance-clean', value: 'ZC_CLEAN_APPLIANCE', label: '家电清洗' },
  { key: 'maternity', value: 'ZC_NANNY_MATERNITY', label: '月嫂' },
  { key: 'childcare', value: 'ZC_NANNY_CHILDCARE', label: '育儿嫂' },
  { key: 'lactation', value: 'ZC_NANNY_LACTATION', label: '催乳师' },
  { key: 'postpartum-care', value: 'ZC_NANNY_POSTPARTUM', label: '产后康复' },
  { key: 'housekeeping', value: 'ZC_NANNY_HOUSEKEEPING', label: '家庭保姆' },
  { key: 'repair', value: 'ZC_REPAIR_APPLIANCE', label: '家电维修' },
  { key: 'plumbing', value: 'ZC_REPAIR_PLUMBING', label: '管道疏通' },
  { key: 'water-electric', value: 'ZC_REPAIR_WATER', label: '水电维修' },
  { key: 'install', value: 'ZC_INSTALL_HOME', label: '上门安装' },
  { key: 'elder-care', value: 'ZC_CARE_ELDER', label: '老人陪护' },
  { key: 'escort-care', value: 'ZC_CARE_ESCORT', label: '陪诊陪护' },
  { key: 'elder-rehab', value: 'ZC_CARE_REHAB', label: '康复护理' }
]

const form = reactive({
  name: '',
  phone: '',
  cityId: '',
  cityName: '',
  healthCertUrl: '',
  skillCertUrl: '',
  skillCategoryIds: '',
  skillSelectionKeys: []
})

const selectedSkills = computed(() => form.skillSelectionKeys)

// 审核中不允许重复提交，避免误操作把申请重置；已通过仍允许更新资料。
const readonly = computed(() => masterStore.isPending)

const noticeText = computed(() =>
  masterStore.isPending
    ? '资料审核中，审核通过前无法修改。如需变更请联系客服。'
    : '资料更新后将重新进入审核流程。'
)

const submitText = computed(() => (masterStore.applied ? '更新资料' : '提交申请'))

function validate () {
  if (!form.name.trim()) return '请填写姓名'
  if (!/^1\d{10}$/.test(form.phone.trim())) return '联系电话格式不正确'
  if (!form.cityId) return '请选择服务城市'
  if (!form.healthCertUrl) return '请上传健康证图片'
  return ''
}

function chooseCity () {
  uni.navigateTo({ url: `/pages/city/index?current=${encodeURIComponent(form.cityName || '')}` })
}

function toggleSkill (key) {
  const keys = new Set(selectedSkills.value)
  if (keys.has(key)) keys.delete(key)
  else keys.add(key)
  form.skillSelectionKeys = [...keys]
  form.skillCategoryIds = [...new Set(skillOptions.filter(option => keys.has(option.key)).map(option => option.value))].join(',')
}

async function chooseCertificate (type) {
  if (uploading.value) return
  try {
    const result = await uni.chooseImage({ count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'] })
    const path = result?.tempFilePaths?.[0]
    if (!path) return
    uploading.value = true
    const response = await uploadFile(path)
    const url = response?.url || response?.data?.url
    if (!url) throw new Error('上传未返回图片地址')
    if (type === 'health') form.healthCertUrl = url
    else form.skillCertUrl = url
  } catch (e) {
    if (!String(e?.errMsg || e?.message || '').includes('cancel')) showToast(e.message || '证书上传失败')
  } finally {
    uploading.value = false
  }
}

async function submit () {
  const message = validate()
  if (message) {
    showToast(message)
    return
  }
  submitting.value = true
  try {
    await masterStore.apply({
      masterName: form.name.trim(),
      realName: form.name.trim(),
      phone: form.phone.trim(),
      cityId: form.cityId.trim(),
      healthCertUrl: form.healthCertUrl.trim(),
      skillCertUrl: form.skillCertUrl.trim(),
      skillCategoryIds: form.skillCategoryIds.trim()
    })
    showToast('资料已提交，请等待审核', { type: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch (e) {
    showToast(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

// 未登录也可能进入本页（入口已对游客开放），用 onShow 保证登录后回跳能拉到档案。
// 只回填一次、且仅在档案发生真实变化时回填，避免切后台/选图返回时覆盖未提交的输入。
let filledOnce = false
let filledSignature = ''

function fillFromProfile (profile) {
  Object.assign(form, {
    name: profile.masterName || profile.realName || '',
    phone: profile.phone || '',
    cityId: profile.cityId || '',
    cityName: profile.cityName || '',
    healthCertUrl: profile.healthCertUrl || '',
    skillCertUrl: profile.skillCertUrl || '',
    skillCategoryIds: profile.skillCategoryIds || '',
    skillSelectionKeys: skillOptions
      .filter(option => (profile.skillCategoryIds || '').split(',').some(categoryId =>
        categoryId === option.value || option.value.startsWith(`${categoryId}_`)))
      .map(option => option.key)
  })
}

function syncCity () {
  cityStore.restore()
  if (cityStore.cityId || cityStore.cityName) {
    form.cityId = cityStore.cityId || ''
    form.cityName = cityStore.cityName || ''
  }
}

onShow(async () => {
  await cityStore.initialize()
  syncCity()
  await masterStore.loadProfile()
  const profile = masterStore.profile
  const signature = JSON.stringify(profile || null)
  if (filledOnce && signature === filledSignature) return
  filledOnce = true
  filledSignature = signature
  if (profile) fillFromProfile(profile)
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.apply-page { min-height: 100vh; background: $bg; }

.apply-body { padding: 0 $page-gutter $space-8; }

.notice-card {
  display: flex; align-items: flex-start; gap: $space-2;
  padding: $space-3; margin-bottom: $space-3;
  background: $info-bg; border-radius: $radius-md;
}

.notice-text { flex: 1; font-size: $font-caption; color: $ink-500; line-height: 1.6; }

.form-card { padding: 0 $space-4; }

.form-item {
  padding: $space-3 0;
  border-bottom: 2rpx solid $line-soft;
  &--last { border-bottom: none; }
}

.form-label { display: block; font-size: $font-caption; color: $ink-500; margin-bottom: 12rpx; }

.required { color: $danger; margin-left: 4rpx; }

.form-input {
  width: 100%; height: 68rpx; font-size: $font-body; color: $ink-900;
}

.select-field { display: flex; align-items: center; justify-content: space-between; min-height: 68rpx; }
.select-value { font-size: $font-body; color: $ink-900; }
.select-value--placeholder { color: $ink-400; }

.certificate-picker { display: flex; align-items: center; justify-content: center; width: 100%; height: 220rpx; border: 2rpx dashed $line; border-radius: $radius-md; background: $bg-sunken; overflow: hidden; }
.certificate-image { width: 100%; height: 100%; }
.certificate-placeholder { display: flex; flex-direction: column; align-items: center; gap: 10rpx; color: $ink-400; font-size: $font-caption; }

.skill-options { display: flex; flex-wrap: wrap; gap: $space-2; }
.skill-chip { padding: 12rpx 24rpx; border-radius: $radius-pill; background: $bg-sunken; color: $ink-500; font-size: $font-caption; }
.skill-chip--active { background: $brand-50; color: $brand-600; font-weight: $weight-semibold; }
.field-hint { display: block; margin-top: $space-2; color: $ink-400; font-size: $font-micro; }

.tip-text {
  display: block; margin-top: $space-3; padding: 0 $space-1;
  font-size: $font-micro; color: $ink-400; line-height: 1.6;
}

.submit-btn {
  margin-top: $space-4; height: 88rpx; line-height: 88rpx;
  border-radius: $radius-md; background: $brand-500; color: #fff;
  font-size: $font-button; font-weight: $weight-semibold;
  &[disabled] { opacity: 0.6; }
}
</style>
