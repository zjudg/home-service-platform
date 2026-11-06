<template>
  <view class="review-page">
    <UiNavbar :back="true" title="服务评价" />

    <view class="score-card card">
      <text class="score-label">本次服务满意度</text>
      <view class="stars">
        <text
          v-for="item in 5"
          :key="item"
          class="star"
          :class="{ 'star--active': item <= rating }"
          @click="rating = item"
        >★</text>
      </view>
      <text class="score-text">{{ ratingText }}</text>
    </view>

    <view class="content-card card">
      <text class="content-label">评价内容</text>
      <textarea
        v-model="content"
        class="textarea"
        maxlength="300"
        placeholder="服务是否准时？态度如何？说说您的真实感受，帮助我们做得更好"
        placeholder-class="textarea-placeholder"
      />
      <text class="counter">{{ content.length }}/300</text>
    </view>

    <view class="tags">
      <text
        v-for="tag in quickTags"
        :key="tag"
        class="tag"
        :class="{ 'tag--active': content.includes(tag) }"
        @click="toggleTag(tag)"
      >{{ tag }}</text>
    </view>

    <button class="primary-button submit" :loading="submitting" @click="submit">提交评价</button>
  </view>
  <AppFeedback />
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import { submitReview } from '@/api/service'
import { currentPageUrl, ensureLoggedIn } from '@/utils/guard'
import { toastSuccess, showToast } from '@/utils/feedback'

const rating = ref(5)
const content = ref('')
const submitting = ref(false)
const serviceName = ref('本次家政服务')
const orderId = ref('')

const quickTags = ['准时到达', '态度很好', '干活细致', '价格公道']

const ratingText = computed(() => {
  return ({ 5: '非常满意', 4: '满意', 3: '一般', 2: '不太满意', 1: '不满意' })[rating.value] || ''
})

onMounted(() => {
  if (!ensureLoggedIn(currentPageUrl())) return
  orderId.value = getCurrentPages().at(-1)?.options?.id || ''
})

function toggleTag (tag) {
  if (content.value.includes(tag)) {
    content.value = content.value.replace(tag, '').replace(/，{2,}/g, '，').trim()
    return
  }
  content.value = content.value ? `${content.value}，${tag}` : tag
}

async function submit () {
  if (!ensureLoggedIn(currentPageUrl())) return
  if (!orderId.value) return showToast('缺少订单信息')
  if (!content.value.trim()) return showToast('请填写评价内容')
  submitting.value = true
  try {
    await submitReview({ orderId: orderId.value, rating: rating.value, content: content.value })
    toastSuccess('评价成功')
    setTimeout(() => uni.navigateBack(), 600)
  } catch (e) {
    showToast(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.review-page {
  min-height: 100vh;
  padding: 0 $page-gutter calc(#{$space-6} + env(safe-area-inset-bottom));
  background: $bg;
}

.review-head {
  margin: 0 (-$page-gutter);
  padding: $space-4 $page-gutter $space-8;
  background: linear-gradient(180deg, $brand-100 0%, $brand-50 100%);
}

.head-title {
  display: block;
  font-size: 42rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: -0.5rpx;
}

.head-sub { display: block; margin-top: 6rpx; font-size: $font-caption; color: $ink-500; }

/* ---------- 评分 ---------- */
.score-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: $space-3;
  padding: $space-4;
}

.score-label { font-size: $font-caption; color: $ink-500; }

.stars { display: flex; gap: $space-2; margin: $space-3 0 $space-2; }

.star {
  font-size: 60rpx;
  line-height: 1;
  color: $line;
  transition: transform 0.15s ease, color 0.15s ease;
}
.star:active { transform: scale(1.15); }
.star--active { color: $warning; }

.score-text { font-size: $font-body; font-weight: $weight-semibold; color: $warning; }

/* ---------- 内容 ---------- */
.content-card { margin-top: $space-2; padding: $space-4; }

.content-label { display: block; font-size: $font-body; font-weight: $weight-semibold; color: $ink-900; }

.textarea {
  width: 100%;
  height: 220rpx;
  margin-top: $space-2;
  padding: $space-3;
  border-radius: $radius-md;
  background: $bg-sunken;
  font-size: $font-caption;
  line-height: 1.7;
  color: $ink-900;
}

.textarea-placeholder { color: $ink-400; }

.counter { display: block; margin-top: $space-1; text-align: right; font-size: $font-micro; color: $ink-300; }

/* ---------- 快捷标签 ---------- */
.tags { display: flex; flex-wrap: wrap; gap: $space-1; margin-top: $space-3; }

.tag {
  padding: 0 $space-3;
  height: 60rpx;
  line-height: 60rpx;
  border-radius: $radius-pill;
  background: $surface;
  color: $ink-500;
  font-size: $font-micro;
}

.tag--active {
  background: $brand-100;
  color: $brand-600;
  font-weight: $weight-medium;
}

.submit { width: 100%; height: 92rpx; line-height: 92rpx; margin-top: $space-6; }
</style>
