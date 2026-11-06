<template>
  <div class="register">
    <el-form
      ref="registerRef"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
    >
      <h3 class="title">
        <span class="brand-mark">妙</span>
        <span>妙洁家政</span>
        <small>服务管理平台</small>
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
      <span>© 2026 妙洁家政服务管理平台 · 版权所有</span>
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
    radial-gradient(circle at 14% 16%, rgba(249, 213, 197, 0.5), transparent 32%),
    radial-gradient(circle at 84% 82%, rgba(253, 236, 228, 0.85), transparent 38%),
    #faf7f5;
}

.register::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.85;
  background-image:
    linear-gradient(rgba(224, 106, 61, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(224, 106, 61, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(to bottom, black, transparent 92%);
}
.title {
  display: flex;
  align-items: baseline;
  gap: 9px;
  margin: 0 auto 28px;
  color: #1f1c1a;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.title small {
  color: #a39b95;
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
  border-radius: 10px;
  color: #fff;
  background: #e06a3d;
  font-size: 18px;
  letter-spacing: 0;
}

.register-form {
  position: relative;
  z-index: 1;
  width: min(400px, calc(100% - 40px));
  padding: 34px 32px 22px;
  border: 1px solid #f2ede9;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 16px 48px rgba(31, 28, 26, 0.08);
  .el-input {
    height: 42px;
    input {
      height: 42px;
    }
  }
  .input-icon {
    height: 40px;
    width: 14px;
    margin-left: 0px;
  }
}
.register-tip {
  font-size: 13px;
  text-align: center;
  color: #a39b95;
}
.register-code {
  width: 33%;
  height: 42px;
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
  color: #a39b95;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.register-code-img {
  height: 40px;
  padding-left: 12px;
  border-radius: 8px;
}
</style>
