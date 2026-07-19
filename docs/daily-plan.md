## 开工必读

- 每天继续本项目时，先读 `docs/collaboration-rules.md`、`docs/daily-plan.md`、`docs/learning-log.md`。
- 本项目学习方式以“带着做”为主：先讲为什么、再带用户建包/建类/写代码；不要默认一次性生成完整答案。

## 进度总览

| 项目 | 进度 |
|---|---|
| 总步骤 | `██░░░░░░░░` 2 / 36 |
| 当前阶段 | 第 1 阶段：工程与基础业务 |
| 本周任务 | `██░░░░░` 2 / 7 |
| 周验收 | 未开始 |
| 最近提交 | 待提交 |

## 进度看板
| 项目     | 当前状态                         |
| ------ | ---------------------------- |
| 当前阶段   | 第 1 阶段：工程与基础业务               |
| 当前文档   | `01-工程与基础业务开发链.md`           |
| 当前步骤   | 步骤 3：只启动 MySQL 与 Redis（待补）；步骤 4 已完成最小 `/api/ping` 闭环 |
| 本周目标   | 可启动的前后端骨架                    |
| 今日目标   | 校准后端基础配置，跑通 Spring Boot 最小接口 `/api/ping` |
| 昨日完成   | 无，今天是第一天                     |
| 当前卡点   | Swagger/Actuator 仍需最终确认；代码尚未提交 |
| 最近一次提交 | 待提交                           |
| 明日优先   | 验收 Swagger 与 Actuator；补 MySQL、Redis 的 Docker Compose 与 DataGrip 连接 |

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
- [x] 已提交 Git，提交信息：`feat(api): add ping endpoint`

### 今天完成

- 完成了：后端最小接口 `/api/ping` 已跑通，返回 `{"code":0,"message":"ok","data":"pong"}`。
- 没完成：Swagger UI、Actuator 健康检查还需要最终打开确认；Git 还未提交。
- 卡住点：`SecurityConfig` 路径少 `/` 导致 403；`ApiResponse.ok()` 自己调用自己导致 `StackOverflowError`。
- 明天优先做：先完成 Swagger/Actuator 验收与 Git 提交，再补步骤 3：MySQL、Redis、Docker Compose、DataGrip 连接。
