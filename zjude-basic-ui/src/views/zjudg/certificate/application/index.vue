<template>
  <div class="app-container">
    <div class="page-tip">
      线上只收集办理所需的电子照片、联系电话和快递地址；身份证材料由平台线下联系获取。
    </div>
    <el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="申请单号"
        prop="applicationNo"
        min-width="180"
      />
      <el-table-column
        label="证书"
        prop="productName"
        min-width="150"
      />
      <el-table-column
        label="用户"
        prop="userId"
        min-width="150"
      />
      <el-table-column
        label="联系电话"
        prop="contactPhone"
        width="130"
      />
      <el-table-column
        label="快递地址"
        prop="deliveryAddress"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        label="电子照片"
        width="100"
      >
        <template #default="{ row }">
          <el-link
            v-if="row.photoUrl"
            :href="assetUrl(row.photoUrl)"
            target="_blank"
          >
            查看
          </el-link><span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column
        label="状态"
        width="120"
      >
        <template #default="{ row }">
          <el-tag>{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="快递单号"
        prop="trackingNo"
        width="150"
      />
      <el-table-column
        label="操作"
        width="150"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="open(row)"
          >
            处理
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
      title="处理证书申请"
      width="480px"
    >
      <el-form label-width="100px">
        <el-form-item label="申请单号">
          {{ form.applicationNo }}
        </el-form-item><el-form-item label="处理状态">
          <el-select v-model="form.status">
            <el-option
              v-for="item in statuses"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item><el-form-item label="快递单号">
          <el-input
            v-model="form.trackingNo"
            placeholder="寄出后填写"
          />
        </el-form-item>
      </el-form><template #footer>
        <el-button @click="visible = false">
          取消
        </el-button><el-button
          type="primary"
          @click="save"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listCertificateApplications, updateCertificateApplication } from '@/api/zjudg/business'
const assetUrl = value => { if (!value) return ''; if (/^https?:\/\//.test(value)) return value; return `${import.meta.env.VITE_APP_BASE_API || ''}${value.startsWith('/') ? value : `/${value}`}` }
const rows = ref([]); const total = ref(0); const loading = ref(false); const visible = ref(false); const form = reactive({}); const query = reactive({ pageNum: 1, pageSize: 10 })
const statuses = [{ value: '1', label: '待审核' }, { value: '2', label: '办理中' }, { value: '3', label: '已寄出' }, { value: '4', label: '已完成' }]
const statusLabel = status => ({ 0: '待支付', 1: '待审核', 2: '办理中', 3: '已寄出', 4: '已完成', 5: '已取消' }[status] || '未知')
const load = () => { loading.value = true; listCertificateApplications(query).then(res => { rows.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false }) }
const open = row => { Object.keys(form).forEach(key => delete form[key]); Object.assign(form, { applicationId: row.applicationId, applicationNo: row.applicationNo, status: row.status, trackingNo: row.trackingNo || '' }); visible.value = true }
const save = () => updateCertificateApplication(form.applicationId, { status: form.status, trackingNo: form.trackingNo }).then(() => { ElMessage.success('保存成功'); visible.value = false; load() })
onMounted(load)
</script>
<style scoped>.page-tip { margin-bottom: 16px; padding: 12px 16px; color: var(--el-color-info); background: var(--el-color-info-light-9); border-radius: 4px; }</style>
