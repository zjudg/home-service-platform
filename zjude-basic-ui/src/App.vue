<template>
  <router-view />
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'
import { hideAppLoader } from '@/utils/appLoader'

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
    // 至少展示约 2 秒的加载进度，再淡出首屏遮罩
    hideAppLoader()
  })
})
</script>
