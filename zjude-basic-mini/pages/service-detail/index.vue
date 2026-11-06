<template>
  <view class="detail-page">
    <UiNavbar :back="true" title="服务详情" />

    <view v-if="service.serviceId" class="detail-body">
      <!-- 头图 -->
      <view class="hero">
        <image v-if="serviceImage" class="hero-image" :src="assetUrl(serviceImage)" mode="aspectFit" />
        <view v-else class="hero-fallback">
          <uni-icons type="home" size="56" color="#f9d5c5" />
        </view>
      </view>

      <!-- 主信息 -->
      <view class="main-card card">
        <view class="price-row">
          <UiPrice :value="service.price" :unit="service.unitName || '次'" :original="service.originalPrice" size="lg" />
          <text class="sold">已服务 {{ service.reserveCount || 0 }} 单</text>
        </view>

        <text class="detail-title">{{ service.serviceName }}</text>
        <text v-if="service.serviceIntro" class="detail-intro">{{ service.serviceIntro }}</text>
      </view>

      <!-- 服务保障（紧跟主信息，弱化呈现） -->
      <view class="assure">
        <view v-for="item in assurances" :key="item.label" class="assure-item">
          <uni-icons :type="item.icon" size="15" color="#2f9e6b" />
          <text class="assure-label">{{ item.label }}</text>
        </view>
      </view>

      <!-- 服务说明 -->
      <view class="section gap-top">
        <view class="section-head">
          <view class="section-mark" />
          <text class="section-title">服务说明</text>
        </view>
        <view class="section-body card">
          <view
            v-for="(section, sectionIndex) in contentSections"
            :key="`${section.title}-${sectionIndex}`"
            class="detail-section"
          >
            <text class="detail-section__title">{{ section.title }}</text>
            <text
              v-for="(paragraph, paragraphIndex) in section.paragraphs"
              :key="`p-${paragraphIndex}`"
              class="detail-section__text"
            >
              {{ paragraph }}
            </text>
            <view
              v-for="(list, listIndex) in section.lists"
              :key="`list-${listIndex}`"
              class="detail-list"
            >
              <view
                v-for="(item, itemIndex) in list.items"
                :key="`item-${itemIndex}`"
                class="detail-list__item"
              >
                <text
                  v-if="list.ordered"
                  class="detail-list__number"
                >
                  {{ itemIndex + 1 }}
                </text>
                <view v-else class="detail-list__dot" />
                <text class="detail-list__text">{{ item }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 预约须知 -->
      <view class="section gap-top">
        <view class="section-head">
          <view class="section-mark" />
          <text class="section-title">预约须知</text>
        </view>
        <view class="section-body card notice-card">
          <view v-for="(item, index) in notices" :key="item" class="notice-row">
            <text class="notice-index">{{ index + 1 }}</text>
            <text class="notice-text">{{ item }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="detail-empty">
      <UiEmpty
        icon="info"
        :title="errorMessage || '服务不存在或已下架'"
        description="可以返回服务列表挑选其他项目"
      />
    </view>

    <!-- 底部操作 -->
    <view v-if="service.serviceId" class="action-bar">
      <view class="action-side" @click="toggleFavorite">
        <uni-icons
          :type="favorited ? 'heart-filled' : 'heart'"
          size="19"
          :color="favorited ? '#e06a3d' : '#7a726c'"
        />
        <text class="action-side__label">{{ favorited ? '已收藏' : '收藏' }}</text>
      </view>
      <view class="action-side" @click="contactService">
        <uni-icons type="chat" size="19" color="#7a726c" />
        <text class="action-side__label">咨询</text>
      </view>
      <view class="action-price">
        <UiPrice :value="service.price" size="md" />
      </view>
      <button class="primary-button action-button" @click="book">立即预约</button>
    </view>
  </view>

  <AppFeedback />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiPrice from '@/components/ui/UiPrice.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { parseServiceContent } from '@/utils/service-content'
import { getService } from '@/api/service'
import { addFavorite, checkFavorite, removeFavorite } from '@/api/favorite'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'
import { showDialog, showToast } from '@/utils/feedback'
import { goLogin, isAuthenticated } from '@/utils/guard'

const service = ref({})
const errorMessage = ref('')
const favorited = ref(false)
const configStore = useConfigStore()

const assurances = [
  { icon: 'person-filled', label: '实名认证' },
  { icon: 'checkbox-filled', label: '不满意重做' },
  { icon: 'calendar-filled', label: '准时上门' },
  { icon: 'wallet-filled', label: '先行赔付' }
]

const notices = [
  '服务人员将在上门前 30 分钟与您电话确认',
  '预约开始前 2 小时可免费取消',
  '如需调整时间，请联系客服重新预约'
]

const serviceImage = computed(() => {
  const images = service.value?.carouselImages || service.value?.imageUrl || ''
  return (Array.isArray(images) ? images : String(images).split(',')).map(value => String(value).trim()).find(Boolean) || ''
})

const contentSections = computed(() => {
  const sections = parseServiceContent(service.value?.detailContent)
  return sections.length ? sections : [{
    title: '服务内容',
    paragraphs: ['服务人员上门前会与您确认具体需求，按约定范围完成服务后由您验收。'],
    lists: []
  }]
})

onMounted(async () => {
  const id = getCurrentPages().at(-1)?.options?.id
  configStore.load().catch(() => {})
  try {
    const response = await getService(id)
    service.value = response?.data || response
    // 收藏状态依赖登录态，未登录时保持未收藏
    loadFavoriteState(service.value.serviceId)
  } catch (e) {
    errorMessage.value = e.message || '服务加载失败'
  }
})

function book () {
  if (!service.value.serviceId) return showToast('服务信息加载中')
  const target = `/pages/booking/index?id=${service.value.serviceId}`
  // 已登录直接进入预约页；未登录先提示，由用户手动确认后去登录
  if (isAuthenticated()) return uni.navigateTo({ url: target })
  showDialog({
    title: '请先登录',
    content: '登录后才能预约服务，是否现在去登录？',
    confirmText: '去登录'
  }).then(confirmed => {
    if (confirmed) goLogin(target)
  })
}

function contactService () {
  const phone = String(configStore.site?.servicePhone || '').trim()
  if (!phone) return showToast('客服电话暂未配置')
  uni.makePhoneCall({ phoneNumber: phone, fail: () => showToast('拨号未完成') })
}

// 收藏：未登录先引导登录，登录后回到本页继续操作
async function toggleFavorite () {
  const serviceId = service.value.serviceId
  if (!serviceId) return
  if (!isAuthenticated()) {
    const confirmed = await showDialog({
      title: '请先登录',
      content: '登录后才能收藏服务，是否现在去登录？',
      confirmText: '去登录'
    })
    if (confirmed) goLogin(`/pages/service-detail/index?id=${serviceId}`)
    return
  }
  // 先本地翻转，请求失败再回滚，避免连点期间的等待感
  const next = !favorited.value
  favorited.value = next
  try {
    if (next) await addFavorite(serviceId)
    else await removeFavorite(serviceId)
    showToast(next ? '已加入收藏' : '已取消收藏')
  } catch (e) {
    favorited.value = !next
    showToast(e.message || '操作失败')
  }
}

async function loadFavoriteState (serviceId) {
  if (!serviceId || !isAuthenticated()) return
  try {
    const response = await checkFavorite(serviceId)
    favorited.value = Boolean(response?.data?.favorite)
  } catch (e) {
    // 收藏状态拉取失败不影响浏览与预约
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.detail-page {
  min-height: 100vh;
  padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
  background: $bg;
}

/* ---------- 头图：圆角内嵌，不再被导航栏压住 ---------- */
.hero {
  position: relative;
  // Service artwork is a wide banner; keep the full 2.5:1 composition visible.
  height: 280rpx;
  margin: $space-2 $page-gutter 0;
  border-radius: $radius-lg;
  overflow: hidden;
  background: #fff5ef;
}

.hero-image { display: block; width: 100%; height: 100%; }

.hero-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(150deg, $brand-50, $brand-100);
}

.detail-body { position: relative; }

/* ---------- 主信息 ---------- */
.main-card {
  margin: 20rpx $page-gutter 0;
  padding: $space-4;
}

.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.sold { font-size: $font-micro; color: $ink-400; }

.detail-title {
  display: block;
  margin-top: $space-3;
  font-size: 36rpx;
  font-weight: $weight-bold;
  line-height: 1.4;
  color: $ink-900;
}

.detail-intro {
  display: block;
  margin-top: $space-2;
  font-size: $font-caption;
  line-height: 1.7;
  color: $ink-500;
}

/* ---------- 保障条 ---------- */
.assure {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: $space-3 $page-gutter 0;
  padding: $space-2 $space-3;
  border-radius: $radius-md;
  background: rgba(255, 255, 255, 0.7);
}

.assure-item {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}

.assure-label { font-size: $font-micro; color: $ink-500; }

/* ---------- 内容区块 ---------- */
.section { padding: 0 $page-gutter; }

.gap-top { margin-top: $space-4; }

.section-head {
  display: flex;
  align-items: center;
  margin-bottom: $space-2;
}

.section-mark {
  width: 6rpx;
  height: 26rpx;
  margin-right: $space-2;
  border-radius: $radius-pill;
  background: $brand-500;
}

.section-title {
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.section-body { padding: $space-4; }

.detail-section + .detail-section {
  margin-top: $space-4;
  padding-top: $space-4;
  border-top: 1rpx solid $line-soft;
}

.detail-section__title {
  display: block;
  margin-bottom: $space-2;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  color: $ink-700;
}

.detail-section__text,
.detail-list__text {
  display: block;
  font-size: $font-caption;
  line-height: 1.75;
  color: $ink-500;
}

.detail-section__text + .detail-section__text { margin-top: $space-2; }
.detail-list { margin-top: $space-2; }
.detail-list__item { display: flex; align-items: flex-start; gap: $space-2; }
.detail-list__item + .detail-list__item { margin-top: $space-2; }
.detail-list__number {
  display: block;
  flex: 0 0 30rpx;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background: $brand-50;
  color: $brand-600;
  font-size: 18rpx;
  line-height: 30rpx;
  text-align: center;
}
.detail-list__dot {
  flex: 0 0 10rpx;
  width: 10rpx;
  height: 10rpx;
  margin: 12rpx 10rpx 0;
  border-radius: 50%;
  background: $brand-400;
}
.detail-list__text { flex: 1; min-width: 0; }

/* ---------- 须知 ---------- */
.notice-card { padding: $space-1 $space-4; }

.notice-row {
  display: flex;
  align-items: flex-start;
  padding: $space-3 0;
}
.notice-row + .notice-row { border-top: 1rpx solid $line-soft; }

.notice-index {
  flex: 0 0 34rpx;
  width: 34rpx;
  height: 34rpx;
  margin-right: $space-2;
  border-radius: 50%;
  background: $brand-50;
  color: $brand-600;
  font-size: 18rpx;
  font-weight: $weight-bold;
  line-height: 34rpx;
  text-align: center;
}

.notice-text {
  flex: 1;
  font-size: $font-micro;
  line-height: 34rpx;
  color: $ink-500;
}

/* ---------- 空态 ---------- */
.detail-empty { padding-top: $space-8; }

/* ---------- 底部操作 ---------- */
.action-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-2 $page-gutter calc(#{$space-2} + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  box-shadow: $shadow-bar;
}

.action-side {
  display: flex;
  flex: 0 0 72rpx;
  flex-direction: column;
  align-items: center;
  gap: 2rpx;
}

.action-side__label { font-size: 18rpx; color: $ink-500; }

.action-price {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: center;
}

.action-button {
  flex: 0 0 300rpx;
  width: 300rpx;
  height: 84rpx;
  line-height: 84rpx;
  margin: 0;
}
</style>
