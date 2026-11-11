<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="订单编号">
        <el-input
          v-model="query.orderNo"
          clearable
          placeholder="请输入订单编号"
        />
      </el-form-item><el-form-item label="服务名称">
        <el-input
          v-model="query.serviceName"
          clearable
          placeholder="请输入服务名称"
        />
      </el-form-item><el-form-item label="用户信息">
        <el-input
          v-model="query.userInfo"
          clearable
          placeholder="昵称/手机号"
        />
      </el-form-item><el-form-item label="承接师傅">
        <el-input
          v-model="query.masterId"
          clearable
          placeholder="师傅ID，留空查全部"
        />
      </el-form-item><el-form-item label="订单状态">
        <el-select
          v-model="query.orderStatus"
          clearable
        >
          <el-option
            v-for="item in ORDER_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item><el-form-item>
        <el-button
          v-hasPerm="['zjudg:order:add']"
          type="primary"
          plain
          @click="openOfflineDialog"
        >
          录入线下已收款需求
        </el-button>
        <el-button
          type="primary"
          @click="load"
        >
          查询
        </el-button><el-button @click="reset">
          重置
        </el-button>
      </el-form-item>
    </el-form><el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="订单编号"
        prop="orderNo"
        min-width="160"
      /><el-table-column
        label="服务名称"
        prop="serviceName"
      /><el-table-column
        label="用户昵称"
        prop="adminUserInfo"
      /><el-table-column
        label="承接师傅"
        prop="masterId"
        min-width="120"
      >
        <template #default="{row}">
          {{ row.masterId || '未接单' }}
        </template>
      </el-table-column><el-table-column
        label="预约日期"
        prop="appointmentDate"
      /><el-table-column
        label="预约时间段"
        prop="slotName"
      /><el-table-column
        label="金额信息"
        min-width="230"
      >
        <template #default="{row}">
          <span>订单金额 {{ money(row.amount) }}</span>
          <span class="fee-text">服务费 {{ money(serviceFee(row)) }}</span>
          <span>师傅到手 {{ money(row.masterPrice) }}</span>
        </template>
      </el-table-column><el-table-column label="订单状态">
        <template #default="{row}">
          <el-tag :type="orderStatusTagType(row.orderStatus)">
            {{ orderStatusLabel(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column><el-table-column label="支付状态">
        <template #default="{row}">
          {{ paymentStatusLabel(row.paymentStatus) }}
          <el-tag
            v-if="row.paymentSource === 'OFFLINE'"
            size="small"
            type="warning"
          >
            线下收款
          </el-tag>
        </template>
      </el-table-column><el-table-column
        label="下单时间"
        prop="createTime"
      /><el-table-column
        label="操作"
        width="160"
        class-name="order-actions"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openOrderDetail(row)"
          >
            详情
          </el-button>
          <el-button
            v-if="orderStatusLabel(row.orderStatus) === '待接单'"
            link
            type="primary"
            @click="copyGroupNotice(row)"
          >
            复制
          </el-button>
          <el-button
            v-if="canDeleteOrder(row)"
            v-hasPerm="['zjudg:order:remove']"
            link
            type="danger"
            @click="removeOrder(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table><pagination
      v-show="total>0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    />
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="960px"
      class="order-detail-dialog"
    >
      <el-descriptions
        v-if="detailOrder"
        :column="3"
        border
      >
        <el-descriptions-item label="订单编号">
          {{ detailOrder.orderNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="orderStatusTagType(detailOrder.orderStatus)">
            {{ orderStatusLabel(detailOrder.orderStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务名称">
          {{ detailOrder.serviceName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户昵称">
          {{ detailOrder.adminUserInfo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="承接师傅">
          {{ detailOrder.masterId || '未接单' }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          {{ paymentStatusLabel(detailOrder.paymentStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="收款渠道">
          {{ paymentSourceLabel(detailOrder.paymentSource) }}
        </el-descriptions-item>
        <el-descriptions-item label="预约日期">
          {{ detailOrder.appointmentDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约时间段">
          {{ detailOrder.slotName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务地址">
          {{ detailOrder.address || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          label="联系电话"
          :span="3"
        >
          {{ detailOrder.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          {{ money(detailOrder.amount) }}
        </el-descriptions-item>
        <el-descriptions-item label="服务费">
          <span class="fee-text">{{ money(serviceFee(detailOrder)) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="师傅到手">
          {{ money(detailOrder.masterPrice) }}
        </el-descriptions-item>
        <el-descriptions-item
          label="备注"
          :span="3"
        >
          {{ detailOrder.remark || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          label="下单时间"
          :span="3"
        >
          {{ detailOrder.createTime || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    <el-dialog
      v-model="offlineVisible"
      title="录入线下已收款需求"
      width="680px"
      destroy-on-close
    >
      <el-alert
        title="此操作仅记录线下已收款事实并发布服务任务，不会发起微信支付或自动指派师傅。"
        type="info"
        :closable="false"
        show-icon
        class="offline-order-tip"
      />
      <el-form
        ref="offlineFormRef"
        :model="offlineForm"
        :rules="offlineRules"
        label-width="100px"
      >
        <el-form-item
          label="服务城市"
          prop="cityId"
        >
          <el-select
            v-model="offlineForm.cityId"
            filterable
            placeholder="请选择服务城市"
            class="form-control"
            @change="onOfflineCityChange"
          >
            <el-option
              v-for="item in cityOptions"
              :key="item.cityId"
              :label="item.cityName"
              :value="item.cityId"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="服务项目"
          prop="serviceId"
        >
          <el-select
            v-model="offlineForm.serviceId"
            filterable
            :disabled="!offlineForm.cityId"
            placeholder="请先选择服务城市"
            class="form-control"
            @change="onOfflineServiceChange"
          >
            <el-option
              v-for="item in serviceOptions"
              :key="item.serviceId"
              :label="`${item.serviceName}（${money(item.price)}${item.unitName ? `/${item.unitName}` : ''}）`"
              :value="item.serviceId"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="预约日期"
          prop="appointmentDate"
        >
          <el-date-picker
            v-model="offlineForm.appointmentDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择预约日期"
            class="form-control"
            :disabled-date="disablePastDate"
            @change="loadOfflineSlots"
          />
        </el-form-item>
        <el-form-item
          label="预约时段"
          prop="slotId"
        >
          <el-select
            v-model="offlineForm.slotId"
            :disabled="!offlineForm.appointmentDate || !offlineForm.cityId"
            placeholder="请选择预约日期后加载可用时段"
            class="form-control"
          >
            <el-option
              v-for="item in slotOptions"
              :key="item.slotId"
              :label="`${item.slotName}（剩余 ${item.availableCount} 单）`"
              :value="item.slotId"
            />
          </el-select>
          <div
            v-if="offlineForm.appointmentDate && slotOptions.length === 0"
            class="form-tip"
          >
            当前城市和日期没有可预约时段
          </div>
        </el-form-item>
        <el-form-item
          label="客户姓名"
          prop="customerName"
        >
          <el-input
            v-model="offlineForm.customerName"
            maxlength="128"
            show-word-limit
            placeholder="请输入线下客户姓名"
          />
        </el-form-item>
        <el-form-item
          label="联系电话"
          prop="phone"
        >
          <el-input
            v-model="offlineForm.phone"
            maxlength="11"
            placeholder="请输入客户手机号"
          />
        </el-form-item>
        <el-form-item
          label="服务地址"
          prop="address"
        >
          <el-input
            v-model="offlineForm.address"
            maxlength="255"
            placeholder="请输入上门服务地址"
          />
        </el-form-item>
        <el-form-item
          v-if="currentOfflineService && currentOfflineService.measureType !== 'NONE'"
          :label="currentOfflineService.unitName || '服务数量'"
          prop="quantity"
        >
          <el-input-number
            v-model="offlineForm.quantity"
            :min="quantityMin"
            :max="10000"
            :step="quantityStep"
            :precision="quantityPrecision"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="订单金额">
          <span class="offline-amount">{{ money(offlineAmount) }}</span>
          <span class="form-tip">按后台服务价格计算，仅作记录，不会再次扣款</span>
        </el-form-item>
        <el-form-item
          label="备注"
          prop="remark"
        >
          <el-input
            v-model="offlineForm.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可填写客户需求或师傅上门注意事项"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="offlineVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="offlineSaving"
          @click="submitOfflineOrder"
        >
          发布到接单大厅
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOfflinePaidOrder, deleteOrder, listOrder, listOrderCities, listOrderServices, listOrderSlots } from '@/api/zjudg/order'
import { ORDER_STATUS_OPTIONS, orderStatusLabel, orderStatusTagType, paymentSourceLabel, paymentStatusLabel } from '@/constants/order'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const detailVisible = ref(false)
const detailOrder = ref(null)
const offlineVisible = ref(false)
const offlineSaving = ref(false)
const offlineFormRef = ref()
const cityOptions = ref([])
const serviceOptions = ref([])
const slotOptions = ref([])
const offlineForm = reactive({ cityId: '', serviceId: '', appointmentDate: '', slotId: '', customerName: '', phone: '', address: '', quantity: null, remark: '' })
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', serviceName: '', userInfo: '', orderStatus: '', masterId: '' })

const currentOfflineService = computed(() => serviceOptions.value.find(item => item.serviceId === offlineForm.serviceId))
const quantityMin = computed(() => currentOfflineService.value?.measureType === 'HOUR' ? 2 : currentOfflineService.value?.measureType === 'AREA' ? 0.01 : 1)
const quantityStep = computed(() => currentOfflineService.value?.measureType === 'AREA' ? 0.5 : 1)
const quantityPrecision = computed(() => currentOfflineService.value?.measureType === 'AREA' ? 2 : 0)
const offlineAmount = computed(() => {
  const price = Number(currentOfflineService.value?.price)
  if (!Number.isFinite(price)) return null
  const quantity = currentOfflineService.value?.measureType === 'NONE' ? 1 : Number(offlineForm.quantity)
  return Number.isFinite(quantity) ? price * quantity : null
})
const offlineRules = {
  cityId: [{ required: true, message: '请选择服务城市', trigger: 'change' }],
  serviceId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  slotId: [{ required: true, message: '请选择预约时段', trigger: 'change' }],
  customerName: [{ required: true, message: '请填写客户姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请填写联系电话', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '请输入有效的11位手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请填写服务地址', trigger: 'blur' }],
  quantity: [{
    validator: (_rule, value, callback) => {
      const type = currentOfflineService.value?.measureType
      if (type === 'NONE') return callback()
      const number = Number(value)
      if (!Number.isFinite(number) || number < quantityMin.value || (type !== 'AREA' && !Number.isInteger(number))) {
        return callback(new Error(type === 'HOUR' ? '时长须为不少于2小时的整数' : type === 'AREA' ? '请填写大于0的服务面积' : '数量须为正整数'))
      }
      callback()
    },
    trigger: 'change'
  }]
}

const load = () => {
  loading.value = true
  listOrder(query).then((result) => {
    rows.value = result.rows || []
    total.value = result.total || 0
  }).finally(() => {
    loading.value = false
  })
}

const reset = () => {
  Object.assign(query, { pageNum: 1, orderNo: '', serviceName: '', userInfo: '', orderStatus: '', masterId: '' })
  load()
}

const openOrderDetail = (row) => {
  detailOrder.value = row
  detailVisible.value = true
}

const resetOfflineForm = () => {
  Object.assign(offlineForm, { cityId: '', serviceId: '', appointmentDate: '', slotId: '', customerName: '', phone: '', address: '', quantity: null, remark: '' })
  serviceOptions.value = []
  slotOptions.value = []
}

const openOfflineDialog = async () => {
  resetOfflineForm()
  offlineVisible.value = true
  const result = await listOrderCities()
  cityOptions.value = result.data || []
}

const onOfflineCityChange = async () => {
  offlineForm.serviceId = ''
  offlineForm.slotId = ''
  serviceOptions.value = []
  slotOptions.value = []
  if (!offlineForm.cityId) return
  const result = await listOrderServices({ cityId: offlineForm.cityId })
  serviceOptions.value = result.data || []
  if (offlineForm.appointmentDate) await loadOfflineSlots()
}

const onOfflineServiceChange = () => {
  const type = currentOfflineService.value?.measureType
  offlineForm.quantity = type === 'HOUR' ? 2 : type === 'NONE' ? null : 1
}

const loadOfflineSlots = async () => {
  offlineForm.slotId = ''
  slotOptions.value = []
  if (!offlineForm.cityId || !offlineForm.appointmentDate) return
  const result = await listOrderSlots({ cityId: offlineForm.cityId, appointmentDate: offlineForm.appointmentDate })
  slotOptions.value = (result.data || []).filter(item => Number(item.availableCount) > 0)
}

const disablePastDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

const submitOfflineOrder = async () => {
  try {
    await offlineFormRef.value.validate()
  } catch {
    return
  }
  offlineSaving.value = true
  const payload = { ...offlineForm }
  if (currentOfflineService.value?.measureType === 'NONE') payload.quantity = null
  try {
    await createOfflinePaidOrder(payload)
    ElMessage.success('线下已收款需求已发布到师傅接单大厅')
    offlineVisible.value = false
    load()
  } finally {
    offlineSaving.value = false
  }
}

const canDeleteOrder = (row) => String(row.orderStatus) === '3' && String(row.paymentStatus) === '0' && !row.masterId

const removeOrder = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定永久删除订单 ${row.orderNo} 吗？仅未支付、未指派且没有关联记录的已取消订单可以删除。`,
      '删除订单',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await deleteOrder(row.orderId)
  ElMessage.success('删除成功')
  if (rows.value.length === 1 && query.pageNum > 1) query.pageNum -= 1
  load()
}

const money = (value) => value === null || value === undefined || value === '' ? '-' : `¥${Number(value).toFixed(2)}`
const serviceFee = (row) => {
  const amount = Number(row?.amount)
  const masterPrice = Number(row?.masterPrice)
  return Number.isFinite(amount) && Number.isFinite(masterPrice) ? Math.max(0, amount - masterPrice) : null
}

const asDate = (value) => {
  if (!value) return ''
  if (value instanceof Date) return `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`
  return String(value).slice(0, 10)
}

const asTimeRange = (value) => {
  const parts = String(value || '').split(/[-~～至]/).map(part => part.trim())
  if (parts.length !== 2) return String(value || '')
  return parts.map(part => /^\d{2}:\d{2}$/.test(part) ? `${part}:00` : part).join('~')
}

const copyGroupNotice = async (row) => {
  const notice = `产品名称：【${row.serviceName || ''}】\n\n地址：${row.address || '未填写'}\n日期：${asDate(row.appointmentDate)}\n时间：${asTimeRange(row.slotName)}\n到手${Number(row.masterPrice || 0).toFixed(2).replace(/\.00$/, '')}`
  try {
    await navigator.clipboard.writeText(notice)
  } catch {
    const input = document.createElement('textarea')
    input.value = notice
    input.style.position = 'fixed'
    input.style.opacity = '0'
    document.body.appendChild(input)
    input.select()
    const copied = document.execCommand('copy')
    input.remove()
    if (!copied) {
      ElMessage.error('复制失败，请检查浏览器剪贴板权限')
      return
    }
  }
  ElMessage.success('群通知已复制')
}

onMounted(load)
</script>
<style scoped>
.fee-text {
  color: var(--el-color-danger);
  margin-left: 8px;
}

:deep(.order-detail-dialog .el-descriptions__label) {
  white-space: nowrap;
}

:deep(.order-actions .cell) {
  white-space: nowrap;
}

.offline-order-tip {
  margin-bottom: 20px;
}

.form-control {
  width: 100%;
}

.form-tip {
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.offline-amount {
  color: var(--el-color-danger);
  font-size: 18px;
  font-weight: 600;
}
</style>
