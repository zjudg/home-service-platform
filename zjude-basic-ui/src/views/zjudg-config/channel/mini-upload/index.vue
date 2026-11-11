<template>
  <div class="app-container mini-upload">
    <NoticePanel>首次使用请先配置小程序代码上传密钥；上传后需要在微信公众平台提交审核。</NoticePanel><el-card shadow="never">
      <el-divider content-position="left">
        小程序代码上传
      </el-divider><el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="upload-form"
      >
        <el-form-item
          label="版本号"
          prop="version"
        >
          <el-input
            v-model="form.version"
            placeholder="例如：3.6.0"
          />
        </el-form-item><el-form-item label="项目备注">
          <el-input
            v-model="form.projectRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入项目备注（选填）"
          />
        </el-form-item><el-form-item>
          <el-button
            type="primary"
            :loading="saving"
            @click="upload"
          >
            上传小程序
          </el-button>
        </el-form-item>
      </el-form><el-divider content-position="left">
        上传记录
      </el-divider><el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="version"
          label="版本号"
        /><el-table-column
          prop="status"
          label="状态"
        >
          <template #default="scope">
            <el-tag>{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column><el-table-column
          prop="operator"
          label="操作人"
        /><el-table-column
          prop="createTime"
          label="上传时间"
          min-width="180"
        />
      </el-table><pagination
        v-show="total > 0"
        v-model:page="query.pageNum"
        v-model:limit="query.pageSize"
        :total="total"
        @pagination="load"
      />
    </el-card>
  </div>
</template>
<script setup name="ChannelMiniUpload">
import { channelApi } from '@/api/settings/channel'
import NoticePanel from '@/components/Settings/NoticePanel.vue'
const { proxy } = getCurrentInstance(); const formRef = ref(); const saving = ref(false); const form = reactive({ version: '', projectRemark: '' }); const rows = ref([]); const total = ref(0); const query = reactive({ pageNum: 1, pageSize: 10 }); const rules = { version: [{ required: true, message: '请输入版本号', trigger: 'blur' }] }
function statusLabel (status) { return { PENDING: '待上传', UPLOADED: '已上传', FAILED: '失败' }[status] || status }
function load () { channelApi.listUploads(query).then(response => { rows.value = response.rows || []; total.value = response.total || 0 }) }
async function upload () { await formRef.value.validate(); saving.value = true; try { await channelApi.addUpload(form); proxy.$modal.msgSuccess('上传任务已创建，请等待构建完成'); form.version = ''; form.projectRemark = ''; load() } finally { saving.value = false } }
load()
</script>
<style scoped lang="scss">.mini-upload { .upload-form { max-width:650px; } .el-divider { margin-top:24px; } }</style>
