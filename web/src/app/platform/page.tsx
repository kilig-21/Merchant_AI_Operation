import { PlatformShell } from "@/components/PlatformShell";
import { demoMarketplaceProducts, demoStores } from "@/lib/demo-data";
import Link from "next/link";

export default function PlatformPage() {
  return (
    <PlatformShell title="平台总览" eyebrow="MARKETPLACE / CONTROL ROOM">
      <p className="platform-notice">DEMO / 当前仅展示多租户平台的信息架构，不连接真实审核、财务或处罚操作。</p>
      <section className="platform-metrics">
        <article><span>活跃商家</span><strong>{String(demoStores.length).padStart(2, "0")}</strong><small>全部为演示店铺</small></article>
        <article><span>在售商品</span><strong>{demoMarketplaceProducts.length}</strong><small>跨 4 个租户目录</small></article>
        <article><span>待审核申请</span><strong>07</strong><small>接口待接入</small></article>
        <article><span>今日跨店订单</span><strong>18</strong><small>演示统计</small></article>
      </section>
      <section className="platform-grid">
        <article className="platform-panel platform-tenant-map">
          <header><div><span className="eyebrow">TENANT DIRECTORY</span><h2>租户与店铺</h2></div><Link href="/platform/merchants">进入审核 ↗</Link></header>
          {demoStores.map((store) => <div key={store.id}><span className="platform-tenant-id">{store.id}</span><div><strong>{store.name}</strong><small>{store.location} · {store.categories.join(" / ")}</small></div><span>ACTIVE</span></div>)}
        </article>
        <article className="platform-panel platform-queue">
          <header><div><span className="eyebrow">REVIEW QUEUE</span><h2>待处理队列</h2></div></header>
          {["新商家资质审核", "商品内容复核", "退款争议升级", "店铺资料变更"].map((item, index) => <div key={item}><span>{String(index + 1).padStart(2, "0")}</span><strong>{item}</strong><small>{[7, 12, 3, 5][index]} 项</small></div>)}
        </article>
      </section>
    </PlatformShell>
  );
}
