<template>
  <view
    v-if="visible"
    class="contact-sheet"
    @click="close"
  >
    <view
      class="contact-sheet__panel"
      @click.stop
    >
      <text class="contact-sheet__title">
        联系客服
      </text>

      <view
        v-if="hours"
        class="contact-sheet__hours"
      >
        <uni-icons type="calendar" size="13" color="#a39b95" />
        <text>{{ hours }}</text>
      </view>

      <image
        v-if="qrCode"
        class="contact-sheet__qr"
        :src="qrCode"
        mode="aspectFit"
        show-menu-by-longpress
      />
      <text
        v-if="qrCode"
        class="contact-sheet__qr-tip"
      >
        长按二维码识别添加客服
      </text>

      <button
        v-if="phone"
        class="primary-button contact-sheet__action"
        @click="call"
      >
        拨打电话 {{ phone }}
      </button>

      <text
        v-if="!phone && !qrCode"
        class="contact-sheet__empty"
      >
        客服联系方式暂未配置，请稍后再试
      </text>

      <text
        class="contact-sheet__cancel"
        @click="close"
      >
        取消
      </text>
    </view>
  </view>
</template>

<script setup>
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { showToast } from '@/utils/feedback'

const props = defineProps({
  visible: { type: Boolean, default: false },
  phone: { type: String, default: '' },
  hours: { type: String, default: '' },
  qrCode: { type: String, default: '' }
})

const emit = defineEmits(['update:visible'])

function close () {
  emit('update:visible', false)
}

function call () {
  uni.makePhoneCall({
    phoneNumber: props.phone,
    fail: () => showToast('拨号未完成')
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.contact-sheet {
  position: fixed;
  inset: 0;
  z-index: 900;
  display: flex;
  align-items: flex-end;
  background: rgba(0, 0, 0, 0.42);
}

.contact-sheet__panel {
  width: 100%;
  padding: $space-4 $page-gutter calc(#{$space-4} + env(safe-area-inset-bottom));
  border-radius: $radius-xl $radius-xl 0 0;
  background: $surface;
}

.contact-sheet__title {
  display: block;
  text-align: center;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
}

.contact-sheet__hours {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: $space-2;
  font-size: $font-micro;
  color: $ink-400;
}

.contact-sheet__qr {
  display: block;
  width: 320rpx;
  height: 320rpx;
  margin: $space-3 auto 0;
  border-radius: $radius-md;
}

.contact-sheet__qr-tip {
  display: block;
  margin-top: $space-2;
  text-align: center;
  font-size: $font-micro;
  color: $ink-400;
}

.contact-sheet__action { margin-top: $space-4; }

.contact-sheet__empty {
  display: block;
  margin-top: $space-4;
  text-align: center;
  font-size: $font-caption;
  color: $ink-400;
}

.contact-sheet__cancel {
  display: block;
  margin-top: $space-3;
  padding: $space-2;
  text-align: center;
  font-size: $font-caption;
  color: $ink-500;
}
</style>
