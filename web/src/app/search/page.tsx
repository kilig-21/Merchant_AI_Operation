import { MarketplaceSearchClient } from "@/components/MarketplaceSearchClient";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { demoMarketplaceProducts } from "@/lib/demo-data";

export default async function SearchPage({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const query = (await searchParams).q ?? "";
  return (
    <>
      <SiteNav />
      <main className="market-shell market-search-page">
        <header className="market-compact-hero">
          <span className="eyebrow">SEARCH / ALL STORES</span>
          <h1>从整个市集里，<br />找到今天需要的东西。</h1>
          <p>同时搜索商品与店铺，筛选条件只帮助你缩小范围，不打断浏览节奏。</p>
        </header>
        <MarketplaceSearchClient products={demoMarketplaceProducts} initialQuery={query} />
      </main>
      <SiteFooter />
    </>
  );
}
