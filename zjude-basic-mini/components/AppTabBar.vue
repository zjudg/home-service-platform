<template>
  <view class="ui-tabbar" :style="tabBarStyle">
    <view
      v-for="item in items"
      :key="item.key || item.name"
      class="ui-tabbar__item"
      :class="{ 'ui-tabbar__item--active': item.key === currentKey }"
      @click="openItem(item)"
    >
      <view class="ui-tabbar__icon-wrap">
        <image
          v-if="item.key === currentKey ? item.activeIcon : item.icon"
          class="ui-tabbar__icon"
          :src="assetUrl(item.key === currentKey ? item.activeIcon : item.icon)"
          mode="aspectFit"
        />
        <uni-icons
          v-else
          :type="fallbackIcon(item.key)"
          size="22"
          :color="item.key === currentKey ? activeColor : textColor"
        />
      </view>
      <text
        class="ui-tabbar__label"
        :style="{ color: item.key === currentKey ? activeColor : textColor }"
      >
        {{ item.name }}
      </text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'
import { showToast } from '@/utils/feedback'

defineProps({ currentKey: { type: String, default: '' } })

const configStore = useConfigStore()

// 后台未配置导航时给出稳定的默认四栏，避免整页空白
const defaultItems = [
  { key: 'home', name: '首页' },
  { key: 'category', name: '服务' },
  { key: 'cart', name: '订单' },
  { key: 'profile', name: '我的' }
]

const items = computed(() => {
  const configured = configStore.navigation.items || []
  return configured.length ? configured : defaultItems
})

// 后台「底部导航」装修可配色，未配置时回落到设计令牌的默认值
const tabBarStyle = computed(() => {
  const navigation = configStore.navigation || {}
  return {
    background: navigation.backgroundColor || '',
    borderTop: navigation.borderStyle || ''
  }
})

const activeColor = computed(() => configStore.navigation?.activeColor || '#e06a3d')
const textColor = computed(() => configStore.navigation?.textColor || '#a39b95')

const routeMap = {
  home: '/pages/home/index',
  category: '/pages/services/index',
  cart: '/pages/orders/index',
  profile: '/pages/user/index',
  user: '/pages/user/index'
}

const fallbackIcons = {
  home: 'home',
  category: 'list',
  cart: 'list',
  profile: 'person',
  user: 'person'
}

function fallbackIcon (key) {
  return fallbackIcons[key] || 'grid'
}

function openItem (item) {
  const path = routeMap[item.key] || item.path || item.link
  if (!path) return
  if (/^https?:\/\//.test(path)) {
    uni.setClipboardData({ data: path })
    showToast('链接已复制')
    return
  }
  const url = path.startsWith('/') ? path : `/${path}`
  // 用 redirectTo 替换当前页而非 reLaunch：
  // reLaunch 会关闭所有页面并重建页面栈，切换时必然出现明显白屏；
  // redirectTo 只替换栈顶，配合页面保留上一帧内容可显著减少闪烁。
  uni.redirectTo({
    url,
    fail: () => {
      // 页面栈已满（10 层）等异常情况再退回 reLaunch
      uni.reLaunch({ url })
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.ui-tabbar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 20;
  display: flex;
  height: calc(108rpx + env(safe-area-inset-bottom));
  padding: 10rpx 24rpx env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  box-shadow: $shadow-bar;
  border-radius: $radius-xl $radius-xl 0 0;
}

.ui-tabbar__item {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  transition: transform 0.15s ease;
}
.ui-tabbar__item:active { transform: scale(0.94); }

.ui-tabbar__icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  transition: transform 0.2s ease;
}

.ui-tabbar__item--active .ui-tabbar__icon-wrap { transform: translateY(-2rpx) scale(1.06); }

.ui-tabbar__icon { width: 44rpx; height: 44rpx; }

.ui-tabbar__label {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: $font-micro;
  font-weight: $weight-medium;
  color: $ink-400;
}

.ui-tabbar__item--active .ui-tabbar__label {
  color: $brand-500;
  font-weight: $weight-semibold;
}
</style>
