<template><view class="page"><view class="redirect-state">正在打开服务列表...</view></view></template>
<script setup>
import { onMounted } from 'vue'
onMounted(() => { const options = getCurrentPages().at(-1)?.options || {}; const query = options.categoryId ? `?categoryId=${encodeURIComponent(options.categoryId)}` : ''; uni.redirectTo({ url: `/pages/services/index${query}` }) })
</script>
<style scoped>.redirect-state{padding:160rpx 0;text-align:center;color:#82918c}</style>
