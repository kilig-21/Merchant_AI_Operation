# 前后端接口合同

## 0. 基线

- 后端分支：feature/backend
- 后端提交：d8a8801
- 前端分支：feature/web-v2
- 前端提交：0e0e1f6
- 本轮不切换分支、不默认合并；先完成接口合同和单店链路验收。

## 1. 公共约定

- 浏览器页面只访问 Next.js 的 `/api/session/**` 和 `/api/backend/**`。
- JWT 保存在 HttpOnly Cookie 中，由 BFF 转发为后端 Authorization。
- 后端统一响应格式：`{ code, message, data }`。
- 前端必须同时判断 HTTP 状态和响应体中的 `code`。
- 商家接口的 tenantId 必须来自当前登录用户，不能由前端传入。

## 2. S2 认证接口

| 页面操作 | 浏览器接口 | 后端接口 | 请求 | 成功响应 | 主要错误 |
|---|---|---|---|---|---|
| 登录 | `POST /api/session/login` | `POST /api/auth/login` | `username`、`password` | `data.user`，JWT 写入 HttpOnly Cookie | `400` 参数错误，`401` 用户名或密码错误，`403` 账号禁用 |
| 注册 | `POST /api/session/register` | 先注册，再登录 | `username`、`password` | `data.user`，JWT 写入 HttpOnly Cookie | `400` 参数错误，`409` 用户名已存在 |
| 刷新会话 | `GET /api/session/me` | `GET /api/auth/me` | Cookie 中的 JWT | 当前用户；未登录时 `data: null` | `401` 登录失效，`403` 账号禁用，`503` 后端不可达 |
| 退出 | `POST /api/session/logout` | 无 | 无 | 清除 Cookie，`data: null` | 无 |

### 认证验收

- 浏览器不直接接收或保存 `accessToken`。
- 登录成功后 JWT 只写入 HttpOnly Cookie。
- 刷新页面通过 `/api/session/me` 恢复用户状态。
- 登录失败必须展示后端 `message`。
- 未登录访问受保护接口时，前端必须展示登录失效或登录入口。

## 3. S2 商品读取接口

| 页面操作 | 前端调用 | 后端接口 | 请求 | 成功响应 | 权限/状态 |
|---|---|---|---|---|---|
| 店铺商品列表 | 服务端读取后端公共接口 | `GET /api/public/stores/{storeId}/products?page=1&size=48` | 路径参数 `storeId`，可选 `page`、`size` | `data: PublicProductListItemVO[]` | 公开；当前 `storeId` 实际对应后端 `tenantId` |
| 商品详情 | 服务端读取后端公共接口 | `GET /api/public/stores/{storeId}/products/{spuId}` | 路径参数 `storeId`、`spuId` | `data: PublicProductDetailVO` | 公开；商品必须属于该 tenant 且处于可展示状态 |
| SKU 库存检查 | `GET /api/backend/public/skus/{skuId}/availability` | `GET /api/public/skus/{skuId}/availability` | 路径参数 `skuId` | `skuId`、`purchasable`、`availableStock`、`message` | 公开；加入购物车前检查 |

### 商品字段

- 商品列表：`id`、`name`、`description`、`minSalePrice`、`totalAvailableStock`、`updatedAt`。
- 商品详情：`id`、`name`、`description`、`updatedAt`、`skus`。
- SKU：`id`、`skuName`、`salePrice`、`availableStock`。
- 前端 `ProductSummary`、`ProductDetail`、`Sku` 与后端 VO 字段基本一致。

## 3.1 S3 公共店铺目录与跨店搜索

| 页面操作 | 后端接口 | 请求 | 成功响应 | 权限/状态 |
|---|---|---|---|---|
| 公共店铺目录 | `GET /api/public/stores` | 无 | `data: PublicStoreSummaryVO[]`，包含 `id`、`name`、`productCount` | 公开；只返回启用店铺 |
| 跨店商品搜索 | `GET /api/public/stores/products/search` | 可选 `keyword`、`storeId`、`page`、`size` | `data: PublicMarketplaceProductVO[]` | 公开；只返回启用店铺、已上架商品和 SKU |

S3 后端接口已通过服务测试、控制器测试和 FoxAPI 验收；`feature/web-v2` 的目录/搜索页面尚未切换到真实接口。

## 4. S2 购物车接口

