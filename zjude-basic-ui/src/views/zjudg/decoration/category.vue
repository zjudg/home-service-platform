<template>
  <div class="app-container category-decoration">
    <el-card
      v-loading="loading"
      shadow="never"
    >
      <template #header>
        <div class="page-header">
          <span>分类页装修</span>
          <el-button
            type="primary"
            :loading="saving"
            @click="save"
          >
            保存设置
          </el-button>
        </div>
      </template>

      <el-form
        label-width="130px"
        class="decoration-form"
      >
        <el-form-item label="空态提示标题">
          <el-input
            v-model="form.emptyTitle"
            placeholder="如 暂无服务分类"
            maxlength="32"
          />
        </el-form-item>
      </el-form>

      <p class="decoration-tip">
        分类列表与右侧服务由「服务分类」「服务项目」维护；这里只调整分类为空时展示的提示文案。
      </p>
    </el-card>
  </div>
</template>

<script setup name="ZjudgCategoryDecoration">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryDecorationApi, toConfigMap } from '@/api/settings/zjudgDecoration'

const loading = ref(false)
const saving = ref(false)

const form = reactive({
  emptyTitle: ''
})

async function load () {
  loading.value = true
  try {
    const response = await categoryDecorationApi.listConfig()
    const map = toConfigMap(response.rows || response.data?.rows)
    form.emptyTitle = map['decorate.category.emptyTitle'] || '暂无服务分类'
  } catch (e) {
    ElMessage.error(e?.message || '分类页配置加载失败')
  } finally {
    loading.value = false
  }
}

async function save () {
  saving.value = true
  try {
    await categoryDecorationApi.saveConfig({
      'decorate.category.emptyTitle': form.emptyTitle
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

load()
</script>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; }
.decoration-form { max-width: 640px; }
.decoration-tip { margin: 8px 0 0; color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.6; }
</style>
