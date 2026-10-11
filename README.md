# Zjude Aden 基础开发框架

Zjude Aden 是一个可复用的后台管理与多端客户端基础开发框架，面向需要快速搭建管理后台、公开首页、用户体系和系统配置能力的项目。

项目聚焦通用基础设施，不预置具体行业业务。商品、订单、支付等领域功能应在明确业务边界和模块归属后单独扩展。

## 项目组成

```text
zjude-aden/
├─ zjude-basic-service/    Java 后端，Spring Boot 多模块工程
├─ zjude-basic-ui/         Vue 3 管理后台
├─ zjude-basic-mini/       uni-app 多端客户端
├─ AGENTS.md               项目开发约定
└─ README.md               项目说明
```

### 后端服务

后端使用 Java 21、Spring Boot 3.2.2、Maven 和 MySQL 8，采用模块化单体架构：

- `zjude-aden-common`：通用工具、基础响应对象和公共能力
- `zjude-aden-framework`：安全、异常、日志、配置和 Web 基础设施
- `zjude-aden-system`：用户、角色、菜单、部门和系统配置
- `zjude-aden-generator`：代码生成能力
- `zjude-aden-integrated`：文件存储、短信等外部能力适配
- `zjude-aden-settings`：公开页面和多端运行时配置
- `zjude-aden-admin`：应用启动模块

后端默认开发端口为 `8379`，开发配置位于 `zjude-basic-service/zjude-aden-admin/src/main/resources/application-dev.yml`。

### 管理后台

管理后台位于 `zjude-basic-ui`，技术栈包括 Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、ECharts 和 TinyMCE。

后台负责权限管理、系统配置、菜单管理、页面装修和多端配置维护。

### uni-app 客户端

客户端位于 `zjude-basic-mini`，使用 JavaScript、Vue 3 和 `<script setup>`，不使用 TypeScript，支持 H5、微信小程序和 Android / iOS App。

首页是公开入口，不要求用户登录。客户端启动时按功能分别加载后台配置：

```text
GET /api/mini/config/site
GET /api/mini/config/navigation
GET /api/mini/config/home
GET /api/mini/config/category
GET /api/mini/config/profile
```

底部导航、标题、颜色、图标和页面装修内容均来自后台接口，不在客户端固定写死。客户端请求统一经过 `zjude-basic-mini/utils/request.js`，页面不得直接调用 `uni.request`。

详细说明见 [zjude-basic-mini/README.md](zjude-basic-mini/README.md)。

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 18+
- pnpm 8+
- MySQL 8
- Redis 6+
- 微信开发者工具（微信小程序开发时使用）

## 快速开始

### 1. 启动后端

先准备 MySQL 和 Redis，并根据本地环境修改后端配置，然后执行：

```bash
cd zjude-basic-service
mvn -B test
mvn spring-boot:run -pl zjude-aden-admin -am
```

后端启动后地址为 `http://localhost:8379`。

### 2. 启动管理后台

```bash
cd zjude-basic-ui
pnpm install --frozen-lockfile
pnpm run dev
```

管理后台的开发代理默认指向 `http://localhost:8379`，具体配置见 `zjude-basic-ui/vite.config.js`。

### 3. 启动多端客户端

```bash
cd zjude-basic-mini
pnpm install --frozen-lockfile
pnpm run dev:h5
```

微信小程序和 App 使用以下命令构建：

```bash
pnpm run build:mp-weixin
pnpm run build:app
```

微信小程序构建产物位于 `zjude-basic-mini/dist/build/mp-weixin`，使用微信开发者工具导入该目录即可预览。

## 客户端接口地址

客户端通过环境变量配置后端地址：

- `.env.development`：本地开发地址
- `.env.staging`：测试环境地址
- `.env.production`：生产构建地址

示例：

```env
VITE_API_BASE_URL=http://localhost:8379
```

发布到真实环境前，必须将该地址替换为实际后端域名，并在微信开发者工具中配置合法的 request 合法域名。不要把密码、Token、私钥或支付证书写入环境文件并提交到仓库。

## 质量检查

后端：

```bash
cd zjude-basic-service
mvn -B test
```

管理后台：

```bash
cd zjude-basic-ui
pnpm install --frozen-lockfile
pnpm run lint
pnpm run test
pnpm run build:prod
```

客户端：

```bash
cd zjude-basic-mini
pnpm run lint
pnpm run test
pnpm run build:h5
pnpm run build:mp-weixin
pnpm run build:app
```

## 开发约定

- Controller 只负责传输适配，业务规则放在应用服务层。
- 管理后台写操作根据需要使用权限校验和操作日志。
- 多写操作在应用服务边界使用事务。
- 新表必须包含明确主键、状态、时间字段和必要索引。
- 移动端页面不得直接调用 `uni.request`。
- 不提交真实密码、Token、AppSecret、私钥和支付证书。
- 使用 Conventional Commits，例如 `feat:`、`fix:`、`docs:`、`test:`、`refactor:`。

## 许可证

本项目使用 MIT License，详见 [LICENSE](LICENSE)。