| 页面操作 | 前端接口 | 后端接口 | 请求 | 成功响应 | 主要错误 |
|---|---|---|---|---|---|
| 加入购物车 | `POST /api/backend/cart/items` | `POST /api/cart/items` | `skuId`、`quantity` | `CartItem { id, skuId, quantity }` | `400` 参数错误，`401` 未登录，`409` 商品或库存不可购买 |
| 读取购物车 | `GET /api/backend/cart/items` | `GET /api/cart/items` | 无 | `CartItem[]` | `401` 未登录 |
| 修改数量 | `PUT /api/backend/cart/items/{id}` | `PUT /api/cart/items/{id}` | `quantity` | 更新后的 `CartItem` | `400` 参数错误，`401` 未登录，`404/409` 购物车项不存在或库存不足 |
| 删除购物车项 | `DELETE /api/backend/cart/items/{id}` | `DELETE /api/cart/items/{id}` | 无 | `data: null` | `401` 未登录，`404` 购物车项不存在 |

购物车数据必须按当前消费者身份隔离；前端真实模式不能使用 `morrow_demo_cart_v1` 作为结果来源。

## 5. S2 单店订单接口

| 页面操作 | 前端接口 | 后端接口 | 请求 | 成功响应 | 主要错误 |
|---|---|---|---|---|---|
| 创建订单 | `POST /api/backend/orders` | `POST /api/orders` | Header `Idempotency-Key`；Body `cartItemIds: number[]` | `CreateOrderVO { orderId, orderNo, status, totalAmount, expireAt }` | `400` 参数错误，`401` 未登录，`409` 库存/状态/幂等冲突 |
| 我的订单 | `GET /api/backend/orders` | `GET /api/orders` | 无 | `OrderDetailVO[]` | `401` 未登录 |
| 订单详情 | `GET /api/backend/orders/{id}` | `GET /api/orders/{id}` | 路径参数 `id` | `OrderDetailVO` | `401` 未登录，`403/404` 非本人订单或订单不存在 |
| 模拟支付 | `POST /api/backend/orders/{id}/mock-pay` | `POST /api/orders/{id}/mock-pay` | 无 | `data: null` | `401` 未登录，`409` 状态不允许支付 |
| 取消订单 | `POST /api/backend/orders/{id}/cancel` | `POST /api/orders/{id}/cancel` | 无 | `data: null` | `401` 未登录，`409` 状态不允许取消 |

### 订单字段

- 订单：`id`、`orderNo`、`tenantId`、`status`、`totalAmount`、`expireAt`、`createdAt`、`items`。
- 跨店订单附加字段：`checkoutGroupId`；订单详情还可返回 `shippingAddress` 快照。
- 订单明细：`id`、`skuId`、`skuNameSnapshot`、`salePrice`、`quantity`。
- 创建订单的幂等键由前端根据购物车项生成并在重复提交时复用；成功后清理对应的临时状态。
- 旧的 `/api/orders` 仍面向单商家结算；跨店结算副线阶段接口见第 6.2 节，前端尚未接入。

## 6. S2 商家商品接口

| 页面操作 | 前端接口 | 后端接口 | 请求 | 成功响应 | 权限 |
|---|---|---|---|---|---|
| 创建商品 | `POST /api/backend/merchant/products` | `POST /api/merchant/products` | `name`、`description` | `{ id }` | `MERCHANT_ADMIN` / `MERCHANT_OPERATOR`；tenantId 来自当前用户 |
| 创建 SKU | `POST /api/backend/merchant/products/{id}/skus` | `POST /api/merchant/products/{id}/skus` | `skuName`、`salePrice`、`availableStock` | `{ id }` | 只能操作当前 tenant 的商品 |
| 商品列表 | `GET /api/backend/merchant/products?page=1&size=50&keyword=...` | `GET /api/merchant/products` | 可选 `page`、`size`、`keyword` | `MerchantProductVO[]` | 只能读取当前 tenant |
| 上架商品 | `POST /api/backend/merchant/products/{id}/publish` | `POST /api/merchant/products/{id}/publish` | 无 | `data: null` | 当前 tenant 商品 |
| 下架商品 | `POST /api/backend/merchant/products/{id}/unpublish` | `POST /api/merchant/products/{id}/unpublish` | 无 | `data: null` | 当前 tenant 商品 |

商家商品列表字段为：`id`、`name`、`description`、`status`、`createdAt`、`updatedAt`、`skuCount`、`minSalePrice`、`totalAvailableStock`。

