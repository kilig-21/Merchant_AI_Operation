## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `██████░░░░` 6 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `███████` 7 / 7 |
| 周验收 | 已通过 |
| 最近提交 | `feat(db): add initial schema migration` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `01-工程与基础业务开发链.md`           |
| 当前步骤   | 第 1 周验收已通过；下一步进入步骤 7：注册、登录与当前用户接口 |
| 本周目标   | 可启动的前后端骨架                    |
| 今日目标   | 第 1 周复盘和补漏：确认 Swagger/Actuator、从零启动链路、DataGrip 表结构和迁移记录 |
| 昨日完成   | Spring Boot 已连接 MySQL，Flyway 已创建第一批表，并已提交 `feat(db): add initial schema migration` |
| 当前卡点   | 无；今天补漏修改待提交 |
| 最近一次提交 | `feat(db): add initial schema migration`                           |
| 明日优先   | 进入步骤 7：注册、登录与当前用户接口 |

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
- 当前步骤：第 1 周复盘和补漏
- 今日目标：从零验收 MySQL/Redis/后端/接口文档/Flyway，并修复 Swagger UI 兼容问题

### 今天要学

- 知识点 1：从零启动链路：Docker 依赖服务 → Spring Boot → Flyway → 接口请求
- 知识点 2：Swagger UI 与 `/v3/api-docs` 的关系
- 知识点 3：`ex.printStackTrace()` 与 `log.error("Unhandled exception", ex)` 的区别
- 学到什么程度算够：能说清楚一个请求失败时，先判断服务是否启动、请求是否进后端、是否被 Security 拦截、是否进入目标接口，再看真实异常和依赖版本。

### 今天要做

- [x] 任务 1：确认 MySQL/Redis 容器运行，并用 Redis `PING` 验收
- [x] 任务 2：启动后端，验收 `/api/ping`、`/actuator/health`、Swagger UI 和 `/v3/api-docs`
- [x] 任务 3：用 DataGrip 验收业务表和 `flyway_schema_history`，完成第 1 周复盘

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
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [ ] 已提交 Git，提交信息建议：`fix(api-docs): update springdoc and log unexpected exceptions`

### 今天完成

- 完成了：第 1 周核心验收通过；修复 Swagger UI 因 springdoc 版本不兼容导致无法渲染接口文档的问题；全局兜底异常改为正式日志记录。
- 没完成：今天的修复和文档补充尚未提交 Git。
- 卡住点：`/v3/api-docs` 返回统一错误 JSON，Swagger UI 提示 `Unable to render this definition`；真实原因是 springdoc `2.2.0` 与 Spring Boot `3.5.16` 不兼容，升级到 `2.8.17` 后解决。
- 明天优先做：进入 `01` 文档步骤 7，开始注册、登录与 `GET /api/auth/me`。

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
- [ ] Git 已提交到一个清晰状态（今天补漏修改待提交）

### 没通过的地方

- 无核心验收失败项；Swagger UI 曾因 springdoc 版本兼容问题失败，已修复。

### 下周开始前必须补的内容

- 提交今天的 Swagger 兼容修复、全局异常日志和文档更新。
- 开始步骤 7 前，确认 `GlobalExceptionHandler` 中只保留正式日志，不保留临时 `ex.printStackTrace()`。
