<template><view class="page"><view class="redirect-state">正在打开服务详情...</view></view></template>
<script setup>
import { onMounted } from 'vue'
onMounted(() => { const id = getCurrentPages().at(-1)?.options?.id || ''; uni.redirectTo({ url: `/pages/service-detail/index?id=${encodeURIComponent(id)}` }) })
</script>
<style scoped>.redirect-state{padding:160rpx 0;text-align:center;color:#82918c}</style>