## 6.1 S4 地址与订单地址快照基础接口

| 页面操作 | 后端接口 | 请求 | 成功响应 | 权限/状态 |
|---|---|---|---|---|
| 地址列表 | `GET /api/addresses` | 无 | `ConsumerAddressVO[]` | 仅当前消费者自己的地址 |
| 新增地址 | `POST /api/addresses` | 收货人、手机号、省市区、详细地址、可选 `isDefault` | `data: null` | 仅消费者；默认地址由事务保证 |
| 修改地址 | `PUT /api/addresses/{id}` | 完整地址字段、可选 `isDefault` | `data: null` | `id` 必须属于当前消费者 |
| 删除地址 | `DELETE /api/addresses/{id}` | 无 | `data: null` | `id` 必须属于当前消费者 |

V12 已创建 `consumer_address`；V13 已创建 `commerce_order_address` 订单收货地址快照表。当前 `CreateOrderRequest.addressId` 已接入订单创建事务并写入地址快照，同时保留旧构造器兼容已有单店调用。

## 6.2 S4 跨店结算副线阶段接口

| 页面操作 | 后端接口 | 请求 | 成功响应 | 当前状态 |
|---|---|---|---|---|
| 创建结算组草稿 | `POST /api/checkouts/prepare` | Body：`cartItemIds: number[]`、`addressId: number` | `CreateCheckoutGroupVO { checkoutGroupId, checkoutNo, status, totalAmount, orders }`；首次创建时 `orders` 为空 | 后端已完成并通过 FoxAPI 验收 |
| 创建结算组子订单 | `POST /api/checkouts/{checkoutGroupId}/orders` | Header：`Idempotency-Key`；Body：`cartItemIds`、`addressId` | 返回结算组及按 `tenantId` 拆出的 `CreateOrderVO[]` | 后端已完成阶段实现并通过 FoxAPI 验收 |
| 查询结算组详情 | `GET /api/checkouts/{checkoutGroupId}` | 当前消费者身份 | `CheckoutGroupDetailVO { checkoutGroupId, checkoutNo, status, totalAmount, createdAt, orders: OrderDetailVO[] }`；子订单当前为摘要，`items: []`、`shippingAddress: null` | 已完成；未登录为 HTTP/body `401`，不存在或非本人为 HTTP/body `404` |
| 查询订单关联 | `GET /api/orders`、`GET /api/orders/{id}` | 当前消费者身份 | `OrderDetailVO` 返回 `checkoutGroupId`；详情返回 `shippingAddress` | 已完成 |

### 跨店结算字段与边界

- `checkout_group` 是一次结算的父记录；`commerce_order.checkout_group_id` 指向父结算组，一个结算组可以对应多个商家订单。
- `checkoutGroupId` 由后端生成并返回，不能由前端伪造；订单查询同时使用当前 `consumer_id` 做数据隔离。
- 子订单按可信商品快照中的 `tenantId` 分组；金额由当前 `salePrice × quantity` 重新计算。
- 当前接口是 S4 副线阶段能力，`feature/web-v2` 尚未接入；AI 主线步骤 25～30 不受影响。
- 结算组详情查询已按父记录和子订单的 `consumer_id` 双重隔离；当前子订单只返回摘要，商品项和收货地址仍应通过订单详情读取。
- 当前仍缺少组级支付、整体取消/回滚和父级幂等重试闭环，不能把本阶段描述为完整支付闭环。

## 7. 当前明确缺口与暂不接入范围

| 范围 | 真实代码结论 | 处理安排 |
|---|---|---|
| 商家订单列表 | 前端调用 `/api/backend/merchant/orders`，后端暂无对应 Controller | S6 补商家订单分页接口；当前前端必须保留明确 Demo 标识 |
| 店铺目录与跨店搜索 | 后端已新增真实目录/搜索接口并完成 FoxAPI 验收；前端仍使用 `demoStores`、`demoMarketplaceProducts` | S3 后端阶段完成；待前端分支接入 |
| 地址与跨店结算 | 后端已完成地址 CRUD、订单地址快照、结算组创建/拆单及结算组详情查询；前端尚未接入真实接口，组级支付/回滚/父级幂等仍缺失 | S4 继续补齐组级支付、整体回滚和集成测试 |
| 收藏与售后 | 前端使用 `localStorage`，后端暂无领域模型 | 收藏后置；S5 设计真实售后状态机 |
| 商家营销、顾客洞察、平台端 | 当前为 Demo 或只读演示 | 不阻塞 S2；暂不伪造真实接口 |
| 统一业务错误状态 | `BizException` 现已同时返回匹配的 HTTP 状态和 `ApiResponse.code`；安全入口的 401/403 已完成真实验收 | 其他校验/通用异常处理仍按各自 Handler 保持现状，后续统一时单独评估 |

