"use client";

import { apiClient } from "@/lib/client-api";
import { appendUniquePage, hasNextPage } from "@/lib/pagination";
import type { ProductSummary } from "@/lib/types";
import { useEffect, useRef, useState } from "react";
import { ProductCard } from "./ProductCard";
import { RequestFailure } from "./RequestFailure";

const PAGE_SIZE = 48;

export function ProductGridClient({ products, storeId, storeName }: { products: ProductSummary[]; storeId: number; storeName?: string }) {
  const [items, setItems] = useState(products);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [retry, setRetry] = useState(0);
  const [hasMore, setHasMore] = useState(hasNextPage(products.length, PAGE_SIZE));
  const [loading, setLoading] = useState(false);
  const [failure, setFailure] = useState<unknown>(null);
  const firstRender = useRef(true);

  // biome-ignore lint/correctness/useExhaustiveDependencies: retry intentionally restarts a failed request.
  useEffect(() => {
    if (firstRender.current) {
      firstRender.current = false;
      return;
    }
    let cancelled = false;
    async function load() {
      setLoading(true);
      setFailure(null);
      const keyword = query.trim();
      const params = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
      if (keyword) {
        params.set("keyword", keyword);
        params.set("storeId", String(storeId));
      }
      const path = keyword
        ? `/api/backend/public/stores/products/search?${params.toString()}`
        : `/api/backend/public/stores/${storeId}/products?${params.toString()}`;
      try {
        const result = await apiClient<ProductSummary[]>(path);
        if (cancelled) return;
        setItems((current) => page === 1 ? result : appendUniquePage(current, result, (item) => item.id));
        setHasMore(hasNextPage(result.length, PAGE_SIZE));
      } catch (caught) {
        if (!cancelled) {
          if (page === 1) setItems([]);
          setFailure(caught);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    const timer = window.setTimeout(() => void load(), page === 1 ? 180 : 0);
    return () => { cancelled = true; window.clearTimeout(timer); };
  }, [query, page, retry, storeId]);

  function updateQuery(value: string) {
    setQuery(value);
    setPage(1);
    setItems([]);
    setHasMore(false);
    setFailure(null);
    setLoading(true);
  }

  return (
    <>
      <div className="catalog-toolbar">
        <span className="eyebrow">已加载 {items.length} 件店内商品</span>
        <input
          value={query}
          onChange={(event) => updateQuery(event.target.value)}
          placeholder="搜索店内全部商品"
          aria-label="搜索商品"
        />
      </div>
      {loading && page === 1 ? <div className="empty-state"><p>正在搜索店内商品…</p></div> : null}
      {!loading && failure && page === 1 ? <RequestFailure error={failure} onRetry={() => setRetry((value) => value + 1)} title="店铺商品暂时无法读取" /> : null}
      {items.length ? (
        <div className="product-grid">
          {items.map((product) => (
            <ProductCard key={product.id} product={product} storeId={storeId} storeName={storeName} />
          ))}
        </div>
      ) : !loading && !failure ? (
        <div className="empty-state">
          <h2>没有找到相符商品</h2>
          <p>换一个关键词试试。</p>
        </div>
      ) : null}
      {page > 1 && failure ? <p className="form-error" role="alert">后续商品暂时无法读取。请重试。</p> : null}
      {hasMore ? <div className="market-results-head"><button className="button" disabled={loading} onClick={() => failure ? setRetry((value) => value + 1) : setPage((value) => value + 1)} type="button">{loading ? "加载中…" : failure ? "重试加载更多" : "加载更多商品"}</button></div> : null}
    </>
  );
}
