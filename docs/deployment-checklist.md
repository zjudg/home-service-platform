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
db/migration/V23__zjudg_user_address.sql
db/migration/V24__zjudg_decoration_menus.sql
db/migration/V25__zjudg_material_permissions.sql
db/migration/V26__zjudg_user_favorite.sql
db/migration/V27__drop_unused_zjudg_config_tables.sql
db/migration/V28__zjudg_schedule_lock.sql
db/migration/V29__zjudg_backend_menu_repair.sql
db/migration/V30__zjudg_master_audit_columns.sql
db/migration/V39__zjudg_route_path_namespace.sql
db/migration/V40__zjudg_master_skill_categories.sql
db/migration/V41__zjudg_master_audit_accepting_guard.sql
```

所有脚本均为幂等设计（`information_schema` 判断 + `INSERT IGNORE` / `WHERE NOT EXISTS`），重复执行安全。

### 执行前先跑检查脚本

手工执行迁移最大的风险是**漏跑**，而且漏跑往往不会在部署时报错，而是运行期某个接口才 500。本项目已经因此踩过四次：

| 漏跑的迁移 | 运行期症状 |
|---|---|
| V11 | 后台订单列表 500：`Unknown column 'o.address'` |
| V23 | 小程序「我的 → 服务地址」报错 |
| V26 | 「我的收藏」报错 |
| V28 | 定时任务每周期报 `Table 'zjudg_schedule_lock' doesn't exist`，自动派单停摆 |
| V30 | 后台「师傅列表」500：`Unknown column 'm.create_by'` |

因此先跑检查脚本，它会只读核对每个迁移应当留下的产物：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check_migrations.ps1
# 可用参数覆盖默认值：-Db / -User / -Password / -Mysql
```

输出 `通过：26 / 26` 表示全部就绪；否则会列出缺失项及其所属迁移文件。脚本**不执行任何 DDL**，可随时在生产库只读运行。

**V28 / V29 必须执行**，否则会出现以下可见故障：

| 迁移 | 不执行的后果 |
|---|---|
| `V28__zjudg_schedule_lock.sql` | 定时任务每个周期报 `Table 'zjudg_schedule_lock' doesn't exist`，自动派单与批量作业整体停摆 |
| `V29__zjudg_backend_menu_repair.sql` | 后台「服务/订单/用户/师傅/财务/素材/页面装修」7 个模块及其 17 个页面**对所有人不可见**；即便菜单出现，接口也因缺少 `zjudg:*` 权限串返回 403 |

**V29 的行为说明**：

- 新增「城市管理」菜单（`M_ZJUDG_CITY`）与 5 个按钮权限，并授予 `admin` 与 `common` 角色
- 删除 4 组空壳/重复菜单：`日志管理`、`地图设置`、`商家入驻设置`、`装修`（含其下 4 个子菜单）
- 清理历史上直接删 `sys_menu` 留下的孤儿授权
- 删除动作均带存在性/组件路径校验，命中不到就跳过，不会误删
- 重复执行安全，已验证连跑 3 次无报错

执行后请确认：

```sql
-- 期望 0：所有角色的菜单授权都能对应到真实菜单
SELECT COUNT(*) FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id WHERE m.menu_id IS NULL;

-- 期望 0：不再有空壳目录
SELECT COUNT(*) FROM sys_menu m
WHERE m.menu_type = 'M' AND NOT EXISTS (SELECT 1 FROM sys_menu c WHERE c.parent_id = m.menu_id);

-- 期望 admin / common 都能看到家政业务页面（当前为 18 个）
SELECT r.role_key, COUNT(DISTINCT rm.menu_id) AS zjudg_pages FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.component LIKE 'zjudg/%'
GROUP BY r.role_key;
```

执行前请完整备份：

```bash
mysqldump -uroot -p --single-transaction --routines --triggers home-service-system > backup_before_v17.sql
```

**V12/V15 含 `ALTER TABLE`，不可自动回滚。** 如需回滚，请恢复备份。

**V27 会删除两张表**：`zjudg_decoration_config` 与 `zjudg_customer_service_config`。
脚本已内置保护：**仅当表为空时才删除**；表内若有数据会跳过删除并保留。
执行后请确认：

