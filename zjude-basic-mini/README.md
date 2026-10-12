# zjude-basic-mini

Zjude Aden 的 uni-app Vue 3 多端基础客户端，使用 JavaScript 和 `<script setup>`。

## 目标平台

- H5
- 微信小程序
- Android / iOS App（配置预留）

应用首页为公开入口，无需登录即可访问。登录只用于账户信息等需要身份的功能，未登录用户仍可正常浏览基础首页和设置页面。

## 开发

```bash
pnpm install
pnpm run dev:h5
pnpm run dev:mp-weixin
```

H5 开发环境默认通过 `/api` 代理到 `http://localhost:8379`。微信开发者工具或真机访问本地后端时，不能使用设备自身的 `localhost`；请在未纳入 Git 的 `.env.development.local` 中将 `VITE_API_BASE_URL` 配置为开发电脑的局域网 IP（例如 `http://192.168.1.10:8379`），并确保手机与电脑连接同一网络。生产和测试 API 地址分别配置在 `.env.production`、`.env.staging` 中，真实地址请通过本地环境变量覆盖，不要提交密钥。

## 构建

```bash
pnpm run build:h5
pnpm run build:mp-weixin
pnpm run build:app
```

微信小程序 AppID、App 包名、签名和平台权限需要在发布前写入 `manifest.json` 或 HBuilderX 项目设置。

## 目录约定

- `api/`: 后端接口模块
- `stores/`: Pinia 状态
- `utils/request.js`: 唯一请求入口
- `platform/`: 平台能力适配
- `pages/`: 页面
- `components/`: 跨页面通用组件

页面和组件不得直接调用 `uni.request`，所有 HTTP 请求必须通过 `api/` 和 `utils/request.js`。
