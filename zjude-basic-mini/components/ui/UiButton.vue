<template>
  <button
    class="ui-button"
    :class="[`ui-button--${variant}`, `ui-button--${size}`, { 'ui-button--block': block }]"
    :loading="loading"
    :disabled="disabled || loading"
    hover-class="ui-button--pressed"
    :hover-stay-time="80"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  block: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0 $space-4;
  border: 0;
  border-radius: $radius-pill;
  font-size: $font-button;
  font-weight: $weight-semibold;
  line-height: 1.2;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.ui-button::after { border: 0; }
.ui-button--block { display: flex; width: 100%; }

.ui-button--sm { height: 64rpx; padding: 0 $space-3; font-size: $font-caption; }
.ui-button--md { height: 88rpx; }
.ui-button--lg { height: 96rpx; font-size: 31rpx; }

.ui-button--primary {
  color: #fff;
  background: $brand-500;
  box-shadow: 0 6rpx 18rpx rgba(224, 106, 61, 0.24);
}
.ui-button--secondary { color: $brand-600; background: $brand-100; }
.ui-button--ghost { color: $ink-700; background: $surface; border: 1rpx solid $line; box-shadow: none; }
.ui-button--text { color: $brand-600; background: transparent; padding: 0 $space-2; box-shadow: none; }

.ui-button--pressed { transform: scale(0.97); opacity: 0.9; }
.ui-button[disabled] { opacity: 0.45; box-shadow: none; }
</style>
