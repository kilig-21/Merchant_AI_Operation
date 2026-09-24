"use client";

import { apiClient } from "@/lib/client-api";
import { appendUniquePage, hasNextPage } from "@/lib/pagination";
import type { MarketplaceProduct, PublicStoreSummary } from "@/lib/types";
import { useEffect, useState } from "react";
import { ProductCard } from "./ProductCard";
import { RequestFailure } from "./RequestFailure";

const PAGE_SIZE = 48;

export function MarketplaceSearchClient({ stores, initialQuery = "", initialStoreId = "all" }: { stores: PublicStoreSummary[]; initialQuery?: string; initialStoreId?: string }) {
  const [query, setQuery] = useState(initialQuery);
  const [storeId, setStoreId] = useState(initialStoreId);
  const [page, setPage] = useState(1);
  const [retry, setRetry] = useState(0);
  const [products, setProducts] = useState<MarketplaceProduct[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);

  // biome-ignore lint/correctness/useExhaustiveDependencies: retry intentionally restarts a failed request.
  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setFailure(null);
      const params = new URLSearchParams({ keyword: query.trim(), page: String(page), size: String(PAGE_SIZE) });
      if (storeId !== "all") params.set("storeId", storeId);
      try {
        const result = await apiClient<MarketplaceProduct[]>(`/api/backend/public/stores/products/search?${params.toString()}`);
        if (cancelled) return;
        setProducts((current) => page === 1 ? result : appendUniquePage(current, result, (item) => `${item.storeId}-${item.id}`));
        setHasMore(hasNextPage(result.length, PAGE_SIZE));
      } catch (caught) {
        if (!cancelled) {
          if (page === 1) setProducts([]);
          setFailure(caught);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    const timer = window.setTimeout(() => void load(), page === 1 ? 180 : 0);
    return () => { cancelled = true; window.clearTimeout(timer); };
  }, [query, storeId, page, retry]);

  function resetSearch(nextQuery: string, nextStoreId: string) {
    setQuery(nextQuery);
    setStoreId(nextStoreId);
    setPage(1);
    setProducts([]);
    setHasMore(false);
    setFailure(null);
    setLoading(true);
  }

  return (
    <>
      <section className="market-search-panel">
        <label className="market-search-panel__input"><span>搜索整个 Morrow 市集</span><input value={query} onChange={(event) => resetSearch(event.target.value, storeId)} placeholder="商品或店铺名称" /></label>
        <label><span>店铺</span><select value={storeId} onChange={(event) => resetSearch(query, event.target.value)}><option value="all">全部店铺</option>{stores.map((store) => <option value={store.id} key={store.id}>{store.name}</option>)}</select></label>
      </section>
      <div className="market-results-head"><span className="eyebrow">{loading && page === 1 ? "SEARCHING" : `${products.length} OBJECTS LOADED`} / {storeId === "all" ? "ALL STORES" : `STORE ${storeId}`}</span>{(query || storeId !== "all") ? <button type="button" onClick={() => resetSearch("", "all")}>清除条件</button> : null}</div>
      {loading && page === 1 ? <div className="market-empty"><p>正在搜索公开商品…</p></div> : null}
      {!loading && failure && page === 1 ? <RequestFailure error={failure} onRetry={() => setRetry((value) => value + 1)} title="公开搜索暂时不可用" /> : null}
      {products.length ? <div className="product-grid market-product-grid">{products.map((product) => <ProductCard key={`${product.storeId}-${product.id}`} product={product} storeId={product.storeId} storeName={product.storeName} />)}</div> : null}
      {!loading && !failure && !products.length ? <div className="market-empty"><span className="eyebrow">0 RESULTS</span><h2>没有找到相符的物件。</h2><p>试试更短的关键词，或清除店铺条件。</p></div> : null}
      {page > 1 && failure ? <p className="form-error" role="alert">后续商品暂时无法读取。请重试。</p> : null}
      {hasMore ? <div className="market-results-head"><button className="button" disabled={loading} onClick={() => failure ? setRetry((value) => value + 1) : setPage((value) => value + 1)} type="button">{loading ? "加载中…" : failure ? "重试加载更多" : "加载更多商品"}</button></div> : null}
    </>
  );
}
