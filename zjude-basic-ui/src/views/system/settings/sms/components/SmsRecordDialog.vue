<template>
  <el-dialog
    :model-value="modelValue"
    title="发送记录"
    width="620px"
    append-to-body
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-descriptions
      v-if="record"
      :column="1"
      border
    >
      <el-descriptions-item label="短信标题">
        {{ record.title }}
      </el-descriptions-item>
      <el-descriptions-item label="短信内容">
        {{ record.content }}
      </el-descriptions-item>
      <el-descriptions-item label="发送手机号">
        {{ record.mobile }}
      </el-descriptions-item>
      <el-descriptions-item label="发送状态">
        {{ record.statusText }}
      </el-descriptions-item>
      <el-descriptions-item label="发送时间">
        {{ record.sendTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: null
  }
})

defineEmits(['update:modelValue'])
</script>
