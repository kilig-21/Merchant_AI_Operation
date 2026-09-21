import { ProductGridClient } from "@/components/ProductGridClient";
import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProducts, getPublicStores } from "@/lib/backend";
import Link from "next/link";
import { notFound } from "next/navigation";

export const dynamic = "force-dynamic";

export default async function StoreProductsPage({ params }: { params: Promise<{ storeId: string }> }) {
  const storeId = Number((await params).storeId);
  if (!Number.isSafeInteger(storeId) || storeId <= 0) notFound();
  const result = await Promise.all([getPublicStores(), getPublicProducts(storeId)]).catch(() => null);
  if (!result) return <><SiteNav /><ServiceUnavailable /><SiteFooter /></>;
  const [stores, products] = result;
  const store = stores.find((item) => item.id === storeId);
  if (!store) notFound();
  return <><SiteNav /><main className="page-shell"><header className="page-intro"><div><Link className="eyebrow" href={`/stores/${store.id}`}>← {store.name}</Link><h1>从这间店里，选一些真正会<br />陪你生活的东西。</h1></div><p>商品名称、价格和库存来自公开商品接口。</p></header><ProductGridClient products={products} storeId={store.id} storeName={store.name} /></main><SiteFooter /></>;
}
