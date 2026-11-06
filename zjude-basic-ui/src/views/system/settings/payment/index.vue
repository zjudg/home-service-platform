<template>
  <div class="app-container payment-settings">
    <el-card shadow="never">
      <template #header>
        <PageHeader
          title="支付设置"
          hint="家政服务支付方式及支付参数配置"
        >
          <el-button
            type="primary"
            :loading="saving"
            @click="saveAll"
          >
            保存设置
          </el-button>
        </PageHeader>
      </template>

      <NoticePanel>
        <div>* 家政服务支付方式设置，不同应用场景支持的支付方式不同。</div>
        <div>* 请前往微信支付和支付宝平台申请对应的支付端口。</div>
        <div>* 微信支付使用 API v3，需填写商户私钥、商户证书序列号、微信支付公钥和 APIv3 密钥。</div>
        <div>* 支付宝使用 RSA2，私钥和公钥请粘贴完整内容。</div>
      </NoticePanel>

      <el-divider content-position="left">
        支付方式
      </el-divider>
      <PaymentMethodTable
        :rows="paymentRows"
        :loading="loading"
        @config="openConfig"
      />
    </el-card>

    <PaymentConfigDialog
      v-model="dialogVisible"
      :method="activeMethod"
      :fields="activeFields"
      :form="form"
      :saving="saving"
      @save="saveDialog"
      @validate="validateMethod(activeMethod)"
    />
  </div>
</template>

<script setup name="PaymentSettings">
import { ChatDotRound, Coin, Wallet } from '@element-plus/icons-vue'
import { listConfig, saveConfig as savePageConfig, validatePayment } from '@/api/settings/payment'
import NoticePanel from '@/components/Settings/NoticePanel'
import PageHeader from '@/components/Settings/PageHeader'
import PaymentConfigDialog from './components/PaymentConfigDialog'
import PaymentMethodTable from './components/PaymentMethodTable'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const activeMethod = ref(null)
const form = reactive({})
const configRecords = ref({})

const methods = [
  {
    key: 'balance',
    method: '账户余额',
    nameKey: 'payment.balance.name',
    iconKey: 'payment.balance.icon',
    sortKey: 'payment.balance.sort',
    statusKey: 'payment.balance.status',
    iconComponent: Coin,
    defaultName: '余额支付',
    defaultSort: 1
  },
  {
    key: 'wechat',
    method: '微信支付',
    nameKey: 'payment.wechat.name',
    iconKey: 'payment.wechat.icon',
    sortKey: 'payment.wechat.sort',
    statusKey: 'payment.wechat.status',
    iconComponent: ChatDotRound,
    defaultName: '微信支付',
    defaultSort: 2
  },
  {
    key: 'alipay',
    method: '支付宝支付',
    nameKey: 'payment.alipay.name',
    iconKey: 'payment.alipay.icon',
    sortKey: 'payment.alipay.sort',
    statusKey: 'payment.alipay.status',
    iconComponent: Wallet,
    defaultName: '支付宝',
    defaultSort: 3
  }
]

const commonFields = [
  {
    key: 'nameKey',
    label: '支付简称',
    type: 'text',
    placeholder: '请输入支付名称',
    tip: '用户在服务下单页看到的支付名称'
  },
  {
    key: 'iconKey',
    label: '支付图标',
    type: 'image',
    tip: '支付方式图标，建议尺寸：宽100px高100px，jpg、jpeg、png格式'
  },
  {
    key: 'sortKey',
    label: '排序',
    type: 'number',
    tip: '排序越小越前'
  },
  {
    key: 'statusKey',
    label: '状态',
    type: 'status'
  }
]

const wechatStatusField = commonFields.find(field => field.key === 'statusKey')

