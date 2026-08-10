import { DemoNotice } from "@/components/DemoNotice";
import { ProductGridClient } from "@/components/ProductGridClient";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProducts } from "@/lib/backend";
import { demoProducts } from "@/lib/demo-data";

export const dynamic = "force-dynamic";
export default async function StoreProductsPage({ params }: { params: Promise<{ storeId: string }> }) {
  const storeId = Number((await params).storeId) || 1001;
  let products = demoProducts;
  let demo = false;
  try {
    const remote = await getPublicProducts(storeId);
    if (remote.length) products = remote;
    else demo = true;
  } catch {
    demo = true;
  }
  return (
    <>
      <SiteNav />
      <main className="page-shell">
        <header className="page-intro">
          <div>
            <span className="eyebrow">MORROW / STORE {storeId}</span>
            <h1>
              选一些真正会
              <br />
              陪你生活的东西。
            </h1>
          </div>
          <p>为专注、出发、停留和每一个平常时刻，慢慢挑选。</p>
        </header>
        {demo && <DemoNotice />}
        <ProductGridClient products={products} storeId={storeId} />
      </main>
      <SiteFooter />
    </>
  );
}
