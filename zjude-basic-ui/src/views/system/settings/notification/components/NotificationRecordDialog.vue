<template>
  <el-dialog
    :model-value="modelValue"
    title="通知记录"
    width="760px"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-table
      :data="[]"
      border
    >
      <el-table-column
        label="通知场景"
        prop="scene"
      />
      <el-table-column
        label="通知内容"
        prop="content"
      />
      <el-table-column
        label="发送状态"
        prop="status"
      />
      <el-table-column
        label="发送时间"
        prop="sendTime"
      />
    </el-table>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue'])
</script>