## 8. 分支与集成策略

- 当前继续停留在 `feature/backend`，只读比较 `feature/web-v2`，不为阅读而切换分支。
- S1～S2 的接口合同和单店链路未验收前，不建立合并提交。
- 只有一批接口完成浏览器、后端测试和错误态验收后，才由用户决定是否建立集成分支。
- AI 主线步骤 25～30 不因本副线改变进度或状态。

## 9. S1 验收清单

- [ ] S2 范围内每个页面操作都有对应接口，或已明确记录缺口。
- [ ] 合同写明请求字段、响应字段、权限和主要错误码。
- [ ] 商家接口均注明 tenantId 来自当前登录用户。
- [ ] 已区分真实接口、Demo 数据和 localStorage 数据。
- [ ] 已记录两个分支的基线提交和不自动合并的集成策略。

## 10. 2026-08-21 副线阶段记录

- S1：接口合同和分支集成策略达到阶段完成；未切换或合并分支。
- S3：公共店铺目录、跨店搜索后端阶段完成；前端真实接入仍待 `feature/web-v2` 集成。
- S4：地址 CRUD、V12 地址表、V13 订单地址快照表和快照服务完成；订单创建事务、跨店拆单和回滚仍未完成。

## 11. 2026-08-23 副线阶段记录

- S4：V14 结算组模型、按商家拆单、订单 `checkoutGroupId` 关联、订单详情/列表返回和 FoxAPI 阶段验收完成。
- 当前实证：结算组 `3` 创建成功，子订单 `9300000000046` 创建成功，订单详情与列表均返回 `checkoutGroupId = 3`。
- 当前未完成：结算组详情查询、组级支付、整体取消/回滚、父级幂等重试、两个真实商家的集成测试和前端接入。
- 本记录属于副线 S4，不推进 AI 主线步骤 25～30；代码与文档由用户自行检查并提交。

## 12. 2026-08-24 副线阶段记录

- S4：新增 `GET /api/checkouts/{checkoutGroupId}`。父结算组经当前消费者查询，子订单查询同样带 `checkout_group_id + consumer_id` 条件，避免越权读取。
- 实证：本地真实请求 `GET /api/checkouts/3` 返回结算组 `3` 及其一笔子订单；未登录请求返回 HTTP/body `401`，不存在的 `999999` 返回 HTTP/body `404`。
- 为使业务异常的传输状态与响应体一致，`GlobalExceptionHandler` 的 `BizException` 分支改为以异常业务码设置 HTTP 状态。该调整不等于所有校验和未知异常均已统一为相同的 HTTP 状态。
- 本次仍是副线 S4；不切换或合并分支，不推进 AI 主线步骤 25～30。

## 13. 2026-08-24 S4 后端结算组状态机与前端接入合同

| 页面操作 | 前端 BFF 调用 | 后端接口 | 请求 | 成功响应 | 当前状态 |
|---|---|---|---|---|---|
| 一键提交结算 | `POST /api/backend/checkouts` | `POST /api/checkouts` | Header `Idempotency-Key`；Body：`cartItemIds: number[]`、`addressId: number` | `CreateCheckoutGroupVO { checkoutGroupId, checkoutNo, status, totalAmount, orders }` | 后端代码完成；已做单商家真实创建，双商家/幂等 ApiFox 待验收 |
| 查询结算组 | `GET /api/backend/checkouts/{id}` | `GET /api/checkouts/{id}` | 当前消费者身份 | `CheckoutGroupDetailVO` | 后端完成；已有真实成功、401、404 验收 |
| 全组模拟支付 | `POST /api/backend/checkouts/{id}/mock-pay` | `POST /api/checkouts/{id}/mock-pay` | 无 Body | `data: null`；全部子订单支付后父组为 `PAID` | 后端代码完成；ApiFox 待验收 |
| 全组取消 | `POST /api/backend/checkouts/{id}/cancel` | `POST /api/checkouts/{id}/cancel` | 无 Body | `data: null`；全部子订单取消后父组为 `CANCELLED` | 后端代码完成；ApiFox 待验收 |

