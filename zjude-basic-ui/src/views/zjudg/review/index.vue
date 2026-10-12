<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="服务名称">
        <el-input
          v-model="query.serviceName"
          placeholder="服务名称"
          clearable
        />
      </el-form-item><el-form-item label="用户信息">
        <el-input
          v-model="query.userInfo"
          placeholder="昵称/手机号"
          clearable
        />
      </el-form-item><el-form-item label="回复状态">
        <el-select
          v-model="query.replyStatus"
          clearable
        >
          <el-option
            label="全部"
            value=""
          /><el-option
            label="未回复"
            value="0"
          /><el-option
            label="已回复"
            value="1"
          />
        </el-select>
      </el-form-item><el-form-item>
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
        label="服务名称"
        prop="serviceName"
      /><el-table-column
        label="用户信息"
        prop="userInfo"
      /><el-table-column label="评价等级">
        <template #default="{row}">
          <el-rate
            v-model="row.rating"
            disabled
          />
        </template>
      </el-table-column><el-table-column
        label="评价内容"
        prop="content"
        show-overflow-tooltip
      /><el-table-column
        label="回复内容"
        prop="replyContent"
        show-overflow-tooltip
      /><el-table-column label="回复状态">
        <template #default="{row}">
          {{ row.replyStatus==='1'?'已回复':'未回复' }}
        </template>
      </el-table-column><el-table-column
        label="评价时间"
        prop="createTime"
      /><el-table-column label="操作">
        <template #default="{row}">
          <el-button
            link
            type="primary"
            @click="reply(row)"
          >
            回复
          </el-button>
        </template>
      </el-table-column>
    </el-table><pagination
      v-show="total>0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    /><el-dialog
      v-model="visible"
      title="回复评价"
      width="520px"
    >
      <el-input
        v-model="replyContent"
        type="textarea"
        :rows="5"
        placeholder="请输入回复内容"
      /><template #footer>
        <el-button @click="visible=false">
          取消
        </el-button><el-button
          type="primary"
          @click="saveReply"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'; import { ElMessage } from 'element-plus'; import { listReview, replyReview } from '@/api/zjudg/review'; const rows = ref([]); const total = ref(0); const loading = ref(false); const visible = ref(false); const replyContent = ref(''); const current = ref(null); const query = reactive({ pageNum: 1, pageSize: 10, serviceName: '', userInfo: '', replyStatus: '' }); const load = () => { loading.value = true; listReview(query).then(r => { rows.value = r.rows || []; total.value = r.total || 0 }).finally(() => { loading.value = false }) }; const reset = () => { Object.assign(query, { pageNum: 1, serviceName: '', userInfo: '', replyStatus: '' }); load() }; const reply = row => { current.value = row; replyContent.value = row.replyContent || ''; visible.value = true }; const saveReply = () => replyReview({ reviewId: current.value.reviewId, replyContent: replyContent.value }).then(() => { ElMessage.success('回复成功'); visible.value = false; load() }); onMounted(load)
</script>
