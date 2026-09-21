"use client";

import { apiClient } from "@/lib/client-api";
import type { MarketplaceProduct, PublicStoreSummary } from "@/lib/types";
import { useEffect, useState } from "react";
import { ProductCard } from "./ProductCard";
import { RequestFailure } from "./RequestFailure";

export function MarketplaceSearchClient({ stores, initialQuery = "", initialStoreId = "all" }: { stores: PublicStoreSummary[]; initialQuery?: string; initialStoreId?: string }) {
  const [query, setQuery] = useState(initialQuery);
  const [storeId, setStoreId] = useState(initialStoreId);
  const [products, setProducts] = useState<MarketplaceProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setFailure(null);
      const params = new URLSearchParams({ keyword: query.trim(), page: "1", size: "48" });
      if (storeId !== "all") params.set("storeId", storeId);
      try {
        const result = await apiClient<MarketplaceProduct[]>(`/api/backend/public/stores/products/search?${params.toString()}`);
        if (!cancelled) setProducts(result);
      } catch (caught) {
        if (!cancelled) {
          setProducts([]);
          setFailure(caught);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    const timer = window.setTimeout(() => void load(), 180);
    return () => { cancelled = true; window.clearTimeout(timer); };
  }, [query, storeId]);

  return (
    <>
      <section className="market-search-panel">
        <label className="market-search-panel__input"><span>搜索整个 Morrow 市集</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="商品或店铺名称" /></label>
        <label><span>店铺</span><select value={storeId} onChange={(event) => setStoreId(event.target.value)}><option value="all">全部店铺</option>{stores.map((store) => <option value={store.id} key={store.id}>{store.name}</option>)}</select></label>
      </section>
      <div className="market-results-head"><span className="eyebrow">{loading ? "SEARCHING" : `${products.length} OBJECTS`} / {storeId === "all" ? "ALL STORES" : `STORE ${storeId}`}</span>{(query || storeId !== "all") ? <button type="button" onClick={() => { setQuery(""); setStoreId("all"); }}>清除条件</button> : null}</div>
      {loading ? <div className="market-empty"><p>正在搜索公开商品…</p></div> : null}
      {!loading && failure ? <RequestFailure error={failure} onRetry={() => window.location.reload()} title="公开搜索暂时不可用" /> : null}
      {!loading && !failure && products.length ? <div className="product-grid market-product-grid">{products.map((product) => <ProductCard key={`${product.storeId}-${product.id}`} product={product} storeId={product.storeId} storeName={product.storeName} />)}</div> : null}
      {!loading && !failure && !products.length ? <div className="market-empty"><span className="eyebrow">0 RESULTS</span><h2>没有找到相符的物件。</h2><p>试试更短的关键词，或清除店铺条件。</p></div> : null}
    </>
  );
}
