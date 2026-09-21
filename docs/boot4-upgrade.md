# Boot 4 升级与验收记录

日期：2026-09-05。分支：`codex/upgrade-boot4`，起点：`4ff5870`。

## 范围与版本

用户本次明确授权 Agent 直接升级并检验后端。保持模块化单体，先迁移电商后端，再在后续 A2 接入 Spring AI 2；本次没有新增 AI 接口或模型依赖。

| 组件 | 升级前 | 升级后 |
|---|---|---|
| Spring Boot | 3.5.16 | 4.1.1 |
| Java | 21 | 21 |
| MyBatis Spring Boot Starter | 3.0.5 | 4.1.0 |
| Springdoc OpenAPI | 2.8.17 | 3.1.0 |
| 应用 JSON API | Jackson 2 | Jackson 3 |

- Web Starter 改为 `spring-boot-starter-webmvc`，MVC 测试使用 `spring-boot-starter-webmvc-test` 和新注解包路径。
- Flyway 使用 `spring-boot-starter-flyway`，继续保留 MySQL 数据库支持依赖；未修改 V1～V15 迁移文件。
- 6 个生产类的 ObjectMapper 迁移到 `tools.jackson`，3 个类的 JSON 异常处理改为 `JacksonException`。
- `RequestLogFilter` 的 NonNull 注解改用 JSpecify。
- 最终依赖树：Boot 4.1.1、Spring Framework 7.0.9、Jackson 3.1.5、Flyway 12.4.0。`java-jwt` 内部仍依赖 Jackson 2.21.5，由 Boot BOM 管理；它与应用 Jackson 3 使用不同包名，不能强行排除。三类账号的签名与验签已经通过真实登录检查。
- 修复既有 `CheckoutGroupServiceIntegrationTest`：被测真实 Service 错用 `@Mock`，改为 `@Autowired`，实际执行数据库集成验证。
- 前端源码、接口路径、业务 DTO、订单/库存状态机未修改。

## 已验证结果

| 验证 | 结果 |
|---|---|
| 升级前编译及不依赖数据库的测试 | 编译通过，27 项测试通过 |
| 升级后 `mvn -DskipTests package` | 编译、测试编译、JAR 打包通过 |
| 隔离环境 `mvn -Dspring.profiles.active=upgrade-test verify` | 75 项，0 失败、0 错误、0 跳过 |
| Flyway 空库初始化 | V1～V15 全部成功，后续启动校验通过 |
| 新增 JSON 回归 | 统一响应、金额、ISO 日期、旧商品缓存及旧关单消息读取通过 |
| 前端 `npm run check/test/build` | 全部通过，4 项前端测试通过 |
| JAR HTTP 与生产模式前端 BFF | 31 项检查通过 |
| 原 Dockerfile 构建 | 独立镜像 `merchant-ai-operation-server:boot4-upgrade-test` 成功 |
| 新镜像容器运行 | health UP、ping、OpenAPI、真实商品读取全部通过 |

HTTP 验证包括 health、ping、OpenAPI/Swagger、商家 A/B 和消费者登录与身份、401/403、经营汇总/趋势/日期限制、商品缓存重复读取、购物车/订单/售后/活动读取、BFF 登录及 Cookie 转发和店铺页面 HTTP 响应。

这不代表新增了全套浏览器点击验收，也不代表所有售后和跨店交易失败场景都已覆盖。并发库存、支付/关单、幂等和促销等由现有测试验证；经营接口读取成功不等于补齐 A1 的固定指标预期值测试。

## 可复现后端测试

在 PowerShell 7 中、Docker Desktop 已启动且 Java 21/Maven 可用时运行：

```powershell
cd server
./test-upgrade.ps1
```

脚本启动独立 Compose 项目 `merchant-boot4-test`，使用本机 13307/16379/15673 端口，不读取生产 `.env`。先通过应用上下文让 Flyway 建表，再填充历史并发测试依赖的 SKU，最后执行完整 `verify`。退出时只移除该测试项目的容器及匿名卷。

`upgrade-test` 配置和 SKU 夹具均位于 `src/test/resources`，不会打入生产 JAR。固定密码和签名值仅供绑定在回环地址的可丢弃测试环境使用。

`smoke-upgrade.ps1` 用于已经启动的隔离 JAR（18080）和 Next.js 生产服务（13000，`BACKEND_ORIGIN=http://127.0.0.1:18080`）。为验证生产 Secure Cookie 在本地 HTTP 的转发，脚本仅在内存中设置 Cookie 请求头，不输出 Token。

## 已知边界

- 原 `GlobalExceptionHandler` 对参数校验错误返回 HTTP 200 / body code 400；这是既有行为，冒烟脚本分别断言 HTTP 和业务码，没有把它记录为 HTTP 400。后续应单独统一异常 HTTP 语义。
- 不需要为了升级重建原业务数据库。迁移测试使用全新隔离库，未在原数据卷上运行写入测试。
- 生产 Compose 原后端继续使用原镜像；本次新镜像使用独立标签，不自动替换原服务。
- 镜像首次下载依赖约 9 分钟。已清理本轮临时 JAR/前端进程和独立测试容器/卷，保留测试镜像供复查。Docker Desktop 及启动时自动恢复的原部署继续运行。
- 本次不提交、推送、合并分支，也未公网部署。

## 官方依据

- [Spring Boot 4 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.1 发行说明](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes)
- [MyBatis Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Springdoc](https://springdoc.org/)
