# 城市维度设计说明

> 最后更新：2026-10-25
> 相关迁移：`V21__zjudg_city_dimension.sql`、`V22__zjudg_city_scope_narrow.sql`
## 一、业务规则

**轮播图、内容公告等展示类内容全城市通用；只有服务按城市区分。**

这是业务明确的边界，决定了 `city_id` 只加在真正需要区分的表上。

## 二、字段分布

| 表 | `city_id` | 语义 | `NULL` 的含义 |
|---|---|---|---|
| `zjudg_service` | ✅ | 服务发布城市 | 全国可用 |
| `zjudg_order` | ✅ | 下单城市快照 | 历史订单未记录 |
| `zjudg_master` | ✅ | 师傅服务城市 | 未填写，可接任意城市 |
| `zjudg_booking_slot` | ✅ | 时段适用城市 | 全国通用 |
| `zjudg_banner` | ❌ | — | 全城市通用，不设城市维度 |
| `zjudg_content_item` | ❌ | — | 同上 |

`zjudg_city` 是城市字典表，`city_id` 为主键。

## 三、查询约定

所有带城市维度的查询统一使用同一写法：

```sql
and (xxx.city_id is null or xxx.city_id = '' or xxx.city_id = #{cityId})
```

**为什么包含 `NULL`**：全国数据必须在任一城市视图下可见。若只匹配本地，某城市尚未配置服务时列表会直接空白。

### 严格模式（`zjudg.city.strict=1`）

开关为 `1` 时改用「只匹配本地」的分支：

```sql
and xxx.city_id = #{cityId}
```

- 服务列表走 `ZjudgServiceMapper.selectListByCity`
- 预约时段走 `ZjudgBookingSlotMapper.selectListByCity`

分支选择集中在 `IZjudgCityScopeService`（`ZjudgCityScopeServiceImpl`），SQL 里不再散落开关判断。
**未传 `cityId` 时不加城市条件，两种模式行为一致**，避免开关切换后变成全量数据。

启用严格模式前请确认目标城市已按城市发布服务，否则该城市列表会为空。

## 四、各环节行为

### 服务列表
`GET /mini/services?cityId=` 返回「全国服务 + 该城市服务」；`zjudg.city.strict=1` 时只返回该城市服务。

### 下单
订单城市取值优先级：

1. 服务自身有 `city_id` → 用服务的城市
2. 服务为全国 → 用用户下单时选择的城市

由 `ZjudgOrderServiceImpl.createOrder(..., cityId)` 实现。

### 派单
订单城市的用途是**加分项，不是硬过滤**：

```
score += wCity  当 order.cityId == master.cityId
```

不硬过滤的理由：新城市可能暂时没有同城师傅，硬过滤会导致订单永远派不出去。

### 预约时段
`GET /mini/slots?cityId=` 返回「全国时段 + 该城市专属时段」，严格模式同上。

### 轮播与内容
不参与城市过滤。

## 四之二、首页轮播数据源

首页轮播已接入 `zjudg_banner`：

```
后台「轮播图管理」→ GET /api/mini/config/banners?position=index → 小程序首页 swiper
                                      ↓ 列表为空时
                         回退 sys_config 的 decorate.home.banners
```

- 后端只下发 `status=0`（启用）且在有效期内的数据，`start_time`/`end_time` 为空视为不限
- 位置取 `index`/`category`/`activity`，未知值按 `index` 处理
- 字段名与装修配置保持一致：`id/title/subtitle/image/linkType/link`
- `linkType=0`（无跳转）时 `link` 为空串，前端不会误跳
- `linkType=2`（外部链接）在端内不带跳转，改为复制链接

保留回退链路的原因：只配置装修 JSON 的存量环境在升级后首页仍能正常展示。

## 五、前端实现

### cityStore（`stores/city.js`）

城市选择结果同时保存 **ID 与名称**：

- `cityId` 用于接口过滤
- `cityName` 用于界面展示

两者都持久化到 storage，避免只存名称导致接口无法过滤。

### 城市切换的通知机制

```
选择城市 → cityStore.select() → uni.$emit('city-changed', {cityId, cityName})
                                      ↓
                        首页 / 服务列表监听并重新拉取
```

首页额外用 `onShow` 兜底同步：`redirectTo` 重建页面时事件监听可能丢失。

## 六、后台配置

| 页面 | 支持 |
|---|---|
| 服务管理 | 按城市发布（留空=全国）、按城市筛选、列表显示城市 |
| 预约时段 | 每行可设适用城市（留空=全部城市） |
| 轮播图 | **无城市字段**，全城市通用；首页轮播实际读该表 |

## 七、已知限制

1. **存量服务全部为全国**
   迁移后 60 条服务的 `city_id` 均为 `NULL`，即全部全国可见。需要运营按实际情况在城市后台逐个指定。

2. **轮播图与 `zjudg_banner` 的关系**
   首页轮播优先读 `zjudg_banner`（经 `/api/mini/config/banners`），表内无启用数据时回退装修配置 `decorate.home.banners`。两处都配置时以 `zjudg_banner` 为准，上线前请确认后台轮播图已按预期上架。

3. **严格城市过滤需先补数据**
   `zjudg.city.strict` 默认 `0`，分支已实现（见「查询约定 → 严格模式」）。改为 `1` 后只返回本地服务与本地时段，因此必须先把目标城市的服务和时段配置好，否则该城市列表会为空。

4. **时段后台的默认值**
   新建时段的 `cityId` 为 `null`，即全国通用。若业务需要按城市差异化时段，需在后台逐行指定。
