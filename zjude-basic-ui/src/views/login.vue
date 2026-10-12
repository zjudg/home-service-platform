<template>
  <div class="login">
    <div class="login-grid" aria-hidden="true" />
    <div class="login-shell">
      <header class="brand">
        <div class="brand-mark">家</div>
        <div class="brand-copy">
          <span>家政服务运营中台</span>
          <strong>洁序管家</strong>
        </div>
      </header>

      <main class="login-content">
        <section class="login-visual">
          <div class="visual-copy">
            <h1>让订单、师傅、用户和资金流<br>都清清楚楚</h1>
            <p>统一处理预约派单、师傅审核、服务跟进、评价售后和财务结算，适合<br class="desktop-only">家政团队日常高频运营。</p>
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
          <div class="form-eyebrow">工作台登录</div>
          <h2>家政后台管理系统</h2>
          <p class="form-intro">请使用管理员账号进入运营控制台</p>

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
      家政服务管理平台 版权所有
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
  color: #0c2b45;
  background:
    radial-gradient(circle at 90% 4%, rgba(157, 235, 224, 0.52), transparent 26%),
    radial-gradient(circle at 38% 68%, rgba(216, 250, 246, 0.9), transparent 35%),
    linear-gradient(135deg, #fbfefd 0%, #f2fbfa 55%, #fffdf9 100%);
}

