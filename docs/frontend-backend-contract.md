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
- 订单明细：`id`、`skuId`、`skuNameSnapshot`、`salePrice`、`quantity`。
- 创建订单的幂等键由前端根据购物车项生成并在重复提交时复用；成功后清理对应的临时状态。
- 当前订单模型面向单商家结算；前端跨店拆单仍属于 Demo，不纳入 S2 真实验收。

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

V12 已创建 `consumer_address`；V13 已创建 `commerce_order_address` 订单收货地址快照表。当前 `CreateOrderRequest.addressId` 只是兼容旧订单测试的可选字段，尚未接入 `OrderService` 的订单事务。

## 7. 当前明确缺口与暂不接入范围

| 范围 | 真实代码结论 | 处理安排 |
|---|---|---|
| 商家订单列表 | 前端调用 `/api/backend/merchant/orders`，后端暂无对应 Controller | S6 补商家订单分页接口；当前前端必须保留明确 Demo 标识 |
| 店铺目录与跨店搜索 | 后端已新增真实目录/搜索接口并完成 FoxAPI 验收；前端仍使用 `demoStores`、`demoMarketplaceProducts` | S3 后端阶段完成；待前端分支接入 |
| 地址与跨店结算 | 后端已完成地址 CRUD 和订单地址快照基础设施；订单事务尚未写入快照，前端仍使用浏览器本地地址和演示拆单 | S4 继续接入订单快照，再设计跨店拆单和整体回滚 |
| 收藏与售后 | 前端使用 `localStorage`，后端暂无领域模型 | 收藏后置；S5 设计真实售后状态机 |
| 商家营销、顾客洞察、平台端 | 当前为 Demo 或只读演示 | 不阻塞 S2；暂不伪造真实接口 |
| 401/403 错误体 | Spring Security 入口当前主要设置 HTTP 状态，可能没有完整 `ApiResponse` JSON | S2 联调时单独验收，必要时补统一错误响应 |

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
