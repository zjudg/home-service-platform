<template>
  <div class="app-container map-settings">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>腾讯地图设置</span>
            <span class="page-header__hint">配置腾讯地图服务 Key</span>
          </div>
          <el-button
            type="primary"
            :loading="saving"
            @click="saveForm"
          >
            保存设置
          </el-button>
        </div>
      </template>

      <el-form
        :model="form"
        label-width="140px"
        class="map-form"
      >
        <el-form-item label="腾讯地图key">
          <el-input
            v-model="form.mapKey"
            placeholder="请输入腾讯地图 Key"
            clearable
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="TencentMapSettings">
import { listConfig, saveConfig } from '@/api/settings/map-tencent'

const { proxy } = getCurrentInstance()

const saving = ref(false)
const form = reactive({ mapKey: '' })
const configRecords = ref({})
const configKey = 'map.tencent.key'

function getList () {
  listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
    const record = response.rows.find(item => item.configKey === configKey)
    if (!record) return
    configRecords.value[configKey] = record
    form.mapKey = record.configValue
  })
}

async function saveForm () {
  saving.value = true
  try {
    await saveConfig({ [configKey]: form.mapKey || '' })
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

getList()
</script>

<style scoped lang="scss">
.map-settings {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .page-header__title {
    display: flex;
    align-items: baseline;
    gap: 12px;
  }

  .page-header__hint {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .map-form {
    max-width: 700px;
    padding: 12px 0;
  }

  .map-form :deep(.el-form-item__label) {
    line-height: 32px;
  }

  .map-form :deep(.el-input) {
    width: 330px;
  }

  @media (max-width: 768px) {
    .page-header {
      display: block;
    }

    .page-header__title {
      margin-bottom: 12px;
    }

    .page-header__hint {
      display: block;
      margin-top: 6px;
    }

    .map-form :deep(.el-input) {
      width: 100%;
    }
  }
}
</style>
