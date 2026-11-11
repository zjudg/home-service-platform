<template>
  <view class="ui-price" :class="[`ui-price--${size}`, { 'ui-price--muted': muted }]">
    <text class="ui-price__symbol">¥</text>
    <text class="ui-price__value">{{ value }}</text>
    <text v-if="unit" class="ui-price__unit">/{{ unit }}</text>
    <text v-if="original" class="ui-price__original">¥{{ original }}</text>
  </view>
</template>

<script setup>
defineProps({
  value: { type: [String, Number], default: 0 },
  unit: { type: String, default: '' },
  original: { type: [String, Number], default: '' },
  size: { type: String, default: 'md' },
  muted: { type: Boolean, default: false }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-price {
  display: flex;
  align-items: baseline;
  color: $brand-500;
  font-weight: $weight-heavy;
}

.ui-price--muted { color: $ink-700; }

.ui-price__symbol { font-size: $font-micro; font-weight: $weight-semibold; margin-right: 2rpx; }
.ui-price__value { font-size: 36rpx; letter-spacing: -0.5rpx; }
.ui-price__unit { margin-left: 2rpx; font-size: $font-micro; font-weight: $weight-regular; color: $ink-400; }
.ui-price__original {
  margin-left: $space-1;
  font-size: $font-micro;
  font-weight: $weight-regular;
  color: $ink-300;
  text-decoration: line-through;
}

.ui-price--lg .ui-price__value { font-size: $font-display; }
.ui-price--sm .ui-price__value { font-size: $font-body; }
</style>
