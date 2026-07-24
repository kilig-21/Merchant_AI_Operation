## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `████████░░` 8 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `██░░░░░` 2 / 7 |
| 周验收 | 已通过 |
| 最近提交 | `feat(auth): add jwt current user` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `01-工程与基础业务开发链.md`           |
| 当前步骤   | 步骤 8 最小权限边界已通过：消费者注册、商家角色限制与租户上下文验收完成 |
| 本周目标   | 完成鉴权闭环，进入角色、租户隔离与商品管理前置准备 |
| 今日目标   | 补消费者注册接口，并完成 `/api/merchant/**` 的 401/403/200 权限边界验收 |
| 昨日完成   | 真实 JWT 登录与 `GET /api/auth/me` 已跑通，并已提交 `feat(auth): add jwt current user` |
| 当前卡点   | 暂无主线卡点；Docker Desktop 曾启动异常，已由用户自行修复 |
| 最近一次提交 | `feat(auth): add jwt current user`                           |
| 明日优先   | 开始步骤 9：商家商品最小后端闭环，先建商品创建/列表的基础结构 |

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
