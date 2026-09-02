import { ProductCard } from "@/components/ProductCard";
import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProducts, getPublicStores } from "@/lib/backend";
import { storePresentationFor } from "@/lib/store-presentation";
import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";

export const dynamic = "force-dynamic";

export default async function StorefrontPage({ params }: { params: Promise<{ storeId: string }> }) {
  const storeId = Number((await params).storeId);
  if (!Number.isSafeInteger(storeId) || storeId <= 0) notFound();
  const result = await Promise.all([getPublicStores(), getPublicProducts(storeId)]).catch(() => null);
  if (!result) return <><SiteNav /><ServiceUnavailable /><SiteFooter /></>;
  const [stores, products] = result;
  const store = stores.find((item) => item.id === storeId);
  if (!store) notFound();
  const visual = storePresentationFor(store.id);
  return <><SiteNav /><main className="market-shell storefront-shell"><Link className="market-back-link" href="/stores">← 返回店铺市集</Link><section className="storefront-hero"><div className="storefront-hero__copy"><span className="eyebrow">PUBLIC STORE / {store.id}</span><span className="storefront-badge">公开店铺</span><h1>{store.name}</h1><p className="storefront-tagline">店铺名称和在售数量来自公开目录。</p><p>商家介绍、所在地和品牌资料将在店铺资料功能完成后开放。</p><div className="storefront-actions"><Link className="button primary" href={`/stores/${store.id}/products`}>浏览全部商品</Link><Link className="button" href={`/search?storeId=${store.id}`}>搜索店内商品</Link></div></div><div className="storefront-hero__media" style={{ background: visual.tone }}><Image src={visual.image} alt="" fill priority sizes="(max-width: 760px) 92vw, 48vw" /><span>{visual.label}</span></div></section><section className="storefront-facts"><div><span>公开店铺 ID</span><strong>{store.id}</strong></div><div><span>当前在售</span><strong>{store.productCount} 件</strong></div><div><span>本页已加载</span><strong>{products.length} 件</strong></div></section><section className="storefront-selection"><div className="market-section-heading"><div><span className="eyebrow">STORE SELECTION</span><h2>店铺公开商品</h2></div><Link href={`/stores/${store.id}/products`}>查看全部 ↗</Link></div>{products.length ? <div className="product-grid market-product-grid">{products.slice(0, 3).map((product) => <ProductCard key={product.id} product={product} storeId={store.id} storeName={store.name} />)}</div> : <div className="market-empty"><h2>店铺暂时没有公开商品。</h2></div>}</section></main><SiteFooter /></>;
}
