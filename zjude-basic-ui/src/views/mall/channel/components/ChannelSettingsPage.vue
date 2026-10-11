<template>
  <div class="app-container channel-settings">
    <NoticePanel>{{ notice }}</NoticePanel>
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <div><strong>{{ title }}</strong><span class="page-hint">{{ hint }}</span></div>
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
        :model="form"
        label-width="150px"
        class="settings-form"
      >
        <template
          v-for="group in groups"
          :key="group.title"
        >
          <el-divider content-position="left">
            {{ group.title }}
          </el-divider>
          <el-form-item
            v-for="field in groupedFields[group.group]"
            :key="field.key"
            :label="field.label"
            :required="field.required"
          >
            <template v-if="field.type === 'radio'">
              <el-radio-group v-model="form[field.key]">
                <el-radio
                  v-for="option in field.options"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </template>
            <template v-else-if="field.type === 'image'">
              <ImageUpload
                v-model="form[field.key]"
                :limit="1"
                :is-show-tip="false"
              />
              <div class="field-tip">
                {{ field.tip }}
              </div>
            </template>
            <template v-else-if="field.type === 'textarea'">
              <el-input
                v-model="form[field.key]"
                type="textarea"
                :rows="4"
                :placeholder="field.placeholder"
              />
            </template>
            <template v-else>
              <div class="copy-field">
                <el-input
                  v-model="form[field.key]"
                  :type="field.type === 'password' ? 'password' : 'text'"
                  :show-password="field.type === 'password'"
                  :placeholder="field.placeholder"
                  clearable
                /><el-button
                  v-if="field.copy"
                  v-copyText="form[field.key]"
                  type="primary"
                  plain
                >
                  复制
                </el-button>
              </div>
              <div
                v-if="field.tip"
                class="field-tip"
              >
                {{ field.tip }}
              </div>
            </template>
          </el-form-item>
        </template>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { createPageSettingsApi } from '@/api/settings'
import ImageUpload from '@/components/ImageUpload'
import NoticePanel from '@/components/Settings/NoticePanel.vue'

const props = defineProps({ kind: { type: String, required: true } })
const { proxy } = getCurrentInstance()
const saving = ref(false)
const form = reactive({})

