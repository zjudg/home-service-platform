<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="师傅信息">
        <el-input
          v-model="query.masterName"
          clearable
          placeholder="师傅姓名"
        />
      </el-form-item>
      <el-form-item label="手机号码">
        <el-input
          v-model="query.phone"
          clearable
          placeholder="手机号码"
        />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select
          v-model="query.auditStatus"
          clearable
          placeholder="全部"
        >
          <el-option
            v-for="item in AUDIT_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="账号状态">
        <el-select
          v-model="query.status"
          clearable
          placeholder="全部"
        >
          <el-option
            label="正常"
            value="0"
          />
          <el-option
            label="停用"
            value="1"
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

    <div class="mb8">
      <el-button
        type="primary"
        @click="openForm()"
      >
        添加师傅
      </el-button>
      <el-button
        type="danger"
        :disabled="!selected.length"
        @click="handleDelete()"
      >
        删除
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="rows"
      @selection-change="onSelectionChange"
    >
      <el-table-column
        type="selection"
        width="50"
      />
      <el-table-column
        label="师傅姓名"
        prop="masterName"
        min-width="100"
      />
      <el-table-column
        label="登录账号"
        prop="account"
        min-width="120"
      >
        <template #default="{ row }">
          {{ row.account || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="手机号码"
        prop="phone"
        width="130"
      />
      <el-table-column
        label="服务城市"
        min-width="110"
      >
        <template #default="{ row }">
          {{ row.cityName || row.region || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        label="擅长技能"
        min-width="180"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          {{ row.skillNames || '暂无技能' }}
        </template>
      </el-table-column>
      <el-table-column
        label="健康证"
        width="100"
      >
        <template #default="{ row }">
          <el-link
            v-if="row.healthCertUrl"
            type="primary"
            @click="openCertificatePreview(row.healthCertUrl, '健康证')"
          >
            查看证书
          </el-link>
          <span v-else>暂无证书</span>
        </template>
      </el-table-column>
      <el-table-column
        label="技能证书"
        width="100"
      >
        <template #default="{ row }">
          <el-link
            v-if="row.skillCertUrl"
            type="primary"
            @click="openCertificatePreview(row.skillCertUrl, '技能证书')"
          >
            查看证书
          </el-link>
          <span v-else>暂无证书</span>
        </template>
      </el-table-column>
      <el-table-column
        label="评分"
        width="80"
      >
        <template #default="{ row }">
          {{ row.rating ?? '0.0' }}
        </template>
      </el-table-column>
      <el-table-column
        label="完单数"
        prop="orderCount"
        width="90"
      />
      <el-table-column
        label="审核状态"
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="auditTagType(row.auditStatus)">
            {{ auditLabel(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="接单"
        width="80"
      >
        <template #default="{ row }">
          <el-tag :type="acceptingTagType(row)">
            {{ acceptingLabel(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="账号状态"
        width="90"
      >
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="200"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="openAudit(row)"
          >
            审核
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm(row)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
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

    <el-dialog
      v-model="visible"
      :title="form.masterId ? '编辑师傅' : '添加师傅'"
      width="560px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="师傅姓名"
          prop="masterName"
        >
          <el-input
            v-model="form.masterName"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item
          label="登录账号"
          prop="userId"
        >
          <el-input
            v-model="form.userId"
            :disabled="Boolean(form.masterId)"
            placeholder="关联的 sys_user 主键"
          />
        </el-form-item>
        <el-form-item
          label="手机号码"
          prop="phone"
        >
          <el-input
            v-model="form.phone"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="服务城市">
          <el-input
            v-model="form.cityId"
            placeholder="城市ID，关联 zjudg_city"
          />
        </el-form-item>
        <el-form-item label="地区备注">
          <el-input
            v-model="form.region"
            maxlength="128"
          />
        </el-form-item>
        <el-form-item label="接单开关">
          <el-switch
            v-model="form.accepting"
            active-value="1"
            inactive-value="0"
            :disabled="form.auditStatus !== '1'"
          />
          <span
            v-if="form.auditStatus !== '1'"
            class="form-hint"
          >审核通过后才能接单</span>
        </el-form-item>
        <el-form-item label="首页推荐">
          <el-switch
            v-model="form.homeRecommend"
            active-value="1"
            inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-switch
            v-model="form.status"
            active-value="0"
            inactive-value="1"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="saving"
          @click="submitForm"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="auditVisible"
      title="审核师傅入驻申请"
      width="520px"
    >
      <el-descriptions
        :column="1"
        border
        class="audit-summary"
      >
        <el-descriptions-item label="师傅姓名">
          {{ auditTarget.masterName }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ auditTarget.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">
          {{ auditTarget.idCardMasked || '未提交' }}
          <span
            v-if="auditTarget.idCardMasked"
            class="masked-hint"
          >（已脱敏，仅用于核验）</span>
        </el-descriptions-item>
        <el-descriptions-item label="手机号码">
          {{ auditTarget.phone }}
        </el-descriptions-item>
        <el-descriptions-item label="服务城市">
          {{ auditTarget.cityName || auditTarget.region || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="擅长技能">
          {{ auditTarget.skillNames || '暂无技能' }}
        </el-descriptions-item>
        <el-descriptions-item label="健康证">
          <el-link
            v-if="auditTarget.healthCertUrl"
            type="primary"
            @click="openCertificatePreview(auditTarget.healthCertUrl, '健康证')"
          >
            查看
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="技能证书">
          <el-link
            v-if="auditTarget.skillCertUrl"
            type="primary"
            @click="openCertificatePreview(auditTarget.skillCertUrl, '技能证书')"
          >
            查看
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form
        label-width="90px"
        class="audit-form"
      >
        <el-form-item label="审核结论">
          <el-radio-group
            v-model="auditForm.auditStatus"
            :disabled="auditLocked"
          >
            <el-radio value="1">
              审核通过
            </el-radio>
            <el-radio value="2">
              驳回
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="审核意见"
          :required="auditForm.auditStatus === '2'"
        >
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :disabled="auditLocked"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">
          关闭
        </el-button>
        <el-button
          type="primary"
          :loading="auditing"
          :disabled="auditLocked"
          @click="submitAudit"
        >
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="certificatePreviewVisible"
      :title="certificatePreviewTitle"
      width="min(860px, 92vw)"
      append-to-body
      destroy-on-close
      class="certificate-preview-dialog"
    >
      <div class="certificate-preview">
        <el-image
          :src="certificatePreviewUrl"
          fit="contain"
          class="certificate-preview-image"
        >
          <template #error>
            <div class="certificate-preview-error">
              证书图片加载失败
            </div>
          </template>
        </el-image>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMaster, addMaster, updateMaster, auditMaster, deleteMaster } from '@/api/zjudg/master'
import {
  MASTER_AUDIT_OPTIONS,
  masterAuditLabel,
  masterAuditTagType
} from '@/constants/order'

const AUDIT_OPTIONS = MASTER_AUDIT_OPTIONS
const auditLabel = masterAuditLabel
const auditTagType = masterAuditTagType

function assetUrl (value) {
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return `${base}${value.startsWith('/') ? value : `/${value}`}`
}

function acceptingLabel (row) {
  const auditStatus = String(row.auditStatus ?? '')
  if (auditStatus === '0') return '待审核'
  if (auditStatus === '2') return '未通过'
  if (String(row.status ?? '') !== '0') return '已停用'
  return String(row.accepting ?? '') === '1' ? '接单中' : '停接'
}

function acceptingTagType (row) {
  const auditStatus = String(row.auditStatus ?? '')
  if (auditStatus === '2' || String(row.status ?? '') !== '0') return 'danger'
  if (auditStatus !== '1') return 'warning'
  return String(row.accepting ?? '') === '1' ? 'success' : 'info'
}

const loading = ref(false)
const saving = ref(false)
const auditing = ref(false)
const rows = ref([])
const total = ref(0)
const selected = ref([])
const visible = ref(false)
const auditVisible = ref(false)
const auditLocked = ref(false)
const certificatePreviewVisible = ref(false)
const certificatePreviewUrl = ref('')
const certificatePreviewTitle = ref('证书预览')
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, masterName: '', phone: '', auditStatus: '', status: '' })
const form = reactive({})
const auditForm = reactive({ masterId: '', auditStatus: '1', auditRemark: '' })
const auditTarget = ref({})

function openCertificatePreview (url, title) {
  if (!url) return
  certificatePreviewUrl.value = assetUrl(url)
  certificatePreviewTitle.value = title || '证书预览'
  certificatePreviewVisible.value = true
}

const rules = {
  masterName: [{ required: true, message: '请输入师傅姓名', trigger: 'blur' }],
  userId: [{ required: true, message: '请填写关联的登录账号', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

function load () {
  loading.value = true
  listMaster(query)
    .then(res => {
      rows.value = res.rows || []
      total.value = res.total || 0
    })
    .finally(() => { loading.value = false })
}

function handleQuery () {
  query.pageNum = 1
  load()
}

function resetQuery () {
  Object.assign(query, { pageNum: 1, pageSize: 10, masterName: '', phone: '', auditStatus: '', status: '' })
  load()
}

function onSelectionChange (value) {
  selected.value = value
}

function openForm (row) {
  Object.keys(form).forEach(key => delete form[key])
  if (row) {
    Object.assign(form, {
      masterId: row.masterId,
      userId: row.userId,
      masterName: row.masterName,
      phone: row.phone,
      cityId: row.cityId,
      region: row.region,
      auditStatus: row.auditStatus || '0',
      accepting: row.auditStatus === '1' ? (row.accepting || '0') : '0',
      homeRecommend: row.homeRecommend || '0',
      status: row.status || '0'
    })
  } else {
    Object.assign(form, {
      userId: '',
      masterName: '',
      phone: '',
      cityId: '',
      region: '',
      auditStatus: '0',
      accepting: '0',
      homeRecommend: '0',
      status: '0'
    })
  }
  visible.value = true
}

function submitForm () {
  formRef.value?.validate(valid => {
    if (!valid) return
    saving.value = true
    const action = form.masterId ? updateMaster : addMaster
    action({ ...form, accepting: form.auditStatus === '1' ? form.accepting : '0' })
      .then(() => {
        ElMessage.success(form.masterId ? '修改成功' : '新增成功')
        visible.value = false
        load()
      })
      .finally(() => { saving.value = false })
  })
}

function openAudit (row) {
  auditTarget.value = row
  Object.assign(auditForm, {
    masterId: row.masterId,
    auditStatus: row.auditStatus === '2' ? '2' : '1',
    auditRemark: row.auditRemark || ''
  })
  auditLocked.value = row.auditStatus === '1' || row.auditStatus === '2'
  auditVisible.value = true
}

function submitAudit () {
  if (auditForm.auditStatus === '2' && !auditForm.auditRemark.trim()) {
    ElMessage.warning('驳回时必须填写审核意见')
    return
  }
  auditing.value = true
  auditMaster({ ...auditForm })
    .then(() => {
      ElMessage.success('审核完成')
      auditVisible.value = false
      load()
    })
    .finally(() => { auditing.value = false })
}

function handleDelete (row) {
  const ids = row ? [row.masterId] : selected.value.map(item => item.masterId)
  if (!ids.length) return
  ElMessageBox.confirm(`确认删除选中的 ${ids.length} 条师傅档案？`, '提示', { type: 'warning' })
    .then(() => deleteMaster(ids.join(',')))
    .then(() => {
      ElMessage.success('删除成功')
      load()
    })
    .catch(() => {})
}

onMounted(load)
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.audit-summary { margin-bottom: 16px; }
.audit-form { margin-top: 8px; }
.masked-hint { margin-left: 6px; color: #909399; font-size: 12px; }
.form-hint { margin-left: 10px; color: #e6a23c; font-size: 12px; }
.certificate-preview { display: flex; justify-content: center; align-items: center; min-height: 200px; max-height: 72vh; overflow: auto; background: #f5f7fa; }
.certificate-preview-image { width: 100%; height: min(72vh, 760px); }
.certificate-preview-error { display: flex; justify-content: center; align-items: center; width: 100%; height: 240px; color: #909399; }
</style>
