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

## 接口地址

整个小程序只维护一个后端地址，位于 `config/index.js`：

```js
export const API_BASE_URL = 'http://127.0.0.1:8888'
```

- 所有 `api/` 模块只写业务路径（如 `/mini/services`），由 `utils/request.js` 统一拼接小程序接口的 `/api` 前缀。
- 微信开发者工具可在本机调试；真机调试时，把 `API_BASE_URL` 改成开发电脑的局域网 IP（例如 `http://192.168.1.10:8888`），手机与电脑需在同一网络。
- 本地 HTTP 调试需在微信开发者工具中关闭“校验合法域名”相关校验；正式发布必须使用 HTTPS 且将域名加入微信公众平台白名单。
- 生产发布时把它改成线上服务地址（建议 `https://`）即可，无需再维护多个环境变量文件。

## 构建

```bash
pnpm run build:h5
pnpm run build:mp-weixin
pnpm run build:app
```

微信小程序 AppID、App 包名、签名和平台权限需要在发布前写入 `manifest.json` 或 HBuilderX 项目设置。

## 目录约定

- `api/`: 后端接口模块
- `config/index.js`: 唯一的后端地址配置
- `stores/`: Pinia 状态
- `utils/request.js`: 唯一请求入口
- `platform/`: 平台能力适配
- `pages/`: 页面
- `components/`: 跨页面通用组件

页面和组件不得直接调用 `uni.request`，所有 HTTP 请求必须通过 `api/` 和 `utils/request.js`。
