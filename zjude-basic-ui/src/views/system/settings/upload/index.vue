<template>
  <div class="app-container upload-settings">
    <el-card shadow="never">
      <el-collapse
        v-model="activeNames"
        class="upload-notice"
      >
        <el-collapse-item
          title="温馨提示"
          name="notice"
        >
          <div>1.使用对象存储，需要将public目录下的资源文件保留原目录路径传输到对象存储空间。</div>
          <div>2.请勿随意切换存储方式，可能导致图片无法查看。</div>
          <div>3.需要在对象存储后台设置域名跨域，否则图片生成场景无法使用，例如报告合成等。</div>
          <div>4.需将对象存储的图片域名添加到微信小程序官方后台request合法域名和downloadFile合法域名。</div>
        </el-collapse-item>
      </el-collapse>

      <el-divider content-position="left">
        上传设置
      </el-divider>

      <el-table
        v-loading="loading"
        :data="providerRows"
        border
        class="provider-table"
      >
        <el-table-column
          label="存储方式"
          prop="name"
          min-width="180"
        />
        <el-table-column
          label="存储位置"
          prop="description"
          min-width="420"
        />
        <el-table-column
          label="状态"
          width="150"
          align="center"
        >
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              active-text="是"
              inactive-text="否"
              inline-prompt
              @change="toggleProvider(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="160"
          align="center"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              @click="openProvider(row)"
            >
              配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="`编辑${activeProvider?.name || ''}`"
      width="760px"
      append-to-body
    >
      <el-form
        v-if="activeProvider"
        :model="providerForm"
        label-width="150px"
        class="provider-form"
      >
        <el-form-item label="存储方式">
          <span class="readonly-value">{{ activeProvider.name }}</span>
        </el-form-item>

        <el-form-item
          v-for="field in activeFields"
          :key="field.key"
          :label="field.label"
        >
          <el-input
            v-model="providerForm[field.key]"
            :type="field.secret ? 'password' : 'text'"
            :show-password="field.secret"
            :placeholder="field.placeholder"
            clearable
          />
          <div
            v-if="field.tip"
            class="setting-tip"
          >
            {{ field.tip }}
          </div>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch
            v-model="providerEnabled"
            active-text="是"
            inactive-text="否"
            inline-prompt
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button
          type="primary"
          :loading="saving"
          @click="saveProvider"
        >
          确定
        </el-button>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="UploadSettings">
import { getUploadSettings, saveUploadSettings } from '@/api/system/upload'

const { proxy } = getCurrentInstance()

const activeNames = ref(['notice'])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const activeProvider = ref(null)
const providerEnabled = ref(false)
const providerForm = reactive({})

const providerKey = 'upload.storage.provider'

const providers = [
  {
    key: 'local',
    name: '本地存储',
    description: '存储在本地服务器',
    fields: []
  },
  {
    key: 'qiniu',
    name: '七牛云存储',
    description: '存储在七牛云，请前往七牛云开通存储服务',
    fields: [
      {
        key: 'upload.qiniu.bucket',
        label: '存储空间（Bucket）',
        placeholder: '存储空间名称'
      },
      {
        key: 'upload.qiniu.accessKey',
        label: 'ACCESS_KEY（AK）',
        placeholder: '请输入 AccessKey'
      },
      {
        key: 'upload.qiniu.secretKey',
        label: 'SECRET_KEY（SK）',
        secret: true,
        placeholder: '请输入 SecretKey'
      },
      {
        key: 'upload.qiniu.domain',
        label: '空间域名（Domain）',
        placeholder: 'http://',
        tip: '请补全http://或https://，例如https://static.cloud.com/'
      }
    ]
  },
  {
    key: 'aliyun',
    name: '阿里云OSS',
    description: '存储在阿里云，请前往阿里云开通存储服务',
    fields: [
      {
        key: 'upload.aliyun.bucket',
        label: '存储空间（Bucket）',
        placeholder: '存储空间名称'
      },
      {
        key: 'upload.aliyun.endpoint',
        label: '访问节点（Endpoint）',
        placeholder: 'https://oss-cn-hangzhou.aliyuncs.com',
        tip: '用于服务端上传，请填写 OSS 访问节点，不是 CDN 域名'
      },
      {
        key: 'upload.aliyun.accessKeyId',
        label: 'AccessKeyId',
        placeholder: '请输入 AccessKeyId'
      },
      {
        key: 'upload.aliyun.accessKeySecret',
        label: 'AccessKeySecret',
        secret: true,
        placeholder: '请输入 AccessKeySecret'
      },
      {
        key: 'upload.aliyun.dir',
        label: '目录（Dir）',
        placeholder: '例如 mall',
        tip: '对象存储中的相对目录前缀；留空时文件直接放在 Bucket 根目录，不按日期分目录。'
      },
      {
        key: 'upload.aliyun.domain',
        label: '空间域名（Domain）',
        placeholder: 'https://',
        tip: '请补全 http:// 或 https://，例如 https://static.cloud.com/'
      }
    ]
  },
  {
    key: 'tencent',
    name: '腾讯云COS',
    description: '存储在腾讯云，请前往腾讯云开通存储服务',
    fields: [
      {
        key: 'upload.tencent.bucket',
        label: '存储空间（Bucket）',
        placeholder: '存储空间名称'
      },
      {
        key: 'upload.tencent.region',
        label: '所属地区（Region）',
        placeholder: '请输入 Region'
      },
      {
        key: 'upload.tencent.secretId',
        label: 'SecretId',
        placeholder: '请输入 SecretId'
      },
      {
        key: 'upload.tencent.secretKey',
        label: 'SecretKey',
        secret: true,
        placeholder: '请输入 SecretKey'
      },
      {
        key: 'upload.tencent.domain',
        label: '空间域名（Domain）',
        placeholder: 'http://',
        tip: '请补全http://或https://，例如https://static.cloud.com/'
      }
    ]
  }
]

