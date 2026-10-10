<template>
  <el-table
    :data="channels"
    border
    class="platform-table"
  >
    <el-table-column
      label="通知平台"
      prop="name"
      width="220"
    />
    <el-table-column
      label="平台说明"
      prop="description"
      min-width="420"
    />
    <el-table-column
      label="状态"
      width="180"
      align="center"
    >
      <template #default="{ row }">
        <el-switch
          :model-value="status[row.key]"
          active-value="1"
          inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          inline-prompt
          @change="$emit('toggle', row, $event)"
        />
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  channels: {
    type: Array,
    default: () => []
  },
  status: {
    type: Object,
    required: true
  }
})

defineEmits(['toggle'])
</script>

<style scoped lang="scss">
.platform-table {
  width: 100%;
}
</style>