.login-grid {
  position: absolute;
  inset: 0;
  opacity: 0.7;
  background-image:
    linear-gradient(rgba(28, 175, 177, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(28, 175, 177, 0.08) 1px, transparent 1px);
  background-size: 42px 42px;
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(1120px, calc(100% - 56px));
  padding-bottom: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 0 0 28px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(145deg, #13b9a4, #13c66a);
  box-shadow: 0 13px 24px rgba(11, 180, 147, 0.2);
  font-size: 29px;
  font-weight: 800;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.brand-copy span {
  color: #087f83;
  font-size: 12px;
  font-weight: 700;
}

.brand-copy strong {
  color: #0a2943;
  font-size: 23px;
  letter-spacing: 0.04em;
}

.login-content {
  display: grid;
  grid-template-columns: minmax(560px, 1fr) 430px;
  gap: clamp(54px, 8vw, 104px);
  align-items: center;
}

.login-visual {
  min-width: 0;
}

.login-visual h1 {
  margin: 0 0 18px;
  color: #092a45;
  font-size: clamp(34px, 3.5vw, 48px);
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0;
}

.login-visual p {
  max-width: 600px;
  margin: 0;
  color: #59758a;
  font-size: 15px;
  line-height: 1.95;
}

.operations-scene {
  position: relative;
  width: 540px;
  height: 285px;
  margin: 34px 0 16px 26px;
}

.scene-board {
  position: absolute;
  left: 0;
  bottom: 20px;
  width: 350px;
  height: 210px;
  border: 1px solid rgba(171, 231, 225, 0.72);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.75);
  box-shadow: 0 22px 38px rgba(32, 157, 163, 0.15);
  transform: perspective(560px) rotateX(5deg) rotateY(-7deg);
}

.board-glow {
  position: absolute;
  top: 0;
  left: 72px;
  width: 172px;
  height: 100%;
  background: linear-gradient(157deg, rgba(33, 194, 171, 0.95), rgba(93, 211, 198, 0.22));
  clip-path: polygon(0 24%, 100% 0, 100% 68%, 0 85%);
  opacity: 0.88;
}

.board-window {
  position: absolute;
  right: 52px;
  bottom: 22px;
  width: 186px;
  height: 110px;
  padding: 13px 15px;
  border: 1px solid rgba(116, 221, 205, 0.62);
  border-radius: 8px;
  background: rgba(242, 255, 252, 0.96);
}

.window-top { display: flex; gap: 7px; margin-bottom: 12px; }
.window-top i { width: 8px; height: 8px; border-radius: 50%; background: #b4f0e1; }
.window-top i:first-child { background: #f4a08c; }
.window-line { width: 112px; height: 6px; margin-bottom: 7px; border-radius: 5px; background: #bdeee4; }
.window-line.line-short { width: 72px; background: #83d8c9; }
.window-bars { display: flex; gap: 10px; margin-top: 14px; align-items: end; }
.window-bars i { width: 12px; height: 18px; border-radius: 3px 3px 0 0; background: #21bea8; }
.window-bars i:nth-child(2) { height: 29px; background: #ffb63a; }
.window-bars i:nth-child(3) { height: 22px; background: #83d8c9; }

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
  border: 1px solid #9fe6da;
  border-radius: 9px;
  background: linear-gradient(180deg, #fafffe, #edfff9);
}

.scene-card span { width: 34px; height: 28px; border: 1px solid #8fe3d2; border-radius: 7px; background: #ddfaf4; }
.scene-card b { position: absolute; bottom: 0; width: 42px; height: 59px; border-radius: 8px 8px 0 0; background: linear-gradient(#ffb218, #ff7615); }

.scene-metric {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 9px;
  min-width: 140px;
  height: 48px;
  padding: 0 15px;
  border: 1px solid rgba(224, 242, 239, 0.9);
  border-radius: 9px;
  color: #16445c;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 10px 22px rgba(27, 119, 130, 0.09);
  font-size: 13px;
  font-weight: 700;
}
.scene-metric b { color: #103d59; }
.metric-icon { color: #018d88; font-size: 19px; font-weight: 400; }
.metric-orders { top: 10px; right: 0; }
.metric-workers { top: 95px; right: -55px; }
.metric-finance { bottom: 12px; right: 47px; }

.scene-progress { position: absolute; left: 28px; bottom: 0; display: flex; gap: 8px; }
.scene-progress i { width: 80px; height: 8px; border-radius: 8px; background: #b6e9e3; }
.scene-progress i:first-child { background: #16b9a7; }
.scene-progress i:nth-child(2) { width: 56px; background: #ffb53a; }

.feature-row { display: flex; gap: 12px; margin-top: 10px; }
.feature-item { display: flex; align-items: center; gap: 9px; width: 146px; height: 43px; padding: 0 13px; border: 1px solid rgba(186, 232, 226, 0.8); border-radius: 8px; color: #194a63; background: rgba(255, 255, 255, 0.52); font-size: 12px; font-weight: 700; }
.feature-item span { color: #f27869; font-size: 21px; }

.form-eyebrow {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 13px;
  border-radius: 18px;
  color: #087f77;
  background: #e1f8f3;
  font-size: 12px;
  font-weight: 700;
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
  width: 430px;
  min-height: 516px;
  padding: 47px 38px 28px;
  border: 1px solid rgba(255, 255, 255, 0.84);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(36, 102, 109, 0.16);
  h2 { margin: 21px 0 4px; color: #102e48; font-size: 27px; line-height: 1.25; }
  .form-intro { margin: 0 0 17px; color: #71869a; font-size: 12px; }
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
  }
  :deep(.el-form-item) { margin-bottom: 19px; }
  :deep(.el-input__wrapper) { padding: 1px 13px; border-radius: 8px; background: #fff !important; box-shadow: 0 0 0 1px #dce5eb inset !important; }
  :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #1bb89d inset, 0 0 0 3px rgba(27, 184, 157, 0.1) !important; }
  :deep(.el-input__inner) { color: #294259 !important; font-size: 14px; }
  :deep(.el-input__inner::placeholder) { color: #9aa9b5 !important; }
  :deep(.el-input__prefix), :deep(.el-input__suffix) { color: #8b9baa !important; }
  :deep(.captcha-item .el-form-item__content) { display: flex; flex-wrap: nowrap; gap: 12px; }
  :deep(.captcha-item .el-input) { flex: 1; width: auto !important; }
  :deep(.el-checkbox) { height: auto; margin: 3px 0 20px; }
  :deep(.el-checkbox__label) { padding-left: 7px; color: #60778b; font-size: 13px; }
  :deep(.el-checkbox__inner) { border-color: #cfdbe2; border-radius: 3px; }
  :deep(.el-button--primary) { width: 100%; height: 46px; border: 0; border-radius: 8px; background: linear-gradient(100deg, #12b7a5, #16c66a); box-shadow: 0 12px 22px rgba(17, 187, 143, 0.2); font-size: 15px; font-weight: 700; }
  :deep(.el-button--primary:hover) { background: linear-gradient(100deg, #0fae9d, #12b960); }
  .submit-item { margin-bottom: 0; }
  .register-link { margin-top: 10px; text-align: right; font-size: 13px;
    .link-type { color: #0b9d8f; }
  }
}

.login-code {
  display: block;
  width: 31%;
  height: 46px;
  padding: 0;
  overflow: hidden;
  border: 0;
  border-radius: 8px;
  background: #f7fafb;
  cursor: pointer;
  .login-code-img { display: block; width: 100%; height: 46px; object-fit: contain; background: #fff; }
}
.el-login-footer {
  position: absolute;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #7b98a1;
  font-size: 12px;
  line-height: 40px;
}

@media (max-width: 980px) {
  .login-shell { width: min(500px, calc(100% - 32px)); padding-bottom: 44px; }
  .brand { justify-content: center; }
  .login-content { display: block; }
  .login-visual { display: none; }
  .login-form { width: 100%; }
}

@media (max-width: 520px) {
  .login-shell { width: calc(100% - 24px); }
  .login-form { min-height: 0; padding: 34px 22px 24px; }
  .login-form h2 { font-size: 24px; }
  .desktop-only { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .login * { transition: none !important; }
}
</style>