const definitions = {
  wechat: {
    page: 'channel-wechat',
    title: '公众号设置',
    hint: '配置微信公众号基础信息、开发者信息和服务器域名。',
    notice: '请先在微信公众平台完成开发者设置，再保存这里的接口参数。',
    groups: ['微信公众号', '公众号开发者信息', '服务器配置', '功能设置'],
    fields: [
      { key: 'channel.wechat.name', label: '公众号名称', placeholder: '请输入公众号名称', group: 0 }, { key: 'channel.wechat.originalId', label: '原始ID', placeholder: '请输入原始ID', group: 0 }, { key: 'channel.wechat.qrcode', label: '公众号二维码', type: 'image', tip: '建议尺寸：400×400像素', group: 0 }, { key: 'channel.wechat.appId', label: 'AppID', required: true, group: 1 }, { key: 'channel.wechat.appSecret', label: 'AppSecret', type: 'password', required: true, group: 1 }, { key: 'channel.wechat.serverUrl', label: 'URL', copy: true, group: 2 }, { key: 'channel.wechat.token', label: 'Token', copy: true, group: 2 }, { key: 'channel.wechat.encodingAesKey', label: 'EncodingAESKey', copy: true, group: 2 }, { key: 'channel.wechat.messageMode', label: '消息加解密方式', type: 'radio', options: [{ label: '明文模式', value: 'plain' }, { label: '兼容模式', value: 'compatible' }, { label: '安全模式', value: 'safe' }], group: 2 }, { key: 'channel.wechat.businessDomain', label: '业务域名', copy: true, group: 3 }, { key: 'channel.wechat.jsapiDomain', label: 'JS接口安全域名', copy: true, group: 3 }, { key: 'channel.wechat.webpageDomain', label: '网页授权域名', copy: true, group: 3 }
    ]
  },
  mini: {
    page: 'channel-mini',
    title: '小程序设置',
    hint: '配置小程序开发者信息、服务器域名和代码上传参数。',
    notice: '请先在微信公众平台完成小程序认证，再配置 AppID 和服务器域名。',
    groups: ['微信小程序', '开发者ID', '服务器域名', '业务域名', '小程序代码上传'],
    fields: [
      { key: 'channel.mini.name', label: '小程序名称', group: 0 }, { key: 'channel.mini.originalId', label: '原始ID', group: 0 }, { key: 'channel.mini.qrcode', label: '小程序码', type: 'image', tip: '建议尺寸：400×400像素', group: 0 }, { key: 'channel.mini.serviceStatus', label: '发货信息管理', type: 'radio', options: [{ label: '开启', value: '1' }, { label: '关闭', value: '0' }], group: 0 }, { key: 'channel.mini.appId', label: 'AppID', required: true, group: 1 }, { key: 'channel.mini.appSecret', label: 'AppSecret', type: 'password', required: true, group: 1 }, { key: 'channel.mini.requestDomain', label: 'request合法域名', copy: true, group: 2 }, { key: 'channel.mini.socketDomain', label: 'socket合法域名', copy: true, group: 2 }, { key: 'channel.mini.uploadFileDomain', label: 'uploadFile合法域名', copy: true, group: 2 }, { key: 'channel.mini.downloadFileDomain', label: 'downloadFile合法域名', copy: true, group: 2 }, { key: 'channel.mini.udpDomain', label: 'udp合法域名', copy: true, group: 2 }, { key: 'channel.mini.tcpDomain', label: 'tcp合法域名', copy: true, group: 2 }, { key: 'channel.mini.businessDomain', label: '业务域名', copy: true, group: 3 }, { key: 'channel.mini.code', label: '小程序代码', type: 'textarea', placeholder: '请输入小程序代码或上传凭据', group: 4 }
    ]
  },
  open: { page: 'channel-open', title: '开放平台设置', hint: '配置微信开放平台 APP 应用信息。', notice: '填写微信开放平台开发配置，请确保应用已完成认证。', groups: ['APP应用'], fields: [{ key: 'channel.open.appId', label: 'AppID', required: true }, { key: 'channel.open.appSecret', label: 'AppSecret', type: 'password', required: true }] },
  h5: { page: 'channel-h5', title: 'H5商城设置', hint: '配置 H5 商城渠道状态和关闭后跳转地址。', notice: '关闭渠道后，访问 H5 商城会按以下规则跳转。', groups: ['H5商城设置'], fields: [{ key: 'channel.h5.status', label: '渠道状态', type: 'radio', options: [{ label: '关闭', value: '0' }, { label: '开启', value: '1' }] }, { key: 'channel.h5.closePage', label: '关闭后访问页面', type: 'radio', options: [{ label: '空白页', value: 'blank' }, { label: '自定义页面', value: 'custom' }] }, { key: 'channel.h5.customCloseUrl', label: '自定义页面URL', placeholder: '请输入完整的url' }, { key: 'channel.h5.url', label: 'H5商城链接', copy: true }] },
  pc: { page: 'channel-pc', title: 'PC商城设置', hint: '配置 PC 商城渠道状态和关闭后跳转地址。', notice: '关闭渠道后，访问 PC 商城会按以下规则跳转。', groups: ['PC商城设置'], fields: [{ key: 'channel.pc.status', label: '渠道状态', type: 'radio', options: [{ label: '关闭', value: '0' }, { label: '开启', value: '1' }] }, { key: 'channel.pc.closePage', label: '关闭后访问页面', type: 'radio', options: [{ label: '空白页', value: 'blank' }, { label: '自定义页面', value: 'custom' }] }, { key: 'channel.pc.customCloseUrl', label: '自定义页面URL', placeholder: '请输入完整的url' }, { key: 'channel.pc.url', label: 'PC商城链接', copy: true }] }
}
const config = computed(() => definitions[props.kind])
const fields = computed(() => config.value.fields)
const groups = computed(() => config.value.groups.map((title, group) => ({ title, group })))
const groupedFields = computed(() => fields.value.reduce((result, field) => { (result[field.group || 0] ||= []).push(field); return result }, {}))
const title = computed(() => config.value.title)
const hint = computed(() => config.value.hint)
const notice = computed(() => config.value.notice)
const api = computed(() => createPageSettingsApi(config.value.page))
function init () { fields.value.forEach(field => { form[field.key] = field.type === 'radio' ? (field.options?.[0]?.value || '') : '' }) }
async function load () { init(); const response = await api.value.listConfig({ pageNum: 1, pageSize: 200 }); (response.rows || []).forEach(item => { if (item.configKey in form) form[item.configKey] = item.configValue }) }
async function save () { saving.value = true; try { await api.value.saveConfig({ ...form }); proxy.$modal.msgSuccess('保存成功') } finally { saving.value = false } }
load()
</script>

<style scoped lang="scss">
.channel-settings { .page-header { display:flex; justify-content:space-between; align-items:center; gap:16px; } .page-hint,.field-tip { color:var(--el-text-color-secondary); font-size:13px; } .page-hint { margin-left:12px; } .settings-form { max-width:1050px; } .copy-field { display:flex; gap:8px; max-width:720px; } .copy-field .el-input { flex:1; } .field-tip { margin-top:8px; line-height:20px; } .settings-form :deep(.el-form-item) { align-items:flex-start; margin-bottom:22px; } .settings-form :deep(.el-form-item__content) { display:block; } }
</style>
