<template>
  <div class="record-panel">
    <el-form
      :model="query"
      :inline="true"
      class="record-search"
    >
      <el-form-item label="短信标题">
        <el-input
          v-model="query.title"
          placeholder="请输入短信标题"
          clearable
        />
      </el-form-item>
      <el-form-item label="发送手机号">
        <el-input
          v-model="query.mobile"
          placeholder="请输入手机号"
          clearable
        />
      </el-form-item>
      <el-form-item label="发送状态">
        <el-select
          v-model="query.status"
          placeholder="全部"
          clearable
        >
          <el-option
            label="全部"
            value=""
          />
          <el-option
            label="发送成功"
            value="success"
          />
          <el-option
            label="发送失败"
            value="failed"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          clearable
        />
      </el-form-item>
      <el-form-item class="record-search__actions">
        <el-button
          type="primary"
          icon="Search"
          @click="$emit('query')"
        >
          查询
        </el-button>
        <el-button
          icon="Refresh"
          @click="$emit('reset')"
        >
          清空查询
        </el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="rows"
      border
      class="record-table"
    >
      <el-table-column
        label="序号"
        type="index"
        width="72"
        align="center"
      />
      <el-table-column
        label="短信标题"
        prop="title"
        min-width="180"
        align="center"
      />
      <el-table-column
        label="短信内容"
        prop="content"
        min-width="280"
        show-overflow-tooltip
        align="center"
      />
      <el-table-column
        label="发送手机号"
        prop="mobile"
        min-width="160"
        align="center"
      />
      <el-table-column
        label="发送状态"
        prop="statusText"
        width="140"
        align="center"
      />
      <el-table-column
        label="发送时间"
        prop="sendTime"
        min-width="180"
        align="center"
      />
      <el-table-column
        label="创建时间"
        prop="createTime"
        min-width="180"
        align="center"
      />
      <el-table-column
        label="操作"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="$emit('view', row)"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
defineProps({
  query: {
    type: Object,
    required: true
  },
  rows: {
    type: Array,
    default: () => []
  }
})

defineEmits(['query', 'reset', 'view'])
</script>

<style scoped lang="scss">
.record-search {
  padding: 14px 8px 2px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.record-search :deep(.el-form-item) {
  margin-right: 24px;
  margin-bottom: 14px;
}

.record-search :deep(.el-input) {
  width: 166px;
}

.record-search :deep(.el-select) {
  width: 190px;
}

.record-search :deep(.el-date-editor) {
  width: 310px;
}

.record-search__actions {
  margin-right: 0 !important;
}

.record-table {
  width: 100%;
}

@media (max-width: 768px) {
  .record-search :deep(.el-date-editor),
  .record-search :deep(.el-input),
  .record-search :deep(.el-select) {
    width: 100%;
  }
}
</style>
