<template><view class="page"><view class="redirect-state">正在打开订单...</view></view></template>
<script setup>
import { onMounted } from 'vue'
onMounted(() => uni.redirectTo({ url: '/pages/orders/index' }))
</script>
<style scoped>.redirect-state{padding:160rpx 0;text-align:center;color:#82918c}</style>
