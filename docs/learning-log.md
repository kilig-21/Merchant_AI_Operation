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

### 侧边任务/对话补充记录

- Flyway 版本文件名的理解：
  - `V1__init_schema.sql` 中的 `V1` 是给 Flyway 判断执行顺序和是否执行过的。
  - 两个下划线 `__` 是 Flyway 规定的分隔符，用来隔开版本号和描述。
  - `init_schema` 只是描述文字，Flyway 会记录到 `flyway_schema_history`，但不会理解它的业务含义；起清楚名字是为了人以后能看懂。
- Flyway 第二次启动为什么不重复建表：
  - 第一次执行成功后，Flyway 会把 `version = 1`、`script = V1__init_schema.sql`、`success = 1` 写入 `flyway_schema_history`。
  - 第二次启动时，Flyway 对比代码里的 `V1` 和数据库记录，发现已经成功执行过，所以显示 `Schema is up to date. No migration necessary.`
- 表建在哪里：
  - 表实际建在 Docker 容器 `ai-commerce-mysql` 内部的 MySQL 服务里。
  - 本机通过 `localhost:3307` 访问，Docker 再转发到容器内部 `3306`。
  - `mysql-data` volume 会保存 MySQL 数据，普通重启容器不会丢表。
- `flyway-mysql` 是否多余：
  - 不多余。`flyway-core` 是 Flyway 主功能，`flyway-mysql` 是 MySQL 支持模块。
  - 使用 MySQL 8.4 和新版 Flyway 时，保留 `flyway-mysql` 更稳。
- 不要把密码写进项目文件：
  - `deploy/.env` 给 Docker Compose 启动 MySQL 用。
  - IDEA 运行配置里的 `MYSQL_ROOT_PASSWORD` 给 Spring Boot 连接 MySQL 用。
  - `application.properties` 保持 `spring.datasource.password=${MYSQL_ROOT_PASSWORD}`，避免密码进入 Git。

### 明天遇到再补

- 第 1 周复盘和补漏：确认 Swagger/Actuator、从零启动 MySQL/Redis/后端、补 Git 提交。
- 后续如果要改表，不修改已经执行过的 `V1__init_schema.sql`，而是新增 `V2__xxx.sql`。

## Day 4：2026-07-22

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`、`06-每日推进看板与任务安排.md`
- 当前步骤：第 1 周复盘和补漏
- 今日目标：从零验收 MySQL/Redis/后端/接口文档/Flyway，并修复 Swagger UI 兼容问题。

### 今天学了什么

- 从零启动链路：
  - 它解决什么问题：不用只凭“昨天能跑”来判断项目状态，而是按依赖服务、后端启动、数据库迁移、接口请求逐层确认。
  - 我现在会用到哪里：以后每周验收、换电脑启动项目、排查“项目突然打不开”时，都先按这条链路检查。
- Swagger UI 与 `/v3/api-docs`：
  - 它解决什么问题：Swagger UI 只是页面，真正的接口定义来自 `/v3/api-docs`。页面打不开接口列表时，要直接访问 `/v3/api-docs` 看它返回了什么。
  - 我现在会用到哪里：后续新增登录、商品、订单接口后，如果 Swagger 页面异常，先查 `/v3/api-docs`。
- 异常排查链路：
  - 它解决什么问题：避免一遇到 500 就乱改代码。先判断请求是否进后端、是否被 Security 拦截、是否进入目标组件，再看真实异常。
  - 我现在会用到哪里：后续所有后端接口报错都按这个顺序排查。
- `ex.printStackTrace()` 与 `log.error("Unhandled exception", ex)`：
  - 它解决什么问题：`ex.printStackTrace()` 适合临时把异常栈打印到控制台；`log.error("Unhandled exception", ex)` 交给日志系统记录，适合正式保留。
  - 我现在会用到哪里：全局异常处理器保留正式日志，临时排错后移除裸 `printStackTrace()`。
- 依赖版本兼容：
  - 它解决什么问题：`NoSuchMethodError` 常见原因是运行时依赖版本不匹配，不一定是业务代码写错。
  - 我现在会用到哪里：后续升级 Spring Boot、MyBatis、springdoc、Spring AI 等依赖时，要同步看兼容版本。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 第一次 Redis `PING` 提示 `No such container` | 执行 `docker exec` 时容器名拼错 | 改用正确容器名 `ai-commerce-redis` 后返回 `PONG` | 是 |
| Swagger UI 显示 `Unable to render this definition` | 打开 `/swagger-ui/index.html` 时页面能打开，但不能渲染接口列表 | 直接访问 `/v3/api-docs`，发现返回的是统一错误 JSON，而不是 OpenAPI JSON | 是 |
| `/v3/api-docs` 返回 `{"code":500,"message":"系统异常，请稍后再试","data":null}` | springdoc 生成接口文档时内部报错，被 `GlobalExceptionHandler` 兜底包装 | 临时在兜底异常中加入 `ex.printStackTrace()`，让真实异常打印到 IDEA 控制台 | 是 |
| `NoSuchMethodError: ControllerAdviceBean.<init>(java.lang.Object)` | 访问 `/v3/api-docs` 后 IDEA 控制台打印真实异常 | 判断为 springdoc `2.2.0` 与 Spring Boot `3.5.16` / Spring Framework 版本不兼容，将 `springdoc-openapi-starter-webmvc-ui` 升级到 `2.8.17` | 是 |
| 临时异常打印不适合长期保留 | 为排查 Swagger 问题加入了 `ex.printStackTrace()` | 改为 `log.error("Unhandled exception", ex)`，对用户仍返回统一错误，对开发者保留完整异常栈 | 是 |

### 重要记录

- 成功的接口：
  - `GET http://localhost:8080/api/ping` 返回 `{"code":0,"message":"ok","data":"pong"}`。
  - `GET http://localhost:8080/actuator/health` 返回 `{"status":"UP"}`。
  - `GET http://localhost:8080/v3/api-docs` 返回包含 `"openapi":"3.1.0"` 的 OpenAPI JSON。
  - `GET http://localhost:8080/swagger-ui/index.html` 能显示 `debug-controller` 和 `ping-controller`。
- 失败过的接口：
  - 修复前 `GET /v3/api-docs` 返回统一错误 JSON：`{"code":500,"message":"系统异常，请稍后再试","data":null}`。
- DataGrip 看到的数据：
  - `SHOW TABLES;` 返回 `flyway_schema_history`、`product_sku`、`product_spu`、`sys_user`、`tenant`。
  - `SELECT version, description, script, success FROM flyway_schema_history;` 返回 `version = 1`、`description = init schema`、`script = V1__init_schema.sql`、`success` 为成功状态。
- Redis 验收：
  - `docker exec ai-commerce-redis redis-cli ping` 返回 `PONG`。
- 后端启动日志：
  - MySQL 连接成功，数据库地址为 `jdbc:mysql://localhost:3307/ai_commerce`。
  - Flyway 显示 `Current version of schema ai_commerce: 1` 和 `Schema ai_commerce is up to date. No migration necessary.`
  - Tomcat 启动在 `8080` 端口。
- 关键报错：
  - `NoSuchMethodError: ControllerAdviceBean.<init>(java.lang.Object)`。
- 关键修改：
  - `springdoc-openapi-starter-webmvc-ui` 从 `2.2.0` 升级到 `2.8.17`。
  - `GlobalExceptionHandler` 兜底异常使用 `log.error("Unhandled exception", ex)` 记录真实异常。
- 截图记录：
  - `docs/images/day-4/docker-compose-services-running.png`
  - `docs/images/day-4/redis-pong.png`
  - `docs/images/day-4/backend-startup-flyway-up-to-date.png`
  - `docs/images/day-4/api-ping-success.png`
  - `docs/images/day-4/actuator-health-up.png`
  - `docs/images/day-4/v3-api-docs-error-before-fix.png`
  - `docs/images/day-4/v3-api-docs-openapi-after-fix.png`
  - `docs/images/day-4/swagger-ui-api-list.png`
  - `docs/images/day-4/datagrip-show-tables.png`
  - `docs/images/day-4/datagrip-flyway-schema-history.png`
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 4、步骤 6。
  - `06-每日推进看板与任务安排.md` 第 1 周 Day 7 和周验收规则。
  - springdoc 官方兼容矩阵：Spring Boot `3.5.x` 对应 springdoc `2.8.x`。

### 今天还没理解透

- 目前只理解到 `NoSuchMethodError` 多半是依赖版本不兼容；更细的 Spring Framework 方法签名变化以后遇到再深入。
- HTTP 状态仍可能是 200，但业务 JSON 里是 `code: 500`；后续要不要改成 `ResponseEntity` 同步 HTTP 状态码，等登录和权限接口时再统一设计。

### 侧边任务/对话补充记录

- 怎么一步步定位错误：
  - 先确认 `/api/ping` 和 `/actuator/health` 正常，排除“整个后端挂了”。
  - 再确认 Swagger UI 静态页面能打开，说明坏的不是页面资源。
  - 直接访问 `/v3/api-docs`，发现返回统一错误 JSON，说明问题在接口定义生成过程。
  - 临时打印真实异常，看到 `NoSuchMethodError`。
  - 根据异常来源 `org.springdoc` 判断是 springdoc 与 Spring Boot 版本不兼容。
- `log.error("Unhandled exception", ex)` 会记录什么：
  - 第一部分是自定义提示 `Unhandled exception`，方便搜索日志。
  - 第二部分是完整异常对象，日志框架会打印异常类型、异常消息、调用栈和 `Caused by` 原因链。
  - 不要写成 `log.error("Unhandled exception: " + ex)`，那样通常不会打印完整异常栈。
- 为什么正式代码不长期保留 `ex.printStackTrace()`：
  - 它直接向控制台标准错误输出打印，适合临时排错。
  - 正式项目用日志系统更好，因为日志会带时间、级别、线程、类名，也方便后续写入日志文件和按级别过滤。

### 明天遇到再补

- 进入步骤 7：注册、登录与 `GET /api/auth/me`。
- 开始学习 BCrypt、JWT、Bearer Token、SecurityContext 和登录后的当前用户。

### 步骤 7 加餐记录

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 7：实现注册、登录与「当前用户」接口
- 今日加餐目标：先跑通临时 token 登录业务链路，不在今天接入完整 JWT。

#### 加餐学了什么

- `auth`、`security`、`user` 的分工：
  - `auth` 负责登录注册业务，例如 `AuthController`、`AuthService`、登录请求和登录响应。
  - `security` 负责安全基础设施，例如后续的 JWT 解析、过滤器、当前登录人。
  - `user` 负责 `sys_user` 表相关实体和查询。
- DTO / VO / Entity：
  - `LoginRequest` 是 DTO，用来接收前端传来的 `username` 和 `password`。
  - `LoginResponse`、`CurrentUserVO` 是 VO，用来返回给前端看。
  - `SysUser` 是 Entity，对应数据库 `sys_user` 表，里面有 `passwordHash`，不能直接返回给前端。
- BCrypt：
  - 数据库不保存明文密码，只保存 BCrypt 哈希。
  - 登录时用 `passwordEncoder.matches(明文密码, 数据库哈希)` 校验。
  - BCrypt 每次生成的哈希都不同，因为内部带随机盐；只要 `matches` 为 `true` 就说明匹配。
- MyBatis 半自动映射：
  - SQL 由开发者自己写清楚。
  - MyBatis 负责参数绑定、执行 SQL、把结果映射成 Java 对象。
  - 当前使用 `tenant_id AS tenantId`、`password_hash AS passwordHash` 这种方式显式映射数据库下划线字段和 Java 小驼峰字段。
- 构造器注入：
  - Controller 或 Service 需要 Mapper、Service、PasswordEncoder 时，通过构造方法声明依赖。
  - Spring 容器负责把对应 Bean 传进来，不需要手动 `new`。

#### 加餐遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| `security` 包和 `config/SecurityConfig` 容易混 | 建登录模块包结构时疑惑为什么已有 `SecurityConfig` 还要建 `security` 包 | 理解为 `SecurityConfig` 负责组装安全规则，`security` 包后续放 JWT、Filter、CurrentUser 等安全零件 | 是 |
| `secrurity` / `comtroller` 拼写错误 | 建包时把 `security`、`controller` 拼错 | 改为正确包名，避免 Java `package` 与目录长期错位 | 是 |
| `AuthService` 一开始放进 `security` 包 | 登录业务服务被误放到安全基础设施包里 | 移动到 `auth/service`，`security` 留给后续 JWT 和过滤器 | 是 |
| `AuthController` 参数一开始用了 `@PathVariable` | `POST /api/auth/login` 登录请求实际从 JSON Body 读取参数 | 改为 `@Valid @RequestBody LoginRequest request` | 是 |
| `/api/auth/login` 被 Security 拦截风险 | 白名单最初写成 `/api/auth`，不能匹配 `/api/auth/login` | 改为 `"/api/auth/**"` | 是 |
| `UserMapper` 启动时 Bean 定义失败 | 使用 MyBatis Starter `2.3.0` 搭配 Spring Boot `3.5.16` | 升级 `mybatis-spring-boot-starter` 到 `3.0.5` | 是 |
| 初始 BCrypt 哈希和 `123456` 不匹配 | `POST /api/auth/login` 正确账号密码返回 401；临时 `password-match` 返回 `false` | 不修改已执行过的 `V2`，新增 `V3__reset_test_user_password.sql` 重置测试用户密码哈希 | 是 |
| 登录接口曾返回系统异常 | 请求地址写成 `/api/auth/logind`，多了一个 `d` | 改为正确地址 `/api/auth/login` | 是 |

#### 加餐重要记录

- 新增 DTO/VO：
  - `LoginRequest`
  - `CurrentUserVO`
  - `LoginResponse`
- 新增业务代码：
  - `AuthController`
  - `AuthService`
  - `SysUser`
  - `UserMapper`
- 新增迁移：
  - `V2__init_auth_users.sql`：初始化 2 个商家租户和 3 个测试用户。
  - `V3__reset_test_user_password.sql`：重置测试用户 BCrypt 密码哈希。
- 关键依赖调整：
  - `mybatis-spring-boot-starter` 从 `2.3.0` 升级到 `3.0.5`，用于适配 Spring Boot `3.5.16`。
- 成功接口：
  - `POST /api/auth/login`，请求 `merchant_a_admin / 123456`，返回 `todo-access-token` 和当前用户信息。
  - `POST /api/auth/login`，请求错误密码 `wrong`，返回 `code: 401` 和 `用户名或密码错误`。
- 临时接口处理：
  - 曾用 `/api/debug/user/{username}` 验收 `UserMapper` 查询。
  - 曾用 `/api/debug/password-match/{username}/{password}` 验收 BCrypt 哈希匹配。
  - 两个临时接口已删除，避免长期暴露调试能力。
- 截图记录：
  - `docs/images/day-4/password-hash-tool-output.png`
  - `docs/images/day-4/datagrip-auth-users.png`
  - `docs/images/day-4/datagrip-auth-tenants.png`
  - `docs/images/day-4/debug-user-query-success.png`
  - `docs/images/day-4/debug-user-query-not-found.png`
  - `docs/images/day-4/flyway-v3-reset-password.png`
  - `docs/images/day-4/debug-password-match-success.png`
  - `docs/images/day-4/auth-login-success.png`
  - `docs/images/day-4/auth-login-wrong-password.png`

#### 加餐还没理解透

- 现在返回的是临时字符串 `todo-access-token`，还不是真正 JWT。
- `GET /api/auth/me` 还没有实现，当前用户还不能从 token 中恢复。
- 目前登录失败业务 `code` 是 401，但 HTTP 状态仍可能是 200；后续做 JWT 和权限时再统一考虑是否改为 `ResponseEntity`。

#### 明天遇到再补

- 接入 JWT：`JwtService`、JWT secret 环境变量、真实 accessToken。
- 增加 JWT Filter，把 token 解析成当前用户并放入 `SecurityContext`。
- 实现 `GET /api/auth/me`。

## Day 5：2026-07-23

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 7：实现注册、登录与「当前用户」接口
- 今日目标：接入 JWT，完成 `GET /api/auth/me`，让登录态能从 token 中恢复。

### 今天学了什么

- JWT：
  - 它解决什么问题：登录成功后，后端不再返回临时字符串，而是返回一个带签名和过期时间的 accessToken。
  - 我现在会用到哪里：前端以后每次请求受保护接口时，都要把这个 token 放进 `Authorization` 请求头。
- `JwtService`：
  - 它解决什么问题：统一负责创建 token 和解析 token，避免登录接口、过滤器里到处手写 JWT 细节。
  - 我现在会用到哪里：`login` 登录成功时调用 `createToken`；JWT Filter 收到请求时调用 `parse`。
- 构造器注入配置值：
  - 它解决什么问题：`@Value("${app.jwt.secret}")` 和 `@Value("${app.jwt.expire-hours}")` 让 Spring 创建 `JwtService` 时，把配置值传进构造器。
  - 我现在会用到哪里：JWT secret 从 `JWT_SECRET` 环境变量读取，避免把密钥写死到代码里。