const providerRows = computed(() => {
  const currentProvider = providerForm[providerKey] || 'local'
  return providers.map(item => ({
    ...item,
    enabled: currentProvider === item.key
  }))
})

const activeFields = computed(() => activeProvider.value?.fields || [])

function initForm () {
  providerForm[providerKey] = 'local'
  providers.forEach(provider => {
    provider.fields.forEach(field => {
      providerForm[field.key] = ''
    })
  })
}

function getList () {
  loading.value = true
  initForm()
  getUploadSettings().then(response => {
    const settings = response.data || {}
    providerForm[providerKey] = settings.provider || 'local'
    const configs = {
      qiniu: settings.qiniu || {},
      aliyun: settings.aliyun || {},
      tencent: settings.tencent || {}
    }
    providers.forEach(provider => {
      provider.fields.forEach(field => {
        const providerName = provider.key === 'qiniu'
          ? 'qiniu'
          : provider.key === 'aliyun'
            ? 'aliyun'
            : provider.key === 'tencent' ? 'tencent' : ''
        const property = field.key.split('.').pop()
        if (providerName && Object.prototype.hasOwnProperty.call(configs[providerName], property)) {
          providerForm[field.key] = configs[providerName][property] || ''
        }
      })
    })
  }).finally(() => {
    loading.value = false
  })
}

function openProvider (provider) {
  activeProvider.value = provider
  providerEnabled.value = providerForm[providerKey] === provider.key
  dialogVisible.value = true
}

function toggleProvider (provider, enabled) {
  activeProvider.value = provider
  providerEnabled.value = enabled
  saveProvider()
}

async function saveProvider () {
  if (!activeProvider.value) return
  saving.value = true
  try {
    const nextProvider = providerEnabled.value ? activeProvider.value.key : 'local'
    providerForm[providerKey] = nextProvider
    await saveUploadSettings({
      provider: providerForm[providerKey],
      qiniu: {
        bucket: providerForm['upload.qiniu.bucket'],
        accessKey: providerForm['upload.qiniu.accessKey'],
        secretKey: providerForm['upload.qiniu.secretKey'],
        domain: providerForm['upload.qiniu.domain']
      },
      aliyun: {
        bucket: providerForm['upload.aliyun.bucket'],
        endpoint: providerForm['upload.aliyun.endpoint'],
        accessKeyId: providerForm['upload.aliyun.accessKeyId'],
        accessKeySecret: providerForm['upload.aliyun.accessKeySecret'],
        dir: providerForm['upload.aliyun.dir'],
        domain: providerForm['upload.aliyun.domain']
      },
      tencent: {
        bucket: providerForm['upload.tencent.bucket'],
        region: providerForm['upload.tencent.region'],
        secretId: providerForm['upload.tencent.secretId'],
        secretKey: providerForm['upload.tencent.secretKey'],
        domain: providerForm['upload.tencent.domain']
      }
    })
    proxy.$modal.msgSuccess('保存成功')
    dialogVisible.value = false
    getList()
  } finally {
    saving.value = false
  }
}

getList()
</script>

<style scoped lang="scss">
.upload-settings {
  .upload-notice {
    margin-bottom: 18px;
  }

  :deep(.upload-notice .el-collapse-item__content) {
    color: var(--el-text-color-secondary);
    line-height: 26px;
  }

  :deep(.el-divider) {
    margin: 24px 0;
  }

  .provider-table {
    width: 100%;
  }

  .provider-form {
    padding: 10px 24px 0 8px;
  }

  .provider-form :deep(.el-form-item) {
    align-items: flex-start;
    margin-bottom: 22px;
  }

  .provider-form :deep(.el-form-item__label) {
    line-height: 32px;
  }

  .provider-form :deep(.el-form-item__content) {
    display: block;
  }

  .readonly-value {
    line-height: 32px;
    color: var(--el-text-color-primary);
  }

  .setting-tip {
    margin-top: 8px;
    color: var(--el-text-color-secondary);
    font-size: 13px;
    line-height: 20px;
  }

  @media (max-width: 768px) {
    .provider-form {
      padding: 0;
    }
  }
}
</style>
