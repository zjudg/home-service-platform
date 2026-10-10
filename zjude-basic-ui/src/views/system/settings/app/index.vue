<template>
  <div class="app-container app-settings">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>APP设置</span>
            <span class="page-header__hint">配置移动端 APP 下载和登录相关设置</span>
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
        class="app-form"
      >
        <el-form-item
          v-for="item in textSettings"
          :key="item.key"
          :label="item.label"
        >
          <div class="copy-field">
            <el-input
              v-model="form[item.key]"
              :placeholder="item.placeholder"
              clearable
            />
            <el-button
              v-copyText="form[item.key]"
              v-copyText:callback="copySuccess"
              type="primary"
              plain
            >
              复制
            </el-button>
          </div>
          <div class="setting-tip">
            {{ item.tip }}
          </div>
        </el-form-item>

        <el-form-item
          v-for="item in switchSettings"
          :key="item.key"
          :label="item.label"
        >
          <el-switch
            v-model="form[item.key]"
            active-value="1"
            inactive-value="0"
            active-text="是"
            inactive-text="否"
            inline-prompt
          />
          <div class="setting-tip">
            {{ item.tip }}
          </div>
        </el-form-item>
      </el-form>
      <el-divider content-position="left">
        移动端配置预览
      </el-divider>
      <el-descriptions
        :column="1"
        border
        class="runtime-preview"
      >
        <el-descriptions-item label="启动隐私协议">
          {{ runtimeConfig.privacyAgreementRequired ? '需要同意' : '不强制弹出' }}
        </el-descriptions-item>
        <el-descriptions-item label="微信登录">
          {{ runtimeConfig.wechatLoginEnabled ? '已开启' : '已关闭' }}
        </el-descriptions-item>
        <el-descriptions-item label="下载引导文案">
          {{ runtimeConfig.downloadText || '未配置' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup name="AppSettings">
import { getAppRuntimeConfig, listConfig, saveConfig } from '@/api/settings/app'

const { proxy } = getCurrentInstance()

const saving = ref(false)
const form = reactive({})
const configRecords = ref({})
const runtimeConfig = reactive({
  privacyAgreementRequired: false,
  wechatLoginEnabled: false,
  downloadText: ''
})

const textSettings = [
  {
    key: 'app.iosDownloadUrl',
    label: '苹果APP下载链接',
    placeholder: '请输入苹果 APP 下载链接',
    tip: '苹果APP下载链接，可以配置应用宝下载链接'
  },
  {
    key: 'app.androidDownloadUrl',
    label: '安卓APP下载链接',
    placeholder: '请输入安卓 APP 下载链接',
    tip: '安卓APP下载链接，可以配置应用宝下载链接'
  },
  {
    key: 'app.downloadText',
    label: 'APP下载引导文案',
    placeholder: '请输入 APP 下载引导文案',
    default: '欢迎点击下载APP~',
    tip: 'APP下载引导文案，默认文案：欢迎点击下载APP~'
  }
]

const switchSettings = [
  {
    key: 'app.privacyAgreement',
    label: '弹出协议',
    tip: 'APP启动后强制弹窗同意隐私协议'
  },
  {
    key: 'app.wechatLogin',
    label: '开启微信登录',
    tip: '选择开启，则表示APP允许微信授权登录'
  }
]

const settings = [...textSettings, ...switchSettings]

function initForm () {
  settings.forEach(item => {
    form[item.key] = item.default || (item.key.includes('privacy') || item.key.includes('wechat') ? '0' : '')
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
    await loadRuntimeConfig()
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

async function loadRuntimeConfig () {
  const response = await getAppRuntimeConfig()
  Object.assign(runtimeConfig, response.data || response)
}

function copySuccess () {
  proxy.$modal.msgSuccess('复制成功')
}

getList()
loadRuntimeConfig()
</script>

<style scoped lang="scss">
.app-settings {
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

  .app-form {
    max-width: 820px;
    padding: 12px 0;
  }

  .app-form :deep(.el-form-item) {
    align-items: flex-start;
    margin-bottom: 24px;
  }

  .app-form :deep(.el-form-item__label) {
    line-height: 32px;
  }

  .app-form :deep(.el-form-item__content) {
    display: block;
  }

  .copy-field {
    display: flex;
    width: min(100%, 520px);
    gap: 10px;
  }

  .copy-field :deep(.el-input) {
    flex: 1;
  }

  .setting-tip {
    margin-top: 8px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 20px;
  }

  .runtime-preview {
    max-width: 820px;
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

    .copy-field {
      width: 100%;
    }
  }
}
</style>
