import { ProductPurchase } from "@/components/ProductPurchase";
import { ServiceUnavailable } from "@/components/ServiceUnavailable";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProduct, getPublicStores } from "@/lib/backend";
import { storePresentationFor } from "@/lib/store-presentation";
import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";

export const dynamic = "force-dynamic";

export default async function ProductDetailPage({ params }: { params: Promise<{ storeId: string; spuId: string }> }) {
  const values = await params;
  const storeId = Number(values.storeId);
  const productId = Number(values.spuId);
  if (!Number.isSafeInteger(storeId) || storeId <= 0 || !Number.isSafeInteger(productId) || productId <= 0) notFound();
  const result = await Promise.all([getPublicStores(), getPublicProduct(storeId, productId)]).catch(() => null);
  if (!result) return <><SiteNav /><ServiceUnavailable /></>;
  const [stores, product] = result;
  const store = stores.find((item) => item.id === storeId);
  if (!store || !product.skus.length) notFound();
  const visual = storePresentationFor(product.id);
  return <><SiteNav /><main className="detail-shell market-detail-shell"><section className="detail-media" style={{ background: visual.tone }}><Image src={visual.image} alt="" fill priority sizes="60vw" /></section><section className="detail-copy"><Link className="eyebrow" href={`/stores/${store.id}`}>← 返回 {store.name}</Link><span className="eyebrow">PUBLIC PRODUCT</span><h1>{product.name}</h1><p className="tagline editorial">商品详情、SKU 价格和库存来自公开接口。</p><p className="description">{product.description}</p><Link className="market-product-store" href={`/stores/${store.id}`}><span>由 {store.name} 提供</span><strong>查看店铺 ↗</strong></Link><ProductPurchase demo={false} product={product} storeId={store.id} storeName={store.name} /></section></main></>;
}
