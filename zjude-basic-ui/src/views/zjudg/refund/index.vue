<template>
  <div class="app-container refund-page">
    <div class="refund-summary">
      <div><span>累计退款金额（元）</span><strong>0.00</strong></div><div><span>退款中金额（元）</span><strong>0.00</strong></div><div><span>退款成功金额（元）</span><strong>0.00</strong></div><div><span>退款失败金额（元）</span><strong>0.00</strong></div>
    </div><el-form
      :inline="true"
      :model="query"
      class="filter-panel"
    >
      <el-form-item label="退款编号">
        <el-input
          v-model="query.refundNo"
          placeholder="请输入"
        />
      </el-form-item><el-form-item label="来源单号">
        <el-input
          v-model="query.orderNo"
          placeholder="请输入"
        />
      </el-form-item><el-form-item label="买家信息">
        <el-input
          v-model="query.buyerInfo"
          placeholder="请输入"
        />
      </el-form-item><el-form-item label="退款类型">
        <el-select
          v-model="query.type"
          clearable
        >
          <el-option
            label="全部"
            value=""
          /><el-option
            label="售后退款"
            value="0"
          /><el-option
            label="主动退款"
            value="1"
          />
        </el-select>
      </el-form-item><el-form-item label="记录时间">
        <el-date-picker
          v-model="query.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
        />
      </el-form-item><el-form-item>
        <el-button
          type="primary"
          @click="search"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form><el-tabs v-model="query.status">
      <el-tab-pane
        label="全部(0)"
        name=""
      /><el-tab-pane
        label="退款中(0)"
        name="0"
      /><el-tab-pane
        label="退款成功(0)"
        name="1"
      /><el-tab-pane
        label="退款失败(0)"
        name="2"
      />
    </el-tabs><el-table :data="rows">
      <el-table-column
        label="ID"
        prop="refundId"
      /><el-table-column
        label="退款编号"
        prop="refundNo"
      /><el-table-column
        label="买家信息"
        prop="buyerInfo"
      /><el-table-column
        label="来源单号"
        prop="orderNo"
      /><el-table-column
        label="退款金额"
        prop="amount"
      /><el-table-column
        label="退款类型"
        prop="typeLabel"
      /><el-table-column
        label="退款状态"
        prop="statusLabel"
      /><el-table-column
        label="记录时间"
        prop="createTime"
      /><el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="showDetail(row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table><pagination
      v-show="rows.length"
      :total="rows.length"
      :page-size="10"
    /><el-dialog
      v-model="visible"
      title="退款详情"
      width="480px"
    >
      <el-descriptions
        :column="1"
        border
      >
        <el-descriptions-item label="退款编号">
          {{ current.refundNo }}
        </el-descriptions-item><el-descriptions-item label="来源单号">
          {{ current.orderNo }}
        </el-descriptions-item><el-descriptions-item label="退款金额">
          {{ current.amount }}
        </el-descriptions-item><el-descriptions-item label="退款状态">
          {{ current.statusLabel }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'; const visible = ref(false); const current = ref({}); const query = reactive({ refundNo: '', orderNo: '', buyerInfo: '', type: '', status: '', dateRange: [] }); const rows = ref([]); function search () {} function reset () { Object.assign(query, { refundNo: '', orderNo: '', buyerInfo: '', type: '', status: '', dateRange: [] }) } function showDetail (row) { current.value = row; visible.value = true }
</script>
<style scoped>
.refund-page { min-height: calc(100vh - 112px); color: var(--el-text-color-primary); }
.refund-summary, .filter-panel { border: 1px solid var(--el-border-color-light); border-radius: 10px; background: var(--el-bg-color-overlay); box-shadow: 0 8px 24px rgba(0, 0, 0, .08); }
.refund-summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; padding: 20px; margin-bottom: 16px; }
.refund-summary > div { padding: 14px 18px; border: 1px solid var(--el-border-color-light); border-radius: 8px; background: color-mix(in srgb, var(--el-bg-color) 70%, transparent); }
.refund-summary span { display: block; color: var(--el-text-color-secondary); font-size: 13px; }
.refund-summary strong { display: block; margin-top: 11px; color: var(--el-text-color-primary); font-size: 28px; font-weight: 600; }
.filter-panel { display: flex; flex-wrap: wrap; align-items: center; padding: 18px 20px 2px; margin-bottom: 16px; }
.filter-panel :deep(.el-form-item) { margin-right: 22px; margin-bottom: 16px; }
.refund-page :deep(.el-tabs) { padding: 0 14px; border: 1px solid var(--el-border-color-light); border-radius: 10px 10px 0 0; background: var(--el-bg-color-overlay); }
.refund-page :deep(.el-table) { border-right: 1px solid var(--el-border-color-light); border-bottom: 1px solid var(--el-border-color-light); border-left: 1px solid var(--el-border-color-light); border-radius: 0 0 10px 10px; overflow: hidden; }
@media (max-width: 900px) { .refund-summary { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .refund-summary { grid-template-columns: 1fr; padding: 14px; }.filter-panel { padding: 16px 14px 2px; } }
</style>
