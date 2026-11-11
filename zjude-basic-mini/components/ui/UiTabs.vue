<template>
  <view class="ui-tabs" :class="{ 'ui-tabs--centered': centered }">
    <scroll-view
      scroll-x
      class="ui-tabs__scroll"
      :show-scrollbar="false"
      :enhanced="true"
      :bounces="false"
    >
      <view class="ui-tabs__track">
        <view
          v-for="item in items"
          :key="item.value"
          class="ui-tabs__item"
          :class="{ 'ui-tabs__item--active': item.value === modelValue }"
          @click="$emit('update:modelValue', item.value)"
        >
          <text>{{ item.label }}</text>
          <text v-if="item.badge" class="ui-tabs__badge">{{ item.badge }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  modelValue: { type: [String, Number], default: '' },
  centered: { type: Boolean, default: false }
})
defineEmits(['update:modelValue'])
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-tabs { margin: 0 (-$page-gutter); }

.ui-tabs__scroll {
  white-space: nowrap;
  scrollbar-width: none;
}

.ui-tabs__scroll::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
  background: transparent;
}

.ui-tabs__track {
  display: inline-flex;
  gap: $space-1;
  padding: 0 $page-gutter;
}

.ui-tabs--centered .ui-tabs__scroll { width: 100%; }

.ui-tabs--centered .ui-tabs__track {
  display: flex;
  gap: $space-2;
  justify-content: space-between;
  width: 100%;
  box-sizing: border-box;
}

.ui-tabs--centered .ui-tabs__item {
  flex: 1;
  justify-content: center;
}

.ui-tabs__item {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 0 $space-3;
  height: 64rpx;
  border-radius: $radius-pill;
  background: $surface;
  color: $ink-500;
  font-size: $font-caption;
  font-weight: $weight-medium;
  transition: all 0.2s ease;
}

.ui-tabs__item--active {
  background: $brand-500;
  color: #fff;
  font-weight: $weight-semibold;
  box-shadow: 0 4rpx 14rpx rgba(224, 106, 61, 0.24);
}

.ui-tabs__badge {
  font-size: $font-micro;
  opacity: 0.75;
}
</style>
