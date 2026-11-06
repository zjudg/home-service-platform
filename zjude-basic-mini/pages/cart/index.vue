<template>
  <view class="cart-page has-tabbar">
    <UiNavbar :back="false" title="购物车" />


    <UiEmpty
      icon="cart"
      :title="emptyTitle || '还没有添加服务'"
      :description="emptyDescription || '浏览服务并预约，让生活轻松一点'"
    >
      <button class="primary-button empty-action" @click="browseServices">去找服务</button>
    </UiEmpty>

    <AppFeedback />
    <AppTabBar current-key="cart" />
  </view>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppTabBar from '@/components/AppTabBar.vue'
import AppFeedback from '@/components/AppFeedback.vue'
import UiNavbar from '@/components/ui/UiNavbar.vue'
import UiEmpty from '@/components/ui/UiEmpty.vue'
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
const cartConfig = computed(() => configStore.category?.cart || {})
const pageTitle = computed(() => findLabel('cart') || configStore.appTitle)
const cartSubtitle = computed(() => cartConfig.value.subtitle || '')
const emptyTitle = computed(() => cartConfig.value.emptyTitle || '')
const emptyDescription = computed(() => cartConfig.value.emptyDescription || '')

onMounted(async () => {
  await configStore.load().catch(() => {})
})

function findLabel (key) {
  return configStore.navigation.items.find(item => item.key === key)?.name
}

function browseServices () {
  uni.navigateTo({ url: '/pages/services/index' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.cart-page { min-height: 100vh; background: $bg; }

.cart-head {
  padding: $space-3 $page-gutter $space-3;
  background: linear-gradient(180deg, #fdece4 0%, #fff7f3 70%, $bg 100%);
}

.head-title {
  display: block;
  font-size: 44rpx;
  font-weight: $weight-heavy;
  color: $ink-900;
  letter-spacing: -1rpx;
}

.head-sub { display: block; margin-top: 6rpx; font-size: $font-micro; color: $ink-500; }

.empty-action { width: 280rpx; margin-top: $space-4; line-height: 80rpx; }
</style>
