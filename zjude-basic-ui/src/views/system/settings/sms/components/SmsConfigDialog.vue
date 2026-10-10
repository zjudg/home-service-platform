<template>
  <el-dialog
    :model-value="modelValue"
    title="短信设置"
    width="620px"
    append-to-body
    destroy-on-close
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form
      :model="form"
      label-width="120px"
      class="sms-form"
    >
      <el-form-item label="短信通道">
        <span class="readonly-value">阿里云</span>
      </el-form-item>
      <el-form-item label="短信签名">
        <el-input
          v-model="form.signature"
          placeholder="请输入短信签名"
          clearable
        />
      </el-form-item>
      <el-form-item label="APP_KEY">
        <el-input
          v-model="form.appKey"
          placeholder="请输入阿里云短信 APP_KEY"
          clearable
        />
      </el-form-item>
      <el-form-item label="SECRET_KEY">
        <el-input
          v-model="form.secretKey"
          type="password"
          show-password
          placeholder="请输入阿里云短信 SECRET_KEY"
          clearable
        />
      </el-form-item>
      <el-form-item label="短信签名名称">
        <el-input
          v-model="form.signName"
          placeholder="请输入阿里云审核通过的签名名称"
          clearable
        />
      </el-form-item>
      <el-form-item label="服务端点">
        <el-input
          v-model="form.endpoint"
          placeholder="dysmsapi.aliyuncs.com"
          clearable
        />
      </el-form-item>
      <el-form-item label="地域 ID">
        <el-input
          v-model="form.regionId"
          placeholder="cn-hangzhou"
          clearable
        />
      </el-form-item>
      <el-form-item label="登录模板编号">
        <el-input
          v-model="form.loginTemplate"
          placeholder="SMS_XXXXXXXX"
          clearable
        />
      </el-form-item>
      <el-form-item label="注册模板编号">
        <el-input
          v-model="form.registerTemplate"
          placeholder="SMS_XXXXXXXX"
          clearable
        />
      </el-form-item>
      <el-form-item label="重置密码模板">
        <el-input
          v-model="form.resetPasswordTemplate"
          placeholder="SMS_XXXXXXXX"
          clearable
        />
      </el-form-item>
      <el-form-item label="订单通知模板">
        <el-input
          v-model="form.orderTemplate"
          placeholder="SMS_XXXXXXXX"
          clearable
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-switch
          v-model="form.status"
          active-value="1"
          inactive-value="0"
          active-text="开启"
          inactive-text="关闭"
          inline-prompt
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button
        type="primary"
        :loading="saving"
        @click="$emit('save')"
      >
        确定
      </el-button>
      <el-button
        :loading="saving"
        @click="$emit('validate')"
      >
        校验配置
      </el-button>
      <el-button
        :loading="saving"
        @click="testSms"
      >
        发送测试
      </el-button>
      <el-button @click="$emit('update:modelValue', false)">
        取消
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  form: {
    type: Object,
    required: true
  },
  saving: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'save', 'validate', 'test'])

function testSms () {
  const mobile = window.prompt('请输入测试手机号')
  const code = window.prompt('请输入测试验证码')
  if (mobile && code) {
    emit('test', { mobile, code })
  }
}
</script>

<style scoped lang="scss">
.sms-form {
  padding: 10px 28px 0 12px;
}

.sms-form :deep(.el-form-item) {
  align-items: flex-start;
  margin-bottom: 22px;
}

.sms-form :deep(.el-form-item__label) {
  line-height: 32px;
}

.readonly-value {
  line-height: 32px;
  color: var(--el-text-color-primary);
}

@media (max-width: 768px) {
  .sms-form {
    padding: 0;
  }
}
</style>