```sql
SHOW TABLES LIKE 'zjudg_%config%';
-- 期望只返回 zjudg_schedule_lock 等，不再有 zjudg_decoration_config / zjudg_customer_service_config
```

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

## 五、配置存储位置

**所有业务配置都存 `sys_config`，没有第二套存储。**
新增配置项该改哪几处，见 [`docs/config-map.md`](./config-map.md)。

| 类别 | 前缀 | 后台入口 |
|---|---|---|
| 网站基础设置 | `website.*` | 系统设置 → 网站设置 |
| 页面装修 | `decorate.*` | 运营 → 页面装修 |
| 微信小程序 / 订阅消息 | `wechat.*`、`notification.mini.*` | 系统设置 → 微信设置 |
| 支付 | `payment.*` | 系统设置 → 支付设置 |
| 短信 | `sms.*` | 系统设置 → 短信设置 |
| 派单 / 结算 / 城市 | `zjudg.*` | 参数设置 |

> 早期建的 `zjudg_decoration_config`、`zjudg_customer_service_config` 已在 V27 删除。
> `zjudg_user_profile` 保留，供后续「用户资料完善度」扩展使用，当前无代码引用。

## 六、多实例部署注意事项

应用支持水平扩容，但需要注意以下点：

| 项目 | 状态 | 说明 |
|---|---|---|
| 自动派单定时任务 | **已加租约** | `ZjudgScheduleLease` 用 `zjudg_schedule_lock` 表做跨实例互斥，同一周期只有一个实例执行 |
| 批量作业定时任务 | **已加租约** | 同上，锁名 `zjudg:batch:job` |
| 抢单 | 安全 | `claimOrder` 带 `master_id is null` 条件，并发下只有一人成功 |
| 时段容量 | 安全 | `selectByIdForUpdate` 行锁串行化同一时段的下单请求 |
| 支付回调 | 安全 | `markPaid` 带 `payment_status='0'` 条件，重复回调幂等 |

**租约机制说明**：任务开始前 `UPDATE ... WHERE expires_at IS NULL OR expires_at < NOW()` 抢占，
抢不到直接跳过（不排队，下个周期自然再跑）。持锁实例宕机时，租约到期后其他实例自动接管，
不会导致任务永久停摆。租约记录在 `zjudg_schedule_lock`，可用以下 SQL 查看当前状态：

```sql
SELECT lock_name, owner, locked_at, expires_at FROM zjudg_schedule_lock;
```

排障时若发现 `expires_at` 长期停留在过去，说明持锁实例已退出，任务会在下个周期正常接管，无需手工清理。

## 七、上线验证顺序

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
10. **地址簿**：小程序「我的 → 服务地址」新增/设默认/删除，下单页自动带出默认地址
11. **收藏**：服务详情收藏 → 「我的 → 我的收藏」可见 → 取消收藏后消失
12. **退款进度**：后台发起退款 → 用户订单详情显示对应退款状态与说明
13. **多实例定时任务**：同时启动两个实例，观察日志确认同一周期只有一个实例打印「自动派单补偿完成」
14. **后台菜单完整性**：分别用 `admin` 与 `common` 角色登录后台，确认左侧可见「服务管理 / 订单管理 / 用户管理 / 师傅管理 / 财务管理 / 素材管理 / 页面装修」7 个模块，且每个模块下的页面不报 403
15. **城市管理**：进入「服务管理 → 城市管理」，列表有数据；对**已被服务或师傅引用**的城市执行删除，应提示「不能删除，请改为停用」而不是实际删除

## 八、已知限制

