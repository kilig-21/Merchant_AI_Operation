# Morrow Web

Morrow 的 Next.js 16 电商前端。消费者端以安静、编辑感的选物体验承载浏览、购物袋和订单闭环；商家端使用同一套设计令牌提供更紧凑的经营工作台。

## 本地运行

```powershell
npm install
npm run dev
```

默认通过服务端 BFF 连接 `http://localhost:8080`。如需修改：

```powershell
Copy-Item .env.example .env.local
```

然后设置 `BACKEND_ORIGIN`。客户端永远只访问同源的 `/api/session/*` 与 `/api/backend/*`，JWT 保存在 HttpOnly Cookie 中。

## 质量检查

```powershell
npm run check
npm test
npm run build
```

## 部署

Vercel 项目根目录设置为 `web`，并配置可从公网访问的 HTTPS `BACKEND_ORIGIN`。公共商品页在后端不可达时会显示带标识的演示内容；购物袋、订单和商家受保护页面不会伪造业务状态。

第三方许可信息见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
