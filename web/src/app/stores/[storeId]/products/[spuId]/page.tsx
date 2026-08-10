import { DemoNotice } from "@/components/DemoNotice";
import { ProductPurchase } from "@/components/ProductPurchase";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProduct } from "@/lib/backend";
import { demoDetail, visualFor } from "@/lib/demo-data";
import Image from "next/image";
import Link from "next/link";

export const dynamic = "force-dynamic";
export default async function ProductDetailPage({
  params,
}: { params: Promise<{ storeId: string; spuId: string }> }) {
  const values = await params;
  const storeId = Number(values.storeId) || 1001;
  const productId = Number(values.spuId);
  let product = demoDetail(productId);
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
      <main className="detail-shell">
        <section className="detail-media">
          <Image src={visual.image} alt={product.name} fill priority sizes="60vw" />
        </section>
        <section className="detail-copy">
          <Link className="eyebrow" href={`/stores/${storeId}/products`}>
            ← 返回选购
          </Link>
          {demo && <DemoNotice>此商品为演示内容，真实购买功能已暂停。</DemoNotice>}
          <span className="eyebrow">{visual.category}</span>
          <h1>{product.name}</h1>
          <p className="tagline editorial">{visual.tagline}</p>
          <p className="description">{product.description}</p>
          <ProductPurchase product={product} demo={demo} />
        </section>
      </main>
    </>
  );
}
