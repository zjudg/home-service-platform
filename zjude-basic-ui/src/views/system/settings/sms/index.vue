<template>
  <div class="app-container sms-settings">
    <el-card shadow="never">
      <template #header>
        <PageHeader
          title="短信设置"
          hint="配置短信通道和短信发送记录"
        />
      </template>

      <NoticePanel>
        <div>* 配置阿里云短信 AccessKey、签名和不同业务场景的模板编号。</div>
        <div>* 短信通道只能开启一个，选择一个启用后，其他通道会自动关闭。</div>
        <div>* 验证码模板变量必须包含 code，例如：{"code":"验证码"}。</div>
      </NoticePanel>

      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="短信配置"
          name="config"
        >
          <SmsChannelTable
            :rows="channelRows"
            :loading="loading"
            @config="openConfig"
          />
        </el-tab-pane>
        <el-tab-pane
          label="发送记录"
          name="records"
        >
          <SmsRecordTable
            :query="recordQuery"
            :rows="filteredRecords"
            @query="queryRecords"
            @reset="resetRecordQuery"
            @view="viewRecord"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <SmsConfigDialog
      v-model="dialogVisible"
      :form="smsForm"
    :saving="saving"
    @save="saveConfig"
    @validate="validateConfig"
    @test="testConfig"
    />
    <SmsRecordDialog
      v-model="recordDialogVisible"
      :record="currentRecord"
    />
  </div>
</template>

<script setup name="SmsSettings">
import { listConfig, saveConfig as savePageConfig, testSms, validateSms } from '@/api/settings/sms'
import NoticePanel from '@/components/Settings/NoticePanel'
import PageHeader from '@/components/Settings/PageHeader'
import SmsChannelTable from './components/SmsChannelTable'
import SmsConfigDialog from './components/SmsConfigDialog'
import SmsRecordDialog from './components/SmsRecordDialog'
import SmsRecordTable from './components/SmsRecordTable'

const { proxy } = getCurrentInstance()

const activeTab = ref('config')
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const recordDialogVisible = ref(false)
const currentRecord = ref(null)
const configRecords = ref({})

const smsForm = reactive({
  signature: '意象',
  appKey: '',
  secretKey: '',
  signName: '',
  endpoint: 'dysmsapi.aliyuncs.com',
  regionId: 'cn-hangzhou',
  loginTemplate: '',
  registerTemplate: '',
  resetPasswordTemplate: '',
  orderTemplate: '',
  status: '1'
})

const recordQuery = reactive({
  title: '',
  mobile: '',
  status: '',
  dateRange: []
})

const smsSettings = [
  {
    key: 'sms.aliyun.signature',
    field: 'signature',
    label: '短信签名',
    tip: '阿里云短信签名'
  },
  {
    key: 'sms.aliyun.appKey',
    field: 'appKey',
    label: 'APP_KEY',
    tip: '阿里云短信 APP_KEY'
  },
  {
    key: 'sms.aliyun.signName',
    field: 'signName',
    label: '短信签名名称',
    tip: '阿里云短信控制台审核通过的签名名称'
  },
  {
    key: 'sms.aliyun.endpoint',
    field: 'endpoint',
    label: '服务端点',
    tip: '默认 dysmsapi.aliyuncs.com'
  },
  {
    key: 'sms.aliyun.regionId',
    field: 'regionId',
    label: '地域 ID',
    tip: '默认 cn-hangzhou'
  },
  {
    key: 'sms.aliyun.template.login',
    field: 'loginTemplate',
    label: '登录模板编号',
    tip: '模板变量需要包含 code'
  },
  {
    key: 'sms.aliyun.template.register',
    field: 'registerTemplate',
    label: '注册模板编号',
    tip: '模板变量需要包含 code'
  },
  {
    key: 'sms.aliyun.template.resetPassword',
    field: 'resetPasswordTemplate',
    label: '重置密码模板编号',
    tip: '模板变量需要包含 code'
  },
  {
    key: 'sms.aliyun.template.order',
    field: 'orderTemplate',
    label: '订单通知模板编号',
    tip: '订单通知模板可使用业务自定义变量'
  },
  {
    key: 'sms.aliyun.secretKey',
    field: 'secretKey',
    label: 'SECRET_KEY',
    tip: '阿里云短信 SECRET_KEY'
  },
  {
    key: 'sms.aliyun.status',
    field: 'status',
    label: '状态',
    tip: '短信通道启用状态'
  }
]

const channelRows = computed(() => [
  {
    channel: '阿里云',
    description: '阿里云短信服务（Short Message Service）',
    enabled: smsForm.status === '1'
  }
])

const sendRecords = ref([])
const filteredRecords = computed(() => {
  return sendRecords.value.filter(item => {
    const matchesTitle = !recordQuery.title || item.title?.includes(recordQuery.title)
    const matchesMobile = !recordQuery.mobile || item.mobile?.includes(recordQuery.mobile)
    const matchesStatus = !recordQuery.status || item.status === recordQuery.status
    const matchesStart = !recordQuery.dateRange?.[0] || item.createTime?.slice(0, 10) >= recordQuery.dateRange[0]
    const matchesEnd = !recordQuery.dateRange?.[1] || item.createTime?.slice(0, 10) <= recordQuery.dateRange[1]
    return matchesTitle && matchesMobile && matchesStatus && matchesStart && matchesEnd
  })
})

function getList () {
  loading.value = true
  listConfig({ pageNum: 1, pageSize: 100 }).then(response => {
    const records = {}
    response.rows.forEach(item => {
      const setting = smsSettings.find(config => config.key === item.configKey)
      if (setting) {
        records[item.configKey] = item
        smsForm[setting.field] = item.configKey === 'sms.aliyun.secretKey' ? '' : item.configValue
      }
    })
    configRecords.value = records
  }).finally(() => {
    loading.value = false
  })
}

function openConfig () {
  dialogVisible.value = true
}

async function saveConfig () {
  saving.value = true
  try {
    await savePageConfig({
      'sms.aliyun.signature': String(smsForm.signature ?? ''),
      'sms.aliyun.appKey': String(smsForm.appKey ?? ''),
      'sms.aliyun.secretKey': String(smsForm.secretKey ?? ''),
      'sms.aliyun.signName': String(smsForm.signName ?? ''),
      'sms.aliyun.endpoint': String(smsForm.endpoint ?? ''),
      'sms.aliyun.regionId': String(smsForm.regionId ?? ''),
      'sms.aliyun.template.login': String(smsForm.loginTemplate ?? ''),
      'sms.aliyun.template.register': String(smsForm.registerTemplate ?? ''),
      'sms.aliyun.template.resetPassword': String(smsForm.resetPasswordTemplate ?? ''),
      'sms.aliyun.template.order': String(smsForm.orderTemplate ?? ''),
      'sms.aliyun.status': String(smsForm.status ?? '')
    })
    proxy.$modal.msgSuccess('保存成功')
    dialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function validateConfig () {
  await validateSms()
  proxy.$modal.msgSuccess('短信配置完整')
}

async function testConfig ({ mobile, code }) {
  await testSms({ mobile, code })
  proxy.$modal.msgSuccess('测试短信发送成功')
}

function queryRecords () {
  activeTab.value = 'records'
}

function resetRecordQuery () {
  recordQuery.title = ''
  recordQuery.mobile = ''
  recordQuery.status = ''
  recordQuery.dateRange = []
}

function viewRecord (row) {
  currentRecord.value = row
  recordDialogVisible.value = true
}

getList()
</script>
