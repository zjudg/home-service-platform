# 上线部署清单

> 适用范围：`zjude-basic-service` / `zjude-basic-ui` / `zjude-basic-mini`
> 最后更新：2026-10-25

## 一、数据库迁移

数据库为 MySQL 5.7/8.x，`spring.flyway.enabled=false`，**迁移需手工按序执行**：

```
db/migration/V12__zjudg_master_profile.sql
db/migration/V13__zjudg_master_menus.sql
db/migration/V14__wechat_mini_openid_and_notify.sql
db/migration/V15__zjudg_payment_and_wechat_keys.sql
db/migration/V16__wechat_subscribe_menu.sql
db/migration/V17__field_encryption.sql
db/migration/V18__zjudg_refund_fields.sql
db/migration/V19__zjudg_settle_withdraw_dispatch.sql
db/migration/V20__zjudg_auto_transfer.sql
db/migration/V21__zjudg_city_dimension.sql
db/migration/V22__zjudg_city_scope_narrow.sql
```

所有脚本均为幂等设计（`information_schema` 判断 + `INSERT IGNORE` / `WHERE NOT EXISTS`），重复执行安全。

执行前请完整备份：

```bash
mysqldump -uroot -p --single-transaction --routines --triggers home-service-system > backup_before_v17.sql
```

**V12/V15 含 `ALTER TABLE`，不可自动回滚。** 如需回滚，请恢复备份。

## 二、必需的环境变量

以下任一缺失，应用**启动即失败**（`ProductionSecretValidator` 拦截）：

| 变量 | 说明 |
|---|---|
| `JASYPT_ENCRYPTOR_PASSWORD` | jasypt 主密钥，用于解密 `ENC(...)` 配置值 |
| `TOKEN_SECRET` | JWT 签名密钥，建议 64 位随机串 |
| `FIELD_CRYPTO_KEY` | 身份证号等敏感字段加密密钥 |

**`FIELD_CRYPTO_KEY` 一旦设定不可随意更换**，更换会导致历史密文无法解密。变更前必须先备份并做数据迁移。

可选调优变量：

| 变量 | 默认 | 说明 |
|---|---|---|
| `DB_POOL_INITIAL_SIZE` | 5 | Druid 初始连接数 |
| `DB_POOL_MIN_IDLE` | 5 | Druid 最小空闲 |
| `DB_POOL_MAX_ACTIVE` | 20 | Druid 最大连接，经验值 `峰值QPS × 平均RT(秒) × 2` |
| `REDIS_POOL_MIN_IDLE` | 2 | Lettuce 最小空闲 |
| `REDIS_POOL_MAX_IDLE` | 8 | Lettuce 最大空闲 |
| `REDIS_POOL_MAX_ACTIVE` | 16 | Lettuce 最大连接 |

## 三、后台必填配置

登录管理后台 → 公共配置，按序完成：

### 1. APP设置

| 字段 | 来源 |
|---|---|
| 小程序 AppID | 微信公众平台 → 开发管理 → 开发设置 |
| 小程序 AppSecret | 同上（敏感，仅服务端使用） |

### 2. 小程序订阅消息

1. 打开「启用订阅消息」开关
2. 在微信公众平台 → 功能 → 订阅消息申请 4 个模板
3. 填入模板 ID

**关键**：模板字段名必须与 `ZjudgNotifyServiceImpl` 中的硬编码一致，否则微信返回 `47003`：

| 场景 | 使用的字段 |
|---|---|
| 师傅接单通知 | `thing1` 服务名称、`time2` 预约时间、`character_string3` 订单号、`thing4` 提示语 |
| 服务完工通知 | `thing1`、`time2`、`character_string3`、`thing4` |
| 支付成功通知 | `thing1`、`character_string2` 订单号、`amount3` 金额、`thing4` |
| 师傅新单提醒 | `thing1`、`time2`、`thing3` 地址、`character_string4` 订单号 |

若申请到的模板字段名不同，需修改该类后重新部署。

### 3. 支付设置

| 字段 | 说明 |
|---|---|
| 微信 AppID | 与小程序 AppID 一致 |
| 商户号 | 微信支付商户平台 |
| APIv3 密钥 | 32 位 |
| 商户证书内容 | 粘贴 `apiclient_cert.pem` 的**文本内容**（含 `-----BEGIN CERTIFICATE-----`） |
| 商户私钥 | 粘贴 `apiclient_key.pem` 的**文本内容**（含 `-----BEGIN PRIVATE KEY-----`） |
| 证书序列号 | 商户证书序列号 |
| 异步通知地址 | `https://你的域名/api/mini/pay/notify` |
| **平台证书** | 粘贴微信支付平台证书的**文本内容**（含 `-----BEGIN CERTIFICATE-----`），用于回调验签 |
| 平台证书序列号 | 微信支付平台证书序列号 |