### S4 当前状态约定

- 父结算组初始状态为 `PENDING_PAYMENT`；所有子订单支付完成后为 `PAID`，所有子订单主动取消后为 `CANCELLED`，所有子订单超时关单后为 `CLOSED`。
- 对于支付、取消、关闭，后端均锁定父结算组并统计子订单状态；只要仍存在未达目标状态的子订单，父组保持原状态。
- 一键结算的重复提交必须复用相同 `Idempotency-Key`；相同请求指纹返回已有结算组，不同购物车项或地址返回 `409`。
- 旧的 `POST /api/checkouts/prepare` 与 `POST /api/checkouts/{id}/orders` 仍保留为阶段兼容入口；`feature/web-v2` 接入时应以一键接口为准。
- `feature/web-v2` 当前仍调用本地 `demo-commerce`，尚未实现上述 BFF 调用、状态刷新、错误态和真实地址选择；这部分不应标为已完成。

## 14. 2026-08-26 S4 ApiFox 验收记录与 S5 起点

### 已实测通过

- `POST /api/checkouts`：结算组 `5` 与子订单 `9300000000048` 创建成功。
- 相同幂等键同参数重试复用结算组；同 Key 更换地址返回 HTTP/body `409`。
- 结算组 `5` 组级支付后父组和子订单均为 `PAID`。
- 结算组 `6` 过期后父组和子订单均为 `CLOSED`；已关闭组取消返回 `409`。
- 结算组 `7` 主动取消后父组和子订单均为 `CANCELLED`。

### 明确暂缓

- 两个真实商家中一个 SKU 库存不足、整次结算整体回滚：本次按用户决定暂缓，不能标记为已实测通过。
- `feature/web-v2` 前端真实地址/结算接入：本次未切换、未修改、未合并。

### S4/S5 边界

- S4 后端核心状态机与父级幂等已经有单商家真实 ApiFox 证据，可作为进入 S5 的后端检查点。
- “S4 所有扩展场景全部完成”仍不成立，双商家库存不足回滚是保留项。
- 下一阶段进入 S5 售后与商家订单处理最小闭环；不推进 AI 主线步骤 25～30。

## 15. 2026-08-26 S5 售后审核最小闭环

| 页面操作 | 后端接口 | 请求/权限 | 成功响应 | 当前状态 |
|---|---|---|---|---|
| 查询可申请订单项 | `GET /api/after-sales/eligible-orders` | 当前消费者身份；只返回本人 `PAID` 订单项 | `AfterSaleOrderItemContext[]` | 已完成并通过 ApiFox 验收 |
| 提交售后申请 | `POST /api/after-sales` | 当前消费者；Body：`orderItemId`、`quantity`、`reason` | `AfterSaleRequest(status=SUBMITTED)`；金额由后端计算 | 已完成并通过 ApiFox 验收 |
| 查询我的售后列表/详情 | `GET /api/after-sales`、`GET /api/after-sales/{id}` | 当前消费者 + `consumer_id` 条件 | 当前消费者的售后申请 | 已完成并通过 ApiFox 验收 |
| 商家查询售后列表/详情 | `GET /api/merchant/after-sales`、`GET /api/merchant/after-sales/{id}` | 当前商家 + `tenant_id` 条件 | 当前租户的售后申请 | 已完成并通过 ApiFox 验收 |
| 商家审核售后 | `POST /api/merchant/after-sales/{id}/decision` | 当前商家；Body：`decision=APPROVED/REJECTED`、`remark` | 状态推进至 `APPROVED` 或 `REJECTED` | 已完成并通过 ApiFox 验收 |

### S5 状态与边界

- 当前闭环状态：`SUBMITTED → REVIEWING → APPROVED/REJECTED`。
- 只有订单状态为 `PAID` 的订单项可以提交申请。
- 申请金额由订单项 `sale_price × quantity` 计算，不接受前端金额字段。
- 主表：`after_sale_request`；审计表：`after_sale_status_log`。
- 商家跨租户读取和已结束状态重复审核均返回 HTTP/body `409`。
- 本阶段不代表真实退款，不接支付平台、不处理退货物流，不接入 `feature/web-v2`。
