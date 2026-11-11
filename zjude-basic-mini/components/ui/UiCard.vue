<template>
  <view class="ui-card" :class="[`ui-card--${variant}`, { 'ui-card--flush': flush }]">
    <slot name="header">
      <view v-if="title || $slots.extra" class="ui-card__head">
        <view class="ui-card__title-wrap">
          <text v-if="title" class="ui-card__title">{{ title }}</text>
          <text v-if="subtitle" class="ui-card__subtitle">{{ subtitle }}</text>
        </view>
        <view v-if="$slots.extra" class="ui-card__extra"><slot name="extra" /></view>
      </view>
    </slot>
    <slot />
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  variant: { type: String, default: 'elevated' },
  flush: { type: Boolean, default: false }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-card {
  padding: $space-4;
  border-radius: $radius-lg;
  background: $surface;
  box-shadow: $shadow-card;
}

.ui-card--flat {
  box-shadow: none;
  background: $surface;
}

.ui-card--sunken {
  box-shadow: none;
  background: $bg-sunken;
  border-radius: $radius-md;
}

.ui-card--flush { padding: 0; overflow: hidden; }

.ui-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $space-3;
}

.ui-card__title-wrap { display: flex; flex-direction: column; min-width: 0; }
.ui-card__title {
  font-size: $font-title;
  font-weight: $weight-bold;
  color: $ink-900;
}
.ui-card__subtitle {
  margin-top: 6rpx;
  font-size: $font-micro;
  color: $ink-400;
}
.ui-card__extra { flex: 0 0 auto; margin-left: $space-2; }
</style>
