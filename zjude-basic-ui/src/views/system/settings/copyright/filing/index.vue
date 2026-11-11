<template>
  <div class="app-container filing-settings">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>版权备案</span>
            <span class="page-header__hint">配置网站展示的版权、备案及资质信息</span>
          </div>
          <el-button
            type="primary"
            :loading="saving"
            @click="submitForm"
          >
            保存设置
          </el-button>
        </div>
      </template>

      <el-collapse
        v-model="activeNames"
        class="filing-notice"
      >
        <el-collapse-item
          title="操作提示"
          name="notice"
        >
          <div>* 填写的备案信息将会在小程序里展示，请填写完整信息</div>
          <div>* 上传的资质信息将会在小程序里展示，请上传清晰的图片</div>
        </el-collapse-item>
      </el-collapse>

      <el-divider content-position="left">
        备案信息
      </el-divider>

      <el-form
        ref="filingFormRef"
        :model="form"
        label-width="120px"
        class="filing-form"
      >
        <div
          v-for="item in textSettings"
          :key="item.key"
          class="filing-row"
        >
          <el-form-item :label="item.label">
            <el-input
              v-model="form[item.key]"
              :placeholder="item.placeholder"
              clearable
            />
            <div class="setting-tip">
              {{ item.tip }}
            </div>
          </el-form-item>
        </div>

        <el-divider content-position="left">
          资质信息
        </el-divider>

        <div class="qualification-grid">
          <div
            v-for="item in imageSettings"
            :key="item.key"
            class="qualification-item"
          >
            <el-form-item :label="item.label">
              <MaterialImageField
                v-model="form[item.key]"
                :limit="item.limit"
                :file-size="item.fileSize"
                :is-show-tip="false"
                class="filing-image-upload"
              />
              <div class="setting-tip">
                {{ item.tip }}
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="CopyrightFiling">
import { listConfig, saveConfig } from '@/api/settings/copyright'
import MaterialImageField from '@/components/MaterialImageField'

const { proxy } = getCurrentInstance()

const activeNames = ref(['notice'])
const saving = ref(false)
const form = reactive({})
const configRecords = ref({})

const textSettings = [
  {
    key: 'filing.copyright',
    label: '版权信息',
    placeholder: '请输入版权信息',
    default: 'Copyright © 2019-2021 公司名称',
    tip: '版权信息会显示在相关页面中'
  },
  {
    key: 'filing.icpNumber',
    label: 'ICP备案号',
    placeholder: '请输入ICP备案号',
    tip: '请填写网站或小程序对应的ICP备案号'
  },
  {
    key: 'filing.icpUrl',
    label: '备案号链接',
    placeholder: '请输入备案号链接',
    tip: '例如：https://beian.miit.gov.cn/'
  }
]

const imageSettings = [
  {
    key: 'filing.businessLicense',
    label: '营业执照',
    limit: 1,
    fileSize: 5,
    tip: '请上传清晰的营业执照图片'
  },
  {
    key: 'filing.qualifications',
    label: '其他资质',
    limit: 5,
    fileSize: 5,
    tip: '最多上传 5 张'
  }
]

const settings = [...textSettings, ...imageSettings]

function initForm () {
  settings.forEach(item => {
    form[item.key] = item.default || ''
  })
}

function getList () {
  initForm()
  listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
    const records = {}
    response.rows.forEach(item => {
      if (settings.some(setting => setting.key === item.configKey)) {
        records[item.configKey] = item
        form[item.configKey] = item.configValue
      }
    })
    configRecords.value = records
  })
}

async function submitForm () {
  saving.value = true
  try {
    await saveConfig({ ...form })
    proxy.$modal.msgSuccess('保存成功')
    getList()
  } finally {
    saving.value = false
  }
}

initForm()
getList()
</script>

<style scoped lang="scss">
.filing-settings {
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
    font-weight: 400;
  }

  .filing-notice {
    max-width: 1040px;
    margin-bottom: 18px;
  }

  :deep(.filing-notice .el-collapse-item__content) {
    color: var(--el-text-color-secondary);
    line-height: 26px;
  }

  :deep(.el-divider) {
    max-width: 1040px;
    margin: 24px 0;
  }

  .filing-form {
    max-width: 1040px;
  }

  .filing-row {
    max-width: 760px;
  }

  .qualification-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
    max-width: 1040px;
  }

  .qualification-item {
    min-height: 170px;
    padding: 18px 20px 14px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    background: var(--el-fill-color-blank);
  }

  :deep(.filing-form .el-form-item) {
    align-items: flex-start;
    margin-bottom: 20px;
  }

  :deep(.filing-form .el-form-item__label) {
    color: var(--el-text-color-primary);
    font-weight: 600;
    line-height: 32px;
  }

  :deep(.filing-form .el-form-item__content) {
    display: block;
    min-width: 0;
  }

  .setting-tip {
    margin-top: 8px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 18px;
  }

  :deep(.filing-image-upload .el-upload--picture-card),
  :deep(.filing-image-upload .el-upload-list--picture-card .el-upload-list__item) {
    width: 112px;
    height: 76px;
    margin: 0;
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

    .qualification-grid {
      grid-template-columns: 1fr;
    }

    :deep(.filing-form .el-form-item__label) {
      width: 108px !important;
      padding-right: 12px;
    }
  }
}
</style>
