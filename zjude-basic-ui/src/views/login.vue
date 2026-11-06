<template>
  <div class="login">
    <div class="login-grid" aria-hidden="true" />
    <div class="login-shell">
      <header class="brand">
        <div class="brand-mark">妙</div>
        <div class="brand-copy">
          <span>妙洁家政</span>
          <strong>妙洁家政服务管理平台</strong>
        </div>
      </header>

      <main class="login-content">
        <section class="login-visual">
          <div class="visual-copy">
            <h1>一次预约，全程安心<br>让每一次上门都被认真对待</h1>
            <p>从接单派单到家政师上门，从服务进度同步到售后回访，妙洁家政把<br class="desktop-only">每一个环节都打理得妥帖，让客户放心托付，让团队省心运营。</p>
          </div>

          <div class="operations-scene" aria-hidden="true">
            <div class="scene-board">
              <div class="board-glow" />
              <div class="board-window">
                <div class="window-top"><i /><i /><i /></div>
                <div class="window-line line-short" />
                <div class="window-line" />
                <div class="window-bars"><i /><i /><i /></div>
              </div>
              <div class="scene-card">
                <span />
                <span />
                <b />
              </div>
            </div>
            <div class="scene-metric metric-orders"><span class="metric-icon">◷</span>今日待派 <b>28</b></div>
            <div class="scene-metric metric-workers"><span class="metric-icon">♧</span>师傅在线 <b>64</b></div>
            <div class="scene-metric metric-finance"><span class="metric-icon">¥</span>结算核对</div>
            <div class="scene-progress"><i /><i /><i /></div>
          </div>

          <div class="feature-row">
            <div class="feature-item"><span>☷</span>订单调度</div>
            <div class="feature-item"><span>♧</span>师傅管理</div>
            <div class="feature-item"><span>▣</span>售后跟进</div>
            <div class="feature-item"><span>¥</span>财务结算</div>
          </div>
        </section>

        <el-form
          ref="loginRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <div class="form-eyebrow">妙洁家政 · 商家后台</div>
          <h2>妙洁家政服务管理平台</h2>
          <p class="form-intro">请使用管理员账号登录，开始今天的服务管理</p>

          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              size="large"
              auto-complete="off"
              placeholder="请输入管理员账号"
            >
              <template #prefix>
                <svg-icon icon-class="user" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              show-password
              auto-complete="off"
              placeholder="请输入登录密码"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg-icon icon-class="password" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>
          <el-form-item v-if="captchaEnabled" prop="code" class="captcha-item">
            <button type="button" class="login-code" aria-label="刷新验证码" @click="getCode">
              <img :src="codeUrl" class="login-code-img" alt="验证码">
            </button>
            <el-input
              v-model="loginForm.code"
              size="large"
              auto-complete="off"
              placeholder="请输入验证码"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg-icon icon-class="validCode" class="el-input__icon input-icon" />
              </template>
            </el-input>
          </el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
          <el-form-item class="submit-item">
            <el-button :loading="loading" size="large" type="primary" @click.prevent="handleLogin">
              <span v-if="!loading">进入管理后台</span>
              <span v-else>正在登录...</span>
            </el-button>
            <div v-if="register" class="register-link">
              <router-link class="link-type" :to="'/register'">立即注册</router-link>
            </div>
          </el-form-item>
        </el-form>
      </main>
    </div>
    <div class="el-login-footer">
      妙洁家政服务管理平台 版权所有
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
  color: #1f1c1a;
  background:
    radial-gradient(circle at 88% 6%, rgba(249, 213, 197, 0.55), transparent 30%),
    radial-gradient(circle at 30% 80%, rgba(253, 236, 228, 0.9), transparent 42%),
    #faf7f5;
}

