<template>
  <div class="app-container">
    <div class="mb8">
      <el-button
        type="primary"
        @click="open()"
      >
        添加单位
      </el-button>
    </div><el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="单位名称"
        prop="unitName"
      /><el-table-column
        label="排序"
        prop="sortNum"
      /><el-table-column label="状态">
        <template #default="{row}">
          {{ row.status === '0' ? '正常' : '停用' }}
        </template>
      </el-table-column><el-table-column
        label="添加时间"
        prop="createTime"
      /><el-table-column label="操作">
        <template #default="{row}">
          <el-button
            link
            type="primary"
            @click="open(row)"
          >
            编辑
          </el-button><el-button
            link
            type="danger"
            @click="remove(row.unitId)"
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
    /><el-dialog
      v-model="visible"
      :title="form.unitId?'编辑单位':'添加单位'"
      width="520px"
    >
      <el-form
        :model="form"
        label-width="90px"
      >
        <el-form-item
          label="单位名称"
          required
        >
          <el-input
            v-model="form.unitName"
            placeholder="请输入"
          />
        </el-form-item><el-form-item label="排序">
          <el-input-number
            v-model="form.sortNum"
            :min="0"
          />
        </el-form-item><el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-value="0"
            inactive-value="1"
          />
        </el-form-item>
      </el-form><template #footer>
        <el-button @click="visible=false">
          取消
        </el-button><el-button
          type="primary"
          @click="save"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { listUnit, addUnit, updateUnit, deleteUnit } from '@/api/zjudg/unit'; const rows = ref([]); const total = ref(0); const loading = ref(false); const visible = ref(false); const query = reactive({ pageNum: 1, pageSize: 10 }); const form = reactive({}); const load = () => { loading.value = true; listUnit(query).then(r => { rows.value = r.rows || []; total.value = r.total || 0 }).finally(() => { loading.value = false }) }; const open = row => { Object.keys(form).forEach(k => delete form[k]); Object.assign(form, row || { unitName: '', sortNum: 0, status: '0' }); visible.value = true }; const save = () => { (form.unitId ? updateUnit : addUnit)(form).then(() => { ElMessage.success('保存成功'); visible.value = false; load() }) }; const remove = id => ElMessageBox.confirm('确认删除此单位吗？', '提示', { type: 'warning' }).then(() => deleteUnit(id).then(() => { ElMessage.success('删除成功'); load() })); onMounted(load)
</script><style scoped>.mb8{margin-bottom:8px}</style>
