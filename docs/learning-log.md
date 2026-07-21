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
- `BizException` 与全局异常处理：
  - 它解决什么问题：业务代码只负责判断规则并抛出业务异常，`GlobalExceptionHandler` 统一把异常转换成 `ApiResponse` JSON。
  - 我现在会用到哪里：后续库存不足、商品不存在、订单状态不允许支付等业务规则都会用 `throw new BizException(...)` 表达。
- 参数校验 DTO：
  - 它解决什么问题：用 `@RequestBody` 把前端 JSON 转成 DTO，再用 `@Valid` 触发 DTO 字段上的 `@NotBlank`、`@Min` 等规则。
  - 我现在会用到哪里：后续注册、登录、创建商品、提交订单等接口都需要先校验请求参数。
- 请求日志过滤器：
  - 它解决什么问题：每次请求进入后端后，控制台能看到请求方法、URL、HTTP 状态和耗时，方便判断请求是否进入后端以及耗时是否异常。
  - 我现在会用到哪里：后续排查接口 400、403、500 或慢请求时先看日志。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| Docker Compose 第一次启动失败 | 执行 `docker compose up -d` 时提示无法连接 Docker API | 打开 Docker Desktop，等待 Docker 引擎启动后重新执行命令，MySQL 和 Redis 成功启动 | 是 |
| 不清楚为什么配置放在 `deploy/docker-compose.yml` | 以为所有 yml 都应该写在后端项目里 | 理解为：`deploy/docker-compose.yml` 是给 Docker 启动依赖服务看的；后端 `application.yml/properties` 是给 Spring Boot 连接服务看的 | 是 |
| MySQL 端口不是文档里的 `3306` | 本机采用 `3307:3306` 避免端口冲突 | DataGrip 连接本机端口 `3307`，Docker 再转发到容器内 MySQL 的 `3306` | 是 |
| 参数非法时一开始返回 `500` | `POST /api/debug/validate` 传空名称和数量 0 | 新增 `MethodArgumentNotValidException` 处理方法，从字段错误里取第一条提示，并返回 `ApiResponse.error(400, message)` | 是 |
| 不理解 DTO 和 Controller 的关系 | `validate(@Valid @RequestBody DebugValidateRequest request)` 参数看起来像直接传了一个类 | 理解为：`@RequestBody` 把 JSON 转成 DTO 对象，`@Valid` 触发 DTO 字段校验，Controller 方法里通过 `request.name()`、`request.quantity()` 使用数据 | 是 |
| `ApiResponse<Void>` 方法里只 `throw` 异常也能通过 | `DebugController.bizError()` 声明返回 `ApiResponse<Void>`，但代码里没有 `return` | 理解为：`throw` 会中断当前方法，异常由 `GlobalExceptionHandler` 接住并统一返回 JSON | 是 |

### 重要记录

- 成功的接口：`GET /api/debug/biz-error` 返回 `code: 409`；`GET /api/debug/system-error` 返回 `code: 500`；`POST /api/debug/validate` 合法参数返回 `code: 0`，非法参数返回 `code: 400`。
- 失败过的接口：无。
- DataGrip 看到的数据：连接 `ai_commerce` 成功，执行 `SELECT 1;` 返回 `1`。
- Redis 验收：`docker exec ai-commerce-redis redis-cli ping` 返回 `PONG`。
- MySQL 连接信息：`localhost:3307`，数据库名 `ai_commerce`。
- 请求日志：IDEA 控制台能看到类似 `GET http://localhost:8080/api/ping 200 24ms`、`POST http://localhost:8080/api/debug/validate 200 18ms`。
- 截图记录：`docs/images/day-2/docker-containers-running.png`、`docs/images/day-2/redis-pong.png`、`docs/images/day-2/datagrip-select-1.png`、`docs/images/day-2/debug-validate-success.png`、`docs/images/day-2/debug-validate-400.png`、`docs/images/day-2/request-log-console.png`。
- 关键报错：Docker Desktop 未启动时，Docker 命令无法连接 Docker API。
- 参考资料：`01-工程与基础业务开发链.md` 步骤 3；`06-每日推进看板与任务安排.md`。

