<template>
  <div class="app-container website-settings">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>网站设置</span>
            <span class="page-header__hint">配置商城及管理后台的基础品牌信息</span>
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

      <el-form
        ref="websiteFormRef"
        :model="form"
        label-width="150px"
        class="website-form"
      >
        <el-row
          class="settings-grid"
          :gutter="20"
        >
          <el-col
            v-for="item in settings"
            :key="item.key"
            :span="12"
            :xs="24"
          >
            <div class="setting-item">
              <el-form-item :label="item.label">
                <el-input
                  v-if="item.type === 'text'"
                  v-model="form[item.key]"
                  :placeholder="item.placeholder"
                  clearable
                />
                <ImageUpload
                  v-else
                  v-model="form[item.key]"
                  :limit="1"
                  :file-size="item.fileSize"
                  :is-show-tip="false"
                  class="website-image-upload"
                />
                <div class="setting-tip">
                  {{ item.tip }}
                </div>
              </el-form-item>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="WebsiteSettings">
import { getWebsiteSettings, saveConfig } from '@/api/settings/website'
import ImageUpload from '@/components/ImageUpload'

const { proxy } = getCurrentInstance()

const saving = ref(false)
const form = reactive({})

const settings = [
  {
    key: 'website.shopName',
    label: '商城名称',
    type: 'text',
    placeholder: '请输入商城名称',
    default: 'LikeShop',
    tip: '商城名称会显示在网站标题及相关页面中'
  },
  {
    key: 'website.favicon',
    label: '商城浏览器图标',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：32px×32px，支持 jpg、jpeg、png、ico 格式'
  },
  {
    key: 'website.mobileLoginLogo',
    label: '移动端登录页 Logo',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：172px×48px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.mobileLogo',
    label: '移动端商城 Logo',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：172px×48px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.pcLogo',
    label: 'PC 端商城 Logo',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：160px×56px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.adminLoginLogo',
    label: '管理后台登录页 Logo',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：152px×42px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.adminHomeImage',
    label: '管理后台首页主题',
    type: 'image',
    fileSize: 5,
    tip: '建议尺寸：500px×500px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.adminLoginName',
    label: '管理后台登录页名称',
    type: 'text',
    placeholder: '请输入管理后台名称',
    default: 'LikeShop 管理后台',
    tip: '登录页展示的系统名称'
  },
  {
    key: 'website.adminLogo',
    label: '管理后台 Logo',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：116px×30px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.memberDefaultAvatar',
    label: '会员默认头像',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：200px×200px，支持 jpg、jpeg、png 格式'
  },
  {
    key: 'website.goodsDefaultImage',
    label: '商品默认封面',
    type: 'image',
    fileSize: 2,
    tip: '建议尺寸：400px×400px，支持 jpg、jpeg、png 格式'
  }
]

function initForm () {
  settings.forEach(item => {
    form[item.key] = item.default || ''
  })
}

async function getList () {
  initForm()
  const response = await getWebsiteSettings()
  settings.forEach(item => {
    const field = item.key.replace('website.', '')
    if (response.data && response.data[field] !== undefined) {
      form[item.key] = response.data[field] || ''
    }
  })
}

async function submitForm () {
  saving.value = true
  try {
    await saveConfig({ ...form })
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

initForm()
getList()
</script>

<style scoped lang="scss">
.website-settings {
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

  .website-form {
    max-width: 1360px;
  }

  .settings-grid {
    margin-bottom: 4px;
  }

  .setting-item {
    height: 100%;
    padding: 18px 20px 16px;
    border: 1px solid var(--el-border-color-light);
    border-radius: 6px;
    background: var(--el-fill-color-blank);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
  }

  .setting-item:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 4px 14px rgb(0 0 0 / 10%);
  }

  :deep(.setting-item .el-form-item) {
    align-items: flex-start;
    min-height: 112px;
    margin: 0;
  }

  :deep(.setting-item .el-form-item__label) {
    padding-right: 18px;
    color: var(--el-text-color-primary);
    font-weight: 600;
    line-height: 32px;
  }

  :deep(.setting-item .el-form-item__content) {
    display: block;
    min-width: 0;
  }

  .setting-tip {
    min-height: 18px;
    margin-top: 10px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 18px;
  }

  :deep(.website-image-upload .el-upload--picture-card),
  :deep(.website-image-upload .el-upload-list--picture-card .el-upload-list__item) {
    width: 132px;
    height: 82px;
    margin: 0;
  }

  :deep(.website-image-upload .el-upload-list--picture-card .el-upload-list__item) {
    overflow: hidden;
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

    .setting-item {
      padding: 16px;
    }

    :deep(.setting-item .el-form-item__label) {
      width: 132px !important;
      padding-right: 12px;
    }
  }
}
</style>
