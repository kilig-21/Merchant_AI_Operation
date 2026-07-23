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
