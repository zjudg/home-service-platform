<template>
  <!-- 占位：让页面内容从导航栏下方开始 -->
  <view
    class="ui-navbar__placeholder"
    :style="{ height: `${totalHeight}px` }"
  />

  <view
    class="ui-navbar"
    :class="[`ui-navbar--${tone}`, { 'ui-navbar--solid': solid }]"
    :style="{ paddingTop: `${statusBarHeight}px`, height: `${totalHeight}px` }"
  >
    <view class="ui-navbar__bar" :style="{ height: `${navBarHeight}px` }">
      <!-- 左侧：只在需要返回时出现 -->
      <view class="ui-navbar__side">
        <view v-if="back" class="ui-navbar__back" @click="handleBack">
          <uni-icons type="left" size="20" :color="iconColor" />
        </view>
      </view>

      <view class="ui-navbar__center">
        <slot name="title">
          <text class="ui-navbar__title">{{ title }}</text>
        </slot>
      </view>

      <view class="ui-navbar__side ui-navbar__side--right">
        <slot name="right" />
      </view>
    </view>

    <slot name="bottom" />
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'

const props = defineProps({
  title: { type: String, default: '' },
  back: { type: Boolean, default: false },
  solid: { type: Boolean, default: true },
  tone: { type: String, default: 'default' }
})

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 与微信胶囊按钮对齐：导航栏高度 = 胶囊高度 + 上下留白
function resolveNavBarHeight () {
  if (process.env.UNI_PLATFORM === 'mp-weixin') {
    try {
      const rect = uni.getMenuButtonBoundingClientRect()
      if (rect && rect.height) {
        return rect.height + (rect.top - statusBarHeight.value) * 2
      }
    } catch (error) { /* 非小程序环境回退到默认值 */ }
  }
  return 44
}

const navBarHeight = ref(resolveNavBarHeight())
const totalHeight = computed(() => statusBarHeight.value + navBarHeight.value)
const iconColor = computed(() => (props.tone === 'light' ? '#ffffff' : '#1f1c1a'))

function handleBack () {
  const pages = getCurrentPages()
  if (pages.length > 1) return uni.navigateBack()
  uni.reLaunch({ url: '/pages/home/index' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

/* 固定在顶部，背景与系统暖灰底一致 */
.ui-navbar {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 100;
  box-sizing: border-box;
  background: $bg;
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

/* 所有二级页面的主体与 TopBar 保持统一呼吸感，避免内容贴在导航栏下方。 */
.ui-navbar__placeholder { margin-bottom: 24rpx; }

.ui-navbar--solid {
  background: $navbar-drop;
  backdrop-filter: blur(24rpx);
  -webkit-backdrop-filter: blur(24rpx);
  box-shadow: 0 1rpx 0 rgba(236, 230, 225, 0.9);
}

.ui-navbar--light.ui-navbar--solid {
  background: rgba(31, 28, 26, 0.55);
  box-shadow: none;
}

.ui-navbar__bar {
  display: flex;
  align-items: center;
  padding: 0 $space-2;
}

/* 左右两侧固定等宽，保证中间标题真正居中 */
.ui-navbar__side {
  display: flex;
  flex: 0 0 76rpx;
  align-items: center;
  width: 76rpx;
  min-width: 76rpx;
}

.ui-navbar__side--right { justify-content: flex-end; }

.ui-navbar__back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: $radius-pill;
  background: rgba(255, 255, 255, 0.7);
}
.ui-navbar--light .ui-navbar__back { background: rgba(31, 28, 26, 0.32); }
.ui-navbar__back:active { opacity: 0.7; }

.ui-navbar__center {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: center;
  justify-content: center;
  padding: 0 $space-1;
}

.ui-navbar__title {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-body;
  font-weight: $weight-semibold;
  color: $ink-900;
  text-align: center;
}

.ui-navbar--light .ui-navbar__title { color: #fff; }
</style>
