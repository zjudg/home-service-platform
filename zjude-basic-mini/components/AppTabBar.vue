<template>
  <view v-if="items.length" class="app-tabbar" :style="barStyle">
    <view
      v-for="item in items"
      :key="item.key || item.name"
      class="tab-item"
      :class="{ 'tab-item--active': item.key === currentKey }"
      :style="item.key === currentKey ? activeStyle : inactiveStyle"
      @click="openItem(item)"
    >
      <image
        v-if="item.key === currentKey ? item.activeIcon : item.icon"
        class="tab-icon"
        :src="assetUrl(item.key === currentKey ? item.activeIcon : item.icon)"
        mode="aspectFit"
      />
      <uni-icons
        v-else
        :type="fallbackIcon(item.key)"
        size="24"
        :color="item.key === currentKey ? activeStyle.color : inactiveStyle.color"
      />
      <text class="tab-name">{{ item.name }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useConfigStore } from '@/stores/config'
import { assetUrl } from '@/utils/assets'

defineProps({ currentKey: { type: String, default: '' } })

const configStore = useConfigStore()
const items = computed(() => configStore.navigation.items || [])
const barStyle = computed(() => ({
  backgroundColor: configStore.navigation.backgroundColor || '#ffffff',
  borderTopColor: configStore.navigation.borderStyle === 'white' ? '#ffffff' : '#e8edf5'
}))
const activeStyle = computed(() => ({ color: configStore.navigation.activeColor || '#2f6fed' }))
const inactiveStyle = computed(() => ({ color: configStore.navigation.textColor || '#8c98a8' }))

const routeMap = {
  home: '/pages/home/index',
  category: '/pages/category/index',
  cart: '/pages/cart/index',
  profile: '/pages/user/index',
  user: '/pages/user/index'
}

const fallbackIcons = {
  home: 'home',
  category: 'list',
  cart: 'cart',
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
    uni.showToast({ title: '链接已复制', icon: 'none' })
    return
  }
  uni.reLaunch({ url: path.startsWith('/') ? path : `/${path}` })
}
</script>

<style lang="scss" scoped>
.app-tabbar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 20; display: flex; height: calc(112rpx + env(safe-area-inset-bottom)); padding-bottom: env(safe-area-inset-bottom); border-top: 1rpx solid; box-shadow: 0 -8rpx 28rpx rgba(31, 50, 82, .06); }
.tab-item { display: flex; flex: 1; min-width: 0; flex-direction: column; align-items: center; justify-content: center; padding: 12rpx 8rpx 8rpx; font-size: 22rpx; }
.tab-icon { width: 46rpx; height: 46rpx; margin-bottom: 6rpx; }
.tab-name { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
