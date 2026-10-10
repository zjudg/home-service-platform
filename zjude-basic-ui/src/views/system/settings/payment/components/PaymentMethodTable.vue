<template>
  <el-table
    v-loading="loading"
    :data="rows"
    border
    class="payment-table"
  >
    <el-table-column
      label="支付方式"
      prop="method"
      min-width="170"
    />
    <el-table-column
      label="图标"
      width="150"
      align="center"
    >
      <template #default="{ row }">
        <div class="payment-icon">
          <el-image
            v-if="row.icon"
            :src="resolveImage(row.icon)"
            fit="contain"
          />
          <span
            v-else
            class="payment-icon__fallback"
            :class="`payment-icon__fallback--${row.key}`"
          >
            <el-icon>
              <component :is="row.iconComponent" />
            </el-icon>
          </span>
        </div>
      </template>
    </el-table-column>
    <el-table-column
      label="名称"
      prop="name"
      min-width="200"
    />
    <el-table-column
      label="状态"
      width="150"
      align="center"
    >
      <template #default="{ row }">
        <el-tag
          :type="row.enabled ? 'success' : 'info'"
          effect="plain"
        >
          {{ row.enabled ? '启用' : '关闭' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column
      label="排序"
      prop="sort"
      width="150"
      sortable
      align="center"
    />
    <el-table-column
      label="操作"
      width="150"
      fixed="right"
      align="center"
    >
      <template #default="{ row }">
        <el-button
          type="primary"
          link
          @click="$emit('config', row)"
        >
          配置
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
const baseUrl = import.meta.env.VITE_APP_BASE_API

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

function resolveImage (path) {
  return path?.startsWith('http') ? path : `${baseUrl}${path}`
}
</script>

<style scoped lang="scss">
.payment-table {
  width: 100%;
}

.payment-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.payment-icon :deep(.el-image) {
  width: 44px;
  height: 44px;
  border-radius: 6px;
}

.payment-icon__fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  color: #fff;
  font-size: 26px;
}

.payment-icon__fallback--balance {
  background: #f56c6c;
}

.payment-icon__fallback--wechat {
  background: #07c160;
}

.payment-icon__fallback--alipay {
  background: #409eff;
}
</style>
