<template><view class="page"><view class="redirect-state">正在打开预约页面...</view></view></template>
<script setup>
import { onMounted } from 'vue'
onMounted(() => { const options = getCurrentPages().at(-1)?.options || {}; const query = options.serviceId ? `?id=${encodeURIComponent(options.serviceId)}` : ''; uni.redirectTo({ url: `/pages/booking/index${query}` }) })
</script>
<style scoped>.redirect-state{padding:160rpx 0;text-align:center;color:#82918c}</style>
