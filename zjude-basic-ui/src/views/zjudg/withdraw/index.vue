<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="提现单号">
        <el-input
          v-model="query.withdrawNo"
          clearable
          placeholder="提现单号"
        />
      </el-form-item>
      <el-form-item label="师傅姓名">
        <el-input
          v-model="query.masterName"
          clearable
          placeholder="师傅姓名"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="query.withdrawStatus"
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
        label="提现单号"
        prop="withdrawNo"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="师傅"
        prop="masterName"
        width="110"
      />
      <el-table-column
        label="申请金额"
        width="110"
      >
        <template #default="{ row }">
          ¥{{ fenToYuan(row.amount) }}
        </template>
      </el-table-column>
      <el-table-column
        label="手续费"
        width="100"
      >
        <template #default="{ row }">
          ¥{{ fenToYuan(row.fee) }}
        </template>
      </el-table-column>
      <el-table-column
        label="实际到账"
        width="110"
      >
        <template #default="{ row }">
          ¥{{ fenToYuan(row.actualAmount) }}
        </template>
      </el-table-column>
      <el-table-column
        label="状态"
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.withdrawStatus)">
            {{ statusLabel(row.withdrawStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="收款账号"
        prop="payAccount"
        width="130"
      />
      <el-table-column
        label="申请时间"
        prop="createTime"
        width="160"
      />
      <el-table-column
        label="操作"
        width="230"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            v-if="row.withdrawStatus === '0'"
            link
            type="primary"
            @click="handleApprove(row)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.withdrawStatus === '0'"
            link
            type="danger"
            @click="handleReject(row)"
          >
            驳回
          </el-button>
          <el-button
            v-if="row.withdrawStatus === '1'"
            link
            type="primary"
            @click="handlePaid(row)"
          >
            已打款
          </el-button>
          <el-button
            v-if="row.withdrawStatus === '1'"
            link
            type="danger"
            @click="handleFailed(row)"
          >
            打款失败
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
import { approveWithdraw, listWithdraw, markWithdrawFailed, markWithdrawPaid, rejectWithdraw } from '@/api/zjudg/withdraw'

const LABELS = { 0: '待审核', 1: '待打款', 2: '已打款', 3: '已驳回', 4: '打款失败' }
const TAGS = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'danger' }

const STATUS_OPTIONS = Object.keys(LABELS).map(value => ({ label: LABELS[value], value }))

const statusLabel = value => LABELS[String(value)] || '-'
const statusTagType = value => TAGS[String(value)] || 'info'
const fenToYuan = fen => (Number(fen || 0) / 100).toFixed(2)

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, withdrawNo: '', masterName: '', withdrawStatus: '' })

function load () {
  loading.value = true
  listWithdraw(query)
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
  Object.assign(query, { pageNum: 1, pageSize: 10, withdrawNo: '', masterName: '', withdrawStatus: '' })
  load()
}

function handleApprove (row) {
  ElMessageBox.prompt('可填写审核备注（选填）', '确认通过该提现申请', {
    inputPlaceholder: '审核备注',
    inputValue: ''
  })
    .then(({ value }) => approveWithdraw(row.withdrawId, value))
    .then(() => {
      ElMessage.success('已通过，等待打款')
      load()
    })
    .catch(() => {})
}

function handleReject (row) {
  ElMessageBox.prompt('请填写驳回原因，冻结金额将退回师傅余额', '驳回提现', {
    inputValidator: value => (value && value.trim() ? true : '驳回原因不能为空')
  })
    .then(({ value }) => rejectWithdraw(row.withdrawId, value.trim()))
    .then(() => {
      ElMessage.success('已驳回，金额已退回')
      load()
    })
    .catch(() => {})
}

function handlePaid (row) {
  ElMessageBox.prompt('请填写微信转账单号，确认后将从冻结金额中扣除', '标记已打款', {
    inputValidator: value => (value && value.trim() ? true : '转账单号不能为空')
  })
    .then(({ value }) => markWithdrawPaid(row.withdrawId, value.trim()))
    .then(() => {
      ElMessage.success('已标记打款完成')
      load()
    })
    .catch(() => {})
}

function handleFailed (row) {
  ElMessageBox.prompt('请填写打款失败原因，冻结金额将退回师傅余额', '打款失败', {
    inputValidator: value => (value && value.trim() ? true : '失败原因不能为空')
  })
    .then(({ value }) => markWithdrawFailed(row.withdrawId, value.trim()))
    .then(() => {
      ElMessage.success('已标记失败，金额已退回')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>
