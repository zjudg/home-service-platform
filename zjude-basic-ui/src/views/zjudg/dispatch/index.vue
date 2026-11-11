<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="订单号">
        <el-input
          v-model="query.orderNo"
          clearable
          placeholder="订单号"
        />
      </el-form-item>
      <el-form-item label="结果">
        <el-select
          v-model="query.result"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in RESULT_OPTIONS"
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

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip"
    >
      <template #title>
        自动派单需在「小程序订阅消息」同级的配置项中开启 <code>zjudg.dispatch.enabled</code>；开启后订单支付成功即自动派单，
        并每 5 分钟补偿一次未派出的订单。
      </template>
    </el-alert>

    <div class="mb8">
      <el-button
        type="primary"
        @click="openManual"
      >
        手动派单
      </el-button>
      <el-button @click="openCandidates">
        查看候选
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="订单号"
        prop="orderNo"
        min-width="140"
      />
      <el-table-column
        label="候选师傅"
        prop="masterName"
        width="110"
      />
      <el-table-column
        label="评分"
        prop="score"
        width="90"
      />
      <el-table-column
        label="结果"
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="resultTagType(row.result)">
            {{ resultLabel(row.result) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="说明"
        prop="reason"
        min-width="200"
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

    <el-dialog
      v-model="manualVisible"
      title="手动派单"
      width="460px"
    >
      <el-form
        :model="manualForm"
        label-width="90px"
      >
        <el-form-item label="订单号">
          <el-input
            v-model="manualForm.orderId"
            placeholder="请输入订单ID"
          />
        </el-form-item>
        <el-form-item label="师傅ID">
          <el-input
            v-model="manualForm.masterId"
            placeholder="请输入师傅ID"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="manualVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="submitManual"
        >
          确认指派
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="candidatesVisible"
      title="候选师傅评分"
      width="600px"
    >
      <el-form :inline="true">
        <el-form-item label="订单ID">
          <el-input
            v-model="candidateOrderId"
            placeholder="订单ID"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="loadCandidates"
          >
            评估
          </el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="candidates"
        size="small"
      >
        <el-table-column
          label="师傅"
          prop="masterName"
          width="120"
        />
        <el-table-column
          label="评分"
          prop="score"
          width="90"
        />
        <el-table-column
          label="说明"
          prop="reason"
          min-width="200"
        />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listCandidates, listDispatch, manualDispatch } from '@/api/zjudg/dispatch'

const RESULT_LABELS = { 0: '候选', 1: '派单成功', 2: '被拒绝', 3: '超时' }
const RESULT_TAGS = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }

const RESULT_OPTIONS = Object.keys(RESULT_LABELS).map(value => ({ label: RESULT_LABELS[value], value }))

const resultLabel = value => RESULT_LABELS[String(value)] || '-'
const resultTagType = value => RESULT_TAGS[String(value)] || 'info'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', result: '' })

const manualVisible = ref(false)
const manualForm = reactive({ orderId: '', masterId: '' })

const candidatesVisible = ref(false)
const candidateOrderId = ref('')
const candidates = ref([])

function load () {
  loading.value = true
  listDispatch(query)
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
  Object.assign(query, { pageNum: 1, pageSize: 10, orderNo: '', result: '' })
  load()
}

function openManual () {
  manualForm.orderId = ''
  manualForm.masterId = ''
  manualVisible.value = true
}

function submitManual () {
  if (!manualForm.orderId.trim() || !manualForm.masterId.trim()) {
    ElMessage.warning('请填写订单ID与师傅ID')
    return
  }
  manualDispatch({ orderId: manualForm.orderId.trim(), masterId: manualForm.masterId.trim() })
    .then(() => {
      ElMessage.success('指派成功')
      manualVisible.value = false
      load()
    })
}

function openCandidates () {
  candidateOrderId.value = ''
  candidates.value = []
  candidatesVisible.value = true
}

function loadCandidates () {
  if (!candidateOrderId.value.trim()) {
    ElMessage.warning('请输入订单ID')
    return
  }
  listCandidates(candidateOrderId.value.trim()).then(response => {
    candidates.value = response.data || response.rows || []
  })
}

onMounted(load)
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.tip { margin-bottom: 16px; }
</style>
