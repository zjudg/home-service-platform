<template>
  <!-- Toast -->
  <view v-if="toast.visible" class="ui-toast" :class="`ui-toast--${toast.type}`">
    <view class="ui-toast__icon">
      <uni-icons :type="toastIcon" size="18" :color="toastIconColor" />
    </view>
    <text class="ui-toast__text">{{ toast.message }}</text>
  </view>

  <!-- Dialog -->
  <view v-if="dialog.visible" class="ui-dialog-mask" @click.self="onCancel">
    <view class="ui-dialog">
      <view class="ui-dialog__head">
        <text class="ui-dialog__title">{{ dialog.title }}</text>
      </view>

      <text v-if="dialog.content" class="ui-dialog__content">{{ dialog.content }}</text>

      <view class="ui-dialog__actions">
        <button
          v-if="dialog.showCancel"
          class="ui-dialog__btn ui-dialog__btn--cancel"
          @click="onCancel"
        >{{ dialog.cancelText }}</button>
        <button
          class="ui-dialog__btn ui-dialog__btn--confirm"
          :class="{ 'ui-dialog__btn--danger': dialog.danger }"
          @click="onConfirm"
        >{{ dialog.confirmText }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { dialog, resolveDialog, toast } from '@/utils/feedback'

const ICONS = {
  info: { icon: 'info-filled', color: '#4a7fb5' },
  success: { icon: 'checkbox-filled', color: '#2f9e6b' },
  warning: { icon: 'info-filled', color: '#d98a1f' },
  error: { icon: 'clear-filled', color: '#d9503f' }
}

const toastIcon = computed(() => (ICONS[toast.value.type] || ICONS.info).icon)
const toastIconColor = computed(() => (ICONS[toast.value.type] || ICONS.info).color)

function onConfirm () { resolveDialog(true) }
function onCancel () { resolveDialog(false) }
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

/* ---------- Toast ---------- */
.ui-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  z-index: 300;
  display: flex;
  align-items: center;
  gap: $space-2;
  max-width: 560rpx;
  padding: $space-3 $space-4;
  transform: translate(-50%, -50%);
  border-radius: $radius-lg;
  background: rgba(31, 28, 26, 0.9);
  backdrop-filter: blur(12rpx);
  -webkit-backdrop-filter: blur(12rpx);
  box-shadow: $shadow-float;
  animation: toast-in 0.22s ease-out;
}

@keyframes toast-in {
  from { opacity: 0; transform: translate(-50%, -46%) scale(0.96); }
  to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
}

.ui-toast__icon {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.ui-toast__text {
  color: #fff;
  font-size: $font-caption;
  line-height: 1.5;
}

/* ---------- Dialog ---------- */
.ui-dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 310;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 72rpx;
  background: $overlay;
  animation: mask-in 0.18s ease-out;
}

@keyframes mask-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.ui-dialog {
  width: 100%;
  padding: $space-6 $space-4 $space-3;
  border-radius: $radius-xl;
  background: $surface;
  box-shadow: $shadow-float;
  animation: dialog-in 0.22s cubic-bezier(0.2, 0.9, 0.3, 1.1);
}

@keyframes dialog-in {
  from { opacity: 0; transform: translateY(24rpx) scale(0.94); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.ui-dialog__head {
  display: flex;
  justify-content: center;
}

.ui-dialog__title {
  font-size: $font-title;
  font-weight: $weight-bold;
  color: $ink-900;
  text-align: center;
}

.ui-dialog__content {
  display: block;
  margin-top: $space-2;
  font-size: $font-caption;
  line-height: 1.7;
  color: $ink-500;
  text-align: center;
}

.ui-dialog__actions {
  display: flex;
  gap: $space-2;
  margin-top: $space-4;
}

.ui-dialog__btn {
  flex: 1;
  height: 84rpx;
  padding: 0;
  border: 0;
  border-radius: $radius-pill;
  font-size: $font-caption;
  font-weight: $weight-semibold;
  line-height: 84rpx;
}
.ui-dialog__btn::after { border: 0; }
.ui-dialog__btn:active { transform: scale(0.98); }

.ui-dialog__btn--cancel {
  background: $bg-sunken;
  color: $ink-500;
}

.ui-dialog__btn--confirm {
  background: $brand-500;
  color: #fff;
  box-shadow: 0 6rpx 18rpx rgba(224, 106, 61, 0.22);
}

.ui-dialog__btn--danger {
  background: $danger;
  box-shadow: 0 6rpx 18rpx rgba(217, 80, 63, 0.22);
}
</style>
