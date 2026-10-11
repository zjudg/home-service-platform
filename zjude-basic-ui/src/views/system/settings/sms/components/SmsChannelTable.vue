<template>
  <el-table
    v-loading="loading"
    :data="rows"
    border
    class="channel-table"
  >
    <el-table-column
      label="序号"
      width="72"
      align="center"
    >
      <template #default>
        1
      </template>
    </el-table-column>
    <el-table-column
      label="短信通道"
      prop="channel"
      min-width="220"
      align="center"
    />
    <el-table-column
      label="通道描述"
      prop="description"
      min-width="360"
      align="center"
    />
    <el-table-column
      label="状态"
      width="220"
      align="center"
    >
      <template #default="{ row }">
        <el-tag
          :type="row.enabled ? 'success' : 'info'"
          effect="plain"
        >
          {{ row.enabled ? '开启' : '关闭' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column
      label="操作"
      width="180"
      align="center"
    >
      <template #default>
        <el-button
          type="primary"
          @click="$emit('config')"
        >
          配置
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  rows: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['config'])
</script>

<style scoped lang="scss">
.channel-table {
  width: 100%;
}
</style>
