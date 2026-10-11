<template>
  <div class="app-container notification-settings">
    <el-card shadow="never">
      <template #header>
        <PageHeader
          title="通知设置"
          hint="配置各个业务场景下的通知方式和内容模板"
        />
      </template>

      <NoticePanel>
        <div>* 平台配置在各个场景下的通知发送方式和内容模板。</div>
      </NoticePanel>

      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="通知会员"
          name="member"
        >
          <NotificationMemberTable
            :scenes="notificationScenes"
            :channels="channels"
            :get-config="getChannelConfig"
            @toggle="toggleChannel"
            @record="openRecord"
            @config="openConfig"
          />
        </el-tab-pane>
        <el-tab-pane
          label="通知平台"
          name="platform"
        >
          <NotificationPlatformTable
            :channels="channels"
            :status="platformStatus"
            @toggle="togglePlatform"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <NotificationConfigDialog
      v-model="configDialogVisible"
      :scene="activeScene"
      :channel="activeChannel"
      :form="configForm"
      :saving="saving"
      @save="saveConfig"
    />
    <NotificationRecordDialog v-model="recordDialogVisible" />
  </div>
</template>

<script setup name="NotificationSettings">
import { listConfig, saveConfig as savePageConfig } from '@/api/settings/notification'
import NoticePanel from '@/components/Settings/NoticePanel'
import PageHeader from '@/components/Settings/PageHeader'
import NotificationConfigDialog from './components/NotificationConfigDialog'
import NotificationMemberTable from './components/NotificationMemberTable'
import NotificationPlatformTable from './components/NotificationPlatformTable'
import NotificationRecordDialog from './components/NotificationRecordDialog'
import { channels, notificationScenes } from './data'

const { proxy } = getCurrentInstance()

const activeTab = ref('member')
const saving = ref(false)
const configDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const activeScene = ref(null)
const activeChannel = ref(null)
const activeConfigKey = ref('')
const configRecords = ref({})
const platformRecords = ref({})
const platformStatus = reactive({
  system: '1',
  sms: '1',
  wechat: '1',
  mini: '1'
})
const configForm = reactive({
  enabled: '1',
  title: '',
  content: '',
  appPush: '1',
  templateId: '',
  templateNo: '',
  templateName: '',
  fields: [],
  preview: '',
  footer: ''
})

function configKey (scene, channel) {
  return `notification.${scene.key}.${channel.key}`
}

function defaultConfig (scene) {
  return {
    enabled: '1',
    title: scene.name,
    content: `{nickname}您好！您的${scene.name}，订单号{order_sn}，时间{time}。`,
    appPush: '1',
    templateId: '',
    templateNo: '',
    templateName: `${scene.name}通知`,
    fields: scene.variables.slice(0, 4).map((item, index) => ({
      id: `${scene.key}-${index}`,
      name: item[0],
      key: `keyword${index + 1}`,
      value: item[1]
    })),
    preview: scene.variables
      .slice(0, 4)
      .map(item => `${item[0]}：{{${item[1].replace(/[{}]/g, '')}.DATA}}`)
      .join('\n'),
    footer: ''
  }
}

function getChannelConfig (scene, channelKey) {
  const record = configRecords.value[configKey(scene, { key: channelKey })]
  if (!record) return defaultConfig(scene)
  try {
    return { ...defaultConfig(scene), ...JSON.parse(record.configValue) }
  } catch {
    return defaultConfig(scene)
  }
}

function getList () {
  listConfig({ pageNum: 1, pageSize: 500 }).then(response => {
    const records = {}
    response.rows.forEach(item => {
      if (!item.configKey.startsWith('notification.')) return
      if (item.configKey.startsWith('notification.platform.')) {
        platformRecords.value[item.configKey] = item
        const key = item.configKey.replace('notification.platform.', '').replace('.enabled', '')
        if (platformStatus[key] !== undefined) platformStatus[key] = item.configValue
      } else {
        records[item.configKey] = item
      }
    })
    configRecords.value = records
  })
}

function openConfig (scene, channel) {
  activeScene.value = scene
  activeChannel.value = channel
  activeConfigKey.value = configKey(scene, channel)
  Object.assign(configForm, defaultConfig(scene), getChannelConfig(scene, channel.key))
  configForm.fields = (configForm.fields || []).map(field => ({ ...field }))
  configDialogVisible.value = true
}

function allSettings () {
  const settings = {}
  Object.entries(configRecords.value).forEach(([key, record]) => {
    settings[key] = record.configValue
  })
  Object.entries(platformRecords.value).forEach(([key, record]) => {
    settings[key] = record.configValue
  })
  Object.entries(platformStatus).forEach(([platform, value]) => {
    const key = `notification.platform.${platform}.enabled`
    if (settings[key] === undefined) settings[key] = value
  })
  return settings
}

function setConfigValue (key, value) {
  configRecords.value[key] = {
    ...(configRecords.value[key] || {}),
    configKey: key,
    configValue: value
  }
}

async function saveConfig () {
  if (!activeScene.value || !activeChannel.value) return
  saving.value = true
  try {
    const key = activeConfigKey.value
    setConfigValue(key, JSON.stringify(configForm))
    await savePageConfig(allSettings())
    proxy.$modal.msgSuccess('保存成功')
    configDialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function toggleChannel (scene, channelKey) {
  const channel = channels.find(item => item.key === channelKey)
  const current = getChannelConfig(scene, channelKey)
  current.enabled = current.enabled === '1' ? '0' : '1'
  activeScene.value = scene
  activeChannel.value = channel
  activeConfigKey.value = configKey(scene, channel)
  Object.assign(configForm, current)
  await saveConfig()
}

function openRecord () {
  recordDialogVisible.value = true
}

async function togglePlatform (platform, enabled) {
  platformStatus[platform.key] = enabled
  const key = `notification.platform.${platform.key}.enabled`
  if (!platformRecords.value[key]) {
    platformRecords.value[key] = {
      configKey: key,
      configValue: platformStatus[platform.key]
    }
  } else {
    platformRecords.value[key].configValue = platformStatus[platform.key]
  }
  await savePageConfig(allSettings())
}

getList()
</script>