const providerFields = {
  wechat: [
    { key: 'payment.wechat.appId', label: '微信 AppID', type: 'text', placeholder: '请输入微信公众平台或小程序 AppID' },
    { key: 'payment.wechat.mchId', label: '微信支付商户号', type: 'text', placeholder: '请输入微信支付商户号' },
    { key: 'payment.wechat.certificateSerialNo', label: '商户证书序列号', type: 'text', placeholder: '请输入商户 API 证书序列号' },
    { key: 'payment.wechat.privateKey', label: '微信支付商户私钥', type: 'textarea', secret: true, placeholder: '请粘贴商户 API 私钥完整 PEM 内容' },
    { key: 'payment.wechat.publicKeyId', label: '微信支付公钥 ID', type: 'text', placeholder: 'PUB_KEY_ID_ 开头的公钥 ID' },
    { key: 'payment.wechat.publicKey', label: '微信支付公钥', type: 'textarea', secret: true, placeholder: '请粘贴微信支付公钥完整 PEM 内容' },
    { key: 'payment.wechat.apiV3Key', label: 'APIv3 密钥', type: 'text', secret: true, placeholder: '请输入 32 字节 APIv3 密钥' },
    { key: 'payment.wechat.notifyUrl', label: '支付通知地址', type: 'text', placeholder: 'https://你的域名/api/mini/pay/notify' },
    { key: 'payment.wechat.refundNotifyUrl', label: '退款通知地址', type: 'text', placeholder: 'https://你的域名/api/mini/pay/refund-notify' },
    { key: 'payment.wechat.orderDescriptionPrefix', label: '订单描述前缀', type: 'text', placeholder: '订单描述前缀' }
  ],
  alipay: [
    { key: 'payment.alipay.appId', label: '应用ID', type: 'text', placeholder: '请输入支付宝应用ID' },
    { key: 'payment.alipay.privateKey', label: '应用私钥', type: 'textarea', secret: true, placeholder: '请粘贴应用私钥完整内容' },
    { key: 'payment.alipay.publicKey', label: '支付宝公钥', type: 'textarea', secret: true, placeholder: '请粘贴支付宝公钥完整内容' },
    { key: 'payment.alipay.gateway', label: '网关地址', type: 'text', placeholder: 'https://openapi.alipay.com/gateway.do' },
    { key: 'payment.alipay.signType', label: '签名算法', type: 'text', placeholder: 'RSA2' },
    { key: 'payment.alipay.charset', label: '字符集', type: 'text', placeholder: 'UTF-8' },
    { key: 'payment.alipay.notifyUrl', label: '异步通知地址', type: 'text', placeholder: 'https://example.com/api/payment/alipay/notify' },
    { key: 'payment.alipay.returnUrl', label: '同步跳转地址', type: 'text', placeholder: 'https://example.com/payment/result' }
  ]
}

const secretKeys = new Set([
  'payment.wechat.apiV3Key',
  'payment.wechat.privateKey',
  'payment.wechat.publicKey',
  'payment.alipay.privateKey',
  'payment.alipay.publicKey'
])

const paymentRows = computed(() => methods
  .map(item => ({
    ...item,
    name: form[item.nameKey],
    icon: form[item.iconKey],
    sort: Number(form[item.sortKey] || 0),
    enabled: form[item.statusKey] === '1'
  }))
  .sort((left, right) => left.sort - right.sort))

const activeFields = computed(() => {
  if (!activeMethod.value) return []
  if (activeMethod.value.key === 'wechat') {
    return [...providerFields.wechat, { ...wechatStatusField, key: activeMethod.value.statusKey }]
  }
  const fields = commonFields.map(field => ({
    ...field,
    key: activeMethod.value[field.key]
  }))
  return activeMethod.value.key === 'balance'
    ? fields
    : [...fields.slice(0, 2), ...providerFields[activeMethod.value.key], ...fields.slice(2)]
})

function initForm () {
  methods.forEach(item => {
    form[item.nameKey] = item.defaultName
    form[item.iconKey] = ''
    form[item.sortKey] = item.defaultSort
    form[item.statusKey] = '1'
  })
  Object.values(providerFields).flat().forEach(field => {
    form[field.key] = ''
  })
}

function getList () {
  loading.value = true
  initForm()
  listConfig({ pageNum: 1, pageSize: 200 }).then(response => {
    const records = {}
    response.rows.forEach(item => {
      const known = methods.some(method => [method.nameKey, method.iconKey, method.sortKey, method.statusKey].includes(item.configKey)) ||
        Object.values(providerFields).flat().some(field => field.key === item.configKey)
      if (known) {
        records[item.configKey] = item
        form[item.configKey] = secretKeys.has(item.configKey)
          ? ''
          : item.configValue
      }
    })
    configRecords.value = records
  }).finally(() => {
    loading.value = false
  })
}

function openConfig (row) {
  activeMethod.value = row
  dialogVisible.value = true
}

async function validateMethod (method) {
  await validatePayment(method.key)
  proxy.$modal.msgSuccess(`${method.method}配置完整`)
}

async function saveMethod (method) {
  const keys = method.key === 'wechat'
    ? [method.statusKey]
    : [method.nameKey, method.iconKey, method.sortKey, method.statusKey]
  if (method.key !== 'balance') providerFields[method.key].forEach(field => keys.push(field.key))
  const payload = {}
  keys.forEach(key => {
    payload[key] = String(form[key] ?? '')
  })
  await savePageConfig(payload)
}

async function saveDialog () {
  saving.value = true
  try {
    await saveMethod(activeMethod.value)
    proxy.$modal.msgSuccess('保存成功')
    dialogVisible.value = false
  } finally {
    saving.value = false
  }
}

async function saveAll () {
  saving.value = true
  try {
    const payload = {}
    methods.forEach(method => Object.assign(payload, {
      ...methodPayload(method)
    }))
    await savePageConfig(payload)
    proxy.$modal.msgSuccess('保存成功')
  } finally {
    saving.value = false
  }
}

function methodPayload (method) {
  const keys = method.key === 'wechat'
    ? [method.statusKey]
    : [method.nameKey, method.iconKey, method.sortKey, method.statusKey]
  if (method.key !== 'balance') providerFields[method.key].forEach(field => keys.push(field.key))
  return keys.reduce((payload, key) => {
    payload[key] = String(form[key] ?? '')
    return payload
  }, {})
}

getList()
</script>
