<template>
  <el-form-item label="场景变量">
    <div class="variable-buttons">
      <el-button
        v-for="item in variables"
        :key="item[1]"
        size="small"
        @click="$emit('insert', item[1])"
      >
        {{ item[0] }}
      </el-button>
      <div class="setting-tip">
        系统在当前场景预定义好的通知变量
      </div>
    </div>
  </el-form-item>
</template>

<script setup>
defineProps({
  variables: {
    type: Array,
    default: () => []
  }
})

defineEmits(['insert'])
</script>

<style scoped lang="scss">
.variable-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.variable-buttons :deep(.el-button) {
  margin: 0;
}

.setting-tip {
  width: 100%;
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
