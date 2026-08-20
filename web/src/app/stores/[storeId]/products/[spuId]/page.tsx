import { DemoNotice } from "@/components/DemoNotice";
import { ProductPurchase } from "@/components/ProductPurchase";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProduct } from "@/lib/backend";
import { demoDetailForStore, storeFor, visualFor } from "@/lib/demo-data";
import Image from "next/image";
import Link from "next/link";

export const dynamic = "force-dynamic";
export default async function ProductDetailPage({
  params,
}: { params: Promise<{ storeId: string; spuId: string }> }) {
  const values = await params;
  const storeId = Number(values.storeId) || 1001;
  const productId = Number(values.spuId);
  const store = storeFor(storeId);
  let product = demoDetailForStore(store.id, productId);
  let demo = false;
  try {
    product = await getPublicProduct(storeId, productId);
    if (!product.skus.length) throw new Error("empty");
  } catch {
    demo = true;
  }
  const visual = visualFor(product.id);
  return (
    <>
      <SiteNav />
      <main className="detail-shell market-detail-shell">
        <section className="detail-media">
          <Image src={visual.image} alt={product.name} fill priority sizes="60vw" />
        </section>
        <section className="detail-copy">
          <Link className="eyebrow" href={`/stores/${store.id}`}>
            ← 返回 {store.name}
          </Link>
          {demo && <DemoNotice>此商品为演示内容，真实购买功能已暂停。</DemoNotice>}
          <span className="eyebrow">{visual.category}</span>
          <h1>{product.name}</h1>
          <p className="tagline editorial">{visual.tagline}</p>
          <p className="description">{product.description}</p>
          <Link className="market-product-store" href={`/stores/${store.id}`}>
            <span>由 {store.name} 提供</span>
            <strong>查看店铺 ↗</strong>
          </Link>
          <ProductPurchase product={product} demo={demo} storeId={store.id} storeName={store.name} />
        </section>
      </main>
    </>
  );
}
