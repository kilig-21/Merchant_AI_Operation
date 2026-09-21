# 商家经营指标字典

> 适用范围：R7 商家 Dashboard 与后续只读经营查询。本文是页面、后端查询、DataGrip 验收三方共同使用的口径来源；未在此定义的指标不得在页面或 AI 回答中作为真实经营事实展示。

## 通用规则

| 项目 | 当前约定 |
|---|---|
| 数据归属 | 每次查询的 `tenantId` 必须从当前已登录商家的安全上下文获取；客户端不得传入或指定其他租户。 |
| 时间范围 | 接口传入 `startDate`、`endDate`，均为包含端点的自然日。查询换算为 `[startDate 00:00:00, endDate + 1 day 00:00:00)`。 |
| 归因时间 | 当前所有订单类指标按 `commerce_order.created_at` 归因，而不是按支付完成时间归因。现有模型没有独立的支付完成时间字段；将来新增该字段后，应新建版本化口径，不能静默改变本页历史含义。 |
| 时区 | 当前本地部署按应用/数据库使用的本地业务时区切自然日；R7 不在 SQL 中混用浏览器时区。生产部署前需将业务时区配置化并补回归测试。 |
| 金额单位 | 接口与 Java 使用 `BigDecimal` 表示人民币“元”；数据库聚合结果不得转换为 `double`/`float`。 |
| 空数据 | 汇总金额返回 `0`；按日趋势在每一个请求日期均返回一个点，订单数和营业额为 `0` 的日期不可省略。 |
| 排序 | 趋势点按日期升序返回，前端不得自行重排或填造缺失日期。 |

## 当前四项汇总指标

| 指标 | 返回字段 | 计算公式 | 包含状态 | 排除状态 | 验证方式 |
|---|---|---|---|---|---|
| 有效订单数 | `validOrderCount` | 在日期范围内创建的订单数 | `PENDING_PAYMENT`、`PAID` 等非终止状态 | `CANCELLED`、`CLOSED` | 按当前商家 `tenant_id`、`created_at` 与状态条件执行 `COUNT(*)`。 |
| 已支付营业额 | `paidRevenue` | 在日期范围内创建且当前为 `PAID` 的订单 `total_amount` 之和 | `PAID` | 所有非 `PAID` 状态 | 按当前商家 `tenant_id`、`created_at` 和 `status = 'PAID'` 执行 `COALESCE(SUM(total_amount), 0)`。 |
| 待支付订单数 | `pendingPaymentCount` | 在日期范围内创建且当前待支付的订单数 | `PENDING_PAYMENT` | 其他状态 | 按当前商家 `tenant_id`、`created_at` 和 `status = 'PENDING_PAYMENT'` 执行 `COUNT(*)`。 |
| 低库存商品数 | `lowStockProductCount` | 可售 SKU 的可用库存不高于阈值的不同 SPU 数 | SKU、SPU 均为 `ON_SALE`，且 `available_stock <= 5` | 下架 SKU、下架 SPU、其他租户商品 | 在 `product_sku` 与 `product_spu` 联表后按商家 `tenant_id` 执行 `COUNT(DISTINCT spu_id)`。此项不受订单日期范围影响。 |

## R7 按日趋势指标

| 指标 | 建议返回字段 | 计算公式 | 说明 |
|---|---|---|---|
| 按日订单数 | `orderCount` | 某自然日内创建、且当前状态不是 `CANCELLED`/`CLOSED` 的订单数 | 与 `validOrderCount` 使用同一状态口径，防止图表累计与汇总不一致。 |
| 按日已支付营业额 | `paidRevenue` | 某自然日内创建、且当前状态为 `PAID` 的订单 `total_amount` 之和 | 与现有 `paidRevenue` 使用同一创建时间归因和支付状态口径。 |

建议每个趋势点使用 `date`、`orderCount`、`paidRevenue` 三个字段；日期为 ISO `yyyy-MM-dd` 字符串或 `LocalDate` 的 JSON 序列化结果。

## DataGrip 对照 SQL 模板

将 `:tenantId`、`:startAt`、`:endAt` 替换为本次验收值。`endAt` 必须是结束日期次日零点，保持左闭右开范围。

```sql
SELECT
    DATE(o.created_at) AS metric_date,
    SUM(CASE WHEN o.status NOT IN ('CANCELLED', 'CLOSED') THEN 1 ELSE 0 END) AS order_count,
    COALESCE(SUM(CASE WHEN o.status = 'PAID' THEN o.total_amount ELSE 0 END), 0) AS paid_revenue
FROM commerce_order o
WHERE o.tenant_id = :tenantId
  AND o.created_at >= :startAt
  AND o.created_at < :endAt
GROUP BY DATE(o.created_at)
ORDER BY metric_date ASC;
```

验收时还要用商家 B 的会话执行同一日期范围：其结果只能来自 B 的订单，不能出现商家 A 的订单数或金额。消费者访问商家 Dashboard 接口应保持 `403`。


## A1 扩展指标

### 客单价

