import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { StoreDirectoryClient } from "@/components/StoreDirectoryClient";
import { getPublicStores } from "@/lib/backend";
import Link from "next/link";

export const dynamic = "force-dynamic";

export default async function StoresPage() {
  try {
    const stores = await getPublicStores();
    return (
      <>
        <SiteNav />
        <main className="market-shell market-directory">
          <header className="market-hero"><div><span className="eyebrow">MORROW / INDEPENDENT STORES</span><h1>在不同店铺里，<br />找到同一种认真。</h1></div><div className="market-hero__aside"><p>店铺名称和在售商品数来自公开目录；图片仅用于浏览体验，不代表商家资料。</p><Link href="/search">搜索全部商品 ↗</Link></div></header>
          <section className="market-trust-strip" aria-label="市集说明"><div><strong>{String(stores.length).padStart(2, "0")}</strong><span>间公开店铺</span></div><div><strong>01</strong><span>统一购物体验</span></div><div><strong>100%</strong><span>店铺身份清晰可见</span></div><p>每件商品都标明所属店铺；跨店加入购物袋后，将按店铺拆分订单。</p></section>
          <StoreDirectoryClient stores={stores} />
        </main>
        <SiteFooter />
      </>
    );
  } catch {
    return <><SiteNav /><ServiceUnavailable /><SiteFooter /></>;
  }
}
