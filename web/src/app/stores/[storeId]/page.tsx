import { ProductCard } from "@/components/ProductCard";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { demoProductsForStore, storeFor, visualFor } from "@/lib/demo-data";
import Image from "next/image";
import Link from "next/link";

export default async function StorefrontPage({ params }: { params: Promise<{ storeId: string }> }) {
  const storeId = Number((await params).storeId) || 1001;
  const store = storeFor(storeId);
  const products = demoProductsForStore(store.id);
  const heroVisual = visualFor(store.heroProductId);

  return (
    <>
      <SiteNav />
      <main className="market-shell storefront-shell">
        <Link className="market-back-link" href="/stores">← 返回店铺市集</Link>
        <section className="storefront-hero">
          <div className="storefront-hero__copy">
            <span className="eyebrow">STORE {store.id} / {store.location}</span>
            <span className="storefront-badge">{store.badge}</span>
            <h1>
              {store.name.split(/\s+/).map((part) => (
                <span key={part}>{part}</span>
              ))}
            </h1>
            <p className="storefront-tagline">{store.tagline}</p>
            <p>{store.description}</p>
            <div className="storefront-actions">
              <Link className="button primary" href={`/stores/${store.id}/products`}>浏览全部商品</Link>
              <Link className="button" href={`/search?q=${encodeURIComponent(store.name)}`}>在店内搜索</Link>
            </div>
          </div>
          <div className="storefront-hero__media" style={{ background: store.tone }}>
            <Image src={heroVisual.image} alt={`${store.name}代表商品`} fill priority sizes="(max-width: 760px) 92vw, 48vw" />
            <span>{store.englishName}</span>
          </div>
        </section>
        <section className="storefront-facts">
          <div><span>店铺所在地</span><strong>{store.location}</strong></div>
          <div><span>主营分类</span><strong>{store.categories.join(" · ")}</strong></div>
          <div><span>当前在售</span><strong>{products.length} 件</strong></div>
        </section>
        <section className="storefront-selection">
          <div className="market-section-heading">
            <div><span className="eyebrow">STORE SELECTION</span><h2>店主本周选择</h2></div>
            <Link href={`/stores/${store.id}/products`}>查看全部 ↗</Link>
          </div>
          <div className="product-grid market-product-grid">
            {products.slice(0, 3).map((product) => <ProductCard key={product.id} product={product} storeId={store.id} storeName={store.name} />)}
          </div>
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