- Bearer Token：
  - 它解决什么问题：约定前端用 `Authorization: Bearer <token>` 把登录凭证传给后端。
  - 我现在会用到哪里：请求 `/api/auth/me`、后续商品管理、订单管理等受保护接口时都要这样带 token。
- `JwtAuthentication` 过滤器：
  - 它解决什么问题：每个请求进入 Controller 前，先读取 `Authorization` 请求头；如果有合法 JWT，就把当前用户写入 `SecurityContext`。
  - 我现在会用到哪里：后续所有需要登录、角色、租户隔离的接口，都依赖它恢复当前用户。
- `SecurityContext`：
  - 它解决什么问题：保存当前请求的认证结果，让后续代码能知道“当前是谁”。
  - 我现在会用到哪里：`CurrentUser.required()` 从这里取出 `LoginPrincipal`。
- `CurrentUser`：
  - 它解决什么问题：封装从 `SecurityContext` 获取当前登录人的细节，避免业务代码到处直接操作 Spring Security API。
  - 我现在会用到哪里：`AuthService.me()` 已经使用它；后续商家商品接口会用它取得当前商家的 `tenantId`。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| IDEA 环境变量格式容易写错 | 配置 `JWT_SECRET` 时，环境变量栏前面还有一段 MySQL 密码 | 理解为 IDEA 环境变量必须写成 `MYSQL_ROOT_PASSWORD=xxx;JWT_SECRET=xxx`，每段都要有变量名和值 | 是 |
| `JwtService` 构造器看起来不像构造器注入 | 构造器参数是 `@Value(...) String secret`，不是 `UserMapper` 这种 Bean | 理解为构造器注入既可以注入 Bean，也可以注入配置值；Spring 负责把配置值传进构造器 | 是 |
| `LoginPrincipal` 字段写成 `userid` | 新建 record 时字段命名不统一 | 改成 `userId`，保持 Java 小驼峰，后续调用 `principal.userId()` 更自然 | 是 |
| JWT Filter 判断条件写反 | 最初写成 `header == null || !header.startsWith("Bearer ")` 时才解析 token | 改为 `header != null && header.startsWith("Bearer ")`，只有真的带 Bearer token 才解析 | 是 |
| 不理解 `catch` | 看到 JWT Filter 里的 `catch (Exception e)` 不清楚作用 | 理解为 `try` 里解析 token 出错时，`catch` 抓住异常并返回 401，不继续进入 Controller | 基本理解 |
| 不理解 `HttpStatus.UNAUTHORIZED.value()` | 看到 `value()` 不知道作用 | 理解为 `HttpStatus.UNAUTHORIZED` 表示 401 这个状态，`.value()` 是取出数字 `401`，传给 `response.setStatus` | 是 |
| `PasswordEncoder` 想删掉 | 接入 JWT 后疑惑 BCrypt 的 `PasswordEncoder` 是否还需要 | 理解为 `PasswordEncoder` 负责登录时校验密码，`JwtService` 负责登录成功后生成/解析 token，二者不能互相替代 | 是 |
| `/api/auth/me` 不带 token 返回 403 | 收窄白名单后访问受保护接口，Spring Security 默认返回 403 | 增加 `exceptionHandling().authenticationEntryPoint(...)`，让未登录访问返回 401 | 是 |
| 带 token 仍返回 401 | `Authorization` Header 已写但没有生效，或 JWT Filter 未挂进链路 | 在 Apifox 勾选 `Authorization` Header，并在 `SecurityConfig` 中补上 `addFilterBefore(jwtAuthentication, UsernamePasswordAuthenticationFilter.class)` | 是 |
| 请求方法写错 | 把 `/api/auth/me` 用 `POST` 请求，并带了登录 Body | 改为 `GET /api/auth/me`，Body 选 `none`，身份只通过 `Authorization` Header 传递 | 是 |
| 登录接口空 Body 返回 `code: 500` | `POST /api/auth/login` 时 Body 为空 | 判断为请求体缺失异常未单独处理；今天先记录，后续可补请求体缺失返回 400 | 是 |
| Maven 测试失败 | 用命令行跑 `mvnw test` 时 Spring 测试上下文启动失败 | 测试报告显示 MySQL `root` 密码不匹配，说明测试命令环境变量与 IDEA 不一致；接口在 IDEA 中已验收通过 | 是 |

### 重要记录

- 成功的接口：
  - `POST http://localhost:8080/api/auth/login`，请求 `merchant_a_admin / 123456`，返回三段式 JWT accessToken。
  - `GET http://localhost:8080/api/auth/me`，带 `Authorization: Bearer <accessToken>`，返回 `code: 0` 和当前用户完整信息。
- 失败过的接口：
  - `GET /api/auth/me` 不带 token 或未勾选 Header 时返回 HTTP 401。
  - `POST /api/auth/me` 返回 401，因为接口实际是 `GET /api/auth/me`。
  - `POST /api/auth/login` 空 Body 返回 `code: 500`，后续可补请求体缺失异常处理。
- DataGrip 看到的数据：今天未新增表；`/api/auth/me` 通过 `userId=2` 查询 `sys_user`，返回 `merchant_a_admin`。
- 关键修改：
  - 新增 `java-jwt` 依赖。
  - 新增 `app.jwt.secret=${JWT_SECRET}` 和 `app.jwt.expire-hours=2`。
  - 新增 `LoginPrincipal`、`JwtService`、`JwtAuthentication`、`CurrentUser`。
  - `AuthService.login` 返回真实 JWT。
  - `SecurityConfig` 改为无状态 Session，接入 JWT Filter，并让未登录访问返回 401。
  - `UserMapper` 新增 `selectById`，`AuthService.me` 从 token 取 `userId` 后查数据库返回完整用户。
- 截图记录：
  - `docs/images/day-5/auth-login-jwt-success.png`
  - `docs/images/day-5/auth-me-no-token-401.png`
  - `docs/images/day-5/auth-me-success.png`
- 验证记录：
  - IDEA 启动后端并通过 Apifox 验收成功。
  - 命令行执行 `mvnw test` 时，代码已进入编译和测试启动阶段，但测试上下文因 MySQL 密码环境不一致失败：`Access denied for user 'root'`。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 7。
  - `06-每日推进看板与任务安排.md` 的每日任务与验收规则。

### 侧边任务/对话补充记录

- `Content-Type: application/json` 为什么没手动写也能成功：
  - Apifox 中 Body 选择 JSON 时，会自动添加 `Content-Type: application/json`。
  - 后端的 `@RequestBody` 依赖这个请求头判断要把请求体按 JSON 转成 DTO。
- `catch` 的理解：
  - `try` 里放“可能出错”的代码，例如解析 JWT。
  - `catch` 负责接住异常，例如 token 过期、签名错误、格式不对。
  - 在 JWT Filter 里，解析失败就设置 HTTP 401 并 `return`，不让请求继续进入 Controller。
- `value()` 的理解：
  - `HttpStatus.UNAUTHORIZED` 是枚举，表示 401 这个状态。
  - `HttpStatus.UNAUTHORIZED.value()` 取出真正的数字 `401`，因为 `response.setStatus` 需要整数。
- `Authorization` Header 要勾选：
  - Apifox 里 Header 行即使显示了值，如果左侧没勾选，实际请求也不会发送。
  - 本次 `/api/auth/me` 从 401 到成功，就是确认 Header 启用并接入过滤器后跑通的。
- 401 和 403 的区别：
  - 401 更偏向“没有有效登录身份”。
  - 403 更偏向“已经识别身份，但权限不够”。
  - 今天通过 `authenticationEntryPoint` 把未登录访问受保护接口统一调整为 401。
- 为什么 `/me` 后来要查数据库：
  - 第一版只从 JWT 恢复 `userId/tenantId/userType`，所以 `username` 为 `null`。
  - 升级后通过 `userId` 查 `sys_user`，能返回完整用户信息，也能检查账号是否被禁用。
- 为什么不能删 `PasswordEncoder`：
  - JWT 处理的是“登录成功之后的身份凭证”。
  - BCrypt `PasswordEncoder` 处理的是“登录时密码是否正确”。
  - 两个环节相邻但职责不同。

### 今天还没理解透

- `UsernamePasswordAuthenticationToken` 名字里有 username/password，但当前用法其实是“放入已认证用户和权限”的通用认证对象；后续做角色权限时再加深。
- `SecurityFilterChain` 中多个过滤器的完整顺序还没完全展开；目前先理解 JWT Filter 要放在默认用户名密码过滤器前面。
- 登录接口空 Body 返回 500 还需要后续补一个更精确的异常处理，让它返回 400。

### 明天遇到再补

- 补消费者注册接口：`POST /api/auth/register`。
- 补请求体缺失异常处理，让空 Body 不再返回 500。
- 开始步骤 8：商家/消费者角色权限、401/403 验收和租户边界。

## Day 6：2026-07-24

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 7 收口；步骤 8：先把权限边界锁住
- 今日目标：补消费者注册接口，并完成商家/消费者角色权限与租户边界的最小验收。

### 今天学了什么

- 消费者注册接口：
  - 它解决什么问题：让普通消费者可以自己创建账号，而商家账号仍由初始化数据或后台流程提供。
  - 我现在会用到哪里：后续消费者浏览商品、购物车、下单、查询订单都需要消费者账号。
- 注册 DTO：
  - 它解决什么问题：`RegisterRequest` 只接收 `username/password`，不允许前端传 `userType` 或 `tenantId` 来伪造身份。
  - 我现在会用到哪里：所有写接口都会先用 DTO 明确允许前端传什么，不信任前端多传的敏感字段。
- `PasswordEncoder` 与 BCrypt：
  - 它解决什么问题：注册时用 `passwordEncoder.encode(...)` 把明文密码转为 BCrypt 哈希；数据库永远不存 `123456` 这种明文。
  - 我现在会用到哪里：注册用 `encode`，登录用 `matches`，二者是一对。
- Spring Security 角色规则：
  - 它解决什么问题：登录只能证明“你是谁”，`hasAnyRole` 才能证明“你能不能访问商家后台”。
  - 我现在会用到哪里：`/api/merchant/**` 只允许 `MERCHANT_ADMIN` 和 `MERCHANT_OPERATOR`，消费者即使登录也不能访问。
- 401 与 403：
  - 它解决什么问题：把“没登录/凭证无效”和“已登录但权限不够”区分清楚。
  - 我现在会用到哪里：前端后续可以根据 401 跳登录，根据 403 显示无权限页面。
- 租户上下文：
  - 它解决什么问题：商家接口从 `CurrentUser.requiredMerchantTenantId()` 取得当前商家租户 ID，而不是相信前端传来的 `tenantId`。
  - 我现在会用到哪里：后续商品、订单、经营数据查询都必须带当前租户条件。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| Docker Desktop 启动异常 | Docker Desktop 报 `vpnkit-bridge handshake failed`，Docker Engine 无法启动 | 先排查 WSL、Docker 服务和 `.docker` 权限；用户最终自行修复 Docker Desktop，恢复 MySQL/Redis 验收环境 | 基本理解 |
