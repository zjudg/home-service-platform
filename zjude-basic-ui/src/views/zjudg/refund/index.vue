<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="退款单号">
        <el-input
          v-model="query.refundNo"
          clearable
          placeholder="退款单号"
        />
      </el-form-item>
      <el-form-item label="订单编号">
        <el-input
          v-model="query.orderNo"
          clearable
          placeholder="订单编号"
        />
      </el-form-item>
      <el-form-item label="退款状态">
        <el-select
          v-model="query.refundStatus"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in STATUS_OPTIONS"
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
        label="退款单号"
        prop="refundNo"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="订单编号"
        prop="orderNo"
        min-width="140"
      />
      <el-table-column
        label="退款金额"
        width="110"
      >
        <template #default="{ row }">
          ¥{{ row.refundAmount }}
        </template>
      </el-table-column>
      <el-table-column
        label="退款状态"
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.refundStatus)">
            {{ statusLabel(row.refundStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="申请原因"
        prop="remark"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="失败/拒绝原因"
        prop="failReason"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="申请时间"
        prop="createTime"
        width="160"
      />
      <el-table-column
        label="操作"
        width="200"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            v-if="row.refundStatus === '0'"
            link
            type="primary"
            @click="handleApprove(row)"
          >
            同意退款
          </el-button>
          <el-button
            v-if="row.refundStatus === '0'"
            link
            type="danger"
            @click="handleReject(row)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.refundStatus === '1'"
            link
            type="primary"
            @click="handleSync(row)"
          >
            同步状态
          </el-button>
        </template>
      </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRefund, approveRefund, rejectRefund, syncRefund } from '@/api/zjudg/refund'

const STATUS_LABELS = { 0: '待处理', 1: '退款中', 2: '已退款', 3: '退款失败', 4: '已拒绝' }
const STATUS_TAGS = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info' }

const STATUS_OPTIONS = Object.keys(STATUS_LABELS).map(value => ({
  label: STATUS_LABELS[value],
  value
}))

const statusLabel = value => STATUS_LABELS[String(value)] || '-'
const statusTagType = value => STATUS_TAGS[String(value)] || 'info'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, refundNo: '', orderNo: '', refundStatus: '' })

function load () {
  loading.value = true
  listRefund(query)
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
  Object.assign(query, { pageNum: 1, pageSize: 10, refundNo: '', orderNo: '', refundStatus: '' })
  load()
}

function handleApprove (row) {
  ElMessageBox.confirm(
    `确认同意退款 ¥${row.refundAmount}？提交后将调用微信退款，资金原路退回。`,
    '退款确认',
    { type: 'warning' }
  )
    .then(() => approveRefund(row.refundId))
    .then(() => {
      ElMessage.success('退款已提交')
      load()
    })
    .catch(() => {})
}

function handleReject (row) {
  ElMessageBox.prompt('请填写拒绝原因', '拒绝退款', {
    inputValidator: value => (value && value.trim() ? true : '拒绝原因不能为空')
  })
    .then(({ value }) => rejectRefund(row.refundId, value.trim()))
    .then(() => {
      ElMessage.success('已拒绝退款')
      load()
    })
    .catch(() => {})
}

function handleSync (row) {
  syncRefund(row.refundId)
    .then(() => {
      ElMessage.success('状态已同步')
      load()
    })
}

onMounted(load)
</script>
