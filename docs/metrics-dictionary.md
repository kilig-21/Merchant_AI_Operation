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
