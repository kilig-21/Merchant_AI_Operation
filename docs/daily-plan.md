## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `██████░░░░` 6 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `██████░` 6 / 7 |
| 周验收 | 未开始 |
| 最近提交 | `chore: add mysql redis compose` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `01-工程与基础业务开发链.md`           |
| 当前步骤   | 步骤 6：Spring Boot 已连接 MySQL，Flyway 已创建第一批表 |
| 本周目标   | 可启动的前后端骨架                    |
| 今日目标   | 配置 Spring Boot 连接 MySQL，并用 Flyway 创建第一批表 |
| 昨日完成   | MySQL/Redis 启动验收；步骤 5 加餐能力完成                    |
| 当前卡点   | 无；待补文档记录与 Git 提交 |
| 最近一次提交 | `chore: add mysql redis compose`                           |
| 明日优先   | 第 1 周复盘和补漏：确认 Swagger/Actuator、从零启动链路、提交代码 |

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
- [ ] 已提交 Git，提交信息：建议 `feat(db): add initial schema migration`

### 今天完成

- 完成了：Spring Boot 已通过 `localhost:3307` 连接 MySQL；Flyway 已执行 `V1__init_schema.sql`；第二次启动显示数据库已是版本 1，无需重复迁移。
- 没完成：今天还未提交 Git；第 1 周复盘还未做。
- 卡住点：一开始没有 Flyway 日志，因为启动类里排除了 `DataSourceAutoConfiguration` 和 `FlywayAutoConfiguration`；移除 `exclude` 后解决。
- 明天优先做：做第 1 周复盘和补漏，确认 Swagger/Actuator、从零启动链路，并提交代码。