- 返回字段：`averageOrderValue`
- 公式：指定日期范围内已支付营业额 ÷ 已支付订单数
- 分子：当前商家、范围内创建、当前状态为 `PAID` 的订单金额之和
- 分母：同一条件下的订单数
- 空数据：已支付订单数为 0 时返回 `0`
- 金额单位：人民币元，使用 `BigDecimal`
- 精度：保留两位小数，采用 `HALF_UP`

### 热销商品

- 返回形式：热销 SKU 列表
- 建议字段：`skuId`、`skuName`、`soldQuantity`、`paidRevenue`
- 统计范围：当前商家、指定日期范围内创建、当前状态为 `PAID` 的订单明细
- 销量：订单明细 `quantity` 之和
- 销售额：订单明细 `sale_price × quantity` 之和
- 商品名称：优先展示当前 `product_sku.sku_name`；历史 SKU 不存在时回退订单明细中的 `sku_name_snapshot`。销量和金额始终使用订单明细快照计算
- 主排序：`soldQuantity` 降序
- 次排序：`paidRevenue` 降序
- 最终稳定排序：`skuId` 升序
- 返回数量：`limit` 限制为 1～10
- 空数据：返回空列表 `[]`

### 促销表现

- 返回形式：按促销活动汇总的列表
- 建议字段：`activityId`、`activityName`、`reservationCount`、`orderCreatedCount`、`successfulQuantity`、`promotionRevenue`、`orderConversionRate`
- 统计范围：当前商家、指定日期范围内创建的促销资格记录
- 资格申请数：活动下 `promotion_reservations` 的记录数
- 成功订单数：当前状态为 `ORDER_CREATED` 的资格记录数
- 成功购买件数：状态为 `ORDER_CREATED` 的资格记录 `quantity` 之和
- 促销营业额：状态为 `ORDER_CREATED` 的 `unit_price_snapshot × quantity` 之和
- 下单转化率：成功订单数 ÷ 资格申请数
- 空数据：计数和金额返回 `0`，除数为 0 时转化率返回 `0`
- 精度：营业额和转化率均保留两位小数，采用 `HALF_UP`
- 返回约定：转化率返回 `0～1` 的小数，例如 `0.25` 表示 25%；接口不返回带 `%` 的字符串
- 排序：`promotionRevenue` 降序，再按 `activityId` 升序
- 返回数量：`limit` 限制为 1～10

### 售后申请率

- 返回字段：`afterSaleRate`
- 建议辅助字段：`paidOrderItemCount`、`afterSaleOrderItemCount`
- 统计范围：当前商家、指定日期范围内创建且当前状态为 `PAID` 的订单及其订单明细
- 分母：符合统计范围的不同订单明细 `commerce_order_item.id` 数量
- 分子：分母中的订单明细里，至少存在一条售后申请的不同 `order_item_id` 数量
- 计算公式：`afterSaleOrderItemCount ÷ paidOrderItemCount`
- 售后状态：`SUBMITTED`、`REVIEWING`、`APPROVED`、`REJECTED` 均计入“曾发起售后”
- 重复处理：同一订单明细即使存在多条售后记录，也只计算一次
- 空数据：已支付订单明细数为 0 时返回 `0`
- 精度：保留两位小数，采用 `HALF_UP`
- 返回约定：售后申请率返回 `0～1` 的小数，例如 `0.25` 表示 25%；接口不返回带 `%` 的字符串
- 语义边界：该指标表示已支付订单明细中曾发起售后的比例，不表示退款率、退款完成率或售后通过率；当前系统尚无真实退款完成状态

## A1 查询边界

- 汇总、趋势、客单价、热销商品、促销表现和售后申请率的 `tenantId` 均由当前商家安全上下文提供，客户端不得传入。
- 所有日期参数均为包含首尾的自然日，并转换为 `[startDate 00:00:00, endDate + 1 day 00:00:00)`。
- 日期范围最多包含 31 个自然日；倒序日期、空日期和超出范围均返回明确的参数错误。
- 列表类查询的 `limit` 只能取 1～10；排序使用服务端固定规则，不接收任意数据库列名。
- 查询服务只返回专用 VO/DTO，不向页面或后续 AI 暴露数据库 Entity，也不提供任意 SQL 能力。
- 金额与比率均使用 `BigDecimal`；需要除法时保留两位小数并采用 `HALF_UP`。

## A1 验收原则

- 使用两个租户、多日期、多订单状态、促销资格和售后申请组成固定测试数据。
- 同一组数据必须能通过 DataGrip 手工 SQL、后端查询服务和前端页面得到一致结果。
- 商家 A 的查询结果不得包含商家 B 的订单、商品、促销或售后数据；消费者访问商家查询保持 `403`。
- 无订单范围返回零值和空列表；趋势仍返回范围内每天的零值点。
- 跨月但不超过 31 天的查询必须有确定结果；超过 31 天必须拒绝。
- 每个指标都需要固定预期值集成测试，不能只断言接口成功或结果非空。
