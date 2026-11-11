<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="师傅ID">
        <el-input
          v-model="query.masterId"
          clearable
          placeholder="师傅ID"
        />
      </el-form-item>
      <el-form-item label="业务单号">
        <el-input
          v-model="query.bizNo"
          clearable
          placeholder="订单号或提现单号"
        />
      </el-form-item>
      <el-form-item label="业务类型">
        <el-select
          v-model="query.bizType"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in BIZ_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="handleQuery"
        >
          查询
        </el-button>
        <el-button @click="resetQuery">
          重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="师傅"
        prop="masterName"
        width="110"
      />
      <el-table-column
        label="业务类型"
        width="120"
      >
        <template #default="{ row }">
          {{ bizLabel(row.bizType) }}
        </template>
      </el-table-column>
      <el-table-column
        label="变动金额"
        width="120"
      >
        <template #default="{ row }">
          <span :class="Number(row.amount) < 0 ? 'amount-out' : 'amount-in'">
            {{ Number(row.amount) > 0 ? '+' : '' }}{{ fenToYuan(row.amount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        label="变动后余额"
        width="120"
      >
        <template #default="{ row }">
          ¥{{ fenToYuan(row.balanceAfter) }}
        </template>
      </el-table-column>
      <el-table-column
        label="业务单号"
        prop="bizNo"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="备注"
        prop="remark"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        label="时间"
        prop="createTime"
        width="160"
      />
    </el-table>

    <pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listSettleLog } from '@/api/zjudg/settle'

const BIZ_LABELS = {
  order_income: '订单收入',
  extra_time_income: '补时收入',
  order_reverse: '订单冲回',
  withdraw: '提现冻结',
  withdraw_refund: '提现退回'
}

const BIZ_OPTIONS = Object.keys(BIZ_LABELS).map(value => ({ label: BIZ_LABELS[value], value }))

const bizLabel = value => BIZ_LABELS[value] || '-'
const fenToYuan = fen => (Number(fen || 0) / 100).toFixed(2)

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, masterId: '', bizNo: '', bizType: '' })

function load () {
  loading.value = true
  listSettleLog(query)
    .then(response => {
      rows.value = response.rows || []
      total.value = response.total || 0
    })
    .finally(() => { loading.value = false })
}

function handleQuery () {
  query.pageNum = 1
  load()
}

function resetQuery () {
  Object.assign(query, { pageNum: 1, pageSize: 10, masterId: '', bizNo: '', bizType: '' })
  load()
}

onMounted(load)
</script>

<style scoped>
.amount-in { color: #67c23a; }
.amount-out { color: #909399; }
</style>
