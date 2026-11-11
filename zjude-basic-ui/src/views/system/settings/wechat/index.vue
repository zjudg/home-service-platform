<template>
  <div class="app-container">
    <el-card
      v-loading="loading"
      shadow="never"
    >
      <template #header>
        <div class="page-header">
          <div class="page-header__title">
            <span>小程序订阅消息</span>
            <span class="page-header__hint">配置微信小程序订阅消息模板，用于通知用户、师傅及管理员</span>
          </div>
          <el-button
            type="primary"
            :loading="saving"
            @click="save"
          >
            保存设置
          </el-button>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="tip"
      >
        <template #title>
          模板 ID 需在微信公众平台「功能 → 订阅消息」中申请后填入。管理员账号还需在小程序「我的 → 订单消息提醒」主动授权；微信订阅通常为一次性授权，每条提醒会消耗对应授权。留空模板或未授权时会跳过推送，不影响下单与履约。
        </template>
      </el-alert>

      <el-form
        :model="form"
        label-width="180px"
        class="settings-form"
      >
        <el-form-item label="启用订阅消息">
          <el-switch
            v-model="form['notification.mini.enabled']"
            active-value="1"
            inactive-value="0"
          />
          <div class="setting-tip">
            关闭后所有订阅消息推送都会被跳过
          </div>
        </el-form-item>

        <el-divider content-position="left">
          模板 ID
        </el-divider>

        <el-form-item
          v-for="item in templateFields"
          :key="item.key"
          :label="item.label"
        >
          <el-input
            v-model="form[item.key]"
            :placeholder="item.placeholder"
            clearable
          />
          <div class="setting-tip">
            {{ item.tip }}
          </div>
        </el-form-item>

        <el-divider content-position="left">
          模板字段名
        </el-divider>

        <div class="setting-notice">
          <div>* 换过订阅模板后，这里要按新模板的字段名填写，否则微信会因字段不匹配拒绝发送。</div>
          <div>* 每个模板四个字段，用英文逗号分隔，顺序为：服务名称, 时间, 附加字段, 备注；留空使用默认值。</div>
        </div>

        <el-form-item
          v-for="item in fieldFields"
          :key="item.key"
          :label="item.label"
        >
          <el-input
            v-model="form[item.key]"
            :placeholder="item.placeholder"
            clearable
          />
          <div class="setting-tip">
            {{ item.tip }}
          </div>
        </el-form-item>

        <el-divider content-position="left">
          跳转页面
        </el-divider>

        <el-form-item
          v-for="item in pageFields"
          :key="item.key"
          :label="item.label"
        >
          <el-input
            v-model="form[item.key]"
            :placeholder="item.placeholder"
            clearable
          />
          <div class="setting-tip">
            {{ item.tip }}
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const form = reactive({})

const templateFields = [
  {
    key: 'notification.mini.template.accepted',
    label: '师傅接单通知',
    placeholder: '请输入模板 ID',
    tip: '订单被师傅接单后通知用户，建议模板含服务名称、预约时间、订单号'
  },
  {
    key: 'notification.mini.template.finished',
    label: '服务完工通知',
    placeholder: '请输入模板 ID',
    tip: '师傅点击完工后通知用户确认领取与评价'
  },
  {
    key: 'notification.mini.template.paid',
    label: '支付成功通知',
    placeholder: '请输入模板 ID',
    tip: '用户支付成功后发送，含订单号与金额'
  },
  {
    key: 'notification.mini.template.newOrder',
    label: '师傅新单提醒',
    placeholder: '请输入模板 ID',
    tip: '有订单指派给师傅时提醒师傅'
  },
  {
    key: 'notification.mini.template.adminCreated',
    label: '管理员新订单提醒',
    placeholder: '请输入模板 ID',
    tip: '用户提交订单后提醒管理员；订单可能仍处于待支付状态'
  },
  {
    key: 'notification.mini.template.adminAccepted',
    label: '管理员师傅接单提醒',
    placeholder: '请输入模板 ID',
    tip: '师傅接单成功后提醒管理员'
  }
]

const pageFields = [
  {
    key: 'notification.mini.page.order',
    label: '用户侧跳转页',
    placeholder: '/pages/home/index',
    tip: '用户点击通知后打开的小程序页面'
  },
  {
    key: 'notification.mini.page.masterOrder',
    label: '师傅侧跳转页',
    placeholder: '/pages/master/orders/index',
    tip: '师傅点击通知后打开的小程序页面'
  },
  {
    key: 'notification.mini.page.adminOrder',
    label: '管理员订单通知跳转页',
    placeholder: '/pages/orders/index',
    tip: '管理员点击通知后打开的小程序页面'
  }
]

const fieldFields = [
  {
    key: 'notification.mini.field.accepted',
    label: '师傅接单通知',
    placeholder: 'thing1,time2,character_string3,thing4',
    tip: '第三个字段填订单号字段名'
  },
  {
    key: 'notification.mini.field.finished',
    label: '服务完工通知',
    placeholder: 'thing1,time2,character_string3,thing4',
    tip: '第三个字段填订单号字段名'
  },
  {
    key: 'notification.mini.field.paid',
    label: '支付成功通知',
    placeholder: 'thing1,character_string2,amount3,thing4',
    tip: '第三个字段填金额字段名'
  },
  {
    key: 'notification.mini.field.newOrder',
    label: '师傅新单提醒',
    placeholder: 'thing1,time2,thing3,character_string4',
    tip: '第三个字段填地址字段名'
  },
  {
    key: 'notification.mini.field.adminCreated',
    label: '管理员新订单提醒',
    placeholder: 'thing1,time2,character_string3,thing4',
    tip: '字段顺序：服务名称、预约时间、订单号、订单状态'
  },
  {
    key: 'notification.mini.field.adminAccepted',
    label: '管理员师傅接单提醒',
    placeholder: 'thing1,time2,character_string3,thing4',
    tip: '字段顺序：服务名称、预约时间、订单号、接单状态'
  }
]

const allKeys = [
  'notification.mini.enabled',
  ...templateFields.map(item => item.key),
  ...fieldFields.map(item => item.key),
  ...pageFields.map(item => item.key)
]

function load () {
  loading.value = true
  request({ url: '/manage/settings/wechat-subscribe/config/list', method: 'get' })
    .then(response => {
      const rows = response.rows || response.data?.rows || []
      const map = {}
      rows.forEach(row => { map[row.configKey] = row.configValue })
      allKeys.forEach(key => {
        form[key] = map[key] !== undefined && map[key] !== null ? map[key] : (key === 'notification.mini.enabled' ? '0' : '')
      })
    })
    .finally(() => { loading.value = false })
}

function save () {
  saving.value = true
  request({ url: '/manage/settings/wechat-subscribe', method: 'put', data: { ...form } })
    .then(() => {
      ElMessage.success('保存成功')
      load()
    })
    .finally(() => { saving.value = false })
}

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; align-items: center; justify-content: space-between; }
.page-header__title { display: flex; flex-direction: column; gap: 4px; }
.page-header__title > span:first-child { font-size: 16px; font-weight: 600; }
.page-header__hint { font-size: 12px; color: #909399; }
.tip { margin-bottom: 20px; }
.settings-form { max-width: 760px; }
.setting-tip { font-size: 12px; color: #909399; line-height: 1.6; margin-top: 4px; }
.setting-notice { max-width: 760px; margin: 0 0 16px; padding: 10px 14px; border-left: 3px solid var(--el-color-primary); border-radius: 4px; background: var(--el-fill-color-light); color: var(--el-text-color-regular); font-size: 12px; line-height: 1.8; }
</style>
