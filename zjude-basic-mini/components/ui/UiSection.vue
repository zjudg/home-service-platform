<template>
  <view class="ui-section">
    <view class="ui-section__head">
      <view class="ui-section__titles">
        <view class="ui-section__title-row">
          <view class="ui-section__mark" />
          <text class="ui-section__title">{{ title }}</text>
        </view>
        <text v-if="subtitle" class="ui-section__subtitle">{{ subtitle }}</text>
      </view>
      <view v-if="more" class="ui-section__more" @click="$emit('more')">
        <text>{{ more }}</text>
        <uni-icons type="right" size="12" color="#a39b95" />
      </view>
    </view>
    <slot />
  </view>
</template>

<script setup>
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'

defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  more: { type: String, default: '' }
})
defineEmits(['more'])
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-section { margin-top: $space-4; }

.ui-section__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: $space-3;
}

.ui-section__titles { display: flex; flex-direction: column; min-width: 0; }

.ui-section__title-row { display: flex; align-items: center; }

.ui-section__mark {
  width: 6rpx;
  height: 28rpx;
  margin-right: $space-2;
  border-radius: $radius-pill;
  background: $brand-500;
}

.ui-section__title {
  font-size: $font-title;
  font-weight: $weight-bold;
  color: $ink-900;
}

.ui-section__subtitle {
  margin-top: 8rpx;
  margin-left: 14rpx;
  font-size: $font-micro;
  color: $ink-400;
}

.ui-section__more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: $font-caption;
  color: $ink-400;
}
</style>