### 今天还没理解透

- Spring Boot 连接 MySQL 的配置还没正式写，后续进入步骤 6 时再系统处理。
- Redis 今天只完成启动和 `PING` 验收，还没有接入后端业务。
- 目前异常处理统一了 JSON 里的业务 `code`，HTTP 状态仍可能是 200；后续需要时再升级为 `ResponseEntity` 同步 HTTP 状态码。

### 侧边任务/对话补充记录

- 为什么 MySQL 用 `3307:3306`：
  - 左边 `3307` 是本机端口，DataGrip、Spring Boot 从电脑访问时用它。
  - 右边 `3306` 是容器内部 MySQL 端口，MySQL 自己仍然运行在容器内的 `3306`。
  - 这样可以避开本机已有 MySQL 占用 `3306` 的情况。
- 为什么 `deploy/docker-compose.yml` 不放在 `server` 里：
  - `deploy/docker-compose.yml` 是给 Docker 看，用来启动 MySQL、Redis 等外部依赖服务。
  - `server/src/main/resources/application.yml/properties` 是给 Spring Boot 看，用来连接这些已经启动的服务。
- 为什么 `ApiResponse<Void>` 方法里只 `throw` 也可以：
  - `throw` 会中断当前 Controller 方法，不再要求本方法正常 `return`。
  - 抛出的 `BizException` 会被 `GlobalExceptionHandler` 接住，再由异常处理器统一返回 `ApiResponse` JSON。
- DTO 和 Controller 的关系：
  - DTO 定义请求体应该有哪些字段，例如 `name`、`quantity`。
  - `@RequestBody` 把前端 JSON 转成 DTO 对象。
  - `@Valid` 触发 DTO 字段上的校验注解，例如 `@NotBlank`、`@Min`。
  - Controller 方法里通过 `request.name()`、`request.quantity()` 使用转换后的数据。
- `record` 的理解：
  - `record` 适合写只装数据的 DTO。
  - 它会自动生成构造方法、字段读取方法、`equals`、`hashCode`、`toString`。
  - `record` 取值用 `request.name()`，不是 `request.getName()`。
- 参数校验异常处理链路：
  - `ex` 是完整的参数校验异常对象。
  - `ex.getBindingResult()` 拿到本次绑定和校验结果。
  - `.getFieldErrors()` 拿到字段错误列表，例如 `name -> 名称不能为空`、`quantity -> 数量必须大于等于1`。
  - `.stream()` 把错误列表变成流水线，方便继续处理。
  - `.findFirst()` 先取第一个错误。
  - `.map(error -> error.getDefaultMessage())` 把错误对象转换成 DTO 注解里写的提示文字。
  - `.orElse("参数错误")` 是没有取到错误提示时的兜底文案。

### 明天遇到再补

- 步骤 6：连接数据库并创建第一批表。
- 使用 Flyway 创建迁移文件后，要在 DataGrip 中确认业务表和 `flyway_schema_history`。

## Day 3：2026-07-21

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 6：连接数据库并创建第一批表
- 今日目标：让 Spring Boot 连接 Docker 中的 MySQL，并用 Flyway 自动创建第一批表。

### 今天学了什么

- Spring Boot 数据库连接配置：
  - 它解决什么问题：告诉后端 MySQL 在哪里、用哪个数据库、哪个账号和密码连接。
  - 我现在会用到哪里：后端通过 `jdbc:mysql://localhost:3307/ai_commerce` 连接 Docker 容器里的 MySQL。
- IDEA 环境变量：
  - 它解决什么问题：避免把真实数据库密码写进 `application.properties` 和 Git。
  - 我现在会用到哪里：`spring.datasource.password=${MYSQL_ROOT_PASSWORD}` 会在后端启动时读取 IDEA 运行配置里的 `MYSQL_ROOT_PASSWORD`。
- Flyway：
  - 它解决什么问题：把数据库结构变更也变成有版本、有记录、可重复验收的迁移脚本。
  - 我现在会用到哪里：`V1__init_schema.sql` 第一次启动时创建业务表，执行记录保存在 `flyway_schema_history`。