**证书与私钥均以文本内容录入，不是上传文件，也不需要填写服务器文件路径。** 用记事本打开对应 `.pem` 文件，全选复制粘贴即可。

私钥同时兼容两种格式：`-----BEGIN PRIVATE KEY-----`（PKCS#8）与 `-----BEGIN RSA PRIVATE KEY-----`（PKCS#1）。

**平台证书未配置时的行为**：回调不解密报文，改用主动查单确认。功能可用，但每次回调会多一次微信 API 调用，建议配置。

### 4. 短信设置（如启用手机号登录）

按阿里云短信控制台的 AccessKey、签名、模板编号填写。

### 5. 轮播图

首页轮播读 `zjudg_banner`，在「轮播图管理」中维护：

1. 位置选「首页」
2. 状态保持「正常」，否则不下发
3. 生效时间可留空表示长期有效
4. 跳转类型为「无跳转」时不要填地址；「外部链接」在端内改为复制链接

`zjudg_banner` 中没有可用数据时，首页回退到 `sys_config` 的 `decorate.home.banners` 装修配置。

### 6. 城市维度开关

| 配置键 | 默认 | 说明 |
|---|---|---|
| `zjudg.city.filterEnabled` | 1 | 是否按城市过滤服务 |
| `zjudg.city.defaultCityId` | 1101 | 用户未选城市时的默认城市 |
| `zjudg.city.strict` | 0 | 1=只显示本地服务/时段，0=本地 + 全国 |

**开启 `zjudg.city.strict=1` 之前，先确认目标城市已有按城市发布的服务和时段**，否则该城市列表为空。
当前存量服务 `city_id` 均为 `NULL`（全国），直接开启严格模式会导致所有城市列表为空。

## 四、健康检查

部署后配置探针：

| 端点 | 用途 | 期望 |
|---|---|---|
| `GET /health/live` | 存活探针 | 200，`msg=UP` |
| `GET /health/ready` | 就绪探针 | 200 且 `status=UP`；依赖不可用返回 500 |

两个端点均匿名可访问，响应体不含连接串、异常堆栈等内部信息。

Druid 监控台：`/druid`，账号密码见 `application-prod.yml` 的 `stat-view-servlet`。

## 五、上线验证顺序

按依赖关系依次验证，任一环节失败即停止：

1. **启动**：环境变量齐全，`/health/ready` 返回 UP
2. **微信登录**：小程序授权 → 能拿到 token
3. **下单**：选择服务 → 提交 → 订单状态为「待接单」
4. **支付**：唤起微信支付 → 支付成功 → 订单 `payment_status=1`
5. **师傅入驻**：提交资料 → 后台审核通过 → 账号获得 `master` 角色
6. **接单**：师傅接单 → 用户收到订阅消息
7. **完工**：师傅上传凭证完工 → 用户收到通知
8. **评价**：用户评价 → 师傅评分更新
9. **首页轮播**：`GET /api/mini/config/banners?position=index` 返回后台已上架的轮播

## 六、已知限制

| 项 | 说明 |
|---|---|
| 订阅消息模板字段 | 硬编码在 `ZjudgNotifyServiceImpl`，模板变更需改代码 |
| 身份证加密 | 仅 `zjudg_master.id_card_no` 加密，历史明文由 `v_zjudg_master_plaintext_idcard` 视图检测 |
| 支付退款 | 已实现：用户申请 → 后台审核 → 微信退款；已完成订单不支持自助退款，由平台强制退款 |
| 提现打款 | 审核通过后可选自动调用微信商家转账（zjudg.settle.autoTransfer=1）；未开通商户保持手工打款 |
| 结算提现 | 已实现：完工即时结算、师傅申请提现、后台审核打款 |
| 自动派单 | 已实现：支付后按技能/评分/负载加权派单，5 分钟补偿一次；默认关闭，需在 sys_config 开启 zjudg.dispatch.enabled |
| 严格城市过滤 | 已实现：`zjudg.city.strict=1` 只返回本地服务/时段；开启前须先按城市发布数据 |
| 首页轮播 | 优先读 `zjudg_banner`，为空时回退 `decorate.home.banners`；外部链接在端内复制而非跳转 |

## 七、回滚

1. 应用版本回滚到上一镜像
2. 数据库：`V12`–`V22` 均为增量变更（新增字段/表/配置），旧版本代码可正常忽略新字段，**无需回滚 DDL**
3. 若 `FIELD_CRYPTO_KEY` 已用于生产数据，回滚时务必保留同一密钥
4. 回滚前端后，若 `zjudg.city.strict` 曾改为 `1`，请同步改回 `0`（旧版本不识别该开关）
