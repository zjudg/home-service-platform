<template>
  <div class="login">
    <div class="login-content">
      <section class="login-visual">
        <div class="visual-kicker">ENGINEERING OPERATIONS PLATFORM</div>
        <h1>Build systems.<br><strong>Move work forward.</strong></h1>
        <p>统一管理研发协作、平台能力与业务运行状态。</p>
        <div class="signal-grid" aria-hidden="true">
          <span v-for="item in 12" :key="item" />
        </div>
        <div class="visual-status"><i /> ZJUDE CORE / OPERATIONAL</div>
      </section>

      <el-form
        ref="loginRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <div class="form-eyebrow">SECURE ACCESS / 01</div>
        <h3 class="title">
          <span class="brand-mark">J</span>
          <span>ZJUDE</span>
          <small>WORKSPACE</small>
        </h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
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
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
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
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix>
            <svg-icon
              icon-class="validCode"
              class="el-input__icon input-icon"
            />
          </template>
        </el-input>
        <div class="login-code">
          <img
            :src="codeUrl"
            class="login-code-img"
            @click="getCode"
          />
        </div>
      </el-form-item>
      <el-checkbox
        v-model="loginForm.rememberMe"
        style="margin:0px 0px 25px 0px;"
      >
        记住密码
      </el-checkbox>
        <el-form-item style="width:100%;">
          <el-button
            :loading="loading"
            size="large"
            type="primary"
            style="width:100%;"
            @click.prevent="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登 录 中...</span>
          </el-button>
          <div
            v-if="register"
            style="float: right;"
          >
            <router-link
              class="link-type"
              :to="'/register'"
            >
              立即注册
            </router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>© 2026 ZJUDE · Software Development Workspace</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from '@/api/login'
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: 'admin',
  password: 'admin123',
  rememberMe: false,
  code: '',
  uuid: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入您的账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入您的密码' }],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}

const codeUrl = ref('')
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin () {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set('username', loginForm.value.username, { expires: 30 })
        Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set('rememberMe', loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove('username')
        Cookies.remove('password')
        Cookies.remove('rememberMe')
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== 'redirect') {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || '/', query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
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
      // The backend returns a complete data URI. Keep it intact so the browser
      // receives the correct image MIME type instead of nesting data URIs.
      codeUrl.value = res.img && res.img.startsWith('data:')
        ? res.img
        : 'data:image/png;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie () {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(118deg, #061827 0%, #092b42 55%, #0a5266 100%);
}

.login::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.28;
  background-image:
    linear-gradient(rgb(125 211 252 / 8%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(125 211 252 / 8%) 1px, transparent 1px);
  background-size: 48px 48px;
}

.login::after {
  content: "";
  position: absolute;
  top: 14%;
  left: 50%;
  width: 1px;
  height: 72%;
  background: linear-gradient(transparent, rgb(125 211 252 / 30%), transparent);
}

.login-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(320px, 1fr) 400px;
  gap: clamp(56px, 8vw, 128px);
  align-items: center;
  width: min(980px, calc(100% - 64px));
  margin-bottom: 22px;
}

.login-visual {
  color: #e0f2fe;
}

.visual-kicker,
.form-eyebrow {
  color: #67e8f9;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.login-visual h1 {
  margin: 20px 0 18px;
  color: #f8fafc;
  font-size: clamp(34px, 4vw, 54px);
  line-height: 1.08;
  letter-spacing: -0.02em;
}

.login-visual h1 strong {
  color: #67e8f9;
  font-weight: 700;
}

.login-visual p {
  max-width: 390px;
  margin: 0;
  color: rgba(224, 242, 254, 0.7);
  font-size: 15px;
  line-height: 1.8;
}

.signal-grid {
  display: grid;
  grid-template-columns: repeat(6, 10px);
  gap: 10px;
  margin-top: 56px;
}

.signal-grid span {
  width: 10px;
  height: 10px;
  border: 1px solid rgba(103, 232, 249, 0.4);
  background: rgba(34, 211, 238, 0.08);
}

.signal-grid span:nth-child(3n + 1) {
  background: #22d3ee;
  box-shadow: 0 0 14px rgba(34, 211, 238, 0.8);
}

.visual-status {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 28px;
  color: rgba(186, 230, 253, 0.62);
  font-size: 10px;
  letter-spacing: 0.16em;
}

.visual-status i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22d3ee;
  box-shadow: 0 0 12px #22d3ee;
}

.form-eyebrow {
  margin-bottom: 20px;
  color: #0284c7;
}

.title {
  display: flex;
  align-items: baseline;
  gap: 9px;
  margin: 0 auto 30px;
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

.login-form {
  position: relative;
  z-index: 1;
  width: min(400px, calc(100% - 40px));
  padding: 36px 34px 24px;
  border: 1px solid rgb(255 255 255 / 60%);
  border-radius: 10px;
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

  :deep(.el-button--primary) {
    border: 0;
    background: linear-gradient(100deg, #0284c7, #2563eb);
    box-shadow: 0 8px 20px rgba(37, 99, 235, 0.24);
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
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
.login-code-img {
  height: 40px;
  padding-left: 12px;
}

@media (max-width: 800px) {
  .login::after,
  .login-visual {
    display: none;
  }

  .login-content {
    display: block;
    width: min(420px, calc(100% - 32px));
  }

  .login-form {
    width: 100%;
  }
}
</style>