.login-grid {
  position: absolute;
  inset: 0;
  opacity: 0.8;
  background-image:
    linear-gradient(rgba(224, 106, 61, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(224, 106, 61, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  mask-image: linear-gradient(to bottom, black, transparent 92%);
}

.login-shell {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: min(1120px, calc(100% - 56px));
  max-height: 100%;
  min-height: 0;
  padding: 8px 0 56px;
}

.brand {
  display: flex;
  flex: none;
  align-items: center;
  gap: 13px;
  margin: 0 0 28px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  color: #fff;
  background: #e06a3d;
  box-shadow: 0 10px 22px rgba(224, 106, 61, 0.24);
  font-size: 26px;
  font-weight: 800;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.brand-copy span {
  color: #c9582d;
  font-size: 12px;
  font-weight: 600;
}

.brand-copy strong {
  color: #1f1c1a;
  font-size: 22px;
  letter-spacing: 0.04em;
}

.login-content {
  display: grid;
  flex: 1;
  grid-template-columns: minmax(560px, 1fr) 430px;
  gap: clamp(54px, 8vw, 104px);
  align-items: center;
  min-height: 0;
}

.login-visual {
  min-width: 0;
}

.login-visual h1 {
  margin: 0 0 16px;
  color: #1f1c1a;
  font-size: clamp(32px, 3.4vw, 44px);
  font-weight: 700;
  line-height: 1.24;
  letter-spacing: 0;
}

.login-visual p {
  max-width: 600px;
  margin: 0;
  color: #7a726c;
  font-size: 15px;
  line-height: 1.95;
}

.operations-scene {
  position: relative;
  width: 540px;
  height: 285px;
  margin: 32px 0 16px 24px;
}

.scene-board {
  position: absolute;
  left: 0;
  bottom: 20px;
  width: 350px;
  height: 210px;
  border: 1px solid #f2ede9;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 18px 34px rgba(31, 28, 26, 0.07);
  transform: perspective(560px) rotateX(5deg) rotateY(-7deg);
}

.board-glow {
  position: absolute;
  top: 0;
  left: 72px;
  width: 172px;
  height: 100%;
  background: linear-gradient(157deg, rgba(245, 138, 95, 0.55), rgba(253, 236, 228, 0.2));
  clip-path: polygon(0 24%, 100% 0, 100% 68%, 0 85%);
  opacity: 0.9;
}

.board-window {
  position: absolute;
  right: 52px;
  bottom: 22px;
  width: 186px;
  height: 110px;
  padding: 13px 15px;
  border: 1px solid #f2ede9;
  border-radius: 12px;
  background: #fffcf9;
  box-shadow: 0 2px 10px rgba(31, 28, 26, 0.05);
}

.window-top { display: flex; gap: 7px; margin-bottom: 12px; }
.window-top i { width: 8px; height: 8px; border-radius: 50%; background: #f9d5c5; }
.window-top i:first-child { background: #f58a5f; }
.window-line { width: 112px; height: 6px; margin-bottom: 7px; border-radius: 5px; background: #fdece4; }
.window-line.line-short { width: 72px; background: #f9d5c5; }
.window-bars { display: flex; gap: 10px; margin-top: 14px; align-items: end; }
.window-bars i { width: 12px; height: 18px; border-radius: 3px 3px 0 0; background: #e06a3d; }
.window-bars i:nth-child(2) { height: 29px; background: #d98a1f; }
.window-bars i:nth-child(3) { height: 22px; background: #f9d5c5; }

.scene-card {
  position: absolute;
  right: 76px;
  bottom: 28px;
  display: flex;
  justify-content: center;
  align-items: end;
  gap: 25px;
  width: 208px;
  height: 100px;
  padding-bottom: 42px;
  border: 1px solid #f2ede9;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff, #fff7f3);
  box-shadow: 0 2px 10px rgba(31, 28, 26, 0.05);
}

.scene-card span { width: 34px; height: 28px; border: 1px solid #f9d5c5; border-radius: 8px; background: #fdece4; }
.scene-card b { position: absolute; bottom: 0; width: 42px; height: 59px; border-radius: 8px 8px 0 0; background: linear-gradient(#f58a5f, #e06a3d); }

.scene-metric {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 140px;
  height: 48px;
  padding: 0 15px;
  border: 1px solid #f2ede9;
  border-radius: 12px;
  color: #3d3733;
  background: #fff;
  box-shadow: 0 6px 20px rgba(31, 28, 26, 0.07);
  font-size: 13px;
  font-weight: 600;
}
.scene-metric b { color: #1f1c1a; }
.metric-icon { color: #e06a3d; font-size: 18px; font-weight: 400; }
.metric-orders { top: 10px; right: 0; }
.metric-workers { top: 95px; right: -55px; }
.metric-finance { bottom: 12px; right: 47px; }

.scene-progress { position: absolute; left: 28px; bottom: 0; display: flex; gap: 8px; }
.scene-progress i { width: 80px; height: 8px; border-radius: 8px; background: #f2ede9; }
.scene-progress i:first-child { background: #e06a3d; }
.scene-progress i:nth-child(2) { width: 56px; background: #f58a5f; }

.feature-row { display: flex; gap: 12px; margin-top: 10px; }
.feature-item { display: flex; align-items: center; gap: 9px; width: 146px; height: 43px; padding: 0 13px; border: 1px solid #ece6e1; border-radius: 12px; color: #3d3733; background: #fff; font-size: 12px; font-weight: 600; }
.feature-item span { color: #e06a3d; font-size: 20px; }

.form-eyebrow {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 12px;
  border-radius: 999px;
  color: #c9582d;
  background: #fdece4;
  font-size: 12px;
  font-weight: 600;
}

.title small {
  color: #a39b95;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.login-form {
  align-self: center;
  width: 430px;
  min-height: 516px;
  padding: 44px 36px 26px;
  border: 1px solid #f2ede9;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 16px 48px rgba(31, 28, 26, 0.08);
  h2 { margin: 18px 0 4px; color: #1f1c1a; font-size: 25px; line-height: 1.3; font-weight: 700; }
  .form-intro { margin: 0 0 20px; color: #a39b95; font-size: 12px; }
  .el-input {
    height: 46px;
    input {
      height: 46px;
    }
  }
  .input-icon {
    width: 16px;
    height: 45px;
    margin-left: 1px;
    color: #a39b95;
  }
  :deep(.el-form-item) { margin-bottom: 19px; }
  :deep(.el-input__wrapper) { padding: 1px 13px; border-radius: 12px; background: #fff !important; box-shadow: 0 0 0 1px #ece6e1 inset !important; }
  :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #e06a3d inset, 0 0 0 3px rgba(224, 106, 61, 0.12) !important; }
  :deep(.el-input__inner) { color: #1f1c1a !important; font-size: 14px; }
  :deep(.el-input__inner::placeholder) { color: #a39b95 !important; }
  :deep(.el-input__prefix), :deep(.el-input__suffix) { color: #a39b95 !important; }
  :deep(.captcha-item .el-form-item__content) { display: flex; flex-wrap: nowrap; gap: 12px; }
  :deep(.captcha-item .el-input) { flex: 1; width: auto !important; }
  :deep(.el-checkbox) { height: auto; margin: 3px 0 20px; color: #7a726c; }
  :deep(.el-checkbox__label) { padding-left: 7px; color: #7a726c; font-size: 13px; }
  :deep(.el-checkbox__inner) { border-color: #ece6e1; border-radius: 4px; background: #fff; }
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) { border-color: #e06a3d; background: #e06a3d; }
  :deep(.el-button--primary) {
    width: 100%;
    height: 46px;
    border: 0;
    border-radius: 999px;
    color: #fff !important;
    background: #e06a3d;
    box-shadow: 0 6px 18px rgba(224, 106, 61, 0.24);
    font-size: 15px;
    font-weight: 600;
  }
  :deep(.el-button--primary:hover),
  :deep(.el-button--primary:focus) { background: #c9582d; }
  :deep(.el-button--primary:focus-visible) { box-shadow: 0 0 0 3px rgba(224, 106, 61, 0.24), 0 6px 18px rgba(224, 106, 61, 0.24); }
  .submit-item { margin-bottom: 0; }
  .register-link { margin-top: 10px; text-align: right; font-size: 13px;
    .link-type { color: #c9582d; }
  }
}

.login-code {
  display: block;
  width: 31%;
  height: 46px;
  padding: 0;
  overflow: hidden;
  border: 1px solid #ece6e1;
  border-radius: 12px;
  background: #fbf8f6;
  cursor: pointer;
  .login-code-img { display: block; width: 100%; height: 46px; object-fit: contain; background: #fff; }
}
.el-login-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #a39b95;
  font-size: 12px;
  line-height: 40px;
}

@media (max-width: 980px) {
  .login-shell { width: min(500px, calc(100% - 32px)); padding-bottom: 56px; }
  .brand { justify-content: center; }
  .login-content { display: block; }
  .login-visual { display: none; }
  .login-form { width: 100%; margin: 0 auto; }
}

@media (max-width: 520px) {
  .login-shell { width: calc(100% - 24px); }
  .login-form { min-height: 0; padding: 32px 22px 22px; }
  .login-form h2 { font-size: 22px; }
  .desktop-only { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .login * { transition: none !important; }
}
</style>
