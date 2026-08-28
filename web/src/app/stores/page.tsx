import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { StoreDirectoryClient } from "@/components/StoreDirectoryClient";
import { demoStores } from "@/lib/demo-data";
import Link from "next/link";

export default function StoresPage() {
  return (
    <>
      <SiteNav />
      <main className="market-shell market-directory">
        <header className="market-hero">
          <div>
            <span className="eyebrow">MORROW / INDEPENDENT STORES</span>
            <h1>在不同店铺里，<br />找到同一种认真。</h1>
          </div>
          <div className="market-hero__aside">
            <p>不是一整面没有尽头的货架，而是由独立商家共同组成的日常市集。先认识店铺，再挑选真正适合生活的物件。</p>
            <Link href="/search">搜索全部商品 ↗</Link>
          </div>
        </header>
        <section className="market-trust-strip" aria-label="市集说明">
          <div><strong>{String(demoStores.length).padStart(2, "0")}</strong><span>间独立店铺</span></div>
          <div><strong>01</strong><span>统一购物体验</span></div>
          <div><strong>100%</strong><span>店铺身份清晰可见</span></div>
          <p>每件商品都标明所属店铺；跨店加入购物袋后，将按店铺拆分订单。</p>
        </section>
        <StoreDirectoryClient stores={demoStores} />
      </main>
      <SiteFooter />
    </>
  );
}
