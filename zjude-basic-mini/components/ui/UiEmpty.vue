<template>
  <view class="ui-empty" :class="{ 'ui-empty--centered': centered }">
    <image
      v-if="props.image"
      class="ui-empty__image"
      :src="props.image"
      mode="aspectFit"
      :style="imageStyle"
    />
    <view
      v-else
      class="ui-empty__icon"
    >
      <uni-icons :type="icon" size="34" :color="iconColor" />
    </view>
    <text class="ui-empty__title">{{ title }}</text>
    <text v-if="description" class="ui-empty__desc">{{ description }}</text>
    <slot />
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'

const props = defineProps({
  icon: { type: String, default: 'info' },
  image: { type: String, default: '' },
  title: { type: String, default: '暂无内容' },
  description: { type: String, default: '' },
  tone: { type: String, default: 'brand' },
  centered: { type: Boolean, default: false },
  imageSize: { type: Number, default: 360 }
})

const iconColor = computed(() => (props.tone === 'brand' ? '#e06a3d' : '#a39b95'))
const imageStyle = computed(() => ({
  width: `${props.imageSize}rpx`,
  height: `${props.imageSize}rpx`
}))
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 420rpx;
  padding: $space-6 $space-4;
  text-align: center;
}

.ui-empty__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  background: $brand-50;
}

.ui-empty__image { flex: 0 0 auto; }

.ui-empty--centered {
  flex: 1;
  min-height: 0;
  padding-top: $space-3;
  padding-bottom: $space-3;
}

.ui-empty__title {
  margin-top: $space-3;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-700;
}

.ui-empty__desc {
  margin-top: $space-1;
  max-width: 440rpx;
  font-size: $font-caption;
  line-height: 1.6;
  color: $ink-400;
}
</style>
