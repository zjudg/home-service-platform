<template>
  <div class="app-container share-settings">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>分享设置</span>
            <span class="page-header__hint">配置 H5 端和小程序端的分享内容</span>
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

      <el-tabs
        v-model="activeTab"
        class="share-tabs"
      >
        <el-tab-pane
          label="H5端"
          name="h5"
        >
          <el-form
            :model="form"
            label-width="110px"
            class="share-form"
          >
            <el-form-item label="分享标题">
              <el-input
                v-model="form['share.h5.title']"
                placeholder="请输入分享标题"
                clearable
              />
              <div class="setting-tip">
                H5端分享页面时，显示的分享标题
              </div>
            </el-form-item>
            <el-form-item label="分享简介">
              <el-input
                v-model="form['share.h5.description']"
                placeholder="请输入分享简介"
                clearable
              />
              <div class="setting-tip">
                H5端分享页面时，显示的分享简介
              </div>
            </el-form-item>
            <el-form-item label="分享图片">
              <MaterialImageField
                v-model="form['share.h5.image']"
                :limit="1"
                :file-size="5"
                :is-show-tip="false"
                class="share-image-upload"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane
          label="小程序端"
          name="mini"
        >
          <el-form
            :model="form"
            label-width="110px"
            class="share-form"
          >
            <el-form-item label="分享标题">
              <el-input
                v-model="form['share.mini.title']"
                placeholder="请输入分享标题"
                clearable
              />
              <div class="setting-tip">
                小程序端分享页面时，显示的分享标题
              </div>
            </el-form-item>
            <el-form-item label="分享图片">
              <MaterialImageField
                v-model="form['share.mini.image']"
                :limit="1"
                :file-size="5"
                :is-show-tip="false"
                class="share-image-upload"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="ShareSettings">
import { listConfig, saveConfig } from '@/api/settings/share'
import MaterialImageField from '@/components/MaterialImageField'

const { proxy } = getCurrentInstance()

const activeTab = ref('h5')
const saving = ref(false)
const form = reactive({})
const configRecords = ref({})

const settings = [
  {
    key: 'share.h5.title',
    label: 'H5端分享标题',
    tip: 'H5端分享页面时，显示的分享标题'
  },
  {
    key: 'share.h5.description',
    label: 'H5端分享简介',
    tip: 'H5端分享页面时，显示的分享简介'
  },
  {
    key: 'share.h5.image',
    label: 'H5端分享图片',
    tip: 'H5端分享页面时，显示的分享图片'
  },
  {
    key: 'share.mini.title',
    label: '小程序端分享标题',
    tip: '小程序端分享页面时，显示的分享标题'
  },
  {
    key: 'share.mini.image',
    label: '小程序端分享图片',
    tip: '小程序端分享页面时，显示的分享图片'
  }
]

function initForm () {
  settings.forEach(item => {
    form[item.key] = ''
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

async function saveForm () {
  saving.value = true
  try {
    await saveConfig({ ...form })
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

getList()
</script>

<style scoped lang="scss">
.share-settings {
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

  .share-tabs {
    max-width: 920px;
  }

  .share-form {
    max-width: 720px;
    padding: 20px 8px 8px;
  }

  .share-form :deep(.el-form-item) {
    align-items: flex-start;
    margin-bottom: 24px;
  }

  .share-form :deep(.el-form-item__label) {
    line-height: 32px;
  }

  .share-form :deep(.el-form-item__content) {
    display: block;
  }

  .setting-tip {
    margin-top: 8px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 20px;
  }

  .share-image-upload {
    min-height: 100px;
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

    .share-form {
      padding: 12px 0;
    }
  }
}
</style>