| 项 | 说明 |
|---|---|
| 订阅消息模板字段 | 已支持配置：后台「微信设置 → 模板字段名」按模板填四个字段（逗号分隔，顺序为 服务名称, 时间, 附加字段, 备注）；留空回落到代码内置默认值，换模板无需改代码 |
| 身份证加密 | 仅 `zjudg_master.id_card_no` 加密，历史明文由 `v_zjudg_master_plaintext_idcard` 视图检测 |
| 支付退款 | 已实现：用户申请 → 后台审核 → 微信退款；已完成订单不支持自助退款，由平台强制退款 |
| 提现打款 | 审核通过后可选自动调用微信商家转账（zjudg.settle.autoTransfer=1）；未开通商户保持手工打款 |
| 结算提现 | 已实现：完工即时结算、师傅申请提现、后台审核打款 |
| 自动派单 | 已实现：支付后按技能/评分/负载加权派单，5 分钟补偿一次；默认关闭，需在 sys_config 开启 zjudg.dispatch.enabled |
| 严格城市过滤 | 已实现：`zjudg.city.strict=1` 只返回本地服务/时段；开启前须先按城市发布数据 |
| 首页轮播 | 优先读 `zjudg_banner`，为空时回退 `decorate.home.banners`；外部链接在端内复制而非跳转 |
| 客服联系方式 | 后台「网站设置」维护 `servicePhone` / `serviceHours` / `serviceQrCode`，小程序「我的 - 联系客服」弹出拨号与二维码；两者都为空时提示未配置 |
| 用户管理 | 后台「用户管理」只列小程序注册用户（`sys_user.wx_openid` 有值），与系统内建员工账号管理分离 |
| 用户本地缓存 | 「设置 - 清理缓存」清空表单草稿与临时数据，保留登录态与所选城市 |
| 服务地址 | 每用户上限 20 条；第一条自动设为默认；删除默认地址后自动补位 |
| 用户收藏 | 同一用户同一服务只允许一条记录；服务下架后收藏保留并置灰，不影响历史数据 |
| 师傅拒单 | 已开工订单不允许拒单；近期拒单次数按 `zjudg.dispatch.weight.reject`（默认 15）扣分，最多计 4 次 |
| 派单权重配置 | `zjudg.dispatch.weight.skill/city/rating/load/reject`、`zjudg.dispatch.reject.windowDays`（默认 30 天） |
| 退款状态展示 | 订单详情返回退款单状态与文案；不返回微信退款单号与失败原始原因 |
| 财务看板 | 营业额按支付成功时间归集，退款按申请时间归集；区间上限 180 天 |
| 素材中心 | 删除分组时素材自动迁移到「未分组」，不会产生孤儿素材 |
| 城市管理 | 后台「服务管理 → 城市管理」维护开通城市；删除前会校验师傅/服务/订单/预约时段的引用，**被引用时拒绝删除并提示改为停用**，避免关联记录的城市名称变成 NULL |
| 菜单授权 | `V29` 之前家政业务 7 个目录及 17 个页面的 `sys_role_menu` 授权为空，导致除超管外全部不可见；现已修复并同时补齐 `common` 角色的 `settings:*:list` / `channel:*:list`。新建角色时仍需在「角色管理」中手动勾选业务菜单 |

## 九、回滚

1. 应用版本回滚到上一镜像
2. 数据库：`V12`–`V28` 均为增量变更（新增字段/表/配置），旧版本代码可正常忽略新字段，**无需回滚 DDL**
3. 若 `FIELD_CRYPTO_KEY` 已用于生产数据，回滚时务必保留同一密钥
4. 回滚前端后，若 `zjudg.city.strict` 曾改为 `1`，请同步改回 `0`（旧版本不识别该开关）
5. **V27 删表不可自动恢复**：若回滚到 V27 之前的版本，且旧代码仍引用 `zjudg_decoration_config`，需从备份恢复该表结构（数据为空，只需重建表）
6. 回滚到不支持租约的旧版本时，`zjudg_schedule_lock` 表可保留不删，旧代码不感知；但**旧版本没有多实例互斥**，请确保此时只运行单实例
7. **V29 的菜单变更对旧版本是破坏性的**：V29 删除了 `装修`、`日志管理`、`地图设置`、`商家入驻设置` 四组菜单，并把「财务中心」改名为「经营看板」。若回滚到 V29 之前的前端版本，这些菜单会消失或名称不符。按以下顺序处理：
   - 先回滚前端，确认新版页面不再依赖被删菜单
   - 如需完整恢复，从备份中按 `menu_id` 重新插入这 5 条 `sys_menu` 记录及其 `sys_role_menu` 授权
   - **不要**为了回滚而重跑 V29——它对新增菜单是幂等的，但删除动作已执行完毕，重跑不会恢复任何内容
8. V29 新增的 `zjudg:city:*` 权限串，旧版 `ZjudgCityController` 不识别（原为 `zjudg:service:list`），回滚后端后城市列表接口会退回到复用服务权限；此时 `M_ZJUDG_CITY` 菜单的按钮权限对旧后端无效，属预期行为
