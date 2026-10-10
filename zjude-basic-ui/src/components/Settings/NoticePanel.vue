<template>
  <el-collapse
    v-model="activeNames"
    class="settings-notice"
  >
    <el-collapse-item
      :title="title"
      name="notice"
    >
      <slot />
    </el-collapse-item>
  </el-collapse>
</template>

<script setup>
const activeNames = ref(['notice'])

defineProps({
  title: {
    type: String,
    default: '操作提示'
  }
})
</script>

<style scoped lang="scss">
.settings-notice {
  margin-bottom: 18px;
}

.settings-notice :deep(.el-collapse-item__content) {
  color: var(--el-text-color-secondary);
  line-height: 26px;
}
</style>
