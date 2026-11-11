<template>
  <view
    class="ui-skeleton"
    :class="`ui-skeleton--${variant}`"
  >
    <!-- 卡片列表骨架：工作台、接单池、订单列表共用 -->
    <template v-if="variant === 'card-list'">
      <view
        v-for="index in count"
        :key="index"
        class="sk-card"
      >
        <view class="sk-row sk-row--head">
          <view class="skeleton sk-line sk-line--sm" />
          <view class="skeleton sk-pill" />
        </view>
        <view class="skeleton sk-line sk-line--lg" />
        <view class="sk-row">
          <view class="skeleton sk-line sk-line--md" />
        </view>
        <view class="sk-row">
          <view class="skeleton sk-line sk-line--md" />
        </view>
        <view class="sk-row sk-row--foot">
          <view class="skeleton sk-line sk-line--sm" />
          <view class="skeleton sk-button" />
        </view>
      </view>
    </template>

    <!-- 钱包骨架：余额卡 + 流水列表 -->
    <template v-else-if="variant === 'wallet'">
      <view class="sk-balance">
        <view class="skeleton sk-line sk-line--xs sk-on-brand" />
        <view class="skeleton sk-line sk-line--xl sk-on-brand" />
        <view class="sk-balance-meta">
          <view
            v-for="index in 3"
            :key="index"
            class="skeleton sk-line sk-line--xs sk-on-brand"
          />
        </view>
      </view>
      <view
        v-for="index in count"
        :key="index"
        class="sk-card sk-card--row"
      >
        <view class="sk-grow">
          <view class="skeleton sk-line sk-line--md" />
          <view class="skeleton sk-line sk-line--sm" />
        </view>
        <view class="skeleton sk-line sk-line--amount" />
      </view>
    </template>

    <!-- 详情骨架：状态卡 + 若干信息块 -->
    <template v-else>
      <view class="sk-card">
        <view class="skeleton sk-line sk-line--lg" />
        <view class="skeleton sk-line sk-line--md" />
      </view>
      <view
        v-for="index in count"
        :key="index"
        class="sk-card"
      >
        <view class="skeleton sk-line sk-line--sm" />
        <view class="skeleton sk-line sk-line--md" />
        <view class="skeleton sk-line sk-line--md" />
      </view>
    </template>
  </view>
</template>

<script setup>
/**
 * 页面骨架屏。
 *
 * 用途：替代整页 loading，在数据返回前给出与真实布局同构的占位，
 * 避免切换 tab 时出现"白屏 → 内容突现"的跳动。
 *
 * 注意：骨架只用于首次加载；已有数据时的刷新应静默进行，不要再切回骨架。
 */
defineProps({
  /** 变体：card-list / wallet / detail */
  variant: { type: String, default: 'card-list' },
  /** 占位卡片数量 */
  count: { type: Number, default: 3 }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-skeleton { padding-bottom: $space-4; }

.sk-card {
  padding: $space-4;
  margin-bottom: $space-3;
  background: $surface;
  border-radius: $radius-md;
}

.sk-card--row {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.sk-grow { flex: 1; }

.sk-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  margin-top: $space-2;

  &--head {
    justify-content: space-between;
    margin-top: 0;
  }

  &--foot {
    justify-content: space-between;
    margin-top: $space-3;
    padding-top: $space-3;
    border-top: 2rpx solid $line-soft;
  }
}

.sk-line {
  height: 28rpx;
  border-radius: $radius-sm;

  &--xs { width: 140rpx; height: 22rpx; }
  &--sm { width: 180rpx; height: 24rpx; }
  &--md { width: 70%; }
  &--lg { width: 55%; height: 34rpx; margin-top: $space-2; }
  &--xl { width: 60%; height: 64rpx; margin-top: $space-3; }
  &--amount { width: 120rpx; height: 34rpx; }
}

.sk-pill { width: 96rpx; height: 34rpx; border-radius: $radius-pill; }

.sk-button { width: 140rpx; height: 56rpx; border-radius: $radius-pill; }

.sk-balance {
  padding: $space-6 $space-4;
  margin-bottom: $space-4;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $brand-400, $brand-500);
}

.sk-balance-meta {
  display: flex;
  justify-content: space-between;
  gap: $space-3;
  margin-top: $space-6;
  padding-top: $space-3;
  border-top: 2rpx solid rgba(255, 255, 255, 0.25);

  .sk-line { flex: 1; }
}

/* 卡片底色的骨架用半透明白，避免在彩色背景上突兀 */
.sk-on-brand { background: rgba(255, 255, 255, 0.35); }
</style>
