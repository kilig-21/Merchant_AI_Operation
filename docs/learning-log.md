# 学习与问题记录

## Day 1：2026-07-19

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 2：准备开发环境；步骤 4：创建最小 Spring Boot 应用
- 今日目标：统一后端基础环境，理解最小接口链路，跑通 `GET /api/ping`

### 今天学了什么

- JDK 版本统一：
  - 它解决什么问题：避免项目 `pom.xml`、IDEA、Maven、PowerShell 使用不同 Java 版本导致依赖下载、编译或启动结果不一致。
  - 我现在会用到哪里：后端 Spring Boot 项目统一使用 JDK 21。
- `SecurityConfig`：
  - 它解决什么问题：引入 Spring Security 后，默认所有接口会被保护；自定义 `SecurityFilterChain` 可以明确放行 `/api/ping`、`/actuator/health`、Swagger 等调试接口。
  - 我现在会用到哪里：后续登录、JWT、商家权限、消费者权限都会继续在这里扩展规则。
- `ApiResponse`：
  - 它解决什么问题：让后端接口统一返回 `code`、`message`、`data`，前端以后不用猜每个接口的返回格式。
  - 我现在会用到哪里：`/api/ping` 已经返回统一 JSON，后续登录、商品、订单接口也沿用这个格式。
- Controller：
  - 它解决什么问题：把浏览器或 Apifox 的 HTTP 请求映射到 Java 方法。
  - 我现在会用到哪里：`PingController` 用 `GET /api/ping` 验证后端最小链路。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| PowerShell 中 `java -version` 显示 Java 24，但项目希望统一为 Java 21 | 检查本机 Java 与 Maven 版本时发现不一致 | `JAVA_HOME` 已指向 `D:\Program Files\Java\jdk-21`，但 Path 中 `C:\Program Files\Common Files\Oracle\Java\javapath` 排在 JDK 21 前面；调整 Path 顺序，让 `D:\Program Files\Java\jdk-21\bin` 优先生效 | 是 |
| `/api/ping` 返回 403 | 访问浏览器和 Apifox 时被 Spring Security 拦截 | `SecurityConfig` 中写成了 `"api/ping"`，少了开头的 `/`；改成 `"/api/ping"` 后可以匹配请求路径 | 是 |
| Swagger UI 显示 `Failed to load remote configuration` | 打开 `/swagger-ui/index.html` 时页面没有加载接口配置 | Swagger 还会请求 `/v3/api-docs/swagger-config`，所以白名单需要写成 `"/v3/api-docs/**"` | 基本理解 |
| `StackOverflowError` | `/api/ping` 已进入 Controller，但返回时报错 | `ApiResponse.ok()` 里面错误地再次调用 `ApiResponse.ok("ok")`，造成无限递归；改成 `return new ApiResponse<>(0, "ok", data);` | 是 |

### 重要记录

- 成功的接口：`GET http://localhost:8080/api/ping`，返回 `{"code":0,"message":"ok","data":"pong"}`。
- 失败过的接口：`GET http://localhost:8080/api/ping` 曾返回 403；原因是 Security 白名单路径少 `/`。
- DataGrip 看到的数据：无，今天还没连接数据库。
- 关键报错：`HTTP ERROR 403`；`java.lang.StackOverflowError`。
- 参考资料：`01-工程与基础业务开发链.md` 步骤 2、步骤 4；`06-每日推进看板与任务安排.md`。
- 环境确认：Java、Maven、Node、npm、Docker 已能查看版本；`npm` 在 PowerShell 中可用 `npm.cmd -v` 验证。

### 今天还没理解透

- Swagger UI 背后的 `/v3/api-docs/**` 路径还需要通过实际页面再确认一次。
- Spring Security 生成默认密码的日志暂时不用深挖，后续做登录/JWT 时再系统理解。

### 明天遇到再补

- `GET /actuator/health` 的最终截图或响应。
- `/swagger-ui/index.html` 的最终截图或响应。
- Docker Compose 启动 MySQL、Redis 后的 DataGrip 连接记录。

## Day 2：2026-07-20

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 3：只启动 MySQL 与 Redis
- 今日目标：用 Docker Compose 启动 MySQL 和 Redis，并用 DataGrip 验证 MySQL 连接

### 今天学了什么

- `deploy/docker-compose.yml` 与后端配置的分工：
  - 它解决什么问题：`docker-compose.yml` 负责启动 MySQL、Redis 这类外部依赖服务；后端 `application.yml/properties` 负责告诉 Spring Boot 怎么连接这些服务。
  - 我现在会用到哪里：后续 Spring Boot 连接 MySQL、Redis 时，会连接 Docker Compose 启动出来的服务。
- Docker 端口映射：
  - 它解决什么问题：把容器内部端口暴露给本机工具访问。
  - 我现在会用到哪里：MySQL 容器内部仍是 `3306`，但本机 DataGrip 使用 `localhost:3307` 连接，因为 Compose 中写的是 `3307:3306`。
- DataGrip 连接验证：
  - 它解决什么问题：不用猜数据库是否可用，直接通过图形工具连接 MySQL 并执行 SQL 验证。
  - 我现在会用到哪里：后续 Flyway 建表、商品数据、订单数据都要用 DataGrip 做验收。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| Docker Compose 第一次启动失败 | 执行 `docker compose up -d` 时提示无法连接 Docker API | 打开 Docker Desktop，等待 Docker 引擎启动后重新执行命令，MySQL 和 Redis 成功启动 | 是 |
| 不清楚为什么配置放在 `deploy/docker-compose.yml` | 以为所有 yml 都应该写在后端项目里 | 理解为：`deploy/docker-compose.yml` 是给 Docker 启动依赖服务看的；后端 `application.yml/properties` 是给 Spring Boot 连接服务看的 | 是 |
| MySQL 端口不是文档里的 `3306` | 本机采用 `3307:3306` 避免端口冲突 | DataGrip 连接本机端口 `3307`，Docker 再转发到容器内 MySQL 的 `3306` | 是 |

### 重要记录

- 成功的接口：今天没有新增后端接口。
- 失败过的接口：无。
- DataGrip 看到的数据：连接 `ai_commerce` 成功，执行 `SELECT 1;` 返回 `1`。
- Redis 验收：`docker exec ai-commerce-redis redis-cli ping` 返回 `PONG`。
- MySQL 连接信息：`localhost:3307`，数据库名 `ai_commerce`。
- 关键报错：Docker Desktop 未启动时，Docker 命令无法连接 Docker API。
- 参考资料：`01-工程与基础业务开发链.md` 步骤 3；`06-每日推进看板与任务安排.md`。

### 今天还没理解透

- Spring Boot 连接 MySQL 的配置还没正式写，后续进入步骤 6 时再系统处理。
- Redis 今天只完成启动和 `PING` 验收，还没有接入后端业务。

### 明天遇到再补

- 步骤 5：统一响应、业务异常、全局异常处理、参数校验。
- 今日加餐中创建的异常处理相关代码需要单独验收和提交。
