import { DemoNotice } from "@/components/DemoNotice";
import { ProductGridClient } from "@/components/ProductGridClient";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getPublicProducts } from "@/lib/backend";
import { demoProductsForStore, storeFor } from "@/lib/demo-data";
import Link from "next/link";

export const dynamic = "force-dynamic";
export default async function StoreProductsPage({ params }: { params: Promise<{ storeId: string }> }) {
  const storeId = Number((await params).storeId) || 1001;
  const store = storeFor(storeId);
  let products = demoProductsForStore(storeId);
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
            <Link className="eyebrow" href={`/stores/${store.id}`}>← {store.name}</Link>
            <h1>
              从这间店里，选一些真正会
              <br />
              陪你生活的东西。
            </h1>
          </div>
          <p>{store.description}</p>
        </header>
        {demo && <DemoNotice />}
        <ProductGridClient products={products} storeId={store.id} />
      </main>
      <SiteFooter />
    </>
  );
}
