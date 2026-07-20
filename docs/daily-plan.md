## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `████░░░░░░` 4 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `████░░░` 4 / 7 |
| 周验收 | 未开始 |
| 最近提交 | `chore: add mysql redis compose` |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `01-工程与基础业务开发链.md`           |
| 当前步骤   | 步骤 3：MySQL 与 Redis 已启动并通过 DataGrip 验收；步骤 4 已完成最小 `/api/ping` 闭环；步骤 5 加餐已开始，待单独验收 |
| 本周目标   | 可启动的前后端骨架                    |
| 今日目标   | 用 Docker Compose 启动 MySQL 与 Redis，并用 DataGrip 验证 MySQL 连接 |
| 昨日完成   | 后端最小 `/api/ping` 闭环                    |
| 当前卡点   | 步骤 5 加餐代码待单独验收 |
| 最近一次提交 | `chore: add mysql redis compose`                           |
| 明日优先   | 继续步骤 5：统一响应、业务异常、全局异常处理与参数校验 |

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
- 当前步骤：步骤 3：只启动 MySQL 与 Redis；加餐预习步骤 5：统一响应、异常和校验
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
- [ ] 加餐：开始步骤 5，修正 `ApiResponse` 并创建业务异常相关类（待单独验收和提交）

### 今天验收

- [x] MySQL 容器 `ai-commerce-mysql` 正常运行
- [x] Redis 容器 `ai-commerce-redis` 正常运行
- [x] Redis 返回 `PONG`
- [x] DataGrip 成功连接 `ai_commerce`
- [x] DataGrip 执行 `SELECT 1;` 返回 `1`
- [x] 截图/请求记录已写入 `docs/learning-log.md`
- [x] 已提交 Git，提交信息：`chore: add mysql redis compose`

### 今天完成

- 完成了：步骤 3 已通过验收；MySQL 使用本机端口 `3307` 映射容器端口 `3306`，Redis 使用 `6379`；`deploy/.env` 已加入 `.gitignore`。
- 没完成：步骤 5 加餐只完成部分代码，尚未完整验收。
- 卡住点：第一次执行 `docker compose up -d` 时 Docker Desktop 没有启动，导致无法连接 Docker API；启动 Docker Desktop 后解决。
- 明天优先做：如果今天不继续加餐，明天从步骤 5 的全局异常处理和临时校验接口继续。
