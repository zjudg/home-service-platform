<template>
  <div class="app-container">
    <div class="mb8">
      <el-button
        type="primary"
        @click="open()"
      >
        新增证书商品
      </el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="证书名称"
        prop="productName"
        min-width="180"
      /><el-table-column
        label="价格"
        prop="price"
        width="100"
      /><el-table-column
        label="状态"
        width="90"
      >
        <template #default="{ row }">
          {{ row.status === '0' ? '上架' : '下架' }}
        </template>
      </el-table-column><el-table-column
        label="排序"
        prop="sortNum"
        width="80"
      /><el-table-column
        label="操作"
        width="180"
      >
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
            @click="remove(row.productId)"
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
      :title="form.productId ? '编辑证书商品' : '新增证书商品'"
      width="620px"
    >
      <el-form
        :model="form"
        label-width="100px"
      >
        <el-form-item
          label="证书名称"
          required
        >
          <el-input v-model="form.productName" />
        </el-form-item><el-form-item
          label="价格"
          required
        >
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
          />
        </el-form-item><el-form-item label="商品说明">
          <el-input
            v-model="form.productIntro"
            type="textarea"
          />
        </el-form-item><el-form-item label="封面图片">
          <MaterialImageField
            v-model="form.coverUrl"
            :limit="1"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import MaterialImageField from '@/components/MaterialImageField'
import { listCertificateProducts, addCertificateProduct, updateCertificateProduct, deleteCertificateProduct } from '@/api/zjudg/certificateProduct'
const rows = ref([]); const total = ref(0); const loading = ref(false); const visible = ref(false); const query = reactive({ pageNum: 1, pageSize: 10 }); const form = reactive({})
const load = () => { loading.value = true; listCertificateProducts(query).then(res => { rows.value = res.rows || []; total.value = res.total || 0 }).finally(() => { loading.value = false }) }
const open = row => { Object.keys(form).forEach(key => delete form[key]); Object.assign(form, row || { productName: '', productIntro: '', price: 0, coverUrl: '', status: '0', sortNum: 0 }); visible.value = true }
const save = () => (form.productId ? updateCertificateProduct(form) : addCertificateProduct(form)).then(() => { ElMessage.success('保存成功'); visible.value = false; load() })
const remove = id => ElMessageBox.confirm('确认删除这个证书商品吗？', '提示', { type: 'warning' }).then(() => deleteCertificateProduct(id).then(() => { ElMessage.success('删除成功'); load() }))
onMounted(load)
</script>
<style scoped>.mb8 { margin-bottom: 8px; }</style>
