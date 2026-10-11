<template>
  <el-table
    :data="scenes"
    border
    class="notification-table"
  >
    <el-table-column
      label="通知类型"
      prop="name"
      width="150"
    />
    <el-table-column
      label="通知场景说明"
      prop="description"
      width="180"
      show-overflow-tooltip
    />
    <el-table-column
      v-for="channel in channels"
      :key="channel.key"
      :label="channel.name"
      min-width="270"
    >
      <template #default="{ row }">
        <div
          v-if="row.channels.includes(channel.key)"
          class="channel-actions"
        >
          <el-button
            :type="getConfig(row, channel.key).enabled === '1' ? 'success' : 'info'"
            size="small"
            @click="$emit('toggle', row, channel.key)"
          >
            {{ getConfig(row, channel.key).enabled === '1' ? '已开启' : '已关闭' }}
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="$emit('record', row, channel)"
          >
            通知记录
          </el-button>
          <el-button
            type="primary"
            size="small"
            @click="$emit('config', row, channel)"
          >
            设置
          </el-button>
        </div>
        <span v-else>-</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  scenes: {
    type: Array,
    default: () => []
  },
  channels: {
    type: Array,
    default: () => []
  },
  getConfig: {
    type: Function,
    required: true
  }
})

defineEmits(['toggle', 'record', 'config'])
</script>

<style scoped lang="scss">
.notification-table {
  width: 100%;
}

.channel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.channel-actions :deep(.el-button) {
  margin: 0;
}
</style>