- Flyway 文件名版本：
  - 它解决什么问题：让 Flyway 能判断迁移脚本的执行顺序和哪些脚本已执行。
  - 我现在会用到哪里：`V1`、`V2`、`V3` 决定执行顺序；`__` 后面的 `init_schema` 只是描述文字，Flyway 会记录它但不理解业务含义。
- Spring Boot 自动配置：
  - 它解决什么问题：根据依赖和配置自动创建 `DataSource`、Flyway、MyBatis 等基础对象。
  - 我现在会用到哪里：启动类保持 `@SpringBootApplication`，不要排除 `DataSourceAutoConfiguration` 和 `FlywayAutoConfiguration`，否则数据库连接和迁移不会执行。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| `${MYSQL_ROOT_PASSWORD}` 在 IDEA 里没有特殊颜色 | 编辑 `application.properties` 时看到占位符不像普通配置一样高亮 | 理解为 Spring Boot 在运行时读取环境变量，不靠 IDEA 静态高亮判断是否可用；在运行配置里手动添加 `MYSQL_ROOT_PASSWORD` | 是 |
| IDEA 打开的是 `server` 文件夹，不会自动读根目录 `deploy/.env` | 想让后端直接拿 Docker Compose 的 `.env` 密码 | 理解为 `deploy/.env` 给 Docker Compose 用，IDEA 环境变量给 Spring Boot 用；两边使用同名变量保持一致 | 是 |
| 后端启动成功但没有 Flyway 日志，数据库也没有表 | 第一次配置完数据源和迁移脚本后重启，控制台只显示 Tomcat 启动 | 启动类里排除了 `DataSourceAutoConfiguration`、`FlywayAutoConfiguration` 等自动配置，导致数据库和迁移都被跳过；移除 `exclude` 后解决 | 是 |
| 只加 `flyway-core` 不够稳 | 使用 MySQL 8.4 和新版 Flyway 时需要明确数据库支持模块 | 在 `pom.xml` 中保留 `flyway-core` 和 `flyway-mysql`，前者是 Flyway 主功能，后者是 MySQL 支持 | 是 |
| SQL 表名和状态值拼写容易出错 | 编写 `V1__init_schema.sql` 时出现 `sys_uers/sys_uer`、`create_at`、`DARFT` | 检查并改为 `sys_user`、`created_at`、`DRAFT` | 是 |

### 重要记录

- 成功的接口：`GET http://localhost:8080/api/ping` 返回 `{"code":0,"message":"ok","data":"pong"}`。
- 失败过的接口：无。
- DataGrip 看到的数据：`SHOW TABLES;` 返回 `flyway_schema_history`、`product_sku`、`product_spu`、`sys_user`、`tenant`。
- Flyway 记录：`SELECT version, description, script, success FROM flyway_schema_history;` 返回 `version = 1`、`description = init schema`、`script = V1__init_schema.sql`、`success = 1`。
- 关键日志：第一次成功迁移时看到 `Migrating schema ai_commerce to version "1 - init schema"` 和 `Successfully applied 1 migration`；第二次启动看到 `Schema ai_commerce is up to date. No migration necessary.`
- 关键报错：无红色报错；曾经的问题是没有 Flyway 日志，原因是自动配置被排除。
- 参考资料：`01-工程与基础业务开发链.md` 步骤 6；`06-每日推进看板与任务安排.md`。

### 今天还没理解透

- `tenant`、`sys_user`、`product_spu`、`product_sku` 之间还只是表结构层面的理解，后续写登录和商品接口时再继续加深。
- MyBatis 还没有正式使用，后续步骤写 Mapper 时再系统理解它如何把 Java 方法和 SQL 连接起来。

### 明天遇到再补

- 第 1 周复盘和补漏：确认 Swagger/Actuator、从零启动 MySQL/Redis/后端、补 Git 提交。
- 后续如果要改表，不修改已经执行过的 `V1__init_schema.sql`，而是新增 `V2__xxx.sql`。
