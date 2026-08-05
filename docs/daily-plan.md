## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `████████████████░` 16 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `███████` 7 / 7 |
| 周验收 | 已通过 |
| 最近提交 | 待提交：`feat(order): add idempotent order creation` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `02-交易库存限量促销开发链.md`           |
| 当前步骤   | 步骤 18：请求幂等与防重复下单已完成，3 个自动化测试通过 |
| 本周目标   | 后端继续推进可靠交易基础：库存账本、重复操作和并发基线 |
| 今日目标   | 完成步骤 18：幂等键、重复提交、参数冲突和并发防重复下单 |
| 昨日完成   | 步骤 17 并发基线和完整下单链路并发测试已通过 |
| 当前卡点   | 暂无主线卡点；正式 ID 方案后续再替换 |
| 最近一次提交 | 待提交：`feat(order): add idempotent order creation`                           |
| 明日优先   | 进入步骤 19：评估商品查询缓存，先确认缓存边界和一致性要求 |

## 每日任务
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
