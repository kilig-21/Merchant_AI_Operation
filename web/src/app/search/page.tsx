import { MarketplaceSearchClient } from "@/components/MarketplaceSearchClient";
import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getPublicStores } from "@/lib/backend";

export const dynamic = "force-dynamic";

export default async function SearchPage({ searchParams }: { searchParams: Promise<{ q?: string; storeId?: string }> }) {
  const params = await searchParams;
  const query = params.q ?? "";
  try {
    const stores = await getPublicStores();
    const initialStoreId = stores.some((store) => String(store.id) === params.storeId) ? params.storeId : "all";
    return <><SiteNav /><main className="market-shell market-search-page"><header className="market-compact-hero"><span className="eyebrow">SEARCH / ALL STORES</span><h1>从整个市集里，<br />找到今天需要的东西。</h1><p>商品、价格、库存和所属店铺均来自公开接口。</p></header><MarketplaceSearchClient initialQuery={query} initialStoreId={initialStoreId} stores={stores} /></main><SiteFooter /></>;
  } catch {
    return <><SiteNav /><ServiceUnavailable /><SiteFooter /></>;
  }
}
