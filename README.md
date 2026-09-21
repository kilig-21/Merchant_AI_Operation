# Morrow · Merchant AI Operation

> 一个面向多租户场景的全栈电商与商家智能运营项目。

Morrow 将消费者商城、商家运营后台与受控 AI 经营助手放在同一套业务系统中：消费者可以浏览商品、购物和发起售后；商家可以管理商品、订单、营销活动并查看经营数据；AI 助手则只能在授权范围内读取当前商家的经营汇总，不具备业务写入能力。

## 在线体验

- 前端演示：[https://merchant-ai-operation.vercel.app/](https://merchant-ai-operation.vercel.app/)
- 源码仓库：[kilig-21/Merchant_AI_Operation](https://github.com/kilig-21/Merchant_AI_Operation)

> 线上环境仍在持续完善。公共商品页面在后端暂不可用时会明确显示演示数据；购物车、订单和商家后台等受保护业务不会伪造真实状态。

## 项目亮点

- **完整电商闭环**：覆盖用户注册登录、商品与 SKU、购物车、结算、模拟支付、订单、地址与售后。
- **多租户与权限隔离**：商家租户从服务端安全上下文获取；JWT 鉴权区分未登录（401）与无权限（403），客户端不能指定其他商家的数据范围。
- **高并发场景设计**：库存扣减、请求幂等、Redis 缓存与 Lua 原子预占；使用 RabbitMQ、Outbox 和补偿任务保证异步链路可靠性。
- **可追溯的经营分析**：提供经营汇总、趋势、热销商品、促销表现与售后申请率，并用独立指标字典统一数据口径。
- **受控 AI 助手**：基于 Spring AI 接入 OpenAI-compatible 模型；当前仅允许查询“当前商家 + 明确日期范围”的经营汇总，禁止任意 SQL、跨租户读取和所有业务写操作。
- **面向真实部署**：使用 Flyway 管理数据库演进，提供本地、升级验证与生产 Docker Compose 配置。

## 功能概览

| 角色 | 已实现能力 |
| --- | --- |
| 消费者 | 注册/登录、店铺与商品浏览、搜索、促销活动、购物车、结算与模拟支付、订单、收货地址、售后与账户管理 |
| 商家 | 商品与 SKU 管理、订单处理、促销创建与预热、售后处理、经营仪表盘、客户视图与 AI 经营问答 |
| 平台 | 商家、订单与治理视图 |
| AI 助手 | 中文对话；在受控范围内查询当前商家的有效订单、已支付订单、营业额、客单价、待付款订单和低库存商品 |

## 技术栈

| 分层 | 技术 |
| --- | --- |
| 前端 | Next.js 16、React 19、TypeScript、ECharts、GSAP、Vitest、Biome |
| 后端 | Java 21、Spring Boot 4、Spring Security、MyBatis、Spring AI、Maven |
| 数据与中间件 | MySQL 8.4、Redis 7.4、RabbitMQ 3.13、Flyway |
| 部署 | Docker Compose、Vercel（前端） |

## 核心架构

```text
浏览器
  │
  ▼
Next.js 前端 / BFF
  ├─ HttpOnly Cookie 保存登录态
  └─ 同源转发 /api/session/*、/api/backend/*
  │
  ▼
Spring Boot API
  ├─ JWT 认证与商家租户隔离
  ├─ 商品、购物车、订单、售后、促销与经营分析
  ├─ Spring AI 受控工具调用
  ├─ MySQL + Flyway
  ├─ Redis 缓存 / Lua 原子预占
  └─ RabbitMQ + Outbox 异步事件与补偿
```

## 本地运行

### 1. 启动基础服务

在 `deploy` 目录准备 `.env`，至少配置 MySQL、RabbitMQ、JWT 所需密码和密钥，然后启动 MySQL、Redis 与 RabbitMQ：

```powershell
docker compose up -d
```

### 2. 启动后端

在 `server` 目录配置环境变量后运行：

```powershell
./mvnw spring-boot:run
```

后端默认地址为 `http://localhost:8080`，接口文档为 `http://localhost:8080/swagger-ui/index.html`。

### 3. 启动前端

```powershell
cd web
npm install
npm run dev
```

默认情况下，前端 BFF 会连接 `http://localhost:8080`；如需修改，请复制 `web/.env.example` 为 `web/.env.local` 并配置 `BACKEND_ORIGIN`。

## 质量检查

```powershell
# 前端
cd web
npm run check
npm test
npm run build

# 后端
cd ../server
./mvnw test
```

## 当前阶段与后续计划

项目正在持续开发与完善中。当前已完成消费者交易主链路、商家经营后台、促销异步化和第一条 AI 只读经营查询工具；后续将继续完善 AI 会话与审计能力、线上基础设施、端到端测试与更多商家运营功能。

## 文档

- [每日开发与验收记录](./docs/daily-plan.md)
- [经营指标字典](./docs/metrics-dictionary.md)
- [学习与排障记录](./docs/learning-log.md)
- [Spring Boot 4 升级记录](./docs/boot4-upgrade.md)
- [前端设计说明](./web/DESIGN.md)

---

本项目用于全栈电商、分布式业务链路与受控 AI 应用的学习和实践。
