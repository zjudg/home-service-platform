<template>
  <div class="app-container channel-reply">
    <NoticePanel>微信公众号回复管理：关注回复、关键词回复和默认回复均可新增、编辑、停用或删除。</NoticePanel>
    <el-card shadow="never">
      <el-tabs
        v-model="activeType"
        @tab-change="load"
      >
        <el-tab-pane
          label="关注回复"
          name="FOLLOW"
        /><el-tab-pane
          label="关键词回复"
          name="KEYWORD"
        /><el-tab-pane
          label="默认回复"
          name="DEFAULT"
        />
      </el-tabs>
      <div class="toolbar">
        <el-button
          type="primary"
          @click="openDialog()"
        >
          新增回复
        </el-button>
      </div>
      <el-table
        :data="rows"
        border
        stripe
      >
        <el-table-column
          prop="ruleName"
          label="规则名称"
          min-width="160"
        /><el-table-column
          prop="matchType"
          label="匹配类型"
          width="120"
        >
          <template #default="scope">
            {{ matchLabel(scope.row.matchType) }}
          </template>
        </el-table-column><el-table-column
          prop="keywords"
          label="关键词"
          min-width="180"
          show-overflow-tooltip
        /><el-table-column
          prop="replyContent"
          label="回复内容"
          min-width="260"
          show-overflow-tooltip
        /><el-table-column
          label="启用状态"
          width="110"
        >
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">
              {{ scope.row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column><el-table-column
          label="操作"
          width="180"
        >
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openDialog(scope.row)"
            >
              编辑
            </el-button><el-button
              link
              type="danger"
              @click="remove(scope.row)"
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
    </el-card>
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.form.replyId ? '编辑回复' : '新增回复'"
      width="560px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="dialog.form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item
          label="规则名称"
          prop="ruleName"
        >
          <el-input v-model="dialog.form.ruleName" />
        </el-form-item><el-form-item
          v-if="activeType === 'KEYWORD'"
          label="关键词"
        >
          <el-input
            v-model="dialog.form.keywords"
            placeholder="多个关键词用逗号分隔"
          />
        </el-form-item><el-form-item
          label="回复内容"
          prop="replyContent"
        >
          <el-input
            v-model="dialog.form.replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容"
          />
        </el-form-item><el-form-item label="排序">
          <el-input-number
            v-model="dialog.form.sortNum"
            :min="0"
            :max="999"
          />
        </el-form-item><el-form-item label="启用状态">
          <el-switch
            v-model="dialog.form.status"
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
            inline-prompt
          />
        </el-form-item>
      </el-form><template #footer>
        <el-button @click="dialog.visible = false">
          取消
        </el-button><el-button
          type="primary"
          :loading="dialog.saving"
          @click="save"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ChannelReply">
import { channelApi } from '@/api/settings/channel'
import NoticePanel from '@/components/Settings/NoticePanel.vue'
const { proxy } = getCurrentInstance(); const activeType = ref('FOLLOW'); const rows = ref([]); const total = ref(0); const formRef = ref(); const dialog = reactive({ visible: false, saving: false, form: {} }); const query = reactive({ pageNum: 1, pageSize: 10 })
const rules = { ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }], replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }] }
function matchLabel (value) { return { FOLLOW: '关注回复', KEYWORD: '关键词', DEFAULT: '默认回复' }[value] || value || '关键词' }
function load () { channelApi.listReplies({ ...query, replyType: activeType.value }).then(response => { rows.value = response.rows || []; total.value = response.total || 0 }) }
function openDialog (row) { dialog.form = { replyType: activeType.value, matchType: activeType.value, status: '1', sortNum: 0, ...(row || {}) }; dialog.visible = true }
async function save () { await formRef.value.validate(); dialog.saving = true; try { await channelApi.saveReply(dialog.form); dialog.visible = false; proxy.$modal.msgSuccess('保存成功'); load() } finally { dialog.saving = false } }
function remove (row) { proxy.$modal.confirm(`是否删除规则“${row.ruleName}”？`).then(() => channelApi.removeReply(row.replyId)).then(() => { proxy.$modal.msgSuccess('删除成功'); load() }) }
load()
</script>

<style scoped lang="scss">.channel-reply { .toolbar { padding:14px 0; } }</style>
