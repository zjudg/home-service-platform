<template>
  <div class="app-container">
    <el-form
      :inline="true"
      :model="query"
    >
      <el-form-item label="分类名称">
        <el-input
          v-model="query.categoryName"
          clearable
          placeholder="请输入分类名称"
        />
      </el-form-item><el-form-item label="显示状态">
        <el-select
          v-model="query.status"
          clearable
        >
          <el-option
            label="显示"
            value="0"
          /><el-option
            label="停用"
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
    </el-form><div class="mb8">
      <el-button
        type="primary"
        @click="open()"
      >
        新增分类
      </el-button>
    </div><el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="分类名称"
        prop="categoryName"
      /><el-table-column label="分类图片">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            style="width:40px;height:40px"
          />
        </template>
      </el-table-column><el-table-column
        label="排序"
        prop="sortNum"
      /><el-table-column label="首页推荐">
        <template #default="{ row }">
          {{ row.homeRecommend === '1' ? '推荐' : '不推荐' }}
        </template>
      </el-table-column><el-table-column label="状态">
        <template #default="{ row }">
          <el-tag>{{ row.status === '0' ? '显示' : '停用' }}</el-tag>
        </template>
      </el-table-column><el-table-column
        label="添加时间"
        prop="createTime"
      /><el-table-column label="操作">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="open(row)"
          >
            编辑
          </el-button><el-button
            link
            type="danger"
            @click="remove(row.categoryId)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table><pagination
      v-show="total > 0"
      v-model:page="query.pageNum"
      v-model:limit="query.pageSize"
      :total="total"
      @pagination="load"
    /><el-dialog
      v-model="visible"
      :title="form.categoryId ? '编辑分类' : '新增分类'"
      width="560px"
    >
      <el-form
        :model="form"
        label-width="100px"
      >
        <el-form-item
          label="分类名称"
          required
        >
          <el-input v-model="form.categoryName" />
        </el-form-item><el-form-item label="父级分类">
          <el-select v-model="form.parentId">
            <el-option
              label="无父级分类"
              value="0"
            />
          </el-select>
        </el-form-item><el-form-item label="分类图片">
          <image-upload
            v-model="form.imageUrl"
            :limit="1"
          />
        </el-form-item><el-form-item label="排序">
          <el-input-number
            v-model="form.sortNum"
            :min="0"
          />
        </el-form-item><el-form-item label="首页推荐">
          <el-switch
            v-model="form.homeRecommend"
            active-value="1"
            inactive-value="0"
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
import { ref, reactive, onMounted } from 'vue'; import { ElMessage, ElMessageBox } from 'element-plus'; import { listCategory, addCategory, updateCategory, deleteCategory } from '@/api/zjudg/category'
const rows = ref([]); const total = ref(0); const loading = ref(false); const visible = ref(false); const query = reactive({ pageNum: 1, pageSize: 10, categoryName: '', status: '' }); const form = reactive({})
const load = () => { loading.value = true; listCategory(query).then(r => { rows.value = r.rows || []; total.value = r.total || 0 }).finally(() => { loading.value = false }) }; const reset = () => { Object.assign(query, { pageNum: 1, categoryName: '', status: '' }); load() }; const open = (row) => { Object.keys(form).forEach(k => delete form[k]); Object.assign(form, row || { parentId: '0', sortNum: 0, homeRecommend: '0', status: '0', imageUrl: '' }); visible.value = true }; const save = () => { (form.categoryId ? updateCategory : addCategory)(form).then(() => { ElMessage.success('保存成功'); visible.value = false; load() }) }; const remove = (id) => ElMessageBox.confirm('确认删除此分类吗？', '提示', { type: 'warning' }).then(() => deleteCategory(id).then(() => { ElMessage.success('删除成功'); load() })); onMounted(load)
</script><style scoped>.mb8{margin-bottom:8px}</style>
