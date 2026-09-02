## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 原主线步骤 | `████████████████████████` 24 / 36（步骤 24 已完成） |
| 新主线开发 | `██░░░░░░░░░░░░░` 2 / 15（R1、R2 已完成；R3～R9、A1～A6 待推进） |
| 当前阶段 | 真实电商联调、AI 与版本演进 |
| 周验收 | 已通过 |
| 最近提交 | `1fccdfa feat(web): 完成 R1/R2 真实接口联调` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 真实电商联调、AI 与版本演进 |
| 当前文档   | `D:\ALLAPPS\Note_Apps\Document\Coding\真实电商联调、AI与版本演进开发链.md` |
| 当前步骤   | R3：补齐真实购物车展示数据（待开始；后端由用户逐步编写） |
| 本周目标   | 完成真实购物车详情接口合同与第一轮后端实现，再由前端接线 |
| 今日目标   | R1、R2 已完成真实接口接线与关键失败态验收；整理结果后进入 R3 接口盘点 |
| 昨日完成   | R1：真实/Demo 模式边界；R2：真实店铺目录、店铺商品与全站搜索 |
| 当前卡点   | 真实购物车列表仍缺少前端完整展示所需的商品、SKU、店铺、价格、库存和可售状态快照字段 |
| 最近一次提交 | 前端分支：`1fccdfa feat(web): 完成 R1/R2 真实接口联调`；后端分支文档：`adfddfd daily文档修改:新路线开发启动` |
| 明日优先   | 先确认 R3 `CartItemDetailVO` 接口合同，再由用户完成后端 Join 查询与消费者隔离测试 |

## 每日任务

## Day 32：2026-09-02 / R1、R2 真实前端联调验收

### 今日阶段

- 当前文档：`真实电商联调、AI 与版本演进开发链.md`
- 当前步骤：R1、R2
- 今日目标：消除真实会话错误时的 Demo 伪造结果，并让公开店铺、商品和搜索读取真实后端数据。

### 今天完成

- [x] R1：为显式 Demo 会话增加清晰标识；真实会话遇到接口失败不再回退本地 Demo 购物车、订单或商家业务结果。
- [x] R1：BFF 在后端返回 401 时清理会话 Cookie；前端为 401、403、409、503 提供确定的错误、重试或重新登录提示。
- [x] R2：店铺目录、店铺页、店铺商品页、商品详情和搜索页改为读取公开真实接口；静态图片与色彩映射仅保留为视觉资产。
- [x] R2：补齐 loading、空结果、非法店铺和服务不可用状态，不再把 `demoStores`、`demoMarketplaceProducts` 当作真实目录或搜索结果。

### 今天验收

- [x] 前端 Biome lint、TypeScript 类型检查、Vitest 和 Next.js production build 通过。
- [x] Docker 依赖服务与本地后端启动；`/actuator/health` 返回 `UP`。
- [x] 公开接口和 BFF 验证两家店铺数据归属正确；商家 B 最小测试商品可在公开列表、搜索、店铺页和商品详情读取。
- [x] 无效会话返回 401；消费者访问商家接口返回 403；重复注册返回 409；不可达后端由 BFF 返回 503 与“服务暂时无法连接”。
- [x] 非法店铺页面返回 404；主前端服务返回 200；临时 503 验收实例已关闭。
- [ ] 未生成自动化浏览器视觉截图：本机浏览器自动化环境缺少 Chrome；本次仅将已完成的接口级和构建级证据写为通过。

### 今天完成后的边界

- 商家 B 的最小联调商品保留在本地测试数据库，便于后续查看；未修改任何 `server/` 业务源码。
- R1、R2 前端改动已由用户提交并推送到 `feature/web-v2`：`1fccdfa feat(web): 完成 R1/R2 真实接口联调`。
- 下一步进入 R3：先由用户按“讲一步、写一步、验收一步”的方式完成真实购物车详情接口，接口稳定后再由 Agent 直接补前端。

## Day 1：2026-07-19

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 2：准备开发环境；步骤 4：创建最小 Spring Boot 应用
- 今日目标：确认后端开发环境可用，理解 `SecurityConfig`，跑通 `GET /api/ping`

### 今天要学

- 知识点 1：`SecurityConfig` 与 `SecurityFilterChain`
- 知识点 2：Controller 与统一返回 `ApiResponse`
- 学到什么程度算够：能说清楚 `/api/ping` 请求进入后端后，先经过 Spring Security 放行，再进入 Controller，最后返回统一 JSON。

### 今天要做

- [x] 任务 1：确认项目目录、后端骨架和依赖配置
- [x] 任务 2：创建 `ApiResponse`、`PingController`、`SecurityConfig`
- [x] 任务 3：启动后端并验证 `/api/ping` 返回 `pong`

### 今天验收

- [x] IDEA 后端启动正常
- [x] 接口请求成功或失败结果符合预期
- [ ] DataGrip 中数据正确（今天未做数据库）
- [ ] 前端页面或浏览器 Network 结果正确（今天未做前端）
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息：待补提交

### 今天完成

- 完成了：后端最小接口 `/api/ping` 已跑通，返回 `{"code":0,"message":"ok","data":"pong"}`。
- 没完成：Swagger UI、Actuator 健康检查还需要最终打开确认；Git 提交待补。
- 卡住点：`SecurityConfig` 路径少 `/` 导致 403；`ApiResponse.ok()` 自己调用自己导致 `StackOverflowError`。
- 明天优先做：先完成 Swagger/Actuator 验收与 Git 提交，再补步骤 3：MySQL、Redis、Docker Compose、DataGrip 连接。

## Day 2：2026-07-20

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 3：只启动 MySQL 与 Redis；加餐完成步骤 5：统一响应、异常、校验和请求日志
- 今日目标：用 Docker Compose 启动 MySQL 与 Redis，并用 DataGrip 验收 MySQL 连接

### 今天要学

- 知识点 1：`deploy/docker-compose.yml` 与后端 `application.yml/properties` 的分工
- 知识点 2：Docker 端口映射，例如 `3307:3306`
- 知识点 3：DataGrip 连接 MySQL 并执行基础 SQL
- 学到什么程度算够：能说清楚 Docker Compose 负责启动 MySQL/Redis，Spring Boot 配置负责连接它们；本机访问 MySQL 使用 `localhost:3307`，容器内部仍是 `3306`。

### 今天要做

- [x] 任务 1：创建 `deploy/docker-compose.yml`，配置 MySQL 8.4 与 Redis 7.4
- [x] 任务 2：启动 Docker Desktop 后执行 `docker compose up -d`
- [x] 任务 3：用 Redis `PING` 与 DataGrip `SELECT 1;` 验收服务
- [x] 加餐：完成步骤 5，修正 `ApiResponse`，创建业务异常、全局异常处理、参数校验和请求日志

### 今天验收

- [x] MySQL 容器 `ai-commerce-mysql` 正常运行
- [x] Redis 容器 `ai-commerce-redis` 正常运行
- [x] Redis 返回 `PONG`
- [x] DataGrip 成功连接 `ai_commerce`
- [x] DataGrip 执行 `SELECT 1;` 返回 `1`
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [x] 已提交 Git，提交信息：`chore: add mysql redis compose`
- [x] `/api/debug/biz-error` 返回统一业务异常 JSON
- [x] `/api/debug/system-error` 返回统一系统异常 JSON
- [x] `/api/debug/validate` 合法参数返回 `code: 0`
- [x] `/api/debug/validate` 非法参数返回 `code: 400`
- [x] IDEA 控制台能打印请求方法、URL、HTTP 状态和耗时

### 今天完成

- 完成了：步骤 3 已通过验收；MySQL 使用本机端口 `3307` 映射容器端口 `3306`，Redis 使用 `6379`；`deploy/.env` 已加入 `.gitignore`。加餐完成步骤 5 的统一响应、业务异常、全局异常处理、参数校验和请求日志。
- 没完成：步骤 5 加餐代码尚未提交 Git。
- 卡住点：第一次执行 `docker compose up -d` 时 Docker Desktop 没有启动，导致无法连接 Docker API；启动 Docker Desktop 后解决。
- 明天优先做：步骤 6，配置 Spring Boot 连接 MySQL，并用 Flyway 创建第一批表。

## Day 3：2026-07-21

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 6：连接数据库并落地第一批表
- 今日目标：配置 Spring Boot 连接 MySQL，并用 Flyway 创建第一批业务表

### 今天要学

- 知识点 1：Spring Boot 的 `spring.datasource.*` 配置与 IDEA 环境变量
- 知识点 2：Flyway 版本迁移文件命名、执行记录和 `flyway_schema_history`
- 知识点 3：Spring Boot 自动配置与 `@SpringBootApplication(exclude = ...)` 的影响
- 学到什么程度算够：能说清楚后端启动时先创建 MySQL 连接，再由 Flyway 扫描 `db/migration`，按 `V1`、`V2` 顺序执行未运行过的 SQL，并把结果记录到 `flyway_schema_history`。

### 今天要做

- [x] 任务 1：配置 `application.properties` 连接 Docker 中的 MySQL，密码从 IDEA 环境变量读取
- [x] 任务 2：创建 `V1__init_schema.sql`，包含 `tenant`、`sys_user`、`product_spu`、`product_sku`
- [x] 任务 3：启动后端触发 Flyway，并用 DataGrip 验收表结构和迁移记录

### 今天验收

- [x] IDEA 后端启动正常
- [x] `/api/ping` 返回 `{"code":0,"message":"ok","data":"pong"}`
- [x] DataGrip 中可见 `flyway_schema_history`、`tenant`、`sys_user`、`product_spu`、`product_sku`
- [ ] 前端页面或浏览器 Network 结果正确（今天未做前端）
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [x] 已提交 Git，提交信息：`feat(db): add initial schema migration`

### 今天完成

- 完成了：Spring Boot 已通过 `localhost:3307` 连接 MySQL；Flyway 已执行 `V1__init_schema.sql`；第二次启动显示数据库已是版本 1，无需重复迁移。
- 没完成：第 1 周复盘还未做。
- 卡住点：一开始没有 Flyway 日志，因为启动类里排除了 `DataSourceAutoConfiguration` 和 `FlywayAutoConfiguration`；移除 `exclude` 后解决。
- 明天优先做：做第 1 周复盘和补漏，确认 Swagger/Actuator、从零启动链路，并提交代码。