| 注册接口一开始返回未认证 | 新增 `POST /api/auth/register` 后请求被 Spring Security 拦截 | 用户发现白名单漏加注册路径；补 `/api/auth/register`，后续进一步建议用 `/api/auth/**` 避免遗漏 | 是 |
| `RegisterRequest` 字符串提示曾有引号问题 | 新建 DTO 时中文校验提示字符串没有完整闭合 | 用编译检查定位，修正 `@NotBlank`、`@Size` 的 `message` 字符串 | 是 |
| 登录/注册空 Body 返回不清晰 | 请求体为空时，还没转成 DTO，就不会进入 `@Valid` | 新增 `HttpMessageNotReadableException` 处理，返回 `code: 400` 和清晰提示 | 是 |
| 消费者 token 访问商家接口先返回 401 | 消费者已经登录，但访问 `/api/merchant/context` 仍被当作未认证 | 在 `SecurityConfig.exceptionHandling` 中补 `accessDeniedHandler`，让权限不足返回 403 | 是 |
| `MerchantContextController` 包位置不清晰 | 最初把商家验收接口放在普通 `controller` 包 | 移动到 `merchant/controller`，保持商家端模块结构清楚 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/auth/register`，请求 `consumer_today_01 / 123456`，返回 `code: 0`、`userType=CONSUMER`、`tenantId=null`。
  - `POST /api/auth/login`，请求 `consumer_today_01 / 123456`，返回消费者 JWT。
  - `GET /api/auth/me`，带消费者 token 返回 `consumer_today_01`。
  - `GET /api/merchant/context`，带 `merchant_a_admin` token 返回 `tenantId=1001` 和 `userType=MERCHANT_ADMIN`。
- 失败过的接口：
  - `POST /api/auth/register` 重复用户名返回 `code: 409` 和 `用户名已存在`。
  - `POST /api/auth/register` 空 Body 返回 `code: 400` 和 `请求体不能为空或 JSON 格式不正确`。
  - `GET /api/merchant/context` 不带 token 返回 HTTP 401。
  - `GET /api/merchant/context` 带消费者 token 返回 HTTP 403。
- DataGrip 看到的数据：
  - `sys_user` 中 `consumer_today_01` 的 `tenant_id` 为 `NULL`、`user_type` 为 `CONSUMER`、`status` 为 `1`。
  - `password_hash` 为 BCrypt 哈希，形如 `$2a$10$...`，不是明文密码。
- 关键修改：
  - 新增 `RegisterRequest`。
  - `UserMapper` 新增 `insertConsumer`。
  - `AuthService` 新增 `register`。
  - `AuthController` 新增 `POST /api/auth/register`。
  - `GlobalExceptionHandler` 新增 `HttpMessageNotReadableException` 处理。
  - `SecurityConfig` 收紧 `/api/merchant/**` 角色权限，并补 `accessDeniedHandler`。
  - `CurrentUser` 新增 `requiredMerchantTenantId()`。
  - 新增 `merchant/controller/MerchantContextController`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成注册、重复注册、空 Body、消费者 403、商家 200 验收。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 7、步骤 8。
  - `06-每日推进看板与任务安排.md` 的每日验收与记录规则。

### 侧边任务/对话补充记录

- Docker Desktop 插曲：
  - 疑惑点：Docker Desktop 重启后仍报错，`docker version` 只有 Client 或提示 Engine 无法启动。
  - 最后理解：这是 Docker Desktop/WSL 后端问题，不是项目代码问题；主线开发依赖 MySQL/Redis，所以 Docker 修好前后端接口无法完整验收。
  - 后续会用到哪里：每天开工如果数据库连接失败，先看 Docker Desktop、容器状态和 `docker compose`，再看 Spring Boot。
- 为什么注册接口要进白名单：
  - 疑惑点：`/api/auth/register` 一开始未认证，因为安全白名单没有覆盖它。
  - 最后理解：登录和注册都属于“还没有身份前必须能访问”的认证入口；写成 `/api/auth/**` 比一个个列 `login/register` 更不容易漏。
  - 后续会用到哪里：后续如果加 `refresh-token`、`forgot-password`，要判断它们是否也属于公开认证入口。
- `PasswordEncoder`、接口和实现类：
  - 疑惑点：`PasswordEncoder` 是接口，为什么 `@Bean` 方法可以返回 `new BCryptPasswordEncoder()`。
  - 最后理解：`BCryptPasswordEncoder` 是 `PasswordEncoder` 的实现类；变量类型写接口，运行时对象是真正的 BCrypt 实现，这就是接口引用指向实现类对象。
  - 后续会用到哪里：Service 依赖接口，不依赖具体算法；以后换 Argon2 等算法时，业务代码不用大改。
- `encode` 和 `matches`：
  - 疑惑点：加密是不是只要注入 `PasswordEncoder` 后调用 `.encode()`。
  - 最后理解：注册时 `encode(明文密码)` 得到哈希并入库；登录时 `matches(明文密码, 数据库哈希)` 判断是否匹配。不能把明文再次 `encode` 后直接字符串比较，因为 BCrypt 每次结果都不同。
  - 后续会用到哪里：所有创建或重置密码的地方都用 `encode`，所有登录或校验密码的地方都用 `matches`。
- `requestMatchers` 和后续规则的关系：
  - 疑惑点：`.requestMatchers("/api/merchant/**").hasAnyRole(...)` 是否必须成对出现。
  - 最后理解：`requestMatchers` 负责选中路径，后面的 `permitAll/authenticated/hasRole/hasAnyRole` 负责给这些路径定规则。
  - 后续会用到哪里：配置 `/api/public/**`、`/api/admin/**`、`/api/merchant/**` 等不同接口边界时都按这个模式写。
- 为什么 403 要在 `SecurityConfig.exceptionHandling` 里处理：
  - 疑惑点：消费者 token 访问商家接口时，为什么不是加一个 `GlobalExceptionHandler`。
  - 最后理解：权限不足发生在 Spring Security Filter Chain 中，很多时候请求还没进 Controller；`GlobalExceptionHandler` 主要处理 Controller/Service 层异常，安全过滤器里的认证/授权异常要在 `exceptionHandling` 里配置。
  - 后续会用到哪里：统一处理未登录、权限不足、token 过期等安全层结果时，优先看 Spring Security 配置。

### 今天还没理解透

- Spring Security 内部如何在多个过滤器之间传递 `Authentication`，目前只需要知道 JWT Filter 会写入 `SecurityContext`。
- `hasRole`、`hasAnyRole`、`hasAuthority` 的细微区别，当前先记住 `hasAnyRole` 会自动补 `ROLE_` 前缀。
- 当前注册用户 ID 仍用 `System.currentTimeMillis()` 临时生成，后续进入更正式 ID 方案时需要替换。

### 明天遇到再补

- 开始步骤 9：商家商品最小后端闭环。
- 商品接口必须使用 `CurrentUser.requiredMerchantTenantId()` 取得当前商家租户 ID。
- 先实现创建 SPU 和商家商品列表，再逐步补 SKU、库存和上架状态。

## Day 7：2026-07-25

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 9：完成商品的最小后端闭环
- 今日目标：跑通商家商品最小后端闭环第一版：创建 SPU、新增 SKU、初始库存入库与商家商品列表查询。

### 今天学了什么

- SPU 与 SKU：
  - 它解决什么问题：SPU 保存商品本体信息，例如「蓝牙耳机」；SKU 保存具体可售规格、价格和库存，例如「白色 / 标准版」「黑色 / Pro版」。
  - 我现在会用到哪里：后续消费者浏览商品、选择规格、下单扣库存都要依赖 SPU/SKU 的分工。
- DTO、Entity、VO：
  - 它解决什么问题：DTO 限定前端能传什么，Entity 对应数据库表，VO 决定接口返回给前端看什么，避免把请求、数据库和响应混成一团。
  - 我现在会用到哪里：`CreateProductRequest`、`CreateSkuRequest` 是 DTO；`ProductSpu`、`ProductSku` 是 Entity；`MerchantProductVO` 是商家列表返回对象。
- 租户隔离：
  - 它解决什么问题：商家接口不能相信前端传来的 `tenantId`，否则攻击者可以伪造别的商家 ID 造成数据泄露或篡改。
  - 我现在会用到哪里：创建 SPU、新增 SKU、查询商品列表都从 `CurrentUser.requiredMerchantTenantId()` 获取当前商家租户 ID。
- MyBatis 注解式 Mapper：
  - 它解决什么问题：把 Java 方法和 SQL 绑定起来，`@Insert` 负责插入，`@Select` 负责查询，`@Param` 给 SQL 占位符命名。
  - 我现在会用到哪里：`ProductSpuMapper` 插入 SPU、校验 SPU 是否属于当前租户、查询商家商品列表；`ProductSkuMapper` 插入 SKU。
- `@PathVariable`：
  - 它解决什么问题：从 URL 路径中取变量，例如 `/api/merchant/products/{id}/skus` 里的 `{id}` 是 SPU ID。
  - 我现在会用到哪里：新增 SKU 时，Controller 从路径中取 SPU ID，再交给 Service 做归属校验。
- `BigDecimal`：
  - 它解决什么问题：金额不能用 `double`，否则小数计算可能出现精度误差。
  - 我现在会用到哪里：`ProductSku.salePrice` 和 `CreateSkuRequest.salePrice` 使用 `BigDecimal`。
- `LIMIT/OFFSET` 分页：
  - 它解决什么问题：商品列表不能一次查出全部数据，`LIMIT` 控制一页多少条，`OFFSET` 控制跳过多少条。
  - 我现在会用到哪里：`GET /api/merchant/products?page=1&size=10&keyword=耳机` 查询当前商家的商品列表。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 误以为 DTO 字符串没闭合 | PowerShell 读取中文源码时显示乱码，看起来像缺少右引号 | 通过 IDEA 截图和 `mvnw -DskipTests compile` 编译结果确认代码实际正确；后续不再用终端乱码判断中文字符串语法 | 是 |
| `ProductService` 最初放错包 | 文件建在 `merchant/service`，而不是商品模块下面 | 移动到 `merchant/product/service`，让商品相关 Controller、Service、Mapper、DTO、Entity、VO 聚合在同一模块下 | 是 |
| `ProductService` 初版缺少返回值和分号 | 方法声明返回 `Long`，但插入后没有 `return`，异常语句少分号 | 补 `return spu.getId()` 和分号，并通过编译验证 | 是 |
| 不清楚昨天 token 如何继续使用 | 验收 403/200 时不知道昨天的 token | 重新调用 `POST /api/auth/login` 获取消费者 token 和商家 token；理解 JWT 有过期时间，昨天 token 不必保留 | 是 |
| 新增 SKU 前为什么要查 SPU 归属 | 商家传入 URL 中的 SPU ID | 用 `countByIdAndTenantId(spuId, tenantId)` 确认该 SPU 属于当前商家；查不到就返回「商品不存在」 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/merchant/products` 不带 token 返回 HTTP 401。
  - `POST /api/merchant/products` 带消费者 token 返回 HTTP 403。
  - `POST /api/merchant/products` 带 `merchant_a_admin` token 返回 `code: 0` 和商品 ID。
  - `POST /api/merchant/products/1784967699881/skus` 成功新增「白色 / 标准版」和「黑色 / Pro版」两个 SKU。
  - `GET /api/merchant/products?page=1&size=10` 返回当前商家商品列表。
  - `GET /api/merchant/products?page=1&size=10&keyword=耳机` 能查到「蓝牙耳机」。
- 失败过的接口：
  - 消费者 token 访问商家商品创建接口返回 HTTP 403，属于预期失败。
  - 不带 token 访问商家商品创建接口返回 HTTP 401，属于预期失败。
- DataGrip 看到的数据：
  - `product_spu` 中「蓝牙耳机」的 `tenant_id=1001`，`status=DRAFT`。
  - `product_sku` 中两条 SKU 的 `tenant_id=1001`，`spu_id=1784967699881`，价格分别为 `199.00` 和 `299.00`。
  - 两条 SKU 的初始库存已写入，`locked_stock=0`，`version=0`，`status=ON_SALE`。
- 关键修改：
  - 新增 `CreateProductRequest`、`CreateSkuRequest`。
  - 新增 `ProductSpu`、`ProductSku`。
  - 新增 `ProductSpuMapper`、`ProductSkuMapper`。
  - 新增 `ProductService`。
  - 新增 `MerchantProductController`。
  - 新增 `MerchantProductVO`。
  - `ProductSpuMapper` 新增 `countByIdAndTenantId` 和 `selectMerchantProducts`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成创建 SPU、消费者 403、商家 200、新增 SKU、商品列表和 keyword 查询验收。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 9。
  - `06-每日推进看板与任务安排.md` 的每日任务与验收规则。

### 侧边任务/对话补充记录

- 为什么不直接用 MyBatis 生成器：
  - 疑惑点：既然 MyBatis 或 MyBatis-Plus 可以根据表生成 Entity、Mapper、Service，为什么还手写。
  - 最后理解：当前阶段重点是理解 DTO、Entity、VO、Mapper、Service、Controller 的职责，以及租户隔离 SQL 为什么必须手写清楚；等手写过最小闭环后，再用生成器作为加速工具更合适。
  - 后续会用到哪里：订单、购物车、促销等模块如果重复 CRUD 较多，可以考虑在理解分层后引入代码生成。
- 为什么 `tenantId` 不能从前端传：
  - 疑惑点：创建商品时如果前端也传 `tenantId` 是否更方便。
  - 最后理解：前端输入可能被伪造，租户 ID 必须从 JWT 当前用户中恢复；商品创建、SKU 新增、列表查询都应使用后端取得的 `tenantId`。
  - 后续会用到哪里：商家订单、经营数据、Agent 工具和知识库检索都必须按同样原则限制租户。
- 为什么新增 SKU 要先校验 SPU 归属：
  - 疑惑点：URL 已经带了商品 ID，是否可以直接插入 SKU。
  - 最后理解：商家 A 可能拿商家 B 的 SPU ID 请求新增 SKU，所以必须用当前 `tenantId` 和 `spuId` 一起查询；查不到时返回「商品不存在」更安全。
  - 后续会用到哪里：修改商品、改库存、下架、查看详情、订单归属校验都要按“资源 ID + 当前租户”查询。
- 为什么列表返回 VO：
  - 疑惑点：`ProductSpu` 字段和列表返回差不多，是否可以直接返回 Entity。
  - 最后理解：VO 是接口展示模型，后续可以加入 SKU 数量、最低价、总库存、更新时间等聚合字段，而不污染数据库实体。
  - 后续会用到哪里：消费者公开商品详情尤其需要 VO，避免泄露 `locked_stock`、成本价或内部字段。
- `mvnw -DskipTests compile` 怎么跑：
  - 疑惑点：不知道这条命令在哪里执行。
  - 最后理解：在项目的 `server` 目录执行 `.\mvnw -DskipTests compile`；它会编译后端代码但跳过测试，适合每天小步验收语法和 Spring 装配。
  - 后续会用到哪里：每次新增 Controller、Service、Mapper 后都先跑编译，再去 Apifox 验收。

### 今天还没理解透

- 商品列表暂时只查 SPU，还没有聚合 SKU 数、最低价、总可售库存。
- 商品上架/下架状态还没有接口，当前新建 SPU 是 `DRAFT`，SKU 默认 `ON_SALE`。
- `System.currentTimeMillis()` 仍是临时 ID 方案，后续需要替换成更正式的 ID 生成方式。

### 明天遇到再补

- 补商品上架/下架接口，并明确 SPU 与 SKU 状态的关系。
- 增强商家商品列表：返回 SKU 数、最低价、总可售库存。
- 或进入步骤 10：消费者公开商品列表和商品详情接口。

## Day 8：2026-07-27

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 9 收口：补商品上架/下架与商家商品列表汇总字段
- 今日目标：让商家商品管理闭环补齐“创建 SPU -> 新增 SKU -> 上架/下架 -> 列表看到 SKU 汇总”的完整链路。

### 今天学了什么

- SPU 上架/下架状态：
  - 它解决什么问题：新建商品默认是 `DRAFT`，商家补齐 SKU、价格和库存后再上架，避免消费者看到半成品商品；下架后商品仍保留在商家后台，但后续公开接口不能展示。
  - 我现在会用到哪里：步骤 10 的消费者公开商品接口必须只查询 `ON_SALE` 商品，草稿和下架商品都不可见。
- MyBatis `@Update` 的返回值：
  - 它解决什么问题：Mapper 的更新方法返回 `int`，表示本次 SQL 影响了多少行；通过 `updated != 1` 可以识别商品不存在或不属于当前商家，避免接口“假成功”。
  - 我现在会用到哪里：后续修改商品、调整库存、模拟支付、关闭订单等写操作都要检查更新影响行数。
- 上架前检查 SKU 数量：
  - 它解决什么问题：没有 SKU 的商品没有具体可售规格、价格和库存，不应该进入上架状态。
  - 我现在会用到哪里：后续商品图片、价格、库存和活动发布都要先检查必要业务条件，而不是只改状态。
- 商品列表聚合字段：
  - 它解决什么问题：商家列表不只需要 SPU 基础信息，还要展示 SKU 数、最低价和总可售库存，这些字段来自 `product_sku` 的聚合查询。
  - 我现在会用到哪里：前端商品管理表格会直接展示这些字段；消费者商品列表和详情也会继续使用 VO，而不是直接返回 Entity。
- SQL `COALESCE`：
  - 它解决什么问题：`LEFT JOIN` 后，如果商品没有 SKU，`SUM(s.available_stock)` 会是 `null`；`COALESCE(SUM(...), 0)` 可以把它兜底为 0。
  - 我现在会用到哪里：后续做订单金额、退款统计、销售额汇总等聚合查询时，也会遇到 `null` 兜底问题。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 一开始没有按“带着做”协作规则推进 | 开工后助手直接改了商品模块文件 | 重新回到“讲一步、用户写一步”的节奏，并明确未经 IDEA 同意的改动范围 | 是 |
| 不清楚 `@Update` 更新语句为什么要判断返回值 | 写 `unpublishProduct` 时看到 `int updated` 和 `if (updated != 1)` | 理解为 MyBatis 返回的是 SQL 影响行数；0 表示没命中商品或租户不匹配，不能当成功 | 是 |
| 不理解 `COALESCE(SUM(...), 0)` | 改商品列表聚合 SQL 时看到 `COALESCE` | 理解为从左到右取第一个非 `null` 值；没有 SKU 时 `SUM` 为 `null`，用 0 兜底 | 是 |
| Apifox 上架接口第一次返回 401 | 请求 URL 写成 `/products/{1784967699881}/publish` | 去掉真实请求中的 `{}`，改为 `/products/1784967699881/publish` 后通过 | 是 |
| `.vs/` 出现在 Git 未跟踪文件里 | `git status` 看到 `?? .vs/` | 确认为 Visual Studio 本机缓存目录，并加入 `.gitignore`，不提交 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/merchant/products/1784967699881/publish` 带商家 token 返回 `code: 0`。
  - `POST /api/merchant/products/1784967699881/unpublish` 带商家 token 返回 `code: 0`。
  - `GET /api/merchant/products?page=1&size=10` 返回「蓝牙耳机」及汇总字段：`skuCount=2`、`minSalePrice=199.00`、`totalAvailableStock=70`。
- 失败过的接口：
  - 消费者 token 访问 `POST /api/merchant/products/1784967699881/publish` 返回 HTTP 403，属于预期失败。
  - 商家 token 访问不存在商品 `POST /api/merchant/products/999999999999/publish` 返回 `code: 404` 和 `商品不存在`，属于预期失败。
- DataGrip 看到的数据：
  - 上架后 `product_spu` 中「蓝牙耳机」`status=ON_SALE`。
  - 下架后 `product_spu` 中「蓝牙耳机」`status=OFF_SALE`。
  - 商品列表接口聚合出的总可售库存为 `70`，与两个 SKU 库存加总一致。
- 关键修改：
  - `MerchantProductController` 新增 `publish`、`unpublish` 接口。
  - `ProductService` 新增 `publishProduct`、`unpublishProduct`。
  - `ProductSpuMapper` 新增 `updateStatusByIdAndTenantId`，并增强 `selectMerchantProducts` 聚合查询。
  - `ProductSkuMapper` 新增 `countBySpuIdAndTenantId`。
  - `MerchantProductVO` 新增 `skuCount`、`minSalePrice`、`totalAvailableStock`。
  - `.gitignore` 新增 `.vs/`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成商家上架、商家下架、商品列表汇总、消费者 403、无效商品 404 验收。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 9、步骤 10。
  - `06-每日推进看板与任务安排.md` 的每日任务、验收和 Git 提交规则。

### 侧边任务/对话补充记录

- 为什么真实 URL 里不能带 `{}`：
  - 疑惑点：Controller 里写的是 `/{id}/publish`，Apifox 里一开始也写成了 `{1784967699881}`。
  - 最后理解：`{id}` 是后端路由模板的写法，真实 HTTP 请求要把它替换成具体值，例如 `/1784967699881/publish`。
  - 后续会用到哪里：所有 `@PathVariable` 接口都一样，例如订单详情、商品详情、支付接口。
- 为什么下架可以直接看 `updated`：
  - 疑惑点：下架前为什么不先查商品是否存在。
  - 最后理解：`UPDATE ... WHERE id = ? AND tenant_id = ?` 本身已经同时完成归属校验和修改；影响行数为 0 就说明对当前商家不可操作。
  - 后续会用到哪里：修改商品、调整库存、支付订单状态更新都可以用“条件更新 + 影响行数”表达业务安全边界。
- 为什么商家列表要用 `LEFT JOIN`：
  - 疑惑点：为什么不用普通 `JOIN`。
  - 最后理解：草稿商品可能还没有 SKU，`LEFT JOIN` 能让它仍然出现在商家后台列表里；普通 `JOIN` 会把没有 SKU 的商品过滤掉。
  - 后续会用到哪里：后台管理列表通常要展示未完成配置的数据，而消费者公开接口则只展示可售数据。
- “开始吧”不等于“你来做”：
  - 疑惑点：开工时说“开始吧”，助手一开始直接改了代码，这不符合本项目“带着做”的协作规则。
  - 最后理解：除非明确说“你来做”“你直接改”“你帮我补文档”，否则默认应先讲清楚为什么，再让用户在 IDEA 中手写每一步。
  - 后续会用到哪里：每天继续开发时，普通功能代码按“讲一步、写一步、验收一步”推进；文档更新或用户明确授权的任务才由助手直接改。
- `.vs/` 是什么：
  - 疑惑点：`git status` 里出现 `.vs/`，不确定它是不是项目代码。
  - 最后理解：`.vs/` 是 Visual Studio 的本机缓存、索引或临时配置目录，不属于项目源码；已经加入 `.gitignore`，不提交。
  - 后续会用到哪里：看到 `.idea/`、`.vscode/`、`.vs/`、`target/` 这类 IDE 或构建产物时，先判断是否应忽略，不要随手提交。
- Maven 编译和联网依赖：
  - 疑惑点：助手第一次跑 `mvnw compile` 时出现 Maven Central 下载失败和沙箱联网权限问题。
  - 最后理解：编译本身是本地校验，但如果本地 Maven 缓存缺依赖，就需要联网下载；后来在用户本机 PowerShell 中重新执行，依赖可用并成功 `BUILD SUCCESS`。
  - 后续会用到哪里：编译失败时先区分“代码错误”和“依赖下载/网络环境错误”，不要把网络问题误判为 Java 代码问题。

### 今天还没理解透

- `GROUP BY` 在不同数据库和 SQL 模式下对非聚合字段的要求还可以后续继续加深。
- `ON_SALE`、`OFF_SALE` 是否也要同步影响 SKU 状态，当前第一版只控制 SPU 状态，后续消费者公开接口先以 SPU 状态作为可见边界。
- 正式 ID 生成方案还没替换，当前仍用 `System.currentTimeMillis()`。

### 明天遇到再补

- 进入步骤 10：消费者公开商品接口。
- 公开接口只返回 `ON_SALE` 商品，不泄露 `locked_stock`、成本价、商家内部字段。
- 继续用 VO 表达消费者可见数据，避免直接返回 Entity。

## Day 9：2026-07-28

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 10：实现消费者公开商品接口
- 今日目标：完成公开商品列表、商品详情和 SKU 可售状态查询；只展示已上架商品，不泄露商家内部字段。

### 今天学了什么

- 公开接口白名单：
  - 它解决什么问题：消费者未登录时也能浏览公开商品，所以 `/api/public/**` 需要在 `SecurityConfig` 里 `permitAll()`。
  - 我现在会用到哪里：后续店铺首页、商品详情、促销公开入口都属于公开接口，但仍要在 SQL 层限制只返回可见数据。
- 公开接口和商家接口的边界：
  - 它解决什么问题：商家后台可以看草稿、下架和管理字段；消费者公开接口只能看 `ON_SALE` 商品，不能泄露 `tenantId`、`lockedStock`、`version`。
  - 我现在会用到哪里：后续购物车、订单、前端页面都要基于公开商品接口返回的消费者可见字段。
- VO 拆分：
  - 它解决什么问题：列表、详情、SKU、SKU 可售状态的返回结构不同，拆成 `PublicProductListItemVO`、`PublicProductDetailVO`、`PublicSkuVO`、`PublicSkuAvailabilityVO` 更清楚。
  - 我现在会用到哪里：后续订单快照、购物车展示、促销详情也会继续用 VO 表达接口展示数据。
- `PublicProductBaseVO`：
  - 它解决什么问题：SPU 基础详情 SQL 只返回 `id/name/description/updatedAt`，不能直接映射到带 `List<PublicSkuVO>` 的详情 VO。
  - 我现在会用到哪里：Mapper 负责接收平铺 SQL 结果，Service 负责把基础信息和 SKU 列表组装成嵌套详情。
- SKU 可售状态：
  - 它解决什么问题：前端选择规格、加购物车或下单前，需要知道当前 SKU 是否还能买。
  - 我现在会用到哪里：步骤 12 购物车和步骤 13 普通订单都会再次校验 SKU、SPU 状态和库存。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 公开列表接口一开始返回 `code: 500` | `GET /api/public/stores/1001/products?page=1&size=10` 请求进入后端后报系统异常 | `@PathVariable Long storeId` 需要配合 `@GetMapping("/api/public/stores/{storeId}/products")`，原来只写 `@GetMapping` 导致找不到路径变量 | 是 |
| 分页 offset 公式写错 | `PublicProductService` 里计算偏移量时写成 `(safePage - 1) * safePage` | 改为 `(safePage - 1) * safeSize`，因为偏移量等于“页码前面的页数 * 每页条数” | 是 |
| `PublicSkuVO` 最初放错包 | 公开商品详情里引用到了 `merchant.product.vo.PublicSkuVO` | 把 `PublicSkuVO` 移到 `publicapi.product.vo`，让消费者公开接口的 VO 都归在 publicapi 模块下 | 是 |
| 详情查询一开始可能映射失败 | SPU 详情 SQL 只返回 4 个字段，但 `PublicProductDetailVO` 有 `skus` 列表 | 新增 `PublicProductBaseVO` 承接 SPU 基础 SQL，Service 再查询 SKU 列表并组装成 `PublicProductDetailVO` | 是 |
| 不清楚外键是否能自动返回 `skus` | 讨论是否给 SPU/SKU 建外键来解决详情嵌套返回 | 理解为外键只保证数据库关系完整性，不会让 MyBatis 自动查出 `List<PublicSkuVO>`；接口嵌套结构仍要靠查询和组装 | 是 |
| IDEA 提示 `ASC` 冗余 | `ORDER BY s.sale_price ASC` 中 `ASC` 被标黄 | 理解为 SQL 默认升序，`ASC` 可省略但保留更利于学习；`DESC` 降序时不能省 | 是 |

### 重要记录

- 成功的接口：
  - `GET /api/public/products/ping` 未登录返回 `public-product-pong`。
  - `GET /api/public/stores/1001/products?page=1&size=10` 未登录返回「蓝牙耳机」，包含 `minSalePrice=199.00`、`totalAvailableStock=70`。
  - `GET /api/public/products/1784967699881` 返回商品详情和 `skus` 数组。
  - `GET /api/public/skus/1784970220075/availability` 返回 `purchasable=true`、`availableStock=50`、`message=可购买`。
  - `GET /api/public/skus/999999999999/availability` 返回 `purchasable=false`、`availableStock=0`、`message=商品不存在或已下架`。
  - 商品下架后，`GET /api/public/products/1784967699881` 返回 `code: 404` 和 `商品不存在`。
  - 商品下架后，`GET /api/public/skus/1784970220075/availability` 返回 `purchasable=false` 和 `商品不存在或已下架`。
- 失败过的接口：
  - 公开列表曾因 `@PathVariable` 缺少路径模板返回 `code: 500`。
  - 公开详情曾因 VO 包位置和详情 VO 映射结构不合适返回 `code: 500`。
- DataGrip 看到的数据：
  - 公开列表聚合出的最低价 `199.00` 和总可售库存 `70` 与 SKU 数据一致。
  - 商品下架后，公开详情不可见，SKU 可售状态也变为不可购买。
- 关键修改：
  - `SecurityConfig` 放行 `/api/public/**`。
  - 新增 `PublicProductController` 的公开列表、详情和 SKU 可售状态接口。
  - 新增 `PublicProductService` 处理分页、详情组装和 SKU 可售状态兜底。
  - 新增 `PublicProductMapper` 的公开商品列表、SPU 基础详情、SKU 列表和 SKU 可售状态查询。
  - 新增 `PublicProductListItemVO`、`PublicProductBaseVO`、`PublicProductDetailVO`、`PublicSkuVO`、`PublicSkuAvailabilityVO`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成公开列表、公开详情、SKU 可售、缺失 SKU、商品下架后详情不可见、商品下架后 SKU 不可购买验收。
- 截图记录：
  - `docs/images/day-9/public-list-success.png`
  - `docs/images/day-9/public-detail-success.png`
  - `docs/images/day-9/sku-availability-success.png`
  - `docs/images/day-9/sku-availability-missing.png`
  - `docs/images/day-9/public-detail-after-unpublish.png`
  - `docs/images/day-9/sku-availability-after-unpublish.png`
  - `docs/images/day-9/maven-compile-success.png`
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 10、6.2 消费者公开接口。
  - `06-每日推进看板与任务安排.md` 的每日任务、验收和 Git 提交规则。

### 侧边任务/对话补充记录

- `=` 和 `==` 的区别：
  - 疑惑点：`int safePage = page == null || page < 1 ? 1 : page;` 里为什么既有 `=` 又有 `==`。
  - 最后理解：`=` 是赋值，把右边结果放进变量；`==` 是判断是否相等，用来判断 `page` 是否为 `null`。
  - 后续会用到哪里：所有参数兜底、状态判断、条件更新都要区分“赋值”和“比较”。
- `@PathVariable` 必须对应 URL 模板：
  - 疑惑点：方法参数写了 `@PathVariable Long storeId`，但接口仍然返回系统异常。
  - 最后理解：URL 模板里必须有 `{storeId}`，例如 `@GetMapping("/api/public/stores/{storeId}/products")`，Spring 才知道从哪里取值。
  - 后续会用到哪里：商品详情、SKU 可售状态、购物车项、订单详情等路径参数接口都一样。
- `BaseVO` 与 `DetailVO` 的分工：
  - 疑惑点：为什么不直接在 SPU 查询结果上加 `List<PublicSkuVO> skus`。
  - 最后理解：SPU SQL 是平铺行结果，`skus` 来自另一张表的多行查询；Mapper 接平铺结果，Service 组装嵌套详情。
  - 后续会用到哪里：订单详情里的订单项、购物车列表里的 SKU 信息也可能使用同样的“基础信息 + 子列表”组装方式。
- 外键和接口嵌套返回不是一回事：
  - 疑惑点：是否给 SPU/SKU 建外键就能让详情自动返回 `skus`。
  - 最后理解：外键约束数据完整性，不能替代查询；MyBatis 不会因为外键存在就自动填充 Java 对象里的 List。
  - 后续会用到哪里：后面建购物车和订单表时，外键是否使用要单独讨论，但接口返回仍要靠查询和组装。

### 今天还没理解透

- MyBatis 的 `@Results`、`@Many` 或 XML 嵌套映射暂时没展开，当前先用两次查询和 Service 组装保持清晰。
- 公开接口当前没有店铺表详情展示，只用 `tenantId` 作为 `storeId`，后续前端或店铺页可能要补店铺公开信息。
- 正式 ID 方案仍未替换，当前商品和 SKU 仍沿用 `System.currentTimeMillis()`。

### 明天遇到再补

- 根据实际安排选择进入步骤 11 极简商品管理页面，或先继续后端步骤 12：购物车表与接口。
- 如果继续后端，购物车加入 SKU 前必须复用今天形成的可售校验思路：SPU 上架、SKU 上架、库存大于 0。

## Day 9 加餐：2026-07-28

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`、`04-Vue前端开发链.md`
- 当前步骤：步骤 11 第一版：极简商品管理页面；对应前端步骤 31、32、33 起步
- 今日目标：创建 Vue 3 前端基础壳，完成商家登录态公共能力，并接入真实商家商品列表接口。

### 今天学了什么

- Vue 单页应用启动链路：
  - 它解决什么问题：Vue 项目不是多个独立 HTML 页面，而是先加载一个 `index.html`，再由 `main.ts` 启动应用，`App.vue` 作为根组件，`router-view` 根据地址显示不同页面组件。
  - 我现在会用到哪里：后续登录页、商品管理页、消费者商品详情、购物车和订单页都会通过 `router/index.ts` 映射到不同 `View.vue`。
- Vue Router：
  - 它解决什么问题：把 `/merchant/login`、`/merchant/products`、`/403` 等前端地址和对应 `.vue` 页面组件关联起来。
  - 我现在会用到哪里：商家后台和消费者端都需要多个页面；路由守卫还会根据登录态决定是否放行。
- `ref` 与 `.value`：
  - 它解决什么问题：`ref(0)` 创建的是 Vue 会追踪的响应式数据盒子，真实值在 `.value` 里；值变化后页面会自动更新。
  - 我现在会用到哪里：登录表单、搜索关键字、商品列表、加载状态、错误提示都会用 `ref` 保存页面状态。
- 列表渲染、条件显示和表格：
  - 它解决什么问题：`v-for` 把商品数组渲染成多行，`:key="product.id"` 帮 Vue 识别每一行，`v-if/v-else` 根据库存或状态显示不同文案，`:disabled` 动态禁用按钮。
  - 我现在会用到哪里：商品列表、购物车列表、订单项列表和 SKU 列表都是同样思路。
- Axios、Vite 代理与 Pinia 登录态：
  - 它解决什么问题：Axios 统一请求后端，Vite 把前端 `/api` 转发到后端 `localhost:8080`，Pinia 保存 token 和当前用户，刷新后再用 `/api/auth/me` 恢复用户。
  - 我现在会用到哪里：所有后端接口都通过 `api/*.ts` 封装，所有需要登录的商家页面都从 `authStore` 读取登录态。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 不理解为什么 Vue 项目需要 `vue-router` | 以为 HTML 里本来就能定义多个地址 | 理解为传统 HTML 是多个文件，Vue 单页应用只有一个 `index.html`，需要 Router 根据 URL 切换组件 | 是 |
| 不理解 `router-view` 为什么必要 | 已经在 `router/index.ts` 里写了路径和组件映射，但不清楚为什么还要渲染到 `App.vue` | 理解为 Router 决定显示谁，`router-view` 决定显示在哪里；`App.vue` 可以保留公共布局，页面内容在 `router-view` 处切换 | 是 |
| 误删了 `main.ts` 和 `App.vue` | 清理 Vite 默认演示代码时把启动入口也删掉 | 重新补回 `main.ts` 和 `App.vue`，理解 `index.html -> main.ts -> App.vue` 是必需启动链路 | 是 |
| 不理解 `clickCount.value` | 写 `const clickCount = ref(0)` 和 `clickCount.value = clickCount.value + 1` 时困惑 `.value` | 理解为 `ref` 是响应式盒子，脚本里取值和赋值要用 `.value`，模板里 Vue 会自动拆开 | 是 |
| 输入框写了但页面不明显 | `HomeView.vue` 里已有 `<input v-model="keyword">`，但浏览器中看不到明显输入框 | 给 `input` 添加宽度、内边距、边框和字号后显示正常 | 是 |
| 表格操作列错位 | 给每行加了「减库存」按钮，但表头仍只有 3 列 | 给 `thead` 补上 `<th>操作</th>`，理解表头 `th` 数量应和每行 `td` 对齐 | 是 |
| 登录失败提示像账号密码错误 | 后端未启动时，登录页只显示统一错误文案 | 通过前端终端看到 Vite 代理 `ECONNREFUSED`，确认真实原因是后端 8080 没连上；启动后端后登录成功 | 是 |
| 误删了 `LoginView.vue` 内容 | 修改 redirect 逻辑时把登录页代码删空且无法撤回 | 重新补回完整 `LoginView.vue`，并加入 `redirect` 登录后回跳逻辑 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/auth/login` 经 Vite 代理请求后端成功，返回 `accessToken` 和用户 `merchant_a_admin`。
  - `GET /api/auth/me` 刷新页面后返回当前用户：`id=2`、`username=merchant_a_admin`、`userType=MERCHANT_ADMIN`、`tenantId=1001`。
  - `GET /api/merchant/products?page=1&size=10` 在商家商品管理页成功返回真实商品列表。
- 失败过的接口：
  - 后端未启动时，前端请求 `POST /api/auth/login` 出现 Vite 代理 `ECONNREFUSED`，不是账号密码错误。
- 浏览器看到的结果：
  - `/` 和 `/merchant/products` 可以显示不同 Vue 页面。
  - 登录成功后 `localStorage` 中出现 `access_token`。
  - 删除 `access_token` 后访问 `/merchant/products` 自动跳到 `/merchant/login?redirect=/merchant/products`。
  - 登录成功后按 `redirect` 回到 `/merchant/products`。
  - `/403` 页面显示无权限提示。
  - 商家商品管理页显示真实商品「蓝牙耳机」、状态和 SKU 数。
- 截图记录：
  - `docs/images/day-9-加餐/加餐-未登录跳转登录页.png`
  - `docs/images/day-9-加餐/加餐-登录后回到商品页并保存token.png`
  - `docs/images/day-9-加餐/加餐-403无权限页面.png`
  - `docs/images/day-9-加餐/加餐-商家商品列表真实数据.png`
  - `docs/images/day-9-加餐/加餐-前端构建成功.png`
- 关键修改：
  - 新建 Vue 3 + Vite + TypeScript 前端工程。
  - 新增 `src/router/index.ts`、`HomeView.vue`、`LoginView.vue`、`ProductListView.vue`、`ForbiddenView.vue`。
  - 新增 `src/api/http.ts`、`src/api/auth.ts`、`src/api/product.ts`。
  - 新增 `src/stores/auth.ts` 保存 token、当前用户、登录、退出和 `/auth/me` 恢复逻辑。
  - `vite.config.ts` 配置 `/api` 代理到 `http://localhost:8080`。
  - `App.vue` 在 `onMounted` 调用 `authStore.loadCurrentUser()`。
- 验证记录：
  - `npm.cmd run dev` 前端启动正常。
  - `npm.cmd run build` 构建通过，Vite 输出 `dist/index.html` 和打包资源。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 11。
  - `04-Vue前端开发链.md` 步骤 31、32、33。
  - `06-每日推进看板与任务安排.md` 的加餐任务安排、验收和 Git 提交规则。

### 侧边任务/对话补充记录

- React 和 Vue 怎么选：
  - 疑惑点：不确定当前项目用 React 还是 Vue 更好。
  - 最后理解：当前知识文档和学习路线都围绕 Vue 3，且商家后台表单、表格、分页较多，Vue 3 + Element Plus 更适合先做出项目闭环；React 可以项目后再补。
  - 后续会用到哪里：前端路线继续按 `04-Vue前端开发链.md` 推进，不中途切 React。
- Vite 生成的一堆文件分别是什么：
  - 疑惑点：`web` 下突然出现很多配置和目录，不知道哪些能删。
  - 最后理解：`index.html`、`main.ts`、`App.vue` 是启动链路；`src` 放业务代码；`package.json`、`vite.config.ts`、`tsconfig*.json` 是工具配置，先留在根目录符合默认约定。
  - 后续会用到哪里：清理默认演示代码时只删演示素材，不删启动入口和工具配置。
- 配置文件能不能放到一个文件夹：
  - 疑惑点：根目录配置文件看起来很乱，想集中收纳。
  - 最后理解：Vite 和 TypeScript 默认从根目录找配置；移动到 `config/` 后需要改命令和引用路径，初学阶段收益小、坑多。
  - 后续会用到哪里：当前保留 Vite 默认布局，把业务代码组织在 `src/api`、`src/router`、`src/stores`、`src/views`。
- 前端权限和后端权限的关系：
  - 疑惑点：路由守卫拦截后，是不是就安全了。
  - 最后理解：前端路由守卫只负责用户体验；真正安全仍靠后端 `/api/merchant/**` 的 401/403 和租户隔离。前端不能替代后端授权。
  - 后续会用到哪里：消费者访问商家页面、商家 A/B 数据隔离、订单和 Agent 工作台都要继续以后端权限为最终边界。

### 今天还没理解透

- `computed` 的缓存机制还没展开，只先理解为“根据响应式数据自动计算”。
- Axios 响应拦截器和 401 自动退出还没补，目前只做了请求前自动带 token。
- Element Plus 已安装但还没有正式替换原生表格和表单。

### 明天遇到再补

- 如果继续前端，补 Axios 响应拦截器、退出登录按钮、Element Plus 表格、商品新建/SKU/上下架操作。
- 如果回到后端，进入步骤 12：购物车表与接口，加入购物车前复用 SKU 可售校验思路。

## Day 10：2026-07-29

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 12：购物车表与接口
- 今日目标：创建购物车表，完成消费者加入购物车、查询购物车、修改数量、删除购物车项的后端闭环。

### 今天学了什么

- 购物车里的三个 ID：
  - 它解决什么问题：`cart_item.id` 用来定位购物车里的某一行记录；`consumerId` 用来标识当前消费者；`skuId` 用来标识具体商品规格。
  - 我现在会用到哪里：`PUT /api/cart/items/{id}` 和 `DELETE /api/cart/items/{id}` 用的是 `cart_item.id`，不是 `skuId`；查询和防越权时必须带 `consumerId`。
- `consumer_id + sku_id` 唯一约束：
  - 它解决什么问题：同一个消费者重复加入同一个 SKU 时，不会产生多行重复购物车项，而是在原有行上合并数量。
  - 我现在会用到哪里：`POST /api/cart/items` 先用 `consumerId + skuId` 查是否已有记录，有则 `increaseQuantity`，没有才 `insert`。
- SKU 可售校验复用：
  - 它解决什么问题：加入购物车和修改购物车数量前，都要确认商品仍然可购买，且数量不超过可售库存。
  - 我现在会用到哪里：购物车 Service 调用 `PublicProductMapper.selectSkuAvailability`，复用公开商品侧形成的 `purchasable` 和 `availableStock` 判断。
- `id + consumerId` 防越权：
  - 它解决什么问题：不能只按 `cart_item.id` 修改或删除，否则可能出现用户 A 操作用户 B 购物车记录的风险。
  - 我现在会用到哪里：`selectByIdAndConsumerId`、`updateQuantityByIdAndConsumerId`、`deleteByIdAndConsumerId` 都同时带 `id` 和当前消费者 ID。
- 401 与 403 的边界：
  - 它解决什么问题：没登录是 401；登录了但账号类型不对，例如商家 token 访问消费者购物车，是 403。
  - 我现在会用到哪里：消费者购物车接口统一调用 `CurrentUser.requiredConsumerId()`，账号类型不对时抛 `AccessDeniedException("不是消费者账号")`。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| Flyway 明明执行成功，但 DataGrip 左侧表列表一度看不到 `cart_item` | 后端启动日志显示 v4 迁移成功，`SHOW TABLES` 能查到 `cart_item`，但左侧树还只有 5 张表 | 理解为 DataGrip 左侧对象树有缓存，需要刷新 schema；最终 DataGrip 和 `SHOW TABLES` 都确认表存在 | 是 |
| 不清楚 `skuId`、`consumerId`、`id` 三个 ID 的区别 | 写购物车 DTO、Entity、Mapper 时三个 ID 同时出现 | 明确为：`skuId` 是商品规格，`consumerId` 是消费者，`cart_item.id` 是购物车项记录本身 | 是 |
| 商品明明 SKU 是 `ON_SALE`，加入购物车却返回“商品不可购买” | 查询 SKU 时看到 `sku_status=ON_SALE` 和库存 50，但接口返回 409 | 继续 join 查询 SPU，发现 `spu_status=OFF_SALE`；购物车校验必须同时看 SKU 和 SPU，商家上架 SPU 后加入成功 | 是 |
| 商家 token 查询购物车返回 `code: 500` 系统异常 | 验收“商家 token 应该返回 403”时，接口返回系统异常 | 发现全局异常处理器导入了错误的 `java.nio.file.AccessDeniedException`；改为 `org.springframework.security.access.AccessDeniedException` 后返回 `code: 403` 和 `不是消费者账号` | 是 |
| 不确定查询购物车列表是不是今天任务 | 完成加入购物车后，下一步出现 `GET /api/cart/items` | 确认为步骤 12 的自然范围：只做新增无法验收购物车状态，查询列表是购物车最小闭环的一部分 | 是 |
| 不确定修改数量和删除任务会不会太多 | 查询列表完成后准备继续 `PUT` 和 `DELETE` | 理解为两者复用已有 Mapper/Service/Controller 模式，任务量不大，并且是购物车接口最小闭环必须能力 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/cart/items` 消费者 token 加入上架 SKU 成功，返回 `id=1785313298146`、`skuId=1784970220075`、`quantity=1`。
  - 重复 `POST /api/cart/items` 同一 SKU 成功合并数量，返回同一个购物车项 `id=1785313298146`，`quantity=2`。
  - `GET /api/cart/items` 消费者 token 返回购物车列表。
  - `PUT /api/cart/items/1785313298146`，Body 为 `{"quantity":3}`，返回 `quantity=3`。
  - `DELETE /api/cart/items/1785313298146` 删除成功，随后 `GET /api/cart/items` 返回空数组。
- 失败和边界接口：
  - 不带 token 访问 `GET /api/cart/items` 返回 HTTP 401。
  - 商家 token 访问 `GET /api/cart/items` 返回 `code: 403` 和 `不是消费者账号`。
  - `POST /api/cart/items` 在 SPU 下架时返回 `code: 409` 和 `商品不可购买`。
  - `quantity=0` 返回 `code: 400` 和 `数量必须大于等于1`。
  - `quantity=999` 返回 `code: 409` 和 `库存不足`。
  - 重复删除已删除的购物车项返回 `code: 404` 和 `购物车项不存在`。
- DataGrip 看到的数据：
  - `flyway_schema_history` 中版本 4 的脚本 `V4__add_cart_item.sql` 成功执行。
  - `SHOW TABLES` 可见 `cart_item`。
  - 重复加入同一 SKU 后，`cart_item` 只有一行，`quantity=2`。
  - 删除购物车项后，购物车列表为空。
- 关键修改：
  - 新增 `V4__add_cart_item.sql`，创建 `cart_item` 表、唯一约束和索引。
  - 新增 `cart` 模块的 controller、dto、entity、mapper、service、vo。
  - `CurrentUser` 新增 `requiredConsumerId()`，用于消费者接口身份校验。
  - `GlobalExceptionHandler` 增加 Spring Security `AccessDeniedException` 处理，账号类型不对返回 403。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成新增、重复新增、查询、修改、删除、重复删除、无 token、商家 token、参数错误、库存不足和商品不可购买验收。
- 截图记录：
  - `docs/images/day-10/flyway-v4-migration-success.png`
  - `docs/images/day-10/cart-item-table-created.png`
  - `docs/images/day-10/cart-add-success.png`
  - `docs/images/day-10/cart-list-success.png`
  - `docs/images/day-10/cart-update-success.png`
  - `docs/images/day-10/cart-update-validation-400.png`
  - `docs/images/day-10/cart-update-stock-409.png`
  - `docs/images/day-10/cart-delete-success.png`
  - `docs/images/day-10/cart-list-empty-after-delete.png`
  - `docs/images/day-10/cart-delete-missing-404.png`
  - `docs/images/day-10/cart-merchant-token-403.png`
  - `docs/images/day-10/cart-product-not-purchasable-409.png`
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 12。
  - `06-每日推进看板与任务安排.md` 的每日任务、验收、侧边记录和 Git 提交规则。

### 侧边任务/对话补充记录

- 误改前端文件和 `dist` 删除：
  - 疑惑点：发现前端文件有很多地方修改，不确定是不是助手误改；后来删除了 `dist` 文件夹。
  - 最后理解：`dist` 是前端构建产物，可以删除并通过重新 build 生成；真正要保留的是源码和配置。恢复版本时保留了当天新增的协作规则。
  - 后续会用到哪里：前端构建产物不要当成主线代码学习对象，Git 也不应该提交 `dist`。
- 今天新增协作规则：
  - 疑惑点：恢复版本前，希望先记住“前端由助手直接做，后端继续带着做”的规则。
  - 最后理解：规则 10 已写入 `docs/collaboration-rules.md`；以后 Vue 页面由助手直接实现，后端代码继续由用户跟着写。
  - 后续会用到哪里：每日任务安排时，前端实现不占用户主要学习任务；后端仍按 Controller/Service/Mapper/DTO 分步学习。
- `SHOW TABLES` 与 DataGrip 左侧树不一致：
  - 疑惑点：SQL 结果里有 `cart_item`，但左侧树里看不到。
  - 最后理解：SQL 查询结果以数据库真实状态为准；左侧树可能需要刷新 schema。
  - 后续会用到哪里：Flyway 建表、字段变更或索引变更后，如果图形界面没刷新，先用 SQL 直接确认。
- 商家 token 为什么不能查购物车：
  - 疑惑点：商家也是登录用户，为什么访问购物车不是空列表而是 403。
  - 最后理解：购物车属于消费者业务域，商家账号没有消费者身份；返回 403 能更清楚表达“你登录了，但不是这个接口需要的角色”。
  - 后续会用到哪里：订单、支付、商家后台、消费者前台都要区分账号类型，不能只判断“是否登录”。
- 修改数量和新增数量的区别：
  - 疑惑点：Mapper 里 `increaseQuantity` 和 `updateQuantityByIdAndConsumerId` 看起来都在改数量。
  - 最后理解：新增同 SKU 时是“在原数量上加本次数量”；购物车页面修改数量时是“直接设置成用户输入的目标数量”。
  - 后续会用到哪里：购物车页的加减按钮、数量输入框和订单确认前库存校验都要区分这两种语义。

### 今天还没理解透

- 购物车列表暂时只返回基础字段，还没有扩展为包含 SKU 名称、价格、图片、可售状态的展示型 VO。
- 正式 ID 生成方案还没替换，目前仍使用 `System.currentTimeMillis()` 临时生成。
- 购物车加入和修改数量还没有加事务，后续做订单和库存扣减时需要系统学习事务与并发控制。

### 明天遇到再补

- 进入步骤 13 普通订单时，重点关注“购物车项 -> 订单项”的数据复制、库存扣减和订单状态流转。
- 如果先补前端购物车页，需要把后端购物车列表 VO 扩展到足够展示商品名称、价格和可售状态。

## Day 11：2026-07-30

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 13：先画清订单状态和建表
- 今日目标：理解普通订单状态机，并创建 `commerce_order`、`commerce_order_item` 两张订单表。

### 今天先理解订单状态机

```text
PENDING_PAYMENT --支付成功--> PAID --商家发货--> SHIPPED --确认收货--> COMPLETED
       |
       +--超时关闭--> CLOSED

PAID --申请售后--> AFTER_SALE
```

- `PENDING_PAYMENT`：待支付，订单已经创建，库存已被锁定，但钱还没付。
- `PAID`：已支付，说明用户完成模拟支付，锁定库存会被移出。
- `SHIPPED`：商家已发货。
- `COMPLETED`：消费者确认收货，订单完成。
- `CLOSED`：待支付订单超时关闭，后面要释放锁定库存。
- `AFTER_SALE`：已支付后进入售后流程。

### 我现在要先记住

- 订单状态不能乱跳，比如 `PENDING_PAYMENT` 不能直接变成 `COMPLETED`。
- 历史订单要保存商品名称和价格快照，不能每次查订单时再读取当前商品表。
- 今天只建表，不写下单接口；下单事务放到步骤 14。

### 今天学了什么

- 订单主表与订单明细表：
  - 它解决什么问题：一笔订单有整体信息，也可能包含多个商品明细；主表保存订单整体，明细表保存每个 SKU。
  - 我现在会用到哪里：步骤 14 创建订单时，会先写 `commerce_order`，再为每个购买的 SKU 写 `commerce_order_item`。
- `order_no`：
  - 它解决什么问题：`id` 是数据库内部主键，`order_no` 是给用户、客服、支付回调和订单搜索使用的业务订单号。
  - 我现在会用到哪里：后续下单成功后返回订单号，模拟支付和订单查询也可以围绕订单号或订单 ID 展开。
- 订单项快照：
  - 它解决什么问题：历史订单不能因为商品后来改名、改价而变化，所以订单项要保存下单那一刻的商品名和售价。
  - 我现在会用到哪里：步骤 14 从数据库读取 SKU 真实价格后，把 `sku_name_snapshot` 和 `sale_price` 写入订单项。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 不理解为什么订单要拆成两张表 | 建完 `commerce_order` 和 `commerce_order_item` 后，觉得一笔订单为什么不能只放一张表 | 理解为：主表负责订单整体状态、金额、归属；明细表负责这笔订单里买了哪些 SKU。一个订单可以买多个 SKU，所以一张主表记录会对应多条明细记录 | 是 |

### 重要记录

- 成功的数据库迁移：
  - Flyway 从版本 4 迁移到版本 5。
  - `V5__add_commerce_order.sql` 执行成功。
- DataGrip 看到的数据：
  - `SHOW TABLES` 可见 `commerce_order` 和 `commerce_order_item`。
  - `flyway_schema_history` 最新版本为 `5`，脚本为 `V5__add_commerce_order.sql`。
  - `DESC commerce_order` 可见 `id`、`order_no`、`tenant_id`、`consumer_id`、`status`、`total_amount`、`expire_at`、`created_at`、`updated_at`。
  - `DESC commerce_order_item` 可见 `id`、`order_id`、`sku_id`、`sku_name_snapshot`、`sale_price`、`quantity`、`created_at`。
- 关键修改：
  - 新增 `V5__add_commerce_order.sql`，创建订单主表和订单明细表。
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 13、7.1、7.2。

### 侧边任务/对话补充记录

- 为什么步骤 13 先写学习日志：
  - 疑惑点：今天还没写代码，为什么先写 `learning-log.md`。
  - 最后理解：步骤 13 的重点是先确认订单状态机和订单快照规则，再建表；状态和快照想清楚后，后续下单、支付、关单才不容易返工。
  - 后续会用到哪里：步骤 14 的下单事务、步骤 15 的模拟支付和后续超时关单都依赖这套状态机。

### 今天还没理解透

- 订单号的正式生成方案还没实现，后续会先用日期 + 随机数或临时方案，再统一替换正式 ID/订单号方案。
- 下单事务还没开始，库存条件更新、锁定库存和事务回滚会放到步骤 14 系统学习。

### 明天遇到再补

- 进入步骤 14：实现 `POST /api/orders`，重点关注“购物车项 -> 订单项”的数据复制、后端重新读取价格、库存条件扣减和事务回滚。

## Day 11 加餐：步骤 14 普通下单事务起步

### 今天额外推进到哪里

- 在步骤 13 已完成并提交后，继续开启了步骤 14 的前半段。
- 已新增订单创建请求 `CreateOrderRequest`，使用 `List<Long> cartItemIds` 表示一次可以结算多个购物车项。
- 已新增订单创建返回 `CreateOrderVO`，用于返回 `orderId`、`orderNo`、`status`、`totalAmount`、`expireAt`。
- 已新增订单实体 `CommerceOrder` 和订单明细实体 `CommerceOrderItem`。
- 已新增 `CommerceOrderMapper` 和 `CommerceOrderItemMapper`，先支持订单主表和订单明细单条插入。
- 已新增 `OrderSkuSnapshotVO`，用于下单前把购物车项、SKU、SPU 的关键信息合并查询出来。
- 已扩展 `ProductSkuMapper`：
  - `selectOrderSkuSnapshots(...)`：根据当前消费者和购物车项列表查询下单快照。
  - `lockStock(...)`：通过 `available_stock >= quantity` 做条件扣库存，并把库存转入 `locked_stock`。
- 已新增 `V6__add_auto_increment_to_order_tables.sql`，修复订单表和订单明细表的主键自增策略。
- 已起草 `OrderService.createOrderVO(...)` 的事务骨架，目前完成消费者身份校验、购物车项快照查询、购物车项存在性校验、同商家校验、上下架校验和库存预校验；完整下单流程明天继续。

### 今天额外遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已理解 |
|---|---|---|---|
| `INSERT` 语句为什么没有 `FROM` | 写 `CommerceOrderMapper.insert` 时对 SQL 结构产生疑问 | 理解为 `INSERT INTO 表名 (...) VALUES (...)` 不需要 `FROM`；`FROM` 常用于 `SELECT` 和 `DELETE` | 是 |
| Mapper 里提示主键没有默认值 | `@Options(useGeneratedKeys = true)` 和表结构 `id BIGINT PRIMARY KEY` 不匹配 | 新增 V6，把 `commerce_order.id` 和 `commerce_order_item.id` 改成 `AUTO_INCREMENT` | 是 |
| Flyway V5 checksum mismatch | V5 已执行后，本地文件 checksum 和数据库历史记录不一致 | 在本地开发库中等价执行 repair：更新 `flyway_schema_history` 的 V5 checksum，然后继续执行 V6 | 是 |
| `CommerceOrderItemMapper` 放错包 | 文件最初建到了 `cart.mapper` | 移动到 `order.mapper`，因为它操作的是订单明细表，不属于购物车模块 | 是 |
| 注解 SQL 里能不能写 `<script>` / `<foreach>` | 批量按 `cartItemIds` 查询下单快照时 IDEA 有提示 | 确认 MyBatis 注解支持动态 SQL；多参数方法必须配合 `@Param` 使用 | 是 |
| `CurrentUser` 找不到 Bean | `OrderService` 构造注入了 `CurrentUser` | 理解 `CurrentUser` 是静态工具类，不是 Spring Bean；改为 `CurrentUser.requiredConsumerId()` 静态调用 | 是 |

### 明天继续

- 继续完成 `OrderService.createOrderVO(...)`：
  - 计算 `totalAmount`
  - 生成 `orderNo`
  - 插入 `commerce_order`
  - 条件扣库存
  - 插入 `commerce_order_item`
  - 删除已结算购物车项
- 新增 `OrderController`，暴露 `POST /api/orders`。
- 编译并用 Apifox 验证消费者从购物车创建普通订单。

## Day 12：2026-07-31

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 14：实现普通下单事务
- 今日目标：完成 `POST /api/orders`，从购物车创建待支付订单，并验证库存锁定、订单明细和失败回滚。

### 今天学了什么

- `OrderService` 的职责：
  - 它解决什么问题：下单不是单表操作，而是要同时读购物车与商品快照、写订单主表、锁库存、写订单明细、删除购物车项。
  - 我现在会用到哪里：步骤 15 模拟支付、后续超时关单和促销订单都会继续围绕订单状态与库存账展开。
- `@Transactional`：
  - 它解决什么问题：让订单主表、库存锁定、订单明细和购物车删除处在同一个事务里，中间任何一步失败都整体回滚。
  - 我现在会用到哪里：库存不足、订单项写入失败、后续支付状态变更和关单释放库存都需要事务保证数据不留半截。
- 条件扣库存：
  - 它解决什么问题：`UPDATE product_sku SET available_stock = available_stock - quantity, locked_stock = locked_stock + quantity WHERE available_stock >= quantity` 让库存不足时更新失败，避免库存扣成负数。
  - 我现在会用到哪里：普通下单、模拟支付和后续限量促销最终扣减都要用受影响行数判断业务是否成功。
- 订单明细 `CommerceOrderItem`：
  - 它解决什么问题：订单主表只记录订单整体，订单明细记录这笔订单里买了哪些 SKU、买几个、下单时叫什么名字、下单时是多少钱。
  - 我现在会用到哪里：订单详情、商家订单列表、销量统计和 Agent 经营数据分析都会查询订单明细。
- 订单号生成：
  - 它解决什么问题：`orderNo` 是给用户、客服和支付流程看的业务编号，临时使用 `ORD + yyyyMMddHHmmss + 6 位随机数`。
  - 我现在会用到哪里：下单成功响应、模拟支付、订单查询都可以展示或定位业务订单号；正式方案后续再替换。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 不理解 `CommerceOrderItem` 的作用 | 看 `OrderService` 里把 `snapshot` 转成 `CommerceOrderItem` 的大段代码时 | 理解为订单明细表负责保存“这笔订单买了哪些 SKU”，并保存商品名和价格快照，避免商品以后改名/改价影响历史订单 | 是 |
| 不理解 `generateOrderNo()` | 看到 `LocalDateTime.now().format(...)` 和 `ThreadLocalRandom.current()` | 理解为 `DateTimeFormatter.ofPattern(...)` 把当前时间格式化成订单号需要的样子，`ThreadLocalRandom.current()` 生成当前线程使用的 6 位随机数 | 是 |
| 加入购物车返回 401 | Apifox 中误用 `DELETE /api/cart/items`，并且请求体里混淆了 SKU ID 和购物车项 ID | 改为 `POST /api/cart/items`，Body 使用真正的 `skuId=1784970220075`，成功生成购物车项 | 是 |
| DataGrip 执行 `UPDATE product_sku SET available_stock = 0` 后查询仍是 49 | 做下单前库存变更的失败验收时 | 发现 DataGrip 当前处于事务模式，更新需要提交；提交后查询可见 `available_stock=0` | 是 |
| 下单失败后要不要删除购物车项 | 库存不足时 `POST /api/orders` 返回 `商品库存不足` 后继续查购物车 | 理解为失败下单不能吞掉购物车项，用户可以等商家补库存后继续结算或自己删除 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/cart/items`，消费者 token，Body 为 `{"skuId":1784970220075,"quantity":1}`，返回购物车项 `id=1785483128179`。
  - `POST /api/orders`，Body 为 `{"cartItemIds":[1785483128179]}`，返回 `orderId=1`、`orderNo=ORD20260731153359385003`、`status=PENDING_PAYMENT`、`totalAmount=199.00`。
- 失败和边界接口：
  - `POST /api/cart/items`，Body 为 `{"skuId":1784970220075,"quantity":999}`，返回 `code=409` 和 `库存不足`。
  - 手动把 `available_stock` 改为 `0` 后，`POST /api/orders` 使用 `cartItemIds=[1785485017013]` 返回 `code=409` 和 `商品库存不足`。
- DataGrip 看到的数据：
  - `commerce_order` 只有一笔成功订单：`id=1`、`status=PENDING_PAYMENT`、`total_amount=199.00`。
  - `commerce_order_item` 中存在 `order_id=1`、`sku_id=1784970220075`、`sku_name_snapshot=白色 / 标准版`。
  - 成功下单后 SKU 库存为 `available_stock=49`、`locked_stock=1`。
  - 成功下单后购物车项 `1785483128179` 已删除。
  - 库存不足失败后没有新增订单，购物车项 `1785485017013` 仍保留。
  - 验收结束后已恢复 SKU 库存为 `available_stock=49`、`locked_stock=1`。
- 关键修改：
  - 补完 `OrderService.createOrderVO(...)` 的完整事务逻辑。
  - 新增 `OrderController`，暴露 `POST /api/orders`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 与 DataGrip 完成成功下单、库存不足、失败不新增订单、失败保留购物车项的验收。
- 截图记录：
  - `docs/images/day-12/cart-add-success.png`
  - `docs/images/day-12/order-create-success.png`
  - `docs/images/day-12/datagrip-order-created.png`
  - `docs/images/day-12/datagrip-order-item-created.png`
  - `docs/images/day-12/datagrip-stock-locked.png`
  - `docs/images/day-12/cart-overstock-409.png`
  - `docs/images/day-12/order-stock-insufficient-409.png`
  - `docs/images/day-12/datagrip-no-extra-order.png`
  - `docs/images/day-12/datagrip-cart-item-kept-after-failure.png`
  - `docs/images/day-12/datagrip-stock-restored.png`
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 14。
  - `02-交易库存限量促销开发链.md` 的库存账说明：`available_stock` 创建待支付订单时减少，`locked_stock` 创建待支付订单时增加。

### 侧边任务/对话补充记录

- 订单主表和订单明细表的关系：
  - 疑惑点：为什么 `CommerceOrderItem` 要单独创建，看起来像重复保存商品信息。
  - 最后理解：`commerce_order` 保存订单整体，`commerce_order_item` 保存订单里的商品清单；一笔订单可以有多个 SKU，所以主表一行会对应明细表多行。
  - 后续会用到哪里：订单详情、商家查看订单、销售统计都要依赖订单明细。
- `ThreadLocalRandom.current()` 和 `DateTimeFormatter.ofPattern(...)`：
  - 疑惑点：为什么生成订单号要这两个工具。
  - 最后理解：`DateTimeFormatter.ofPattern("yyyyMMddHHmmss")` 负责把当前时间变成紧凑字符串；`ThreadLocalRandom.current().nextInt(...)` 负责生成当前线程里的随机数，降低同一秒内订单号重复的概率。
  - 后续会用到哪里：临时订单号生成方案先服务步骤 14/15，后面再统一替换为正式 ID 或订单号方案。
- DataGrip 事务提交：
  - 疑惑点：`UPDATE` 执行后查询结果没有变化。
  - 最后理解：图形数据库工具可能处于手动事务模式，更新后需要点提交或执行 `COMMIT`，否则修改不会真正提交。
  - 后续会用到哪里：手动改库存、修测试数据、做账本验收时要注意提交事务。

### 今天还没理解透

- 当前订单号只是临时方案，还没有彻底解决高并发下绝对唯一的问题，后续需要正式 ID/订单号生成方案。
- 现在只完成下单锁库存，支付成功后如何从 `locked_stock` 移出还没做。
- 库存不足失败主要靠手工验收，后续可以补 Service 集成测试。

### 明天遇到再补

- 进入步骤 15：实现模拟支付和消费者订单查询。
- 重点关注：只能支付本人订单；只能支付 `PENDING_PAYMENT` 状态；支付成功后订单变为 `PAID`，并把 `locked_stock` 减少。

## Day 13：2026-08-01

### 今天对应任务

- 当前文档：`01-工程与基础业务开发链.md`
- 当前步骤：步骤 15：实现模拟支付和消费者订单查询
- 今日目标：完成 `POST /api/orders/{id}/mock-pay`，并实现消费者订单列表与订单详情查询。

### 今天学了什么

- 模拟支付的状态条件更新：
  - 它解决什么问题：只有本人且状态仍为 `PENDING_PAYMENT` 的订单才能被更新为 `PAID`。
  - 我现在会用到哪里：`CommerceOrderMapper.markPaidByIdAndConsumerId(...)` 使用 `WHERE id = ? AND consumer_id = ? AND status = 'PENDING_PAYMENT'`，第一次支付影响 1 行，第二次重复支付影响 0 行。
- 支付成功后的库存账：
  - 它解决什么问题：下单时 `available_stock` 已经减少并转入 `locked_stock`，支付成功只需要把锁定库存移出。
  - 我现在会用到哪里：`ProductSkuMapper.deductLockedStock(...)` 只减少 `locked_stock`，不回加 `available_stock`。
- 消费者订单查询的权限边界：
  - 它解决什么问题：消费者只能查自己的订单，不能靠前端传 `consumerId`。
  - 我现在会用到哪里：订单详情先用 `selectByOrderIdAndConsumerId(orderId, consumerId)` 查订单主表，确认归属后再查订单明细。
- 列表接口和详情接口的分工：
  - 它解决什么问题：订单列表只返回订单主信息，避免每条订单都查明细导致接口变重；订单详情再返回 `items`。
  - 我现在会用到哪里：`GET /api/orders` 的 `items=[]`，`GET /api/orders/{id}` 才返回 `OrderItemVO` 明细。
- VO 与查询结果对象：
  - 它解决什么问题：不是所有放在 `vo` 包里的对象都一定是前端返回对象，早期项目里可能混有 Mapper 查询结果承载对象。
  - 我现在会用到哪里：`OrderSkuSnapshotVO` 实际是下单前查询结果，用来做业务判断；`OrderItemVO` 和 `OrderDetailVO` 才是这次订单查询接口的返回 VO。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 误以为两次模拟支付都成功 | Apifox 两次请求 HTTP 状态都显示 200 | 区分 HTTP 状态和业务状态：第一次响应体 `code=0` 是成功；第二次响应体 `code=409` 是重复支付失败，HTTP 200 只是说明后端正常返回了统一 JSON | 是 |
| 不清楚为什么 `OrderSkuSnapshotVO` 不能复用为订单详情返回 | 设计消费者订单详情 VO 时，发现已有下单快照对象字段也包含 SKU 名、价格和数量 | 理解为 `OrderSkuSnapshotVO` 来源于购物车、SKU、SPU 的下单前查询，包含库存和上下架状态；订单详情应来源于 `commerce_order` 和 `commerce_order_item`，展示历史订单快照 | 是 |
| 疑惑“下单前快照为什么叫 VO” | 追问 `OrderSkuSnapshotVO` 既然不是前端展示对象，为什么命名为 VO | 理解为这是早期命名不严谨，它更像 Mapper 查询结果承载对象；今天先不重命名，避免牵扯已有代码，后续重构可改为 `OrderSkuSnapshotRow` 一类名称 | 是 |
| 不清楚几个 Mapper 查询的区别 | 新增 `selectByOrderId`、`selectItemVOByOrderId`、`selectByConsumerId`、`selectByOrderIdAndConsumerId` 后容易混 | 按用途区分：主表 Mapper 查列表和详情归属；明细 Mapper 一个给支付扣锁定库存用，一个给订单详情展示用 | 是 |

### 重要记录

- 成功的接口：
  - `POST /api/orders/1/mock-pay`，消费者 token，第一次返回 `code=0`。
  - `GET /api/orders`，消费者 token，返回订单列表，包含 `id=1`、`status=PAID`、`totalAmount=199.00`，列表 `items=[]`。
  - `GET /api/orders/1`，消费者 token，返回订单详情，`items` 中包含 SKU `1784970220075`、`skuNameSnapshot=白色 / 标准版`、`salePrice=199.00`、`quantity=1`。
- 失败和边界接口：
  - `POST /api/orders/1/mock-pay` 第二次重复支付返回 `code=409` 和 `订单不存在或状态不允许支付`。
  - `GET /api/orders/1` 使用商家 token 返回 `code=403` 和 `不是消费者账号`。
- DataGrip 看到的数据：
  - `commerce_order.id=1` 的 `status=PAID`、`total_amount=199.00`。
  - `product_sku.id=1784970220075` 的 `available_stock=49`、`locked_stock=0`。
- 关键修改：
  - `CommerceOrderMapper` 新增订单状态条件更新、我的订单列表查询、我的订单详情主表查询。
  - `CommerceOrderItemMapper` 新增订单详情明细 VO 查询。
  - `ProductSkuMapper` 新增支付成功后扣减 `locked_stock` 的方法。
  - `OrderService` 新增 `mockPay(...)`、`listMyOrders()`、`getMyOrderDetail(...)`。
  - `OrderController` 新增 `POST /api/orders/{id}/mock-pay`、`GET /api/orders`、`GET /api/orders/{id}`。
  - 新增 `OrderItemVO` 和 `OrderDetailVO`。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成模拟支付、重复支付、消费者订单列表、消费者订单详情、商家越权访问失败验收。
  - DataGrip 完成订单状态和库存账验收。
- 截图记录：
  - `docs/images/day-13/mock-pay-success.png`
  - `docs/images/day-13/mock-pay-repeat-409.png`
  - `docs/images/day-13/datagrip-order-paid.png`
  - `docs/images/day-13/datagrip-stock-paid.png`
  - `docs/images/day-13/order-list-success.png`
  - `docs/images/day-13/order-detail-success.png`
  - `docs/images/day-13/merchant-order-detail-403.png`
- 参考资料：
  - `01-工程与基础业务开发链.md` 步骤 15。
  - `02-交易库存限量促销开发链.md` 的库存账说明：支付成功后 `locked_stock` 减少，`available_stock` 不回加。

### 侧边任务/对话补充记录

- HTTP 200 与业务 `code` 的区别：
  - 疑惑点：Apifox 两次请求都显示 HTTP 200，看起来像两次支付都成功。
  - 最后理解：本项目当前统一返回 JSON，HTTP 200 表示后端正常返回；业务是否成功看响应体里的 `code`。第一次 `code=0`，第二次 `code=409`，所以重复支付已经被拦住。
  - 后续会用到哪里：所有接口验收都要同时看 HTTP 状态和业务 `code/message`，特别是权限、库存不足、重复操作等业务失败。
- `OrderSkuSnapshotVO` 的命名边界：
  - 疑惑点：既然是下单前判断能不能生成订单，为什么叫 VO。
  - 最后理解：它现在更准确地说是 Mapper 查询结果承载对象，名字有历史遗留；真正返回给前端的订单查询对象是 `OrderDetailVO` 和 `OrderItemVO`。
  - 后续会用到哪里：后面如果整理包结构，可以把这类对象移到 `query` 或改名为 `OrderSkuSnapshotRow`，减少概念混淆。
- 多个 Mapper 查询方法的分工：
  - 疑惑点：`selectByOrderId`、`selectItemVOByOrderId`、`selectByConsumerId`、`selectByOrderIdAndConsumerId` 看起来都在查订单，容易混。
  - 最后理解：主表 Mapper 负责订单归属和整体信息；明细 Mapper 负责订单里的 SKU。Entity 查询服务内部业务，VO 查询服务接口返回。
  - 后续会用到哪里：商家订单列表、售后、统计分析和 Agent 经营数据查询都会继续用“主表确认边界，明细提供内容”的思路。

### 今天还没理解透

- 订单查询还没有分页，后续前端订单页面或订单数量变多时需要补分页参数。
- 当前模拟支付没有真实支付流水表，后续接真实支付或支付回调时需要增加支付单、回调幂等和审计记录。
- 订单超时关闭和库存释放还没做，后续进入步骤 17/18/20 时继续验证库存账和幂等。

### 明天遇到再补

- 优先进入步骤 16：补齐第一个完整页面闭环，让消费者能在页面完成浏览商品、购物车、提交订单、模拟支付和订单查询。
- 如果继续后端，则进入步骤 17：手工验证库存账，覆盖下单未支付、模拟支付、后续超时关闭三种路径。

## Day 14：2026-08-03

### 今天对应任务

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 17 起步：建立库存账本与并发基线。
- 今日目标：先完成库存账本的最小闭环，让普通下单和模拟支付都有库存流水记录。
- 说明：今天只完成步骤 17 的“库存流水账本”部分，并发基线测试尚未开始。

### 今天学了什么

- 库存流水：
  - 它解决什么问题：`product_sku.available_stock/locked_stock` 只表示当前库存余额，不能解释库存为什么变成这样；`inventory_movement` 记录每一次库存变化的原因、业务单号、变化量和变化后的余额。
  - 我现在会用到哪里：普通下单写 `ORDER_LOCK`，模拟支付写 `ORDER_PAID`。后续取消订单、超时关单、促销扣库存也要继续写流水。
- `ORDER_LOCK` 与 `ORDER_PAID`：
  - 它解决什么问题：把不同库存动作分清楚，便于后续对账。
  - 我现在会用到哪里：下单时 `available_change=-quantity`、`locked_change=quantity`；支付时 `available_change=0`、`locked_change=-quantity`。
- 库存流水唯一约束：
  - 它解决什么问题：避免同一业务动作重复记账。
  - 我现在会用到哪里：`UNIQUE KEY uk_inventory_business (business_type, business_no, sku_id)` 保证同一种业务动作、同一订单号、同一 SKU 只能有一条流水。例如同一订单同一 SKU 的 `ORDER_PAID` 不能写两次。
- 主表和明细表在支付里的分工：
  - 它解决什么问题：订单主表提供订单号、租户、状态等整体信息；订单明细提供买了哪些 SKU、每个 SKU 买几个。
  - 我现在会用到哪里：`mockPay(...)` 先查 `CommerceOrder` 拿 `orderNo/tenantId` 给流水用，再查 `CommerceOrderItem` 拿 `skuId/quantity` 扣锁定库存。
- 请求方法要匹配 Controller 注解：
  - 它解决什么问题：`@PostMapping("/api/orders/{id}/mock-pay")` 只接受 POST，不接受 GET。
  - 我现在会用到哪里：Apifox 调模拟支付时必须选 `POST`，否则不会进入目标接口逻辑。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 不清楚“库存记录”是什么 | 刚进入步骤 17，看到要新增 `inventory_movement` 表 | 理解为库存变化账本：`product_sku` 看当前余额，`inventory_movement` 看每一笔库存为什么变化 | 是 |
| 不理解为什么支付时查了订单主表后还要查订单明细 | `mockPay(...)` 里先查 `CommerceOrder`，又查 `CommerceOrderItem` | 理解为主表拿订单号和租户，明细拿 SKU 和数量；两者服务不同业务目的 | 是 |
| 注释“创建订单明细表”误导 | 看到 `commerceOrderItemMapper.selectByOrderId(orderId)` 前的注释 | 修正理解为“查询订单明细”，不是创建表也不是新增明细；支付需要通过明细知道扣哪个 SKU、扣几个 | 是 |
| 不理解唯一约束的作用 | 看到 `UNIQUE KEY uk_inventory_business (business_type, business_no, sku_id)` | 理解为数据库保险：同一业务类型、同一业务单号、同一 SKU 不能重复记账，防止重复支付或代码 bug 造成重复流水 | 是 |
| 模拟支付接口返回 `code=500` | Apifox 使用 `GET /api/orders/4/mock-pay` | 改成 `POST /api/orders/4/mock-pay` 后成功；后续可补 `HttpRequestMethodNotSupportedException` 让方法错误返回更清楚 | 是 |
| DataGrip 一开始只看到旧订单流水 | 支付 `orderId=4` 后仍用旧订单号查询库存流水 | 先查 `commerce_order` 找到当前订单的 `order_no`，再按正确 `business_no` 查询，看到 `ORDER_LOCK` 和 `ORDER_PAID` 两条 | 是 |

### 重要记录

- 成功的数据库迁移：
  - Flyway 从版本 6 迁移到版本 7。
  - `V7__add_inventory_movement.sql` 执行成功。
- 成功的接口：
  - `POST /api/cart/items`，消费者 token，Body 为 `{"skuId":1784970220075,"quantity":1}`，返回购物车项 `id=1785740389959`。
  - `POST /api/orders`，消费者 token，使用购物车项创建订单成功，示例订单 `orderId=3`、`orderNo=ORD20260803150038282245`、`status=PENDING_PAYMENT`。
  - `POST /api/orders/4/mock-pay`，消费者 token，返回 `code=0`。
- 失败和边界接口：
  - `GET /api/orders/4/mock-pay` 返回兜底 `code=500`，原因是请求方法错误，正确方法应为 POST。
  - 第二次 `POST /api/orders/4/mock-pay` 返回 `code=409` 和 `订单不存在或状态不允许支付`。
- DataGrip 看到的数据：
  - `SHOW TABLES LIKE 'inventory_movement'` 能看到库存流水表。
  - `DESC inventory_movement` 能看到库存流水字段、主键、索引和自增主键。
  - 某订单号下有两条库存流水：`ORDER_LOCK` 和 `ORDER_PAID`。
  - `ORDER_LOCK` 的 `available_change=-1`、`locked_change=1`。
  - `ORDER_PAID` 的 `available_change=0`、`locked_change=-1`。
  - 重复支付后同一订单号仍只有两条流水，没有第三条。
- 关键修改：
  - 新增 `server/src/main/resources/db/migration/V7__add_inventory_movement.sql`。
  - 新增 `InventoryMovement` 实体。
  - 新增 `InventoryMovementMapper.insert(...)`。
  - `ProductSkuMapper` 新增 `selectByIdAndTenantId(...)`，用于查询库存变化后的余额。
  - `OrderService.createOrderVO(...)` 在下单锁库成功后写入 `ORDER_LOCK` 流水。
  - `OrderService.mockPay(...)` 在支付扣锁定库存成功后写入 `ORDER_PAID` 流水。
  - 库存流水创建方法命名为 `createOrderLockMovement(...)` 和 `createOrderPaidMovement(...)`，避免 `Before/After` 概念模糊。
- 验证记录：
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 完成加购物车、创建订单、模拟支付、重复支付失败验收。
  - DataGrip 完成库存流水表结构、下单锁库流水、支付流水和重复支付不新增流水验收。
- 截图记录：
  - `docs/images/day-14/flyway-v7-migration-success.png`
  - `docs/images/day-14/inventory-table-exists.png`
  - `docs/images/day-14/inventory-table-desc.png`
  - `docs/images/day-14/cart-add-for-inventory-success.png`
  - `docs/images/day-14/order-create-with-lock-movement-success.png`
  - `docs/images/day-14/inventory-order-lock-movement.png`
  - `docs/images/day-14/mock-pay-order-paid-success.png`
  - `docs/images/day-14/inventory-order-lock-and-paid-movements.png`
  - `docs/images/day-14/mock-pay-repeat-409.png`
  - `docs/images/day-14/inventory-two-movements-confirmed.png`
- 参考资料：
  - `02-交易库存限量促销开发链.md` 步骤 17。
  - `01-工程与基础业务开发链.md` 步骤 14/15 的普通下单和模拟支付库存语义。

### 侧边任务/对话补充记录

- 提炼方法什么时候适合：
  - 疑惑点：库存流水对象可以提炼成方法，为什么订单明细对象的那段代码 IDEA 没主动提示。
  - 最后理解：IDEA 的灯泡不会对所有可提炼代码主动提示；可以手动选中后使用 `Ctrl + Alt + M`。库存流水构造是一组更集中的“创建对象并设置字段”，更容易被识别；订单明细那段混有创建对象、插入明细、删除购物车等多个动作，IDEA 不一定知道要提炼哪一部分。
  - 后续会用到哪里：只在能明显提高主流程可读性时提炼方法，避免过度拆分导致来回跳。
- `@Transactional` 和库存流水：
  - 疑惑点：库存流水插入失败时会不会留下订单或库存半截变化。
  - 最后理解：下单和支付方法都在 `@Transactional` 中，订单、库存变化、库存流水在同一个事务里；后续任一步抛异常都会整体回滚。
  - 后续会用到哪里：取消订单、超时关单、促销扣库存也要保持库存更新和流水写入同事务。

### 今天还没理解透

- 步骤 17 的并发基线还没做，当前只完成手工接口验收，还没有自动并发测试证明库存不会变成负数。
- 当前库存流水只有 `available` 和 `locked` 两类变化，没有单独 `sold_stock` 字段；支付后的“已售出”目前体现为从 `locked_stock` 移出，后续如果要做更完整账本可以再扩展。
- 请求方法错误现在被全局异常兜底为 `code=500`，后续可以补专门异常处理，让 `GET` 调 `POST` 接口时返回更清楚的 `405/请求方法不支持`。

### 明天遇到再补

- 继续步骤 17：补并发基线测试，验证多个请求同时下单时 `available_stock` 和 `locked_stock` 不为负，库存流水数量和订单数量一致。
- 可补小优化：请求方法错误的全局异常处理。

## Day 15：2026-08-04

### 今天对应任务

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 17 继续：建立并发基线。
- 今日目标：新增库存并发自动化测试，验证 `ProductSkuMapper.lockStock(...)` 的条件扣库存不会让可售库存变成负数。

### 今天学了什么

- JUnit 测试：
  - 它解决什么问题：不用每次手动用 Apifox 或 DataGrip 验证，测试代码可以自动执行动作并判断结果是否符合预期。
  - 我现在会用到哪里：`@Test` 标记一条测试用例，`assertNotNull(...)`、`assertEquals(...)` 用来表达“预期结果必须是什么”。
- `@SpringBootTest`：
  - 它解决什么问题：启动 Spring Boot 测试环境，让测试里可以注入真实 Mapper、读取配置并连接数据库。
  - 我现在会用到哪里：`InventoryConcurrencyTest` 需要真实调用 `ProductSkuMapper.lockStock(...)`，所以使用 `@SpringBootTest`。
- `JdbcTemplate.update(...)`：
  - 它解决什么问题：测试中可以直接执行准备数据和清理数据的 SQL，不必为了测试专门给业务 Mapper 增加重置库存方法。
  - 我现在会用到哪里：测试开始前把 SKU 重置为 `available_stock=10`、`locked_stock=0`；测试结束后也恢复成同样状态。
- `ExecutorService`、`Future` 与 `CountDownLatch`：
  - 它解决什么问题：把普通循环变成可重复的并发测试，尽量让多个线程同时执行扣库存。
  - 我现在会用到哪里：20 个任务先提交到线程池，每个任务等待在 `startLatch.await()`；主线程执行 `startLatch.countDown()` 后，线程一起继续调用 `lockStock(...)`。
- `@AfterEach`：
  - 它解决什么问题：每个 `@Test` 执行结束后自动做清理，避免测试污染开发数据库或影响后面的测试。
  - 我现在会用到哪里：`cleanUpStock()` 在测试结束后调用 `resetStock()`，把测试 SKU 恢复到 `10/0`。
- 完整下单链路并发测试：
  - 它解决什么问题：Mapper 层测试只能证明单条条件扣库存 SQL 不会超扣，完整链路测试要证明订单主表、库存流水、最终库存和购物车删除在并发下也能对齐。
  - 我现在会用到哪里：`concurrentCreateOrderShouldKeepOrderInventoryAndMovementConsistent()` 让 20 个消费者同时调用 `OrderService.createOrderVO(...)`，库存只有 10 时只允许 10 单成功。
- `Future<Boolean>`：
  - 它解决什么问题：线程池提交任务后不会立刻拿到业务结果，而是先拿到一张“未来结果小票”；等 `future.get()` 时再取出任务内部 `return true/false` 的结果。
  - 我现在会用到哪里：完整下单并发测试用 `List<Future<Boolean>>` 收集 20 个下单任务结果，再统计成功订单数。

### 今天遇到的问题

| 问题 | 出现场景 | 最后怎么解决 | 是否已彻底理解 |
|---|---|---|---|
| 不理解测试骨架和 `@Test` 的关系 | 刚创建 `InventoryConcurrencyTest` 时 | 理解为测试骨架是测试类外壳；`@Test` 标记的方法才会被 JUnit 自动执行 | 是 |
| `assertThat(...)` 冒红 | 使用 AssertJ 写断言时 IDEA 没有自动导入静态方法 | 改用 JUnit 自带的 `assertNotNull(...)` 和 `assertEquals(...)`，减少额外概念 | 是 |
| 不理解 `JdbcTemplate.update(...)` 后面为什么有两个参数 | SQL 中有两个 `?` 占位符 | 理解为参数按顺序填入 `?`：第一个是 `skuId`，第二个是 `tenantId`；同时用 `id + tenant_id` 能避免误改其他商家的 SKU | 是 |
| 不理解 `await()` 明明写在 `for` 里，为什么不是循环卡住 | 写并发任务时看到 `startLatch.await()` 在 `for` 循环代码块里 | 理解为 `for` 只是提交 20 个任务；`submit(() -> {...})` 内部代码由线程池线程执行，所以等待的是任务线程，不是主线程循环 | 是 |
| 测试第一次运行失败 | Maven/IDEA 启动 `@SpringBootTest` 时连接 MySQL 失败 | 在测试运行配置中补齐正确的 `MYSQL_ROOT_PASSWORD` 和 `JWT_SECRET` 后解决 | 是 |
| 测试通过但控制台有红色 Mockito/Byte Buddy 警告 | 测试结束前看到 JVM 动态 agent 加载提示 | 理解为这是测试库在新版 Java 下的警告，不是测试失败；退出码为 0 且左侧绿色表示通过 | 是 |
| 请求方法错误不应该走兜底 500 | 用 `GET /api/orders/4/mock-pay` 调用只支持 POST 的模拟支付接口 | 新增 `HttpRequestMethodNotSupportedException` 全局异常处理，返回 `code=405` 和清晰提示 | 是 |
| 完整链路并发测试第一次只成功 1 单 | `startLatch.countDown()` 和统计断言误写在 `for` 循环内部 | 理解为 `for` 循环应先提交 20 个任务，循环结束后再统一 `countDown()` 放行；不能每提交 1 个任务就开闸统计 | 是 |
| 不理解为什么还要查订单数和流水数 | `successCount` 已经等于 10 后，继续加数据库查询断言 | 理解为 `successCount` 只代表线程内方法返回成功；数据库查询能证明真实落库结果。完整链路要同时验证订单、流水、库存和购物车状态 | 是 |

### 重要记录

- 新增测试文件：
  - `server/src/test/java/org/example/merchant_ai_operation/inventory/InventoryConcurrencyTest.java`
- 新增异常处理：
  - `GlobalExceptionHandler` 增加 `handleHttpRequestMethodNotSupportedException(...)`。
  - 当接口只支持 `POST`，客户端误用 `GET` 时，返回 `code=405` 和 `请求方法不支持，请检查 GET/POST/PUT/DELETE 是否正确`。
- 测试逻辑：
  - 测试开始前将 SKU `1784970220075`、租户 `1001` 的库存重置为 `available_stock=10`、`locked_stock=0`。
  - 使用 20 个线程同时调用 `productSkuMapper.lockStock(TEST_SKU_ID, TEST_TENANT_ID, 1)`。
  - 断言成功扣库存次数为 10。
  - 断言并发后库存为 `available_stock=0`、`locked_stock=10`。
  - 使用 `@AfterEach` 在测试结束后恢复库存为 `available_stock=10`、`locked_stock=0`。
  - 加餐补充完整下单链路并发测试：准备 20 个测试消费者和 20 条购物车项，每个线程模拟一个消费者登录，并调用 `orderService.createOrderVO(new CreateOrderRequest(List.of(cartItemId)))`。
  - 完整链路测试断言：成功下单数为 10、测试订单数为 10、`ORDER_LOCK` 流水数为 10、最终库存为 `available_stock=0` 和 `locked_stock=10`、剩余购物车项为 10。
- 验证结果：
  - IDEA 中 `InventoryConcurrencyTest` 运行通过，2 个测试通过。
  - DataGrip 查询 `product_sku`，确认测试结束后 SKU `1784970220075` 恢复为 `available_stock=10`、`locked_stock=0`。
  - `mvnw -DskipTests compile` 编译通过。
  - Apifox 使用 `GET /api/orders/4/mock-pay` 验证返回 `code=405`，不再返回兜底 `code=500`。
- 截图记录：
  - `docs/images/day-15/inventory-concurrency-test-success.png`
  - `docs/images/day-15/mockito-agent-warning-not-failure.png`
  - `docs/images/day-15/datagrip-stock-restored-after-test.png`
  - `docs/images/day-15/mock-pay-get-method-405.png`
  - `docs/images/day-15/order-flow-concurrency-test-success.png`
- 参考资料：
  - `02-交易库存限量促销开发链.md` 步骤 17：并发普通下单不会让可售库存变成负数。

### 侧边任务/对话补充记录

- `CountDownLatch` 的“放行”到底放行什么：
  - 疑惑点：`await()` 写在 `for` 循环内部，看起来像 `countDown()` 会让循环继续执行。
  - 最后理解：`for` 循环属于主线程，负责把 20 个任务交给线程池；`await()` 在任务内部，由线程池线程执行。`countDown()` 放行的是已经停在 `await()` 的 20 个任务线程，不是让 `for` 循环重新执行。
  - 后续会用到哪里：支付和超时关单竞争、重复下单、促销抢购等并发测试，都需要这种“先到起跑线，再统一放行”的方式。
- 测试数据准备和清理的区别：
  - 疑惑点：既然测试开头已经重置库存，为什么测试结束后还要 `@AfterEach` 再重置一次。
  - 最后理解：测试开头的 SQL 是准备数据，保证测试从固定起点开始；`@AfterEach` 是清理数据，保证测试结束后不污染开发环境和后续测试。
  - 后续会用到哪里：所有会写数据库的自动化测试，都要考虑准备数据、执行动作、断言结果、清理数据四段。
- `@SpringBootTest(webEnvironment = ...)`：
  - 疑惑点：文档里提到 `MOCK`、`RANDOM_PORT`、`DEFINED_PORT`、`NONE`，这是基础内容还是企业级进阶内容。
  - 最后理解：这是 Spring Boot 测试的进阶配置，企业项目会常用。今天的 Mapper 并发测试不需要真实 HTTP 服务，默认 `MOCK` 就够；只有像 Apifox 一样发真实 HTTP 请求时，才考虑 `RANDOM_PORT`。
  - 后续会用到哪里：完整下单接口并发测试、端到端接口测试时，可能会用 `RANDOM_PORT` 和 `TestRestTemplate`。
- 请求方法错误为什么是 405：
  - 疑惑点：之前 `GET /api/orders/{id}/mock-pay` 返回 `code=500`，看起来像系统异常。
  - 最后理解：URL 存在但 HTTP 方法不匹配时，属于客户端调用方式错误，语义上是 Method Not Allowed，所以用 `code=405` 更准确；它必须在兜底 `Exception.class` 前单独处理。
  - 后续会用到哪里：所有接口验收时，除了看业务规则，也要确认请求方法是否和 Controller 注解一致，例如 `@PostMapping`、`@GetMapping`、`@PutMapping`、`@DeleteMapping`。
- 完整下单链路为什么还要查数据库：
  - 疑惑点：并发任务里已经统计出 `successCount=10`，为什么还要查询 `commerce_order`、`inventory_movement` 和 `cart_item`。
  - 最后理解：线程返回成功只说明方法没有抛异常；数据库最终状态才能证明真实落库结果。完整链路并发测试要验证程序返回和数据库账本一致，避免出现“方法看似成功但订单、流水、库存或购物车状态不一致”的问题。
  - 后续会用到哪里：步骤 18 幂等下单、支付与关单竞争、限量促销抢购都要同时看接口结果和数据库最终状态。
- `Future<Boolean>` 和线程结果：
  - 疑惑点：`executorService.submit(...)` 返回的是 `Future`，为什么最后能统计 `Boolean`。
  - 最后理解：任务内部 `return true/false` 决定了未来结果类型是 `Boolean`；`submit(...)` 先返回 `Future<Boolean>` 小票，后面通过 `future.get()` 取出真正的 `Boolean`。
  - 后续会用到哪里：所有并发测试都可以用 `Future` 收集每个线程的业务结果，再统一统计成功、失败和异常。

### 今天还没理解透

- 当前完整链路测试已经覆盖普通下单事务中的订单主表、库存流水、购物车删除和最终库存一致性；还没有覆盖支付与超时关单竞争。
- `ExecutorService`、线程池生命周期、`Future.get()` 的更多细节已经理解到能用，后续做支付/关单竞争测试时再继续加深。

### 明天遇到再补

- 可以进入步骤 18：重点学习幂等键、重复提交、同 key 同参数返回同一结果、同 key 不同参数返回冲突。
- 后续做支付/关单竞争时，继续沿用“程序返回 + 数据库最终状态”的双重验证方式。

## Day 16：2026-08-05

### 今天对应任务

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 18：请求幂等与防重复下单
- 今日目标：理解并实现 `Idempotency-Key`，让重复提交不会重复创建订单。

### 今天学了什么

- 请求头不是只有一个值：一次 HTTP 请求可以同时携带 `Authorization`、`Content-Type` 和 `Idempotency-Key`。`@RequestHeader("Idempotency-Key")` 的含义是按名称取出这一项请求头。
- 幂等键的作用：它代表一次下单意图。服务端按“消费者 ID + 幂等键”查找历史请求，同 key 同参数可以安全返回旧结果。
- 幂等参数指纹：当前先把购物车项 ID 排序后拼成字符串，例如 `1,2,3`。它能判断参数是否变化，但严格说这还不是 SHA-256 等密码学哈希，后续可以再升级真正的摘要算法。
- `KEY` 和 `INDEX`：在 MySQL 建表语句中，`KEY idx_xxx (...)` 就是普通索引写法，`INDEX idx_xxx (...)` 也可以；`UNIQUE KEY` 表示唯一索引，同时承担防重复约束。
- 自增 ID 回填：插入 `idempotent_request` 后，MyBatis 通过 `useGeneratedKeys` 把数据库生成的 `id` 写回 Java 对象，后面用它精准更新 `SUCCESS` 和 `order_id`。
- Stream 和 reduce：`stream()` 是 Java 集合的处理流水线；`sorted()` 排序，`map()` 转换元素，`reduce()` 把多个字符串合并成一个结果，`orElse("")` 处理空集合。
- `assertThrows(...)`：第一个参数是预期异常类型，第二个参数 `() -> ...` 是暂时不执行的测试动作。动作抛出 `BizException` 时断言通过，并返回异常对象用于检查错误码和消息。
- 并发断言：不能把“成功响应次数”直接当成“创建订单数”。并发请求中，多个请求可能都成功返回同一个订单；应该检查成功订单 ID 只有一个，并查询数据库确认订单和幂等记录各只有一条。

### 今天遇到的问题

| 问题 | 原因与解决 | 是否已理解 |
|---|---|---|
| `order_id` 看起来没有和订单表连接 | 当前用逻辑关联保存订单 ID，并建立普通索引；它不是数据库外键。订单创建成功后由 `markSuccess(...)` 写入。 | 是 |
| 同 key 同参数测试为什么要调用两次 | 第二次调用模拟用户双击或网络重试，用来证明服务端返回第一次创建的同一订单。 | 是 |
| 并发测试第一次断言成功数为 1 失败 | 后续请求可能已经看到 `SUCCESS` 并复用旧订单，所以成功响应数可能大于 1；改为断言不同订单 ID 数量为 1，并查询数据库。 | 是 |
| 并发测试出现 Duplicate entry | 多个线程同时查不到记录后一起插入；业务查询无法单独解决竞态，数据库唯一键负责最终裁决，代码捕获 `DuplicateKeyException` 返回处理中冲突。 | 是 |
| 测试启动出现很多 `Caused by` | 最初是测试环境连接 MySQL 的 root 密码不匹配，修正运行配置后恢复；Mockito/Byte Buddy 动态 agent 是警告，不是失败原因。 | 是 |
| IDEA 出现黄色波浪线 | 属于静态检查提示，不等于编译错误；最终以测试运行结果和编译结果为准。 | 是 |

### 重要修改

- 新增 `server/src/main/resources/db/migration/V8__add_idempotent_request.sql`。
- 新增 `idempotency/entity/IdempotentRequest.java` 和 `idempotency/mapper/IdempotentRequestMapper.java`。
- `OrderController` 接收可选的 `Idempotency-Key` 请求头，`OrderService` 完成查询、插入、冲突判断、订单绑定和成功复用。
- `InventoryConcurrencyTest` 为不同消费者生成不同幂等键，避免把真实的不同下单意图误判成同一次请求重试。
- `OrderIdempotencyTest` 新增 3 个场景：同 key 同参数、同 key 不同参数、同 key 并发请求。

### 侧边任务/对话补充记录

- 用户追问请求头：从 Apifox 的 `Authorization` 看到，理解到请求头是一个键值集合，不是只有一个“请求头”；`Idempotency-Key` 与 `Authorization` 是两项不同的键。
- 用户追问 `KEY` 是否就是索引：理解到 MySQL 中 `KEY` 是 `INDEX` 的同义写法，索引名只是便于管理和排查的名字。
- 用户追问 Stream 属于什么：理解到它是 Java SE 集合框架相关能力，列表通过 `stream()` 进入处理流水线；它不会自动修改原列表。
- 用户追问为什么新建幂等测试类：理解到生产代码负责业务规则，测试类负责准备数据、调用业务、断言结果和清理数据，两者职责不同；旧的库存并发测试也要适配新方法签名，但不应承担幂等语义测试。
- 用户追问为什么测试前后都执行清理：理解到测试开始清理是固定起点，测试结束清理是防止污染数据库；两者目的不同。
- 用户追问测试通过但日志中有异常：理解到 `assertThrows` 预期捕获的业务异常会出现在测试执行路径中，不代表测试失败；真正失败要看 JUnit 是否显示测试失败以及异常是否超出断言范围。

### 截图记录

- `docs/images/day-16/flyway-v8-migration-success.png`
- `docs/images/day-16/order-create-idempotency-success.png`
- `docs/images/day-16/order-repeat-same-result.png`
- `docs/images/day-16/order-idempotency-conflict-409.png`
- `docs/images/day-16/idempotent-request-datagrip-success.png`
- `docs/images/day-16/order-idempotency-tests-passed.png`

### 今天还没理解透

- 当前请求参数指纹仍是排序后的字符串，不是严格意义上的加密哈希；后续如果请求参数变复杂，可以使用稳定 JSON 序列化后再做 SHA-256。
- 幂等记录目前用 `PROCESSING/SUCCESS`，后续可继续讨论失败状态、超时恢复、查询接口和清理策略。

### 明天遇到再补

- 进入步骤 19 前先理解商品缓存为什么能减少数据库查询，以及缓存失效、更新顺序和库存实时性之间的取舍。

## Day 17：2026-08-06

### 今天对应任务

- 当前文档：`02-交易库存限量促销开发链.md`
- 当前步骤：步骤 19：Redis 商品缓存与一致性策略。
- 今日目标：为公开商品详情建立 Cache Aside 缓存，验证缓存命中、失效、空值、租户隔离和 Redis 故障回源。

### 今天学了什么

- `StringRedisTemplate`：Spring 提供的 Redis 操作工具；`opsForValue().get(key)` 读取字符串 Value，`set(key, value, ttl)` 写入带过期时间的缓存。
- `ObjectMapper`：负责 Java 对象和 JSON 字符串互相转换；`writeValueAsString` 用于写入 Redis，`readValue` 用于缓存命中时恢复 `PublicProductDetailVO`。
- Cache Aside：先读缓存；未命中查数据库；查到后写缓存；商品更新成功后删除缓存。
- 缓存降级：Redis 读取或写入失败时，不影响商品查询主流程，读取回源 MySQL，写入失败仍返回数据库结果。
- 空值缓存：用 `__EMPTY_PRODUCT_DETAIL__` 短暂表示“已经查过且商品不存在”，TTL 为 30 秒，避免恶意 ID 持续穿透数据库。
- 租户隔离：缓存 Key 使用 `mall:v1:tenant:{tenantId}:product:{spuId}`，不同商家不会共用同一个商品详情缓存。
- 日志级别：缓存故障属于可恢复异常，使用 `log.warn`；正式日志比 `System.out.println` 更容易按级别、线程、类名和异常堆栈管理。

### 今天完成

- [x] 新增 `ProductCacheKey`，统一商品详情 Key 和空值标记。
- [x] `PublicProductService` 接入 Redis 读取、JSON 反序列化、数据库回源和 JSON 回填。
- [x] 商品详情缓存设置 10 分钟 TTL；不存在商品的空值缓存设置 30 秒 TTL。
- [x] Redis 停止时，商品详情仍能回源数据库并返回成功结果。
- [x] 上架、下架成功后删除商品详情缓存。
- [x] 新增 SKU 改价接口，改价成功后删除所属 SPU 的详情缓存。
- [x] 使用 Apifox 和 Redis CLI 验证改价后价格从 `199.00` 更新为 `188.00`，缓存内容同步更新。
- [x] 验证不同租户：租户 `1001` 返回真实商品 JSON，租户 `1002` 返回空值标记，不发生缓存串数据。
- [x] 正式日志替换商品缓存相关的 `System.out.println`，并完成编译验证。

### 今天遇到的问题

| 问题 | 原因与解决 | 是否已理解 |
|---|---|---|
| Redis 停止后 Apifox 一直加载 | Redis 客户端等待连接超时；增加 `spring.data.redis.connect-timeout=1s` 和 `spring.data.redis.timeout=1s` 后，超时即可回源数据库 | 是 |
| Redis 连接失败日志是否代表接口失败 | `Connection refused` 是 Redis 不可用的底层原因；最终 Apifox 返回 `200` 且商品查询成功，说明降级逻辑生效 | 是 |
| `TTL` 为什么返回 `-2` | `-2` 表示 Key 已不存在；30 秒空值缓存已经自动过期 | 是 |
| 改价接口第一次返回系统异常 | Apifox 请求方法/请求配置有误；改为 `PUT` 并传入 JSON 请求体后返回 `code=0` | 是 |
| `GET` 调用只支持 `POST` 的接口 | HTTP 方法不匹配，返回 `code=405` 是正确的请求错误，不是系统异常 | 是 |

### 重要修改

- 新增 `server/src/main/java/org/example/merchant_ai_operation/publicapi/product/cache/ProductCacheKey.java`。
- `PublicProductService` 增加 Redis 商品详情读取、JSON 转换、TTL、空值缓存和 Redis 故障降级。
- `ProductService` 在商品上架、下架和 SKU 改价成功后删除详情缓存。
- 新增 `UpdateSkuPriceRequest`，新增 `ProductSkuMapper.updateSalePrice(...)` 和商家改价接口：
  - `PUT /api/merchant/products/skus/{skuId}/price`
- `PublicProductController` 的商品详情接口使用 `storeId + spuId`，数据库查询和缓存 Key 均按租户隔离。
- `application.properties` 增加 Redis 连接和命令超时配置。

### 截图记录

- `docs/images/day-17/redis-server-ready.png`
- `docs/images/day-17/redis-product-cache-and-ttl.png`
- `docs/images/day-17/redis-fallback-request-success.png`
- `docs/images/day-17/redis-restored-product-query-success.png`
- `docs/images/day-17/empty-cache-marker.png`
- `docs/images/day-17/tenant-cache-isolation.png`
- `docs/images/day-17/tenant-1001-cache-json.png`
- `docs/images/day-17/price-update-cache-refreshed.png`

### 侧边任务/对话补充记录

- `storeId` 与 `tenant_id`：当前项目没有独立的店铺表，对外接口使用 `storeId` 表示消费者访问的店铺，对内数据库使用 `tenant_id` 表示商家租户；当前二者值一一对应，但命名分别服务于接口层和数据隔离层。
- `StringRedisTemplate` 与 `JdbcTemplate`：二者都是 Spring 的数据访问工具；前者按 Redis Key-Value 操作，后者通常执行关系型数据库 SQL。
- 第一次查询为什么仍返回 `result`：第一次结果来自 MySQL 组装的 Java 对象，Redis 只是保存 JSON 副本；后续命中缓存时才由 Redis JSON 转回 Java 对象并直接返回。
- `ObjectMapper` 的作用：`writeValueAsString` 把 Java 对象转 JSON，`readValue` 把 JSON 转回指定 Java 类型；解析失败时回源数据库。
- `System.out.println` 与 `log.warn`：前者只是临时标准输出，后者带日志级别、上下文和异常堆栈，适合正式项目运行。

### 今天还没理解透

- 当前缓存删除是在数据库更新方法成功返回后执行；更复杂的事务提交后失效、消息通知和缓存重建策略，后续继续学习。
- 当前 TTL 使用固定值，随机过期、热点互斥重建和逻辑过期尚未实现，按步骤 19 的基础目标暂不提前扩展。

### 明天优先

- 步骤 20：学习 RabbitMQ、可靠发布与订单关闭消息；继续沿用“程序返回 + 数据库/消息最终状态”的双重验收方式。
