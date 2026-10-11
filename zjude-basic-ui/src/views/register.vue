<template>
  <div class="register">
    <el-form
      ref="registerRef"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
    >
      <h3 class="title">
        <span class="brand-mark">J</span>
        <span>ZJUDE</span>
        <small>WORKSPACE</small>
      </h3>
      <el-form-item prop="username">
        <el-input
          v-model="registerForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix>
            <svg-icon
              icon-class="user"
              class="el-input__icon input-icon"
            />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix>
            <svg-icon
              icon-class="password"
              class="el-input__icon input-icon"
            />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter="handleRegister"
        >
          <template #prefix>
            <svg-icon
              icon-class="password"
              class="el-input__icon input-icon"
            />
          </template>
        </el-input>
      </el-form-item>
      <el-form-item
        v-if="captchaEnabled"
        prop="code"
      >
        <el-input
          v-model="registerForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleRegister"
        >
          <template #prefix>
            <svg-icon
              icon-class="validCode"
              class="el-input__icon input-icon"
            />
          </template>
        </el-input>
        <div class="register-code">
          <img
            :src="codeUrl"
            class="register-code-img"
            @click="getCode"
          />
        </div>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right;">
          <router-link
            class="link-type"
            :to="'/login'"
          >
            使用已有账户登录
          </router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>© 2026 ZJUDE · Software Development Workspace</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import { getCodeImg, register } from '@/api/login'

const router = useRouter()
const { proxy } = getCurrentInstance()

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: ''
})

const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, trigger: 'blur', message: '请输入您的账号' },
    { min: 2, max: 20, message: '用户账号长度必须介于 2 和 20 之间', trigger: 'blur' }
  ],
  password: [
    { required: true, trigger: 'blur', message: '请输入您的密码' },
    { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, trigger: 'blur', message: '请再次输入您的密码' },
    { required: true, validator: equalToPassword, trigger: 'blur' }
  ],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}

const codeUrl = ref('')
const loading = ref(false)
const captchaEnabled = ref(true)

function handleRegister () {
  proxy.$refs.registerRef.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value).then(res => {
        const username = registerForm.value.username
        ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + ' 注册成功！</font>', '系统提示', {
          dangerouslyUseHTMLString: true,
          type: 'success',
        }).then(() => {
          router.push('/login')
        }).catch(() => {})
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode () {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = res.img && res.img.startsWith('data:')
        ? res.img
        : 'data:image/png;base64,' + res.img
      registerForm.value.uuid = res.uuid
    }
  })
}

getCode()
</script>

<style lang='scss' scoped>
.register {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  overflow: hidden;
  background:
    radial-gradient(circle at 14% 18%, rgb(56 189 248 / 14%), transparent 30%),
    linear-gradient(135deg, #071a2b 0%, #0b2942 52%, #0e7490 100%);
}

.register::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.28;
  background-image:
    linear-gradient(rgb(125 211 252 / 8%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(125 211 252 / 8%) 1px, transparent 1px);
  background-size: 48px 48px;
}
.title {
  display: flex;
  align-items: baseline;
  gap: 9px;
  margin: 0 auto 32px;
  color: #0f172a;
  font-size: 26px;
  letter-spacing: 0.08em;
}

.title small {
  color: #64748b;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  font-size: 20px;
  letter-spacing: 0;
}

.register-form {
  position: relative;
  z-index: 1;
  width: min(400px, calc(100% - 40px));
  padding: 36px 34px 24px;
  border: 1px solid rgb(255 255 255 / 60%);
  border-radius: 14px;
  background: rgb(255 255 255 / 96%);
  box-shadow: 0 24px 70px rgb(2 20 35 / 32%);
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.register-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.register-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgb(226 232 240 / 72%);
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.register-code-img {
  height: 40px;
  padding-left: 12px;
}
</style>