## Day 4：2026-07-22

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`、`06-每日推进看板与任务安排.md`
- 当前步骤：第 1 周复盘和补漏；加餐开启步骤 7：注册、登录与当前用户接口
- 今日目标：从零验收 MySQL/Redis/后端/接口文档/Flyway；修复 Swagger UI 兼容问题；跑通临时 token 登录接口

### 今天要学

- 知识点 1：从零启动链路：Docker 依赖服务 → Spring Boot → Flyway → 接口请求
- 知识点 2：Swagger UI 与 `/v3/api-docs` 的关系
- 知识点 3：`ex.printStackTrace()` 与 `log.error("Unhandled exception", ex)` 的区别
- 知识点 4：BCrypt、MyBatis 查询、Entity 转 VO、Controller/Service/Mapper 分层
- 学到什么程度算够：能说清楚一个请求失败时，先判断服务是否启动、请求是否进后端、是否被 Security 拦截、是否进入目标接口，再看真实异常和依赖版本；能说清楚登录接口如何从 username/password 查用户并校验 BCrypt 哈希。

### 今天要做

- [x] 任务 1：确认 MySQL/Redis 容器运行，并用 Redis `PING` 验收
- [x] 任务 2：启动后端，验收 `/api/ping`、`/actuator/health`、Swagger UI 和 `/v3/api-docs`
- [x] 任务 3：用 DataGrip 验收业务表和 `flyway_schema_history`，完成第 1 周复盘
- [x] 加餐：创建登录请求/响应 DTO/VO、初始化测试账号、实现临时 token 登录接口

### 今天验收

- [x] MySQL 容器 `ai-commerce-mysql` 正常运行，端口 `3307 -> 3306`
- [x] Redis 容器 `ai-commerce-redis` 正常运行，端口 `6379 -> 6379`
- [x] Redis 返回 `PONG`
- [x] IDEA 后端启动正常，Flyway 显示 `Schema ai_commerce is up to date. No migration necessary.`
- [x] `/api/ping` 返回 `{"code":0,"message":"ok","data":"pong"}`
- [x] `/actuator/health` 返回 `{"status":"UP"}`
- [x] `/v3/api-docs` 返回包含 `"openapi":"3.1.0"` 的 OpenAPI JSON
- [x] Swagger UI 能正常展示 `debug-controller` 和 `ping-controller`
- [x] DataGrip 中可见 `flyway_schema_history`、`tenant`、`sys_user`、`product_spu`、`product_sku`
- [x] `flyway_schema_history` 中 `V1__init_schema.sql` 的 `success` 为成功状态
- [x] `V2__init_auth_users.sql` 初始化 2 个商家租户和 3 个测试用户
- [x] `V3__reset_test_user_password.sql` 重置测试用户 BCrypt 密码哈希
- [x] `POST /api/auth/login` 正确账号密码返回 `todo-access-token` 和当前用户信息
- [x] `POST /api/auth/login` 错误密码返回 `code: 401`
- [x] 临时 `/api/debug/user/**` 和 `/api/debug/password-match/**` 验收接口已删除
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(auth): add password login flow`

### 今天完成

- 完成了：第 1 周核心验收通过；修复 Swagger UI 因 springdoc 版本不兼容导致无法渲染接口文档的问题；全局兜底异常改为正式日志记录；加餐跑通临时 token 登录接口。
- 没完成：JWT、`GET /api/auth/me` 和消费者注册接口尚未开始；步骤 7 加餐代码和文档尚未提交 Git。
- 卡住点：`/v3/api-docs` 返回统一错误 JSON，Swagger UI 提示 `Unable to render this definition`，原因是 springdoc `2.2.0` 与 Spring Boot `3.5.16` 不兼容；`UserMapper` 启动失败，原因是 MyBatis Starter `2.3.0` 与 Spring Boot 3.5 不兼容；BCrypt 初始哈希不匹配，通过 `V3` 新迁移重置测试密码解决。
- 明天优先做：接入 JWT，完成 `GET /api/auth/me`，让 `todo-access-token` 变成真实 token。

## Week 1 复盘：2026-07-22

### 本周目标

- 完成可启动的前后端骨架，至少跑通后端、MySQL、Redis、接口文档和第一批数据库迁移。

### 本周完成

- [x] 项目目录、Git 仓库、`server`、`web`、`deploy`、`docs` 基础结构已建立
- [x] 后端最小接口 `/api/ping` 已跑通
- [x] MySQL/Redis 已用 Docker Compose 启动并验收
- [x] 统一响应、业务异常、全局异常、参数校验和请求日志已建立
- [x] Spring Boot 已连接 MySQL
- [x] Flyway 已创建第一批表：`tenant`、`sys_user`、`product_spu`、`product_sku`
- [x] Swagger UI 和 Actuator 已验收

### 本周验收

- [x] 项目能从零启动
- [x] 本周核心接口能演示
- [x] DataGrip 中关键数据正确
- [x] `learning-log.md` 已记录主要问题和解决办法
- [x] Git 已提交到一个清晰状态：`fix(api-docs): update springdoc and log unexpected exceptions`

### 没通过的地方

- 无核心验收失败项；Swagger UI 曾因 springdoc 版本兼容问题失败，已修复。

### 下周开始前必须补的内容

- 开始 JWT 前，确认登录接口成功/失败截图已记录。
- 步骤 7 加餐代码提交后，再进入 JWT 与 `/api/auth/me`。

## Day 5：2026-07-23

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 7：实现注册、登录与「当前用户」接口
- 今日目标：接入 JWT，完成 `GET /api/auth/me`，让 `todo-access-token` 变成真实 token。

### 今天要学

- 知识点 1：JWT 的生成、签名、过期时间和解析校验
- 知识点 2：Bearer Token、`Authorization` 请求头和 `Content-Type: application/json`
- 知识点 3：Spring Security Filter、无状态 Session、`SecurityContext`
- 知识点 4：`catch`、`HttpStatus.UNAUTHORIZED.value()`、构造器注入配置值与 Bean 的区别
- 学到什么程度算够：能说清楚登录成功后后端如何生成 JWT；请求 `/api/auth/me` 时后端如何从 `Authorization: Bearer <token>` 解析当前用户，并从数据库返回完整用户信息。

### 今天要做

- [x] 任务 1：增加 `java-jwt` 依赖和 `app.jwt.*` 配置，登录成功返回真实 JWT
- [x] 任务 2：创建 `JwtService`、`LoginPrincipal`、`JwtAuthentication`、`CurrentUser`，并把 JWT Filter 接入 `SecurityConfig`
- [x] 任务 3：实现 `GET /api/auth/me`，从 token 恢复当前用户并查询数据库返回完整信息

### 今天验收

- [x] IDEA 后端启动正常
- [x] `POST /api/auth/login` 正确账号密码返回三段式 JWT
- [x] `GET /api/auth/me` 不带 token 返回 HTTP 401
- [x] `GET /api/auth/me` 带 `Authorization: Bearer <accessToken>` 返回 `code: 0`
- [x] `/api/auth/me` 返回完整用户信息：`id=2`、`username=merchant_a_admin`、`userType=MERCHANT_ADMIN`、`tenantId=1001`
- [ ] DataGrip 中数据正确（今天未新增表，沿用 `sys_user`）
- [ ] 前端页面或浏览器 Network 结果正确（今天未做前端）
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(auth): add jwt current user`

### 今天完成

- 完成了：真实 JWT 登录已跑通；`/api/auth/me` 已受保护；无 token 返回 401；带正确 token 能从 `SecurityContext` 取出当前用户，并通过 `userId` 查询 `sys_user` 返回完整用户信息。
- 没完成：消费者注册接口尚未实现；`POST /api/auth/login` 空 Body 目前仍会被兜底为 `code: 500`，后续可补请求体缺失异常处理。
- 卡住点：Apifox 中 `Authorization` Header 未勾选时实际不会发送；`POST /api/auth/me` 与 `GET /api/auth/me` 混用导致 401；`SecurityConfig` 一度漏掉 `addFilterBefore`，导致 JWT 过滤器没有进入请求链路。
- 明天优先做：补消费者注册接口；再进入步骤 8，收紧商家/消费者角色权限和租户边界。

## Day 6：2026-07-24

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 7 收口；步骤 8：先把权限边界锁住
- 今日目标：补消费者注册接口；补请求体缺失异常处理；完成 `/api/merchant/**` 的 401/403/200 最小权限验收

### 今天要学

- 知识点 1：注册 DTO、Controller、Service、Mapper 的分工
- 知识点 2：`PasswordEncoder`、`BCryptPasswordEncoder`、`encode` 与 `matches`
- 知识点 3：`requestMatchers(...).permitAll()`、`hasAnyRole(...)`、`authenticated()` 的顺序和职责
- 知识点 4：`authenticationEntryPoint` 与 `accessDeniedHandler` 的区别
- 学到什么程度算够：能说清楚注册时为什么只开放消费者、密码为什么存 BCrypt 哈希、消费者为什么不能访问商家接口，以及 401/403 分别表示什么。

### 今天要做

- [x] 任务 1：新增 `POST /api/auth/register`，只允许注册消费者，密码用 BCrypt 哈希入库
- [x] 任务 2：补 `HttpMessageNotReadableException`，让空 Body 或坏 JSON 返回 `code: 400`
- [x] 任务 3：收紧 `/api/merchant/**` 角色权限，新增 `/api/merchant/context` 验收商家租户上下文

### 今天验收

- [x] `POST /api/auth/register` 正常注册 `consumer_today_01`，返回 `userType=CONSUMER`、`tenantId=null`
- [x] 重复注册 `consumer_today_01` 返回 `code: 409` 和 `用户名已存在`
- [x] 注册接口空 Body 返回 `code: 400` 和 `请求体不能为空或 JSON 格式不正确`
- [x] DataGrip 中 `consumer_today_01` 的 `tenant_id` 为 `NULL`、`user_type` 为 `CONSUMER`、`status` 为 `1`
- [x] DataGrip 中 `password_hash` 不是明文 `123456`，而是 BCrypt 哈希
- [x] `GET /api/merchant/context` 不带 token 返回 HTTP 401
- [x] `GET /api/merchant/context` 带消费者 token 返回 HTTP 403
- [x] `GET /api/merchant/context` 带 `merchant_a_admin` token 返回 `code: 0`、`tenantId=1001`、`userType=MERCHANT_ADMIN`
- [x] `MerchantContextController` 已移动到 `merchant/controller` 包
- [x] `mvnw -DskipTests compile` 编译通过
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(auth): add consumer register and merchant guard`

### 今天完成

- 完成了：步骤 7 的消费者注册接口收口；登录、注册、`/auth/me` 形成完整消费者鉴权链路；步骤 8 的最小权限边界通过验收，已区分 401 未认证、403 权限不足和商家成功访问。
- 没完成：还没有开始商品管理接口；今天只做权限边界，不提前进入商品 CRUD。
- 卡住点：Docker Desktop 启动异常曾阻塞 MySQL/Redis；注册接口最初漏加白名单导致未认证；消费者 token 访问商家接口一开始返回 401，补 `accessDeniedHandler` 后正确返回 403。
- 明天优先做：进入步骤 9，开始商家商品最小后端闭环；先从商家当前 `tenantId` 创建 SPU，再逐步补 SKU、库存和列表。

## Day 7：2026-07-25

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 9：完成商品的最小后端闭环
- 今日目标：跑通商家商品最小后端闭环第一版：创建 SPU、新增 SKU、初始库存入库与商家商品列表查询。

### 今天要学

- 知识点 1：SPU 与 SKU 的区别：SPU 表示商品本体，SKU 表示具体可售规格、价格和库存。
- 知识点 2：DTO、Entity、VO 的分工：请求体、数据库对象和接口返回对象不要混在一起。
- 知识点 3：商家商品接口的租户边界：`tenantId` 必须从 JWT 当前用户取得，不能相信前端传值。
- 知识点 4：MyBatis 注解式 Mapper、`@PathVariable`、`LIMIT/OFFSET` 分页和 `BigDecimal` 金额字段。
- 学到什么程度算够：能说清楚商家 token 如何通过 `CurrentUser.requiredMerchantTenantId()` 限定商品创建、SKU 新增和列表查询的租户范围；能解释为什么价格不用 `double`、为什么列表返回 VO。

### 今天要做

- [x] 任务 1：新增商家创建 SPU 接口 `POST /api/merchant/products`
- [x] 任务 2：新增 SKU 创建接口 `POST /api/merchant/products/{id}/skus`，并写入初始库存
- [x] 任务 3：新增商家商品列表接口 `GET /api/merchant/products?page=&size=&keyword=`

### 今天验收

- [x] `POST /api/merchant/products` 不带 token 返回 HTTP 401
- [x] `POST /api/merchant/products` 带消费者 token 返回 HTTP 403
- [x] `POST /api/merchant/products` 带 `merchant_a_admin` token 返回 `code: 0` 和商品 ID
- [x] DataGrip 中 `product_spu` 可见「蓝牙耳机」，`tenant_id=1001`，`status=DRAFT`
- [x] `POST /api/merchant/products/{id}/skus` 成功新增「白色 / 标准版」和「黑色 / Pro版」两个 SKU
- [x] DataGrip 中 `product_sku` 两条 SKU 的 `tenant_id=1001`，`spu_id` 指向同一个 SPU，价格和库存正确
- [x] `GET /api/merchant/products?page=1&size=10` 返回当前商家商品列表
- [x] `GET /api/merchant/products?page=1&size=10&keyword=耳机` 能查询到「蓝牙耳机」
- [x] `mvnw -DskipTests compile` 编译通过
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(product): add merchant product basics`

### 今天完成

- 完成了：步骤 9 的商家商品最小后端闭环第一版已跑通，包含创建 SPU、新增 SKU、初始库存入库、商家列表查询，以及沿用 `/api/merchant/**` 的 401/403/200 权限边界。
- 没完成：商品上架/下架接口还没做；商品列表暂未返回 SKU 数、最低价、总库存等汇总字段；ID 仍使用 `System.currentTimeMillis()` 临时方案。
- 卡住点：PowerShell 读取中文源码时多次显示乱码，导致误判字符串是否闭合；后续以 IDEA 语法检查和 `mvnw -DskipTests compile` 编译结果为准。
- 明天优先做：补商品上架/下架与列表汇总字段，或开始步骤 10：消费者公开商品接口。

## Day 8：2026-07-27

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 9 收口：补商品上架/下架与商家商品列表汇总字段
- 今日目标：让商家商品管理闭环补齐“创建 SPU -> 新增 SKU -> 上架/下架 -> 列表看到 SKU 汇总”的完整链路。

### 今天要学

- 知识点 1：SPU 状态与 SKU 状态的关系：新建 SPU 为 `DRAFT`，上架后为 `ON_SALE`，下架后为 `OFF_SALE`。
- 知识点 2：MyBatis `@Update` 返回 `int` 表示受影响行数，可以判断更新是否真的命中当前商家的商品。
- 知识点 3：列表聚合查询：`LEFT JOIN`、`COUNT`、`MIN`、`SUM`、`COALESCE` 如何把 SKU 信息汇总到 SPU 列表 VO。
- 学到什么程度算够：能说清楚为什么上架前要检查 SKU 数量，为什么更新状态必须带 `id + tenant_id`，以及 `COALESCE(SUM(...), 0)` 为什么能把无 SKU 商品的总库存兜底为 0。

### 今天要做

- [x] 任务 1：新增商品上架接口 `POST /api/merchant/products/{id}/publish`
- [x] 任务 2：新增商品下架接口 `POST /api/merchant/products/{id}/unpublish`
- [x] 任务 3：增强商家商品列表，返回 `skuCount`、`minSalePrice`、`totalAvailableStock`

### 今天验收

- [x] `mvnw -DskipTests compile` 编译通过
- [x] `POST /api/merchant/products/1784967699881/publish` 带商家 token 返回 `code: 0`
- [x] DataGrip 中「蓝牙耳机」`status` 变为 `ON_SALE`
- [x] `POST /api/merchant/products/1784967699881/unpublish` 带商家 token 返回 `code: 0`
- [x] DataGrip 中「蓝牙耳机」`status` 变为 `OFF_SALE`
- [x] `GET /api/merchant/products?page=1&size=10` 返回 `skuCount=2`、`minSalePrice=199.00`、`totalAvailableStock=70`
- [x] 消费者 token 访问商品上架接口返回 HTTP 403
- [x] 商家 token 访问不存在商品 `999999999999` 的上架接口返回 `code: 404` 和 `商品不存在`
- [x] `.vs/` 已加入 `.gitignore`，避免提交 Visual Studio 本机缓存
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(product): add publish status and sku summaries`

### 今天完成

- 完成了：步骤 9 的商家商品最小后端闭环已补强，新增商品上架/下架接口，列表返回 SKU 数、最低价和总可售库存，并完成商家成功、消费者 403、无效商品 404 的验收。
- 没完成：还未进入步骤 10 的消费者公开商品接口；正式 ID 方案仍沿用后续再替换。
- 卡住点：最开始未按“带着做”协作规则推进，后来改为逐步讲解、用户在 IDEA 中手写；Apifox 上架接口曾因 URL 带 `{}` 导致 401，改为真实路径后通过。
- 明天优先做：进入步骤 10，实现消费者公开商品列表、商品详情和 SKU 可售状态查询，只展示已上架商品。

## Day 9：2026-07-28

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 10：实现消费者公开商品接口
- 今日目标：完成公开商品列表、商品详情和 SKU 可售状态查询；只展示已上架商品，不泄露商家内部字段。

### 今天要学

- 知识点 1：公开接口与商家接口的边界：`/api/public/**` 未登录可访问，但必须只返回消费者可见数据。
- 知识点 2：VO 分层：列表 VO、详情 VO、SKU VO、BaseVO 分别服务不同查询和返回结构。
- 知识点 3：MyBatis 查询与对象组装：平铺 SQL 结果由 Mapper 接收，嵌套结构由 Service 组装。
- 知识点 4：SKU 可售状态：同时检查 SKU 状态、SPU 状态和可售库存。
- 学到什么程度算够：能说清楚为什么公开接口要放行但仍要限制 `ON_SALE`，为什么 `PublicProductDetailVO` 里的 `skus` 不能靠单条 SPU SQL 自动获得，以及为什么下架后详情不可见、SKU 不可购买。

### 今天要做

- [x] 任务 1：放行 `/api/public/**`，并用 `GET /api/public/products/ping` 验收公开通道。
- [x] 任务 2：实现公开商品列表 `GET /api/public/stores/{storeId}/products?page=&size=`。
- [x] 任务 3：实现公开商品详情 `GET /api/public/products/{spuId}` 和 SKU 可售状态 `GET /api/public/skus/{skuId}/availability`。

### 今天验收

- [x] `GET /api/public/products/ping` 未登录返回 `public-product-pong`
- [x] `GET /api/public/stores/1001/products?page=1&size=10` 未登录返回「蓝牙耳机」
- [x] 公开商品列表返回 `minSalePrice=199.00`、`totalAvailableStock=70`
- [x] 公开商品列表不返回 `tenantId`、`lockedStock`、`version`
- [x] `GET /api/public/products/1784967699881` 返回商品详情和 `skus` 数组
- [x] 商品详情不返回 `tenantId`、`lockedStock`、`version`
- [x] `GET /api/public/skus/1784970220075/availability` 返回 `purchasable=true`、`availableStock=50`、`message=可购买`
- [x] 不存在 SKU `999999999999` 返回 `purchasable=false`、`availableStock=0`、`message=商品不存在或已下架`
- [x] 商品下架后，公开详情返回 `code: 404` 和 `商品不存在`
- [x] 商品下架后，SKU 可售状态返回 `purchasable=false`
- [x] `mvnw -DskipTests compile` 编译通过
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`feat(product): add public product APIs`

### 今天完成

- 完成了：步骤 10 的消费者公开商品接口闭环已完成，包含公开列表、公开详情、SKU 可售状态、下架不可见和不存在 SKU 兜底结果。
- 没完成：还没有进入前端页面、购物车和订单；正式 ID 方案仍未替换。
- 卡住点：`@PathVariable` 方法一开始缺少带 `{storeId}` 的 `@GetMapping` 路径导致 500；`PublicSkuVO` 最初放到商家包导致公开模块边界混乱；详情查询一开始尝试让 SPU SQL 直接映射带 `skus` 的详情 VO，后来拆出 `PublicProductBaseVO` 后解决。
- 明天优先做：根据精力选择步骤 11 极简商品管理页面，或继续后端优先进入步骤 12 购物车表与接口。

## Day 9 加餐：2026-07-28

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`、`04-Vue前端开发链.md`
- 当前步骤：步骤 11 第一版：极简商品管理页面；对应前端步骤 31、32、33 起步
- 今日目标：创建 Vue 3 前端基础壳，完成商家登录态公共能力，并接入真实商家商品列表接口。

### 今天要学

- 知识点 1：Vue 单页应用启动链路：`index.html -> main.ts -> App.vue -> router-view -> View.vue`
- 知识点 2：Vue 基础语法：`ref`、`.value`、`computed`、`v-model`、`v-for`、`v-if/v-else`、`@click`、`:disabled`
- 知识点 3：Axios、Vite 代理、Pinia 登录态、`localStorage` token、路由守卫
- 学到什么程度算够：能说清楚为什么 Vue 只有一个 `index.html`，为什么路由把不同 URL 映射到不同组件，为什么登录后要保存 token，并在刷新后用 `/api/auth/me` 恢复当前用户。

### 今天要做

- [x] 任务 1：在 `web` 创建 Vue 3 + Vite + TypeScript 工程，并安装 Vue Router、Pinia、Axios、Element Plus
- [x] 任务 2：完成前端登录态公共能力：Axios 实例、Vite `/api` 代理、登录接口封装、Pinia auth store、登录页、`/auth/me` 恢复用户、路由守卫和 403 页面
- [x] 任务 3：完成商家商品列表第一版，调用 `GET /api/merchant/products?page=1&size=10` 显示真实商品数据

### 今天验收

- [x] `npm.cmd run dev` 启动前端，`http://localhost:5173/` 可访问
- [x] `/` 和 `/merchant/products` 能通过 `vue-router` 显示不同页面组件
- [x] Vue 练习页完成变量显示、点击事件、列表渲染、条件显示、搜索过滤和表格操作按钮
- [x] `POST /api/auth/login` 通过 Vite 代理请求后端成功，返回 `accessToken` 和 `merchant_a_admin`
- [x] 登录成功后 `localStorage` 中出现 `access_token`
- [x] 刷新 `/merchant/products` 后，`GET /api/auth/me` 返回当前用户 `id=2`、`userType=MERCHANT_ADMIN`、`tenantId=1001`
- [x] 删除 `access_token` 后访问 `/merchant/products` 自动跳转 `/merchant/login?redirect=/merchant/products`
- [x] 登录成功后能按 `redirect` 回到 `/merchant/products`
- [x] `/403` 页面可访问并显示无权限提示
- [x] 商家商品管理页成功显示后端真实商品「蓝牙耳机」、状态和 SKU 数
- [x] `npm.cmd run build` 构建通过
- [x] 截图/请求记录已写入 `docs/learning-log.md`，截图命名放在 `docs/images/day-9-加餐/加餐-*.png`
- [ ] 已提交 Git，提交信息建议：`feat(web): add merchant product list shell`

### 今天完成

- 完成了：步骤 11 第一版已跑通，前端从空目录变为可启动 Vue 工程；商家登录、token 保存、刷新恢复用户、未登录拦截、403 兜底和真实商品列表都已完成。
- 没完成：还没有做新建商品表单、新增 SKU、上架/下架按钮、分页控件和 Element Plus 组件化改造。
- 卡住点：误删过 `main.ts`、`App.vue` 和 `LoginView.vue` 内容；登录失败曾被误以为账号密码错误，实际是后端未启动导致 Vite 代理 `ECONNREFUSED`；配置文件看起来杂乱，但目前保留在根目录最符合 Vite 默认约定。
- 明天优先做：如果继续前端，补商品管理页的新建商品/SKU/上下架操作；如果回到后端，进入步骤 12 购物车表与接口。

## Day 10：2026-07-29

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 12：购物车表与接口
- 今日目标：完成 `cart_item` 表，并跑通消费者购物车新增、查询、修改数量和删除接口。

### 今天要学

- 知识点 1：购物车里的三个 ID：`cart_item.id` 是购物车项自己的 ID，`consumerId` 是当前消费者，`skuId` 是被加入购物车的具体商品规格。
- 知识点 2：购物车唯一约束：同一个消费者同一个 SKU 只保留一行，用 `consumer_id + sku_id` 唯一约束避免重复行。
- 知识点 3：购物车接口语义：`POST` 新增或合并数量，`GET` 查询列表，`PUT /{id}` 直接设置数量，`DELETE /{id}` 删除购物车项。
- 知识点 4：消费者权限边界：购物车接口必须要求消费者 token；无 token 返回 401，商家 token 返回 403。
- 知识点 5：加入和修改数量时复用 SKU 可售校验：SPU/SKU 必须可售，目标数量不能超过可售库存。
- 学到什么程度算够：能说清楚为什么购物车操作必须使用 `id + consumerId` 防越权，为什么删除购物车不需要校验商品是否上架，以及为什么商家 token 不能访问消费者购物车。

### 今天要做

- [x] 任务 1：新增 Flyway `V4__add_cart_item.sql`，创建 `cart_item` 表、唯一约束和索引。
- [x] 任务 2：实现购物车新增与查询：`POST /api/cart/items`、`GET /api/cart/items`。
- [x] 任务 3：实现购物车修改数量与删除：`PUT /api/cart/items/{id}`、`DELETE /api/cart/items/{id}`。

### 今天验收

- [x] Flyway 成功执行 `V4__add_cart_item.sql`，`ai_commerce` schema 到版本 4。
- [x] DataGrip 中可见 `cart_item` 表，`flyway_schema_history` 记录 `add cart item` 成功。
- [x] `POST /api/cart/items` 消费者 token + 上架 SKU 返回 `code: 0`，并生成购物车项。
- [x] 重复加入同一 SKU 合并数量，`cart_item` 仍只有一行，`quantity` 从 1 变为 2。
- [x] SKU 对应 SPU 下架时，加入购物车返回 `code: 409` 和 `商品不可购买`。
- [x] `quantity=0` 返回 `code: 400` 和 `数量必须大于等于1`。
- [x] `quantity=999` 返回 `code: 409` 和 `库存不足`。
- [x] `GET /api/cart/items` 消费者 token 返回购物车列表。
- [x] `GET /api/cart/items` 不带 token 返回 HTTP 401。
- [x] 商家 token 访问购物车接口返回 `code: 403` 和 `不是消费者账号`。
- [x] `PUT /api/cart/items/{id}` 能把数量修改为 3。
- [x] `DELETE /api/cart/items/{id}` 删除成功后，`GET /api/cart/items` 返回空列表。
- [x] 重复删除同一个购物车项返回 `code: 404` 和 `购物车项不存在`。
- [x] `mvnw -DskipTests compile` 编译通过。
- [x] 截图/请求记录已写入 `docs/learning-log.md`，截图放在 `docs/images/day-10/`。
- [x] 已提交 Git，提交信息：`feat(cart): add consumer cart APIs`

### 今天完成

- 完成了：步骤 12 购物车表与接口闭环已完成，包含建表、加入购物车、合并数量、查询列表、修改数量、删除、重复删除、库存不足、商品不可购买、401/403 权限边界。
- 没完成：购物车列表暂时只返回 `id`、`skuId`、`quantity`，还没有返回 SKU 名称、价格、图片等前端展示字段；正式 ID 方案仍沿用后续再替换。
- 卡住点：DataGrip 左侧表结构一度没有刷新出 `cart_item`，但 `SHOW TABLES` 已能查到；商家 token 查询购物车一度返回系统异常，原因是误导入了 `java.nio.file.AccessDeniedException`，改为 Spring Security 的 `AccessDeniedException` 后正确返回 403。
- 明天优先做：进入步骤 13 普通订单最小闭环；如果想先补前端体验，可先做消费者购物车页面和购物车列表展示字段。

## Day 11：2026-07-30

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 13：先画清订单状态和建表
- 今日目标：理解普通订单状态机，并创建 `commerce_order`、`commerce_order_item` 两张订单表。

### 今天要学

- 知识点 1：订单状态机：`PENDING_PAYMENT`、`PAID`、`SHIPPED`、`COMPLETED`、`CLOSED`、`AFTER_SALE` 的流转边界。
- 知识点 2：订单主表与订单明细表的分工：主表记录订单整体，明细表记录每个 SKU。
- 知识点 3：订单快照：历史订单保存商品名称和价格，不能跟着当前商品改名或改价变化。
- 学到什么程度算够：能说清楚为什么一笔订单要拆成主表和明细表，为什么 `order_no` 要唯一，为什么订单项要保存 `sku_name_snapshot` 和 `sale_price`。

### 今天要做

- [x] 任务 1：把订单状态机写入 `docs/learning-log.md`，明确哪些状态能流转。
- [x] 任务 2：新增 Flyway `V5__add_commerce_order.sql`，创建 `commerce_order` 与 `commerce_order_item`。
- [x] 任务 3：启动后端触发 Flyway，并用 DataGrip 验收两张订单表和版本 5 迁移记录。

### 今天验收

- [x] 后端启动正常，Flyway 从版本 4 迁移到版本 5。
- [x] DataGrip 中可见 `commerce_order` 和 `commerce_order_item`。
- [x] `flyway_schema_history` 最新记录为 `V5__add_commerce_order.sql`，`success` 为成功。
- [x] `DESC commerce_order` 可见订单号、租户、消费者、状态、总金额和过期时间字段。
- [x] `DESC commerce_order_item` 可见订单 ID、SKU ID、商品名快照、售价和数量字段。
- [x] `mvnw -DskipTests compile` 编译通过。
- [x] 截图/请求记录已写入 `docs/learning-log.md`。
- [x] 已提交 Git，提交信息：`feat(order): add order schema migration`

### 今天完成

- 完成了：步骤 13 的订单状态机和订单建表已完成，两张订单表已由 Flyway 迁移到 MySQL。
- 没完成：还没有实现下单接口，库存扣减、锁定库存、写订单和写订单项事务放到步骤 14。
- 卡住点：暂无。
- 明天优先做：进入步骤 14，实现 `POST /api/orders` 普通下单事务。

## Day 12：2026-07-31

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 14：实现普通下单事务
- 今日目标：完成 `POST /api/orders`，从购物车创建待支付订单，并验证库存锁定、订单明细和失败回滚。

### 今天要学

- 知识点 1：`@Transactional` 为什么要包住订单主表、库存锁定、订单明细和购物车删除。
- 知识点 2：条件扣库存 `UPDATE ... WHERE available_stock >= quantity` 和受影响行数。
- 知识点 3：订单主表与订单明细表的关系，以及订单项快照为什么要保存 SKU 名称和下单价格。
- 知识点 4：业务订单号 `orderNo` 的临时生成方式：`ORD + 年月日时分秒 + 6 位随机数`。
- 学到什么程度算够：能说清楚一笔购物车下单为什么要先重新查数据库快照，再在一个事务里写订单、锁库存、写订单项并删除购物车项；也能说明失败时为什么不能留下半截订单。

### 今天要做

- [x] 任务 1：补完 `OrderService.createOrderVO(...)`，完成金额计算、订单号生成、写订单、锁库存、写订单项和删除购物车项。
- [x] 任务 2：新增 `OrderController`，暴露 `POST /api/orders`。
- [x] 任务 3：用 Apifox 和 DataGrip 验收成功下单与库存不足失败路径。

### 今天验收

- [x] `mvnw -DskipTests compile` 编译通过。
- [x] `POST /api/cart/items` 消费者 token 加入 SKU `1784970220075` 成功，生成购物车项 `1785483128179`。
- [x] `POST /api/orders` 使用 `cartItemIds=[1785483128179]` 返回 `code: 0`，生成订单 `id=1`、`status=PENDING_PAYMENT`、`totalAmount=199.00`。
- [x] DataGrip 中 `commerce_order` 可见订单 `id=1`、`tenant_id=1001`、消费者 ID、待支付状态和订单号。
- [x] DataGrip 中 `commerce_order_item` 可见 `order_id=1`、`sku_id=1784970220075`、`sku_name_snapshot=白色 / 标准版`。
- [x] 下单成功后 `product_sku.available_stock=49`、`locked_stock=1`。
- [x] 下单成功后购物车项 `1785483128179` 已删除。
- [x] 明显库存不足时，`POST /api/cart/items` 数量 `999` 返回 `code=409` 和 `库存不足`。
- [x] 模拟“购物车加入时库存足够、下单前库存变 0”后，`POST /api/orders` 返回 `code=409` 和 `商品库存不足`。
- [x] 库存不足下单失败后没有新增订单，购物车项 `1785485017013` 仍保留。
- [x] 手动验收结束后已恢复 SKU 库存为 `available_stock=49`、`locked_stock=1`。
- [x] 截图/请求记录已写入 `docs/learning-log.md`。
- [x] 已提交 Git，提交信息：`feat(order): add create order transaction`

### 今天完成

- 完成了：步骤 14 普通下单事务已跑通。消费者可以从购物车创建 `PENDING_PAYMENT` 订单；订单主表、订单明细、库存锁定和购物车删除已形成事务闭环；库存不足失败时不新增订单，购物车项保留。
- 没完成：还没有实现模拟支付、订单列表和订单详情；失败路径留下的购物车项 `1785485017013` 暂时保留为验收证据。
- 卡住点：一开始加入购物车误用 `DELETE /api/cart/items` 导致 401，改为 `POST /api/cart/items` 后成功；下单失败验收时 DataGrip `UPDATE` 后未立即可见，原因是事务未提交，提交后可见 `available_stock=0`。
- 明天优先做：进入步骤 15，实现 `POST /api/orders/{id}/mock-pay` 和消费者订单查询，重点验证只能支付本人 `PENDING_PAYMENT` 订单，并把 `locked_stock` 正确减少。

## Day 13：2026-08-01

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 15：实现模拟支付和消费者订单查询
- 今日目标：完成 `POST /api/orders/{id}/mock-pay`、`GET /api/orders` 和 `GET /api/orders/{id}`，验证重复支付、库存账和消费者权限边界。

### 今天要学

- 知识点 1：资源归属校验：订单支付和查询必须使用当前登录消费者，不能相信前端传 `consumerId`。
- 知识点 2：状态转换校验：只有 `PENDING_PAYMENT` 订单可以支付成功变成 `PAID`。
- 知识点 3：条件更新：`UPDATE ... WHERE status = 'PENDING_PAYMENT'` 可以让重复支付自然失败。
- 知识点 4：库存账：支付成功后只减少 `locked_stock`，不回加 `available_stock`。
- 知识点 5：接口返回 VO 和内部查询对象的区别：订单详情返回 `OrderDetailVO/OrderItemVO`，下单前快照 `OrderSkuSnapshotVO` 只是 Mapper 查询结果承载对象。
- 学到什么程度算够：能说清楚为什么第一次支付成功、第二次支付返回 `code=409`；能解释支付成功后 `available_stock` 不变、`locked_stock` 变成 0；能说明为什么订单详情必须先用 `orderId + consumerId` 查主表，再查订单明细。

### 今天要做

- [x] 任务 1：实现模拟支付接口 `POST /api/orders/{id}/mock-pay`。
- [x] 任务 2：实现消费者订单列表和详情接口：`GET /api/orders`、`GET /api/orders/{id}`。
- [x] 任务 3：用 Apifox 和 DataGrip 验收重复支付、库存账、订单查询和商家越权访问失败。

### 今天验收

- [x] `mvnw -DskipTests compile` 编译通过。
- [x] `POST /api/orders/1/mock-pay` 第一次使用消费者 token 返回 `code: 0`。
- [x] `POST /api/orders/1/mock-pay` 第二次重复支付返回 `code: 409` 和 `订单不存在或状态不允许支付`。
- [x] DataGrip 中 `commerce_order.id=1` 的 `status` 已变为 `PAID`，`total_amount=199.00`。
- [x] DataGrip 中 SKU `1784970220075` 的 `available_stock=49`、`locked_stock=0`。
- [x] `GET /api/orders` 使用消费者 token 返回订单列表，包含 `id=1`、`status=PAID`、`totalAmount=199.00`，列表中的 `items=[]`。
- [x] `GET /api/orders/1` 使用消费者 token 返回订单详情，`items` 中包含 SKU `1784970220075`、`skuNameSnapshot=白色 / 标准版`、`salePrice=199.00`、`quantity=1`。
- [x] `GET /api/orders/1` 使用商家 token 返回 `code: 403` 和 `不是消费者账号`。
- [x] 截图/请求记录已写入 `docs/learning-log.md`，截图放在 `docs/images/day-13/`。
- [x] 已提交 Git，提交信息：`feat(order): add mock payment and queries`

### 今天完成

- 完成了：步骤 15 已跑通。普通订单可以从 `PENDING_PAYMENT` 模拟支付为 `PAID`；重复支付被状态条件更新拦住；支付成功后锁定库存正确减少；消费者订单列表和订单详情查询完成；商家 token 访问消费者订单返回 403。
- 没完成：还没有做消费者端订单页面；订单查询暂时未分页；商家订单查询还没做；超时关单和库存释放放到后续步骤。
- 卡住点：一开始把 HTTP 200 误认为两次业务都成功，后来确认第二次响应体是 `code=409`，说明重复支付已被正确拦截；`OrderSkuSnapshotVO` 名字容易误导，它实际是下单前 Mapper 查询结果承载对象，不是严格前端 VO。
- 明天优先做：进入步骤 16，补齐第一个完整页面闭环；如果想继续后端，则进入步骤 17 手工验证库存账。

## Day 13：2026-08-01（前端加餐）

### 今日阶段

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 16，补齐第一个消费者页面闭环。
- 当前分支：`feature/web-v2`；`main` 已有步骤 15 的后端实现，但尚未合入。

### 今天完成

- [x] 新增 `web/src/api/order.ts`，对齐创建订单、订单列表、订单详情和模拟支付四个接口，并统一处理后端 `code != 0` 的业务失败提示。
- [x] 改造 `web/src/views/consumer/CartView.vue`：真实购物车点击“继续结算”会调用 `POST /api/orders`，成功后进入新订单详情。
- [x] 新增 `web/src/views/consumer/OrderListView.vue` 和 `OrderDetailView.vue`，完成订单列表、详情、状态、订单明细、模拟支付和失败重试界面。
- [x] 消费者导航新增“订单”入口，`/cart`、`/orders`、`/orders/:id` 增加消费者登录保护。
- [x] 统一 Axios 业务错误、401、403、5xx 和网络错误提示；购物袋数量更新、删除、加购失败时保留真实数据并显示原因。

### 今日验收

- [x] `npm.cmd run build` 通过，`vue-tsc -b && vite build` 成功。
- [x] 未登录访问 `/orders` 会跳转消费者登录页，并保存原目标地址。
- [ ] 真实下单、模拟支付、订单列表和详情的浏览器联调：等待将 `main` 的 `79963ab` 合入当前分支后，用现有消费者账号验证。

### 今日结论

- 前端订单页面和真实接口契约已就绪；缺的不是页面，而是当前分支尚未带入步骤 15 后端接口。
- 下一步先合入 `main`，再做一次“加入购物袋 -> 创建订单 -> 模拟支付 -> 订单列表/详情”的端到端验收。


## Day 14：2026-08-03

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 17 起步：建立库存账本与普通订单库存流水。
- 今日目标：创建 `inventory_movement` 库存流水表，并让普通下单、模拟支付都写入可追踪的库存流水。
- 说明：前端步骤 16 由用户单独推进；今天后端主线先进入步骤 17 的库存账本起步，不把步骤 17 的并发基线虚报完成。

### 今天要学

- 知识点 1：库存流水是库存变化账本，`product_sku` 看当前余额，`inventory_movement` 看每一笔变化原因。
- 知识点 2：下单锁库时 `available_stock - quantity`、`locked_stock + quantity`；支付成功时 `available_stock` 不变、`locked_stock - quantity`。
- 知识点 3：`UNIQUE KEY (business_type, business_no, sku_id)` 用于防止同一业务动作、同一订单、同一 SKU 重复记账。
- 知识点 4：库存流水必须和订单、库存更新处于同一个 `@Transactional` 事务，避免留下半截账。
- 学到什么程度算够：能说明 `ORDER_LOCK` 和 `ORDER_PAID` 两条流水分别代表什么，以及为什么重复支付不能产生第三条库存流水。

### 今天要做

- [x] 任务 1：新增 Flyway `V7__add_inventory_movement.sql`，创建 `inventory_movement` 表、唯一约束和查询索引。
- [x] 任务 2：新增 `InventoryMovement` 实体和 `InventoryMovementMapper`，支持插入库存流水。
- [x] 任务 3：在普通下单锁库存成功后写入 `ORDER_LOCK` 流水。
- [x] 任务 4：在模拟支付扣锁定库存成功后写入 `ORDER_PAID` 流水，并验证重复支付不会重复记账。

### 今天验收

- [x] 后端启动时 Flyway 从版本 6 迁移到版本 7，日志显示 `Successfully applied 1 migration`。
- [x] DataGrip 中 `SHOW TABLES LIKE 'inventory_movement'` 能看到 `inventory_movement` 表。
- [x] `DESC inventory_movement` 可见 `tenant_id`、`sku_id`、`business_type`、`business_no`、`available_change`、`locked_change`、`available_after`、`locked_after`。
- [x] `mvnw -DskipTests compile` 编译通过。
- [x] 消费者 token 调用 `POST /api/cart/items` 成功生成购物车项。
- [x] 消费者 token 调用 `POST /api/orders` 成功生成 `PENDING_PAYMENT` 订单，并写入 `ORDER_LOCK` 流水。
- [x] `ORDER_LOCK` 流水中 `available_change=-1`、`locked_change=1`。
- [x] 消费者 token 调用 `POST /api/orders/{id}/mock-pay` 成功返回 `code=0`，并写入 `ORDER_PAID` 流水。
- [x] `ORDER_PAID` 流水中 `available_change=0`、`locked_change=-1`。
- [x] 重复调用同一订单的模拟支付返回 `code=409` 和 `订单不存在或状态不允许支付`。
- [x] 同一订单号下库存流水仍只有 `ORDER_LOCK` 和 `ORDER_PAID` 两条，没有新增第三条。

### 今天完成

- 完成了：步骤 17 的库存账本起步已跑通。普通下单和模拟支付都能写入库存流水，重复支付不会重复记账。
- 没完成：步骤 17 的并发基线测试还没开始；库存流水目前覆盖普通下单和支付，取消/超时关闭释放库存还未实现。
- 卡住点：第一次验收支付接口时误用 `GET /api/orders/{id}/mock-pay`，后端返回兜底 `code=500`；改为 `POST` 后支付成功。另一次 DataGrip 查询用错旧订单号，导致一开始只看到旧流水。
- 明天优先做：继续步骤 17，补并发基线测试或至少先补重复下单/库存不变式的后端测试。
- 截图记录：截图已放入 `docs/images/day-14/`。

## Day 15：2026-08-04

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 17 继续：补并发基线测试。
- 今日目标：新增一条可重复运行的库存并发自动化测试，验证 `ProductSkuMapper.lockStock(...)` 的数据库条件更新不会让库存扣成负数。

### 今天要学

- 知识点 1：JUnit 的 `@Test`、断言方法与自动化测试运行方式。
- 知识点 2：`@SpringBootTest` 会启动 Spring 测试环境，测试运行前需要能连接 MySQL。
- 知识点 3：`JdbcTemplate.update(...)` 可在测试中直接执行准备/清理数据的 SQL，并通过返回行数判断是否改到目标数据。
- 知识点 4：`ExecutorService`、`Future` 与 `CountDownLatch` 可以制造可重复的并发起跑线。
- 知识点 5：`@AfterEach` 用于每个测试结束后的清理，避免测试污染开发数据库。
- 学到什么程度算够：能说明 `for` 循环只是提交 20 个任务，`submit(() -> {...})` 里的代码由线程池执行；`startLatch.countDown()` 不是让循环重新执行，而是放行已经等待在 `await()` 的线程。

### 今天要做

- [x] 任务 1：创建 `InventoryConcurrencyTest`，注入 `ProductSkuMapper` 和 `JdbcTemplate`。
- [x] 任务 2：测试开始前把指定 SKU 重置为 `available_stock=10`、`locked_stock=0`。
- [x] 任务 3：使用 20 个并发任务同时调用 `lockStock(..., 1)`，断言成功次数为 10，最终库存为 `available_stock=0`、`locked_stock=10`。
- [x] 任务 4：新增 `@AfterEach` 清理方法，测试结束后恢复库存为 `available_stock=10`、`locked_stock=0`。
- [x] 加餐：补 `HttpRequestMethodNotSupportedException` 全局异常处理，让请求方法错误返回 `code=405`。
- [x] 加餐：补完整下单链路并发测试，验证 20 个消费者同时下单同一 SKU、库存只有 10 时，只成功 10 单，订单数、`ORDER_LOCK` 流水数、最终库存和剩余购物车项数量一致。

### 今天验收

- [x] IDEA 中 `InventoryConcurrencyTest` 运行通过，2 个测试通过。
- [x] 20 个并发扣库存请求中成功次数为 10。
- [x] 并发扣库存后断言 `available_stock=0`、`locked_stock=10`。
- [x] 20 个消费者并发下单同一 SKU 时，成功订单数为 10，失败请求数为 10。
- [x] 完整下单链路并发测试中，`commerce_order` 新增 10 条测试订单，`ORDER_LOCK` 库存流水新增 10 条。
- [x] 完整下单链路并发测试后，最终库存为 `available_stock=0`、`locked_stock=10`，失败的 10 个购物车项仍保留。
- [x] `@AfterEach` 执行后，DataGrip 查询 SKU `1784970220075` 显示 `available_stock=10`、`locked_stock=0`。
- [x] 测试启动时曾因 `MYSQL_ROOT_PASSWORD` 环境变量/密码不匹配导致 MySQL 连接失败；补齐测试运行配置后解决。
- [x] `GET /api/orders/4/mock-pay` 返回 `code=405` 和 `请求方法不支持，请检查 GET/POST/PUT/DELETE 是否正确`，不再返回兜底 `code=500`。
- [x] 截图已归档到 `docs/images/day-15/`，本次加餐截图为 `order-flow-concurrency-test-success.png`。
- [x] 已提交 Git，提交信息：`test(inventory): cover concurrent stock locking`

### 今天完成

- 完成了：步骤 17 的 Mapper 层并发基线和完整下单链路并发测试都已完成。完整链路测试证明 20 个消费者同时下单、库存只有 10 时，只成功 10 单；订单数、`ORDER_LOCK` 流水数、最终库存和失败购物车项保留都一致。
- 没完成：还没有进入步骤 18 幂等下单；当前完整链路并发测试覆盖普通下单锁库，还未覆盖支付与超时关单竞争。
- 卡住点：第一次运行测试时 Spring 测试环境无法连接 MySQL，根因是测试运行配置没有拿到正确的 `MYSQL_ROOT_PASSWORD`；补环境变量后测试通过。Mockito/Byte Buddy 的 JVM 红色提示是测试库动态加载 agent 的警告，不影响本次测试。
- 明天优先做：可以进入步骤 18 幂等下单，重点学习幂等键、重复提交、同 key 同参数返回同一结果、同 key 不同参数返回冲突。
- 截图记录：截图已放入 `docs/images/day-15/`，包含完整下单链路并发测试通过截图。

## Day 16：2026-08-05

### 今天对应任务

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 18：请求幂等与防重复下单
- 今日目标：为创建订单增加 `Idempotency-Key`，防止重复下单，并用自动化测试验证同 key 的重试、参数冲突和并发场景。

### 今天验收

- [x] Flyway `V8__add_idempotent_request.sql` 执行成功，新增 `idempotent_request` 表。
- [x] 新增幂等请求实体和 Mapper，记录 `PROCESSING/SUCCESS` 状态，并在成功后绑定 `order_id`。
- [x] 下单接口读取 `Idempotency-Key` 请求头，缺失时返回 `400`。
- [x] 同一个消费者、同一个 key、同一组参数重复提交时返回同一个订单。
- [x] 同一个 key 搭配不同参数时返回 `409`，不会继续创建订单。
- [x] 20 个并发请求使用同一个 key 时，数据库最终只有 1 条订单和 1 条成功幂等记录。
- [x] `OrderIdempotencyTest` 的 3 个测试全部通过。
- [x] Apifox 验证新 key 下单成功、重复请求返回同一订单、不同参数冲突、不传 key 返回 `400`。
- [x] DataGrip 验证 `idempotent_request` 的请求指纹、`SUCCESS` 状态和 `order_id=45`。
- [x] 截图已归档到 `docs/images/day-16/`。

### 今天完成

- 完成了：步骤 18 请求幂等与防重复下单，覆盖数据库迁移、请求头、参数指纹、唯一约束、并发冲突处理和自动化测试。
- 关键设计：幂等范围是“消费者 + Idempotency-Key”；同 key 同参数返回旧订单，同 key 不同参数返回冲突；数据库唯一键负责兜住并发竞态。
- 测试结论：并发请求的成功响应数可以大于 1，因为后续请求会复用同一个已成功订单；真正的唯一性由不同 `orderId` 数量和数据库订单数共同证明。
- 明日优先：进入步骤 19 前，先确认商品查询缓存的读写边界、失效时机和与数据库的一致性要求。

### 截图记录

- `docs/images/day-16/flyway-v8-migration-success.png`
- `docs/images/day-16/order-create-idempotency-success.png`
- `docs/images/day-16/order-repeat-same-result.png`
- `docs/images/day-16/order-idempotency-conflict-409.png`
- `docs/images/day-16/idempotent-request-datagrip-success.png`
- `docs/images/day-16/order-idempotency-tests-passed.png`

## Day 17：2026-08-06

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 19：Redis 商品缓存与一致性策略。
- 今日目标：完成公开商品详情缓存、缓存失效、空值缓存、租户隔离和 Redis 故障回源验收。

### 今日任务

- [x] 商品详情使用 Cache Aside：先读 Redis，未命中查 MySQL，成功后回填 Redis。
- [x] 商品详情 JSON 使用 `ObjectMapper` 转换，缓存 TTL 为 10 分钟。
- [x] Redis 停止时商品查询能够超时回源数据库，接口仍返回成功结果。
- [x] 商品不存在写入空值标记 `__EMPTY_PRODUCT_DETAIL__`，TTL 为 30 秒。
- [x] 商品上架、下架和 SKU 改价成功后删除商品详情缓存。
- [x] 验证租户 `1001` 与 `1002` 使用不同缓存 Key，不发生数据串读。
- [x] 将商品缓存相关临时输出替换为 `log.warn`，并完成编译验证。

### 今日验收

- [x] Apifox 首次和重复查询商品详情均返回成功，重复查询明显更快。
- [x] Redis CLI 查询到商品 JSON，TTL 正常倒计时。
- [x] Redis 停止后 Apifox 仍返回商品详情，控制台记录回源数据库。
- [x] 商品下架后公开查询返回业务 `code=404`，重新上架后恢复 `code=0`。
- [x] SKU 价格从 `199.00` 改为 `188.00` 后，Apifox 和 Redis 中均为 `188.00`。
- [x] 不存在商品第二次查询命中空值缓存，Redis 中可见空值标记，TTL 约 30 秒。
- [x] 租户 `1001` 返回真实商品 JSON，租户 `1002` 返回空值标记。
- [x] `mvnw -DskipTests compile` 编译通过。
- [x] 成功截图已归档到 `docs/images/day-17/`。

### 今日完成

- 完成了：步骤 19 的核心缓存链路和一致性验收，包括商品详情缓存、TTL、空值缓存、缓存失效、改价同步、租户隔离和 Redis 故障降级。
- 未提前扩展：随机 TTL、热点互斥重建、逻辑过期和正式指标暂未实现，保留到后续需要时再补。
- 明日优先：进入步骤 20，学习 RabbitMQ、可靠发布与订单关闭消息。

### 今日截图记录

- `docs/images/day-17/redis-server-ready.png`
- `docs/images/day-17/redis-product-cache-and-ttl.png`
- `docs/images/day-17/redis-fallback-request-success.png`
- `docs/images/day-17/redis-restored-product-query-success.png`
- `docs/images/day-17/empty-cache-marker.png`
- `docs/images/day-17/tenant-cache-isolation.png`
- `docs/images/day-17/tenant-1001-cache-json.png`
- `docs/images/day-17/price-update-cache-refreshed.png`

## Day 18：2026-08-08

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 20：RabbitMQ、可靠发布与订单关闭消息；基础发布链路完成，订单关闭消费者待做。
- 今日目标：启动 RabbitMQ，建立 Outbox 本地消息表，让订单创建事件可靠发布到延迟队列。

### 今日任务

- [x] Compose 加入 RabbitMQ 管理版服务，并通过管理页面看到业务 Exchange 和 3 个队列。
- [x] Flyway 新增 `V9__add_outbox_events.sql`，创建 Outbox 本地消息表。
- [x] 创建订单与写入 `ORDER_CREATED` Outbox 事件放在同一事务。
- [x] 增加 Publisher Confirm，定时发布 `PENDING` 事件并在确认后标记 `PUBLISHED`。
- [x] 定位并修复 MySQL UTC 与 Java 东八区导致的 `next_retry_at` 查询误判。

### 今日验收

- [x] RabbitMQ 容器启动，管理页面可访问，连接数和队列数量符合预期。
- [x] 订单 `60` 创建成功，Outbox 事件初始为 `PENDING`。
- [x] 发布器日志显示查询到 1 条事件，发布成功后更新行数为 `1`。
- [x] DataGrip 验证该事件状态已变为 `PUBLISHED`，`published_at` 已写入。
- [x] Maven 编译通过，最终编译 67 个 Java 源文件。
- [ ] 订单关闭消费者、TTL 到期后的 `CLOSED` 状态和库存释放尚未验收。

### 今日完成

- 完成了：步骤 20 的 RabbitMQ 基础设施、Outbox 可靠落库和发布确认链路。
- 未提前扩展：步骤 21 的超时关闭、支付竞争、手动 ack、退避重试和失败队列消费留到下一阶段。
- 明日优先：新增 `PENDING_PAYMENT → CLOSED` 条件更新，只有成功关闭订单的线程才释放锁定库存并写关闭流水。

### 今日截图记录

- `docs/images/day-18/rabbitmq-management-topology.png`
- `docs/images/day-18/order-create-success.png`
- `docs/images/day-18/outbox-event-pending.png`
- `docs/images/day-18/outbox-timezone-diagnosis.png`
- `docs/images/day-18/outbox-publisher-success-log.png`
- `docs/images/day-18/outbox-published.png`

## Day 19：2026-08-10

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 21：超时关单、支付边界与库存释放。
- 今日目标：完成订单关闭消费者、定时兜底关单、支付过期边界和库存流水验收。

### 今日任务

- [x] 增加 `PENDING_PAYMENT → CLOSED` 条件更新，只有影响行数为 `1` 才继续释放库存。
- [x] 完成订单关闭 Service：释放锁定库存并写入 `ORDER_CLOSE` 流水。
- [x] 完成 RabbitMQ 订单关闭消费者，使用手动 ack；异常时 reject 消息。
- [x] 完成每 60 秒扫描过期订单的兜底任务，并复用统一关单 Service。
- [x] 验证延迟消息关闭订单 `60`，验证兜底任务关闭订单 `62`。
- [x] 支付增加 `expire_at > now` 条件，验证过期订单 `61` 不能支付。
- [x] 验证正常订单 `63` 可以支付，库存和 `ORDER_PAID` 流水正确。
- [x] 将 `OutboxPublisher` 的临时标准输出替换为 `@Slf4j` 日志，并编译通过。
- [ ] 支付与关单并发竞争测试：本次明确暂不实现，后续需要时再补。

### 今日遇到的问题

- 历史订单 `2/3/45/61` 的订单记录和库存汇总不一致，兜底关单会提示“订单锁定库存释放失败”。保留这些数据作为问题证据，没有直接删除或强行改状态；使用干净 SKU 完成了正常验收。
- 下单接口使用的是 `cartItemId`，支付接口路径中的 `{id}` 必须使用 `orderId`；曾因混用两个 ID 得到“订单不存在或状态不允许支付”。
- `Idempotency-Key` 重复使用但请求参数不同会返回 `409`，测试时改用新的 key。
- 修改支付 Mapper 时曾误把关单方法替换成重复的支付方法，已恢复并编译通过。

### 今日验收

- [x] 订单 `60`：延迟消息消费后变为 `CLOSED`，库存释放并写入 `ORDER_CLOSE`。
- [x] 订单 `62`：定时兜底扫描后变为 `CLOSED`，库存从 `19/1` 恢复为 `20/0`。
- [x] 订单 `61`：过期支付返回 `409`，数据库仍为 `PENDING_PAYMENT`。
- [x] 订单 `63`：正常支付成功，状态为 `PAID`，库存为 `19/0`，流水包含 `ORDER_LOCK` 和 `ORDER_PAID`。
- [x] Maven `-DskipTests compile` 编译通过，71 个 Java 源文件编译成功。
- [ ] 支付与关单并发竞争尚未测试，不能记录为已完成。

### 侧边任务/对话补充记录

- 讲解了 RabbitMQ 的 Exchange、Queue、Routing Key、生产者、消费者和配置类之间的关系。
- 讲解了 HTTP 请求线程、定时任务线程和 RabbitMQ 消费者线程不是“手动一分为二”，而是由不同执行入口分别运行。
- 讲解了 `selectById` 同时供 MQ 消费者和兜底任务使用，`selectExpiredPendingOrderIds` 只供兜底任务扫描使用。
- 讲解了为什么不删除旧订单：订单、订单明细、库存流水和 Outbox 记录需要保持关联，直接删除会制造新的脏数据。
- 讲解了 `System.out.println` 与 `@Slf4j` 的区别，以及日志 `{}` 占位符和异常参数的写法。

### 今日截图记录

- 本次会话截图已用于核对：订单 `60/62` 关单、订单 `61` 过期支付拒绝、订单 `63` 正常支付、库存变化和 `ORDER_LOCK/ORDER_CLOSE/ORDER_PAID` 流水。
- 当前这些截图来自聊天附件，尚未复制归档到 `docs/images/day-19/`；后续归档时再补具体文件名。

### 明日优先

- 继续前先决定是否补写支付与关单并发测试；本次不把该测试虚报为完成。
- 若暂不补并发测试，则整理步骤 21 文档、检查工作区改动并准备统一提交。

## Day 20：2026-08-11

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 21：订单超时关闭与支付竞争。
- 今日目标：补齐支付/关单边界并发测试，验证重复运行时订单状态、库存和库存流水保持一致。

### 今日任务

- [x] 新增 `OrderPaymentCloseConcurrencyTest`，准备独立的过期/未过期待支付订单数据。
- [x] 使用 `CountDownLatch` 让支付线程和关单线程同时起跑。
- [x] 验证过期订单最终为 `CLOSED`，库存恢复为 `20/0`，`ORDER_CLOSE` 流水只有 1 条。
- [x] 验证未过期订单最终为 `PAID`，库存为 `19/0`，`ORDER_PAID` 流水只有 1 条。
- [x] 提取公共并发执行方法，并使用 `@RepeatedTest(10)` 重复验证两个场景。
- [x] 引入 `Clock` 和 `MutableTestClock`，使用可控时间验证同一订单在过期临界点的支付/关单竞态。

### 今日验收

- [x] 两个测试场景各重复 10 次，总计 20 次测试全部通过。
- [x] 临界点测试场景重复 10 次；3 个测试方法总计 30 次全部通过。
- [x] 测试启动时 MySQL、RabbitMQ、Flyway 和 JWT 环境变量配置正确。
- [x] `git diff --check` 通过。
- [x] 同一订单最终只能为 `PAID` 或 `CLOSED`，库存与库存流水只产生一次。
- [x] 步骤 21 的代码与测试验收完成；进入步骤 22 前仍需完成本次文档与 Git 收工。

### 今日完成

- 完成了：支付和关单在已过期/未过期两个边界条件下的可重复并发基线，并验证订单、库存和流水结果。
- 完成了加餐：通过 `Clock`、`MutableTestClock` 和线程级时间覆盖，让支付线程观察过期前时间、关单线程观察过期后时间；3 个测试方法各重复 10 次，总计 30 次全部通过。
- 验收结论：同一订单最终只能是 `PAID` 或 `CLOSED`；支付成功时库存为 `19/0`，关单成功时库存为 `20/0`，对应库存流水均只产生 1 条。
- 排错结论：IDEA 的运行配置和 PowerShell 终端不会自动共享环境变量；测试依赖 Docker 中的 MySQL/RabbitMQ，以及 `MYSQL_ROOT_PASSWORD`、`JWT_SECRET`、`RABBITMQ_DEFAULT_USER`、`RABBITMQ_DEFAULT_PASS`。
- 明日优先：检查本次代码与日志 diff，完成 Git 提交；步骤 22 暂不提前展开。

### 今日 Git

- 本次加餐代码和日志尚未提交，等待用户检查 `git diff` 后提交。

## Day 21：2026-08-12

### 今日阶段

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 22：建立限量促销规则与数据模型。
- 今日目标：跑通商家创建促销、普通库存划拨、重叠拦截、取消活动与库存归还的完整闭环。

### 今日完成

- [x] 新增 Flyway `V10__add_promotion_tables.sql`，创建促销活动、活动商品和抢购资格三张表；活动商品明确区分 `stock_total` 与 `stock_available`。
- [x] 新增促销状态枚举、活动/活动商品实体、创建请求 DTO、Mapper、Service 和商家 Controller。
- [x] 实现 `POST /api/merchant/promotions`：校验时间、SKU 上架状态、活动价、活动库存和时间重叠；在同一事务内创建活动、原子划拨普通可售库存、写入 `PROMOTION_ALLOCATE` 流水和活动商品。
- [x] 实现 `DELETE /api/merchant/promotions/{activityId}`：仅允许尚未开始的 `SCHEDULED` 活动取消；清零活动可售库存、归还普通可售库存并写入 `PROMOTION_RELEASE` 流水。
- [x] 取消 SQL 同时校验 `start_at > CURRENT_TIMESTAMP`，避免状态尚未来得及推进时，已开始活动仍被取消。

### 今日验收

- [x] Flyway 成功应用版本 10；DataGrip 验证三个促销表和迁移记录。
- [x] 手工创建活动成功：活动状态为 `SCHEDULED`，普通可售库存从 `10` 变为 `9`，写入 `PROMOTION_ALLOCATE` 流水。
- [x] 手工创建重叠活动被业务码 `409` 拦截，普通库存没有再次扣减。
- [x] 手工取消活动成功：状态变为 `CANCELLED`，活动 `stock_available` 变为 `0`，SKU 可售库存回到 `10`，写入 `PROMOTION_RELEASE` 流水。
- [x] 重复取消被业务码 `409` 拦截，未重复归还库存或写流水。
- [x] `PromotionStatusTest` 通过；`PromotionServiceTest` 的创建、重叠拦截、取消归还三个集成测试通过。
- [x] Maven `-DskipTests test-compile` 通过；新增“开始时间已过不可取消”的第 4 条集成测试已编译。

### 今日关键理解

- 普通 SKU 的 `available_stock` 是日常可售库存；促销商品的 `stock_total/stock_available` 是从普通库存独立划拨出来的活动库存，不能混为同一字段。
- 写入活动、扣减/归还库存和写库存流水必须置于同一事务；任一步失败都会回滚，避免库存与流水不一致。
- 业务失败在本项目通过响应体的 `code` 表示，例如 `code: 409`；页面显示的 HTTP `200` 不代表业务操作成功。
- 条件更新返回 `1` 才表示当前请求获得了状态变更资格；返回 `0` 时必须停止，避免重复取消、重复归还库存。

### 今日 Git

- 步骤 22 已完成代码审查与 `git diff --check`；订单目录下两处既有格式调整不纳入本次促销提交。

## Day 22：2026-08-13 / 步骤 23（进行中）

### 今日目标

- 完成 Redis Lua 抢购资格预扣的前半段：规则/库存预热、消费者调用入口，以及未开始活动的安全拒绝。

### 今日完成

- [x] 新增 `promotion_reserve.lua`：在同一次 Redis Lua 执行中按顺序校验重复请求、预热状态、活动时间、限购和库存；成功时才原子扣减库存、累计用户数量并写入请求幂等资格。
- [x] 设计带 `{itemId}` 哈希标签的规则、库存、用户数量和请求幂等 Key；规则 Hash 保存 `startAt`、`endAt`、`limitPerUser`，库存使用独立 String。
- [x] 新增 Lua 脚本 Bean、商家活动预热 Service 与 `POST /api/merchant/promotions/{activityId}/preheat`。
- [x] 创建活动 `8`，预热成功；Redis 验证规则 Hash 包含时间与限购规则，库存 Key 为 `1`，与 MySQL `promotion_items` 一致。
- [x] 新增消费者 `POST /api/promotions/reservations`、请求 DTO、Lua 结果 DTO 和返回码映射。
- [x] 使用消费者 Token 验证活动 `8` 未开始时返回业务码 `409`、消息“活动尚未开始”；Redis 库存仍为 `1`，证明拒绝路径未扣减库存。

### 今日未完成

- [ ] 尚未验证活动开始后的首次成功抢购、相同 `requestKey` 重试、售罄和超过限购。
- [ ] 尚未将 Lua 成功资格写入 `promotion_reservations`，未通过 Outbox/RabbitMQ 异步创建促销订单。
- [ ] 尚未实现 reservation 查询结果、落单失败补偿和自动化测试；因此步骤 23 仍为进行中。
- [ ] 终端 `mvnw -DskipTests compile` 未能开始编译：Maven 下载 Spring Boot 父 POM 时被当前网络沙箱拒绝（`getsockopt: Permission denied`）；这不是已确认的源码编译失败，后续需在可联网环境再次验证。

### 今日验收

- [x] 商家创建活动响应 `data: 8`，活动状态为 `SCHEDULED`。
- [x] 商家预热接口响应 `code: 0`。
- [x] Redis `HGETALL promotion:item:{8}:rules:v1` 返回 `startAt`、`endAt`、`limitPerUser`；`GET promotion:item:{8}:stock:v1` 返回 `"1"`。
- [x] 消费者抢购接口返回 `code: 409`、`message: "活动尚未开始"`。
- [x] 未开始拒绝后库存 Key 仍为 `"1"`。
- [ ] Maven 编译待在可联网/依赖可用的环境复验。

### 今日 Git

- 待用户提交：Redis Lua 资格预扣前半段、预热接口与消费者抢购入口；未包含 Token、`.env` 或聊天截图。

### 明日优先

- 继续步骤 23：建立活动启动入口，完成首次成功、重复请求、售罄和限购分支验收；之后再落库资格并接入异步订单创建。

## Day 23：2026-08-14～2026-08-15 / 步骤 23（主链验收）

### 今日目标

- 将 Lua 成功资格持久化，并通过 Outbox 和 RabbitMQ 异步创建促销订单；以手工证据验收重复 HTTP 请求和重复 MQ 消息均不会重复建单。

### 今日完成

- [x] 成功抢购后在同一 MySQL 事务内写入 `promotion_reservations`（初始 `PENDING_ORDER`）和 `PROMOTION_ORDER_CREATE` Outbox 事件。
- [x] Outbox 发布器将促销事件路由到 `ai.commerce.promotion.exchange`，由 `ai.commerce.promotion.order.create.queue` 消费。
- [x] 消费者按 `reservationId` 锁定资格记录，创建一笔 `PENDING_PAYMENT` 促销订单及订单明细，并将资格条件更新为 `ORDER_CREATED`。
- [x] 订单创建后，MySQL 活动库存与 Redis 活动库存均为 `0`；活动资格、订单、订单明细和 Outbox 状态相互一致。
- [x] 相同 HTTP `requestKey` 重放返回原资格（内部 `data.code = 2`），没有再次扣减库存或创建订单。
- [x] 库存为 `0` 时，新请求返回业务码 `409`、消息“活动库存不足或已售罄”。
- [x] 通过 RabbitMQ 管理页面手工发布重复 `PROMOTION_ORDER_CREATE` 消息；最终资格数和订单数仍为 `1 / 1`。

### 今日验收

- [x] 用户本人启动后端，`GET /actuator/health` 返回 `{"status":"UP"}`。
- [x] DataGrip 联查显示资格状态 `ORDER_CREATED`，关联订单为 `PENDING_PAYMENT`，订单明细的 SKU、活动价 `99.00` 和数量 `1` 正确。
- [x] Redis `GET promotion:item:{8}:stock:v1` 返回 `0`，与 `promotion_items.stock_available = 0` 一致。
- [x] Outbox 记录为 `aggregate_type = PROMOTION_RESERVATION`、`event_type = PROMOTION_ORDER_CREATE`、`status = PUBLISHED`、`retry_count = 0`，且 `published_at` 非空。
- [x] RabbitMQ 队列 `ai.commerce.promotion.order.create.queue` 显示 `messages = 0`、`consumers = 1`。
- [x] 重复 HTTP 请求后资格数和订单数为 `1 / 1`。
- [x] 重复 MQ 消息发布并消费后资格数和订单数仍为 `1 / 1`。
- [x] `mvn -q -DskipTests compile` 通过。
- [ ] 高并发抢购压测、资格/订单结果查询接口、永久失败后的库存补偿与审计尚未完成；步骤 23 整体仍为进行中。

### 今日 Git

- 代码主链已提交并推送：`1668abe feat(promotion): create orders asynchronously from reservations`。
- 本次仅新增验收文档与截图，待用户执行单独的文档提交。

### 明日优先

- 先补消费者侧的资格/订单结果查询，避免抢购接口只返回资格编号而没有可轮询的最终订单结果。
- 设计永久失败的补偿与审计边界，再用新的活动数据执行高并发压测；未完成前不把步骤 23 标记为整体完成。

## Day 24：2026-08-17 / 步骤 23 收口与提交准备

### 今日目标

- 补齐 Redis 预扣成功后 MySQL 资格或 Outbox 写入失败时的补偿链。
- 检查预热、补偿重试和异步建单代码的并发边界。
- 整理步骤 23 的代码状态、验收证据和提交边界。

### 今日完成

- [x] 新增 `promotion_compensation_records` 补偿审计表及 Flyway V11 迁移。
- [x] 新增 `promotion_compensate.lua`，以补偿标记保证 Redis 回滚幂等。
- [x] 补偿记录持久化与 Redis 执行拆为独立事务；Redis 执行失败时保留 `PENDING` 记录。
- [x] 新增 `PromotionCompensationRetryJob`，每 10 秒扫描待补偿记录并重试。
- [x] 促销订单创建失败时补偿 Redis 库存和用户限购数量，并在资格存在时更新为 `COMPENSATED`。
- [x] 资格落库或 Outbox 写入失败时，创建独立补偿记录并执行 Redis 回滚。
- [x] 预热接口仅允许 `SCHEDULED` 活动，避免活动进行中重新预热覆盖 Redis 库存。
- [x] `mvn -DskipTests compile` 通过，编译 96 个源文件；本次结果截图已归档。

### 当前未完成与边界

- [ ] 高并发抢购自动化压测和故障演练尚未执行；本次按用户要求跳过运行测试，仅完成代码结构与并发逻辑审查。
- [ ] 应用与 MySQL 的时间存储/查询时区尚未完全统一，后续仍需专项处理，避免活动时间误判。
- [ ] 多实例 Outbox 发布抢占和消息重试策略仍可继续加强，当前依靠下游幂等防止重复建单。

### 今日验收

- [x] Maven 编译显示 `BUILD SUCCESS`。
- [x] 编译完成时间为 `2026-08-17T23:51:59+08:00`。
- [x] 编译成功截图：`docs/images/day-24/promotion-step23-compile-success.png`。

### 今日 Git

- 本次代码与文档尚未提交，等待用户执行提交命令。
- 提交前仍应运行 `git diff --check`，确认没有空白错误。

### 下一步

- 当前步骤可以作为“代码收口检查点”提交，但不能把未执行的高并发压测记录为已通过。
- 后续进入步骤 24，重点做促销对账、时区统一、故障演练和压测报告。

## Day 25：2026-08-19 / 步骤 24 验收完成

### 今日目标

- 用可重复的自动化测试替代固定时间窗口和手工 Apifox 验收。
- 验收促销抢购在并发、Outbox 异步建单与故障补偿后的最终一致性。
- 归档验收截图、学习记录与测试代码讲解。

### 今日完成

- [x] 明确应用 `Clock` 为 `Asia/Shanghai`，MySQL 容器启动参数为 `--default-time-zone=+08:00`；促销状态恢复任务每 30 秒推进 `SCHEDULED → ACTIVE → ENDED`。
- [x] 新增 `PromotionReservationConcurrencyTest`，使用 `MutableTestClock` 固定测试时间，避免等待真实活动开始时间。
- [x] 每次测试自动创建独立的活动、活动商品与 Redis 预热数据；结束后按 Outbox、补偿、订单、资格、活动商品、活动的逆序清理，并清理对应 Redis Key。
- [x] 20 个不同消费者同时抢购库存为 10 的活动：成功资格数为 10，Redis 库存为 0，MySQL 资格数为 10。
- [x] 测试中主动执行一次 Outbox 发布器，并等待 RabbitMQ 消费者完成建单：10 条资格均为 `ORDER_CREATED`，10 笔订单创建成功，MySQL 活动库存为 0，10 条促销 Outbox 事件均为 `PUBLISHED`。
- [x] 用测试专用活动商品的不存在 SKU 制造建单失败：资格状态为 `COMPENSATED`，补偿记录为 `COMPLETED`，订单数为 0，Redis 活动库存恢复到 10，用户限购数量回到 0。
- [x] IDEA 运行 `PromotionReservationConcurrencyTest`：3 个测试全部通过。

### 今日验收

- [x] `shouldLoadPromotionConcurrencyTestContext`：真实 Spring、MySQL、Redis、RabbitMQ 与固定时钟测试环境可用。
- [x] `concurrentReservationsShouldNotOversell`：20 并发请求不会超卖，并完成 Outbox → RabbitMQ → 订单的最终对账。
- [x] `orderCreationFailureShouldCompensateReservationAndRedisStock`：受控建单失败会执行幂等补偿，且不会产生孤儿订单。
- [x] 测试截图已归档：`docs/images/day-25/promotion-concurrency-order-flow-tests.png`、`docs/images/day-25/promotion-compensation-tests-success.png`。
- [x] 测试说明文档已生成：`docs/步骤24-促销并发与补偿测试代码讲解.md`。

### 当前边界

- 当前测试使用本地开发环境的真实 MySQL、Redis 与 RabbitMQ；不得指向共享或生产数据库。
- 测试主动调用 `OutboxPublisher.publishPendingEvents()` 以避免等待 5 秒定时器，因此该调用会扫描本地环境的 `PENDING` Outbox 事件。
- 多实例 Outbox 抢占、指数退避和死信队列治理不属于本步骤验收范围，后续可作为可靠消息增强项继续推进。

### 今日 Git

- 待用户提交：时区配置、促销状态恢复任务、步骤 24 自动化测试、验收截图与文档。
- 提交前已通过 `mvn -DskipTests test-compile` 与 `git diff --check`。

## Day 26：2026-08-21 / 前后端联调与部署副线 S1、S3、S4 阶段推进

### 今日目标

- 建立并验收公共店铺目录、跨店商品搜索和地址真实读取能力。
- 开始 S4 地址与订单收货地址快照基础设施，不提前宣称跨店拆单闭环完成。
- 保持当前 `feature/backend` 分支，不切换或合并 `feature/web-v2`，不推进 AI 主线步骤 25～30。

### 今日完成

- [x] 完成前后端接口合同文件 `docs/frontend-backend-contract.md`，记录分支基线、统一响应、权限边界、已知缺口和后续集成策略；S1 达到阶段完成。
- [x] 将 Spring Security 未登录/无权限响应统一为 `ApiResponse` JSON：401 返回“请先登录”，403 返回“没有权限访问该资源”。
- [x] 新增公开店铺目录 `GET /api/public/stores`，返回启用店铺及公开在售商品数量。
- [x] 新增跨店公开搜索 `GET /api/public/stores/products/search`，支持关键词、店铺筛选和分页上限；仅查询启用店铺、已上架商品和已上架 SKU。
- [x] 新增 `PublicStoreServiceTest`、`PublicStoreControllerTest`；控制器测试中补充 `JwtService` Mock，确认 `addFilters = false` 只关闭 MockMvc 请求过滤，不会阻止测试上下文创建安全 Bean。
- [x] Flyway V12 成功创建 `consumer_address`，并完成消费者地址 GET/POST/PUT/DELETE 接口。
- [x] FoxAPI 验收地址创建、地址列表、默认地址切换、地址修改和地址删除；验证同一消费者最多保留一个默认地址，地址查询按当前消费者隔离。
- [x] Flyway V13 成功创建 `commerce_order_address` 订单收货地址快照表；新增快照实体、Mapper、快照服务，并为 `CreateOrderRequest` 增加兼容旧调用的可选 `addressId`。
- [x] 今日涉及代码均通过 `mvn -q -DskipTests compile`；没有执行 Git 提交。

### 今日未完成

- [ ] `addressId` 尚未接入现有 `OrderService.createOrderVO`，现有订单创建还不会真正写入地址快照。
- [ ] 订单详情尚未返回收货地址快照。
- [ ] 尚未实现按 tenant 拆分购物车、一次结算生成多笔商家子订单，以及跨店事务整体回滚。
- [ ] 尚未补 S4 后端集成测试：两个商家成功、一个 SKU 库存不足整体失败、幂等重试、地址越权和空购物车。
- [ ] `feature/web-v2` 前端尚未接入真实地址和跨店结算；当前未切换分支，也未建立工作树合并。

### 今日验收

- [x] FoxAPI：公共店铺目录返回店铺 1001/1002 及商品数量。
- [x] FoxAPI：关键词“耳机”跨店搜索返回蓝牙耳机；按店铺 1002 查询返回空数组。
- [x] FoxAPI：消费者无地址时 GET 返回 `code: 0`、`data: []`。
- [x] FoxAPI：新增两条地址并切换默认地址，旧地址变为 `isDefault: false`，新地址为 `true`。
- [x] FoxAPI：修改地址后字段和 `updatedAt` 正确变化；删除非默认地址后列表只剩默认地址。
- [x] IDEA/Flyway：V12、V13 分别成功应用，数据库版本从 11 升到 12、再升到 13。
- [x] Maven：公共店铺控制器测试 2 条通过；服务测试 2 条通过；后续新增地址代码编译通过。

### 今日关键理解

- `addFilters = false` 只关闭 MockMvc 发请求时的安全过滤器，不等于测试上下文不创建 `JwtAuthentication`；因此仍需为 `JwtService` 提供 `@MockitoBean`。
- 地址主数据和订单地址快照不是同一份数据：消费者地址可以修改/删除，订单快照必须保留下单时的原始内容。
- `consumer_id` 必须来自 `CurrentUser.requiredConsumerId()`，不能由前端传入；Mapper 的查询、修改和删除都必须带消费者条件。
- 新增地址未传 `isDefault` 时使用 `0`；修改地址未传 `isDefault` 时保留原值，不能误取消默认地址。
- Flyway 迁移文件应由后端启动自动执行，不能手动复制 SQL，否则可能与 `flyway_schema_history` 不一致。

### 今日遇到的问题

| 问题 | 原因与解决 | 是否已理解 |
|---|---|---|
| 控制器测试启动失败 | `@WebMvcTest` 上下文仍创建安全相关 Bean，缺少 `JwtService`；增加 `@MockitoBean` 后通过 | 是 |
| 修改地址的默认值 SQL 写错位置 | 把 `is_default =` 误放进 INSERT 的 `VALUES`；恢复为 INSERT 使用 `COALESCE(..., 0)`，UPDATE 使用 `COALESCE(..., is_default)` | 是 |
| 提取方法名为 `extracted` | IDE 默认名称不能表达职责；改为 `buildOrderAddressSnapshot`，突出“组装快照对象”而不是持久化 | 是 |
| Maven 偶尔无法开始 | 沙箱网络阻止访问 Maven Central；申请联网权限后编译通过，确认不是源码错误 | 是 |

### 今日截图记录

- `docs/images/day-26-side-S4/flyway-v12-consumer-address-success.png`：Flyway 成功应用 V12，创建 `consumer_address` 表。
- `docs/images/day-26-side-S4/flyway-v13-order-address-snapshot-success.png`：Flyway 成功应用 V13，创建 `commerce_order_address` 订单地址快照表。
- `docs/images/day-26-side-S4/address-create-success.png`：FoxAPI 新增地址返回 `code: 0`、HTTP 200。
- `docs/images/day-26-side-S4/address-default-switch-success.png`：FoxAPI 新增第二个默认地址返回成功。
- `docs/images/day-26-side-S4/address-update-success.png`：FoxAPI 修改地址返回 `code: 0`、HTTP 200。
- `docs/images/day-26-side-S4/address-list-default-state.png`：FoxAPI 查询结果显示默认地址状态已切换。
- 删除接口成功截图和部分 GET 截图包含可见 JWT，因此不复制到仓库，避免归档敏感凭据。

### 今日 Git

- 当前分支：`feature/backend`。
- AI 主线步骤 25～30 未改动。
- 当前代码、迁移、测试、接口合同、日志和截图均未提交，等待用户执行提交命令。

### 下一步

- 将 `addressId` 接入现有订单创建事务，在订单写入成功后同事务保存 `commerce_order_address` 快照。
- 再补订单详情地址返回，之后进入跨店拆单和整体回滚设计。

## Day 27：2026-08-23 / 跨店结算副线 S4 阶段闭环

### 今日目标

- 在不切换主线、不自动修改工作区的前提下，完成一次“结算组创建 → 按商家拆单 → 订单关联 → 订单查询”的副线闭环。
- 用单元测试、集成测试、Flyway 启动日志和 FoxAPI 实际请求逐层验收。
- 记录当前闭环边界，提交由用户自行执行。

### 今日完成

- [x] 新增 Flyway `V14__add_checkout_group.sql`，创建 `checkout_group`，并为 `commerce_order`、`idempotent_request` 增加可空 `checkout_group_id`；Flyway 成功升级到版本 14。
- [x] 新增 `CheckoutGroup`、`CheckoutGroupMapper`、`CheckoutGroupService`，完成结算组编号生成、`PENDING_PAYMENT` 初始化和当前消费者隔离查询。
- [x] 新增 `CreateCheckoutRequest`、`CreateCheckoutGroupVO`、`CheckoutService` 和 `CheckoutController`。
- [x] 实现 `POST /api/checkouts/prepare`：读取购物车可信快照、按 `tenantId` 分组、计算总金额并创建结算组。
- [x] 实现 `POST /api/checkouts/{checkoutGroupId}/orders`：按商家创建子订单，传递 `checkoutGroupId`，为每个商家生成子幂等键，并在事务中清理购物车项。
- [x] 现有单店 `OrderService.createOrderVO(idempotencyKey, request)` 保持兼容；新增三参数重载支持可选结算组关联。
- [x] 订单详情和订单列表返回 `checkoutGroupId`；订单地址快照仍返回 `shippingAddress`。
- [x] 完成 `CheckoutGroupServiceTest`、`CheckoutServiceTest`、`CheckoutControllerTest` 和 Mapper/Service 集成测试；截图显示相关测试分别通过 4、7、1 条，以及集成测试通过。

### 今日验收

- [x] FoxAPI `POST /api/checkouts/prepare` 成功创建 `checkoutGroupId = 3`，总金额 `299.00`，状态 `PENDING_PAYMENT`。
- [x] FoxAPI `POST /api/checkouts/3/orders` 成功创建子订单 `9300000000046`，金额 `299.00`，状态 `PENDING_PAYMENT`。
- [x] `GET /api/orders/9300000000046` 返回订单明细、商品快照、地址快照和 `checkoutGroupId = 3`。
- [x] `GET /api/orders` 返回同一订单的 `checkoutGroupId = 3`；曾发现列表调用旧构造器导致 `null`，已修正并复验。
- [x] 旧的单店 `POST /api/orders` 仍可用；其历史订单 `9300000000045` 的 `checkoutGroupId = null` 属于预期。
- [x] Flyway、编译和测试截图已归档到 `docs/images/day-27-side-S4/`。

### 今日关键理解

- `checkout_group` 是一次跨店结算的父记录，`commerce_order` 是某个商家的子订单；一个父结算组可以对应多个商家订单。
- 不能直接相信前端价格或商家编号，必须先通过 `ProductSkuMapper` 读取当前消费者的购物车商品快照，再按快照中的 `tenantId` 分组。
- 旧单店入口通过重载方法传入 `null`，从而兼容历史订单；跨店子订单才传入真实 `checkoutGroupId`。
- HTTP 返回 200 不等于业务完成，必须同时检查响应体 `code`、`status`、订单数量和数据库关联字段。
- `checkoutGroupId` 在详情接口出现但列表接口为 `null`，最终定位为列表使用了旧构造器；这类问题属于“SQL 已查出、组装 VO 时丢字段”。

### 当前未完成与边界

- [ ] 结算组查询接口尚未完成；下一步需要增加按 `checkoutGroupId + consumerId` 查询全部子订单的 Mapper、Service 和 Controller。
- [ ] 目前没有结算组级别支付、整体取消和整体状态推进；现有 `mock-pay` 仍按单笔订单工作。
- [ ] `prepare` 与子订单流程尚未完成父级幂等重试闭环；购物车清理后重复请求的恢复策略需要单独设计。
- [ ] 尚未用两个真实商家的购物车数据完成完整集成验收；当前多租户分组由单元测试覆盖，FoxAPI 实际数据为一个商家子订单。
- [ ] 前端 `feature/web-v2` 尚未接入真实跨店结算接口；AI 主线步骤 25～30 未改动。

### 侧边任务/对话补充记录

- 用户明确要求由自己逐步编写代码，助手只提供下一小步、检查真实文件和解释错误；本日未替用户自动修改业务代码。
- 用户确认测试中的 Mockito 自附加 Agent、动态加载和 JVM Sharing 警告不是测试失败，绿色结果和退出码 0 才是验收依据。
- 用户询问“闭环是否完成以及是否可以提交”：已区分“结算组创建与拆单技术闭环”已完成，与“支付、查询、回滚、完整幂等的业务闭环”仍未完成；当前可作为阶段性提交点。
- 用户询问 `git add .`：已提醒当前工作区还包含地址快照等既有改动，不建议未经检查盲目暂存；提交由用户自行执行。
- 用户提供的 FoxAPI、IDEA、Flyway 和侧边栏聊天截图已整理为本日副线记录；截图中的 Token 仅作为本地学习证据，不写入接口合同正文。

### 今日 Git

- 当前分支：`feature/backend`。
- 本次副线代码、迁移、测试、文档和截图均未执行提交，等待用户自行检查后提交。

### 下一步

- 先提交本阶段“结算组创建与跨商家拆单”检查点。
- 然后实现结算组详情查询，再处理父级幂等、组级支付和整体回滚。

## Day 28：2026-08-24 / 前后端联调与部署副线 S4：结算组详情查询

### 今日目标

- 在 `feature/backend` 继续完成结算组详情的后端查询、错误态和真实环境验收。
- 后端保持“讲一步、用户写一步、验收一步”；不切换分支、不自动合并，不推进 AI 主线步骤 25～30。

### 今日完成

- [x] `CommerceOrderMapper` 新增按 `checkoutGroupId + consumerId` 查询子订单的方法。
- [x] 新增 `CheckoutGroupDetailVO`，返回父结算组基本信息和当前消费者的子订单摘要。
- [x] `CheckoutGroupService.getMyDetail` 先读取当前消费者的父结算组，再读取同一消费者的子订单；摘要中的 `items` 暂为空数组、`shippingAddress` 暂为 `null`。
- [x] `CheckoutController` 新增 `GET /api/checkouts/{checkoutGroupId}`，读取职责委托给 `CheckoutGroupService`；原有 `CheckoutService` 继续负责 prepare/拆单写流程。
- [x] `GlobalExceptionHandler` 的 `BizException` 分支改为 `ResponseEntity`，使业务码 `404` 同时成为实际 HTTP 404。

### 今日验收

- [x] `CheckoutGroupServiceTest`：5 条通过。
- [x] `CheckoutControllerTest`：2 条通过。
- [x] `mvnw.cmd -DskipTests compile`：`BUILD SUCCESS`。
- [x] 本地依赖容器恢复、应用启动后，真实请求 `GET /api/checkouts/3` 返回 HTTP 200、`code: 0`、父结算组和子订单摘要。
- [x] 真实请求不存在的 `GET /api/checkouts/999999` 返回 HTTP 404、`code: 404`；无 Authorization 的请求返回 HTTP 401、`code: 401`。

### 今日关键理解

- 结算组详情属于读取模型，Controller 可以统一路由，但应把读取逻辑放在 `CheckoutGroupService`，不能混入负责创建流程的 `CheckoutService`。
- 父结算组和子订单都必须按当前 `consumer_id` 查询；只校验父记录不足以保证子订单读取边界。
- 响应体中的 `code: 404` 不会自动让 HTTP 成为 404；需要用 `ResponseEntity.status(...)` 明确设置传输状态。
- 当前详情接口返回子订单摘要，不应假装已经提供商品项和收货地址快照。

### 侧边任务/对话补充记录

- 用户亲自完成后端代码输入，助手仅基于真实代码给出下一小步、检查和验收。
- 测试/运行环境变量一度丢失；确认部署环境文件仍在后恢复本地依赖和应用启动，文档不记录任何密钥值。
- Maven 的单引号参数和命令前误输入字符导致的失败属于命令格式问题，不是源码失败；使用正确的 Windows 命令后两组测试均通过。
- 真实请求截图含 Bearer Token，不归档；只归档不含凭据的两组测试和编译截图。

### 当前未完成与下一步

- [ ] 将“prepare 后再创建子订单”的两步操作收敛为单命令、跨商家原子提交。
- [ ] 设计父结算组级幂等与全量回滚，覆盖重复请求和任一商家失败。
- [ ] 补结算组级支付、取消、超时及状态推进；当前真实数据已观察到子订单 `CLOSED` 而父组仍 `PENDING_PAYMENT`，不能把状态机视为完成。
- [ ] 用两个真实商家的购物车完成集成验收，并由 `feature/web-v2` 接入真实地址和结算接口。

### 今日截图记录

- `docs/images/day-28-side-S4/checkout-group-service-tests-5-pass.png`：结算组 Service 5 条测试通过。
- `docs/images/day-28-side-S4/checkout-controller-tests-2-pass.png`：结算 Controller 2 条测试通过。
- `docs/images/day-28-side-S4/checkout-group-detail-compile-success.png`：结算组详情改动后的 Maven 编译成功。

### 今日 Git

- 当前分支：`feature/backend`；未切换、未合并、未提交。
- `CheckoutGroupDetailVO.java` 先前有过一次空文件暂存，最终提交前必须再次暂存其当前 record 内容。
- 本日更新仅属于副线 S4，不改变 AI 主线步骤 25～30 的进度。

### Day 28 后续推进：S4 后端四个核心闭环（待 ApiFox 最终验收）

#### 本次完成

- [x] 将旧的 `prepare → 创建子订单` 两步入口收敛为 `POST /api/checkouts`：同一事务内创建父结算组和按商家拆分的子订单；任一环节抛出业务异常时由事务回滚。
- [x] 为一键结算补父级 `Idempotency-Key`：请求指纹由排序后的购物车项与地址组成；成功重试复用原结算组，参数不一致或处理中请求返回 `409`。
- [x] 新增结算组级模拟支付 `POST /api/checkouts/{checkoutGroupId}/mock-pay`；所有子订单均支付后父组才从 `PENDING_PAYMENT` 变为 `PAID`。
- [x] 新增结算组级取消 `POST /api/checkouts/{checkoutGroupId}/cancel`；只允许待支付子订单取消，库存释放和流水写完后，全部子订单均为 `CANCELLED` 才更新父组。
- [x] 超时关单 `OrderCloseService` 已同步结算组：所有子订单均为 `CLOSED` 时，父组更新为 `CLOSED`。
- [x] 父组状态更新统一采用“锁定父记录 → 统计未达目标状态的子订单 → 条件更新父组”的方式，避免并发下提前推进父状态。

#### 本次实际验收与待验收

- [x] 主代码静态检查：`git diff --check -- server/src/main/java` 无空白错误；已逐段核对事务边界、Mapper SQL 条件、Controller 路由与 Service 依赖。
- [x] 先前真实请求：`POST /api/checkouts` 成功创建结算组 `4` 与待支付子订单；随后 `GET /api/checkouts/4` 成功返回父组和子订单摘要。
- [ ] 今天**没有**完成 ApiFox 的组级支付、组级取消、超时关闭、双商家原子失败和幂等重试验收；这些真实接口矩阵留到明天，不能据此把运行时验收写成已完成。
- [ ] `feature/web-v2` 仍为本地演示结算，尚未接入真实结算组接口；本轮未切换或合并分支。

#### 侧边任务/对话补充记录

- 测试运行配置中的环境变量曾丢失，应用上下文测试暴露的是 MySQL/RabbitMQ 等真实依赖配置问题，而不是本轮业务源码的确定性失败；用户决定本轮不再围绕测试环境排障，改为先完成主代码并将 ApiFox 验收延后。
- 复核旧侧边栏 Day 26/Day 27 记录后确认：地址快照是本轮跨店结算的前置闭环，V14 结算组与详情查询是本次四个状态闭环的起点，避免把某一条接口成功误记为整个 S4 已完成。
- 用户原本希望每个闭环都可独立提交；本次四个后端闭环连续完成但尚未提交，现作为一个完整的后端 S4 检查点提交。

#### 截图记录

- `docs/images/day-28-side-S4/checkout-atomic-submit-success.png`：真实 `POST /api/checkouts` 成功创建结算组和待支付子订单。
- `docs/images/day-28-side-S4/checkout-group-detail-after-submit-success.png`：真实 `GET /api/checkouts/4` 返回父结算组和子订单摘要。
- 支付、取消和超时关闭没有新的 ApiFox 截图，故不归档或伪造相应验收证据。

#### 下一步

- 明天先在 ApiFox 用真实数据依次验收：一键结算幂等重试、组级支付、组级取消、超时关闭，以及两个商家时的原子失败路径。
- 后端真实验收完成后，再在用户确认下切至 `feature/web-v2` 接入真实地址与结算组接口。

## Day 29：2026-08-26 / S4 ApiFox 状态机验收收口，准备进入 S5

### 今日完成

- [x] `GET /api/addresses` 返回当前消费者默认地址 `id = 1`。
- [x] 真实加购 SKU `1784970233597`，并完成一键结算：结算组 `5`、子订单 `9300000000048`、金额 `299.00`。
- [x] 相同 `Idempotency-Key` 重试复用结算组 `5`；同 Key 更换地址返回 HTTP/body `409`。
- [x] 结算组 `5` 组级支付后，父组和子订单均为 `PAID`。
- [x] 结算组 `6` 过期后，父组和子订单均为 `CLOSED`；对已关闭组取消返回 `409`。
- [x] 结算组 `7` 主动取消后，父组和子订单均为 `CANCELLED`。
- [x] 不含可见 Token 的 ApiFox 截图已归档到 `docs/images/day-29-side-S4/`。

### 今日未完成与边界

- [ ] 两个真实商家中一个 SKU 库存不足、整次结算整体回滚：按用户决定暂缓，不写成已验收。
- [ ] `feature/web-v2` 前端真实地址/结算接入：本次未切换、未修改、未合并。

### 今日结论与下一步

- S4 后端核心状态机与父级幂等已有单商家真实 ApiFox 证据，可作为进入 S5 的后端检查点。
- 双商家库存不足是明确保留项；不能宣称 S4 所有扩展场景均已实测完成。
- 下一阶段进入 S5：售后与商家订单处理最小闭环；仍按“讲一步、用户写一步、验收一步”推进。

### 截图记录

- `docs/images/day-29-side-S4/api-fox-cart-add-success.png`
- `docs/images/day-29-side-S4/api-fox-cart-readd-success.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-5-submit-success.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-5-idempotency-conflict.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-5-paid-detail.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-6-submit-success.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-6-timeout-closed.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-7-submit-success.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-7-cancel-success.png`
- `docs/images/day-29-side-S4/api-fox-checkout-group-7-cancelled-detail.png`

### 今日 Git

- 当前分支：`feature/backend`；不自动执行提交。
- 建议提交信息：`docs(side-line): record S4 ApiFox acceptance`

## Day 30：2026-08-26 / S5 售后审核后端最小闭环

### 今日完成

- [x] 新增 Flyway `V15__add_after_sale_tables.sql`，创建 `after_sale_request` 和 `after_sale_status_log`。
- [x] 新增售后实体、请求 DTO、订单项归属查询、Mapper、Service 和消费者/商家 Controller。
- [x] 消费者可查询自己的 `PAID` 订单项并提交售后申请；申请金额由后端按成交价和数量计算。
- [x] 商家只能查询当前 `tenant_id` 下的售后申请，并可完成审核。
- [x] 状态闭环真实验收通过：`SUBMITTED → REVIEWING → APPROVED/REJECTED`。
- [x] 消费者查询到最终 `APPROVED` 和 `REJECTED` 结果；状态日志机制已有记录，本次两条最终状态通过 ApiFox 验收。
- [x] 商家跨租户读取返回 HTTP/body `409`；已批准申请重复审核返回 HTTP/body `409`，确认非法动作不产生状态变更。
- [x] 消费者和商家接口统一返回 `AfterSaleRequestVO`，不暴露 `tenantId`、`consumerId`、`decidedBy`。

### 本闭环真实验收数据

- 消费者：`consumerId = 1784881782260`。
- 订单：`orderId = 9300000000048`；订单项：`orderItemId = 9300000000039`。
- 商家租户：`tenantId = 1001`；已验收售后申请：`afterSaleId = 1`（通过）、`afterSaleId = 2`（拒绝）。
- 申请金额：均为 `299.00`；商家审核人：`userId = 2`。

### 明确未包含

- [ ] 真实支付平台退款、退款流水和资金状态同步。
- [ ] 退货物流、收货验货和 `COMPLETED` 终态。
- [ ] `feature/web-v2` 前端售后页面接入；本轮未切换、未修改、未合并前端分支。

### 侧边聊天与截图记录

- 本轮继续按“讲一步、用户写一步、验收一步”推进，后端代码由用户编写，助手只读检查并带领 ApiFox/DataGrip 验收。
- S5 截图统一归档到 `docs/images/day-30-side-S5/`，只保留不含可见 Bearer Token 的编译、ApiFox 和 DataGrip 证据。
- 归档内容包括：可申请订单项、消费者提交、商家列表、商家审核、消费者最终结果、状态日志、跨租户拒绝和重复审核拒绝。
- 本次新增拒绝分支截图：
  - `apifox-s5-consumer-submit-rejected-path.png`
  - `apifox-s5-merchant-review-rejected.png`
  - `apifox-s5-consumer-rejected-detail.png`

### 今日 Git

- 当前分支：`feature/backend`；未切换、未合并。
- 按用户约定，将本次 S5 后端最小闭环、文档和无敏感验收截图作为一个提交。
- 本次不推进 AI 主线步骤 25～30。

## Day 31：2026-08-27 / S6 阶段一：商家订单真实读取

### 今日完成

- [x] 在 `CommerceOrderMapper` 新增按 `tenant_id` 查询商家订单的分页 SQL。
- [x] 在 `OrderService` 新增商家订单列表服务，使用当前商家租户并限制分页大小。
- [x] 新增 `GET /api/merchant/orders`，返回 `OrderDetailVO[]`，列表项暂不加载明细。
- [x] ApiFox 验收商家 A 成功读取订单列表。
- [x] ApiFox 验收商家 B 只能看到自己的订单，当前数据为空，不能读取商家 A 订单。
- [x] ApiFox 验收消费者访问商家订单接口返回 HTTP/body `403`。

### S6 总体状态

- [x] S6 第 1 项：商家订单列表真实读取、分页、租户隔离和角色边界。
- [x] S6 第 2 项：有效订单数、已支付营业额、待支付数、库存预警商品数的书面口径和 SQL 验证。
- [x] S6 第 3 项：只读 Dashboard DTO 和日期范围限制。
- [ ] S6 第 4 项：前端订单页、Dashboard 和目录库存接入真实接口；本项目当前按用户约定暂不推进前端。

当前结论：S6 后端范围已完成；前端接入项按用户约定暂缓，不影响后端阶段收口。

### 本闭环真实验收结果

- 商家 A：HTTP `200`、`code = 0`，返回 `tenantId = 1001` 的订单列表。
- 商家 B：HTTP `200`、`code = 0`、`data = []`。
- 消费者 Token：HTTP `403`、`code = 403`、`data = null`。
- 订单列表项返回 `items = []`、`shippingAddress = null`，详情读取仍使用消费者订单详情接口。
- 商家 A 指标：`validOrderCount = 6`、`paidRevenue = 797.00`、`pendingPaymentCount = 3`、`lowStockProductCount = 0`。
- 商家 B 指标：四项订单指标均为 `0`，未读到商家 A 数据。
- 倒序日期范围：HTTP `400`、`code = 400`、`data = null`。

### 明确未包含

- [ ] 前端 `feature/web-v2` 或 BFF 接入；本轮未切换、未修改、未合并前端分支。
- [ ] 商家订单详情、发货、履约和订单状态修改。

### 侧边聊天与验收记录

- 本轮继续按“讲一步、用户写一步、验收一步”推进，只使用 ApiFox 做接口验收，不新增单元测试。
- ApiFox 截图中的 Authorization 值可见，因此不将原图归档；验收结果以本节文字记录为准。
- 已安全归档：`maven-s6-dashboard-compile-success.png`、`apifox-s6-dashboard-metrics-merchant-a.png`。
- 商家 B、消费者 `403` 和日期错误截图因包含可见 Authorization 值，仅保留文字验收记录。
- 本轮不推进 AI 主线步骤 25～30。

### 今日 Git

- 当前分支：`feature/backend`；不切换、不合并。
- S6 当前提交范围为商家订单列表、Dashboard 指标后端代码和本节阶段文档记录；前端接入项暂缓。
