"use client";
import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import { appendUniquePage, hasNextPage } from "@/lib/pagination";
import type { MerchantProduct } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const PAGE_SIZE = 50;

export function MerchantProducts() {
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [retry, setRetry] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [demo, setDemo] = useState(false);
  const [error, setError] = useState("");
  const [failure, setFailure] = useState<unknown>(null);
  const [busy, setBusy] = useState<number | null>(null);
  const { user, loading } = useSession();
  // biome-ignore lint/correctness/useExhaustiveDependencies: retry intentionally restarts a failed request.
  useEffect(() => {
    if (loading) return;
    setFailure(null);
    if (user?.isDemo === true) {
      setProducts(demoMerchantProducts);
      setDemo(true);
      setHasMore(false);
      setLoadingProducts(false);
      return;
    }
    let cancelled = false;
    async function loadPage() {
      setLoadingProducts(true);
      const params = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
      if (query.trim()) params.set("keyword", query.trim());
      try {
        const result = await apiClient<MerchantProduct[]>(`/api/backend/merchant/products?${params.toString()}`);
        if (cancelled) return;
        setProducts((current) => page === 1 ? result : appendUniquePage(current, result, (item) => item.id));
        setHasMore(hasNextPage(result.length, PAGE_SIZE));
        setDemo(false);
      } catch (caught) {
        if (!cancelled) {
          if (page === 1) setProducts([]);
          setFailure(caught);
        }
      } finally {
        if (!cancelled) setLoadingProducts(false);
      }
    }
    const timer = window.setTimeout(() => void loadPage(), page === 1 ? 180 : 0);
    return () => { cancelled = true; window.clearTimeout(timer); };
  }, [loading, user?.isDemo, query, page, retry]);
  const visible = useMemo(
    () => demo ? products.filter((p) => p.name.toLowerCase().includes(query.toLowerCase())) : products,
    [products, query, demo],
  );

  function changeQuery(value: string) {
    setQuery(value);
    if (demo) return;
    setPage(1);
    setProducts([]);
    setHasMore(false);
    setFailure(null);
    setLoadingProducts(true);
  }
  async function toggle(product: MerchantProduct) {
    if (demo) return;
    setBusy(product.id);
    setError("");
    const publish = product.status !== "ON_SALE";
    try {
      await apiClient<null>(
        `/api/backend/merchant/products/${product.id}/${publish ? "publish" : "unpublish"}`,
        { method: "POST" },
      );
      setProducts((current) =>
        current.map((p) => (p.id === product.id ? { ...p, status: publish ? "ON_SALE" : "OFF_SALE" } : p)),
      );
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "操作失败。");
    } finally {
      setBusy(null);
    }
  }
  return (
    <MerchantShell
      title="商品管理"
      eyebrow="CATALOG / PRODUCTS"
      actions={
        <Link className="button primary" href="/merchant/products/new">
          ＋ 新增商品
        </Link>
      }
    >
      {demo && <DemoNotice>当前展示演示目录，演示条目不可执行上下架。</DemoNotice>}
      {failure && page === 1 ? <RequestFailure error={failure} loginHref="/merchant/login?redirect=/merchant/products" onRetry={() => setRetry((value) => value + 1)} title="商品目录暂时无法读取" /> : null}
      {!failure || page > 1 ? <>
      <div className="merchant-toolbar surface">
        <input aria-label="搜索商品名称" value={query} onChange={(event) => changeQuery(event.target.value)} placeholder="搜索全部商品名称" />
        <span className="eyebrow">{loadingProducts && page === 1 ? "LOADING" : `${visible.length} LOADED`}</span>
      </div>
      {error && <p className="form-error">{error}</p>}
      {loadingProducts && page === 1 ? <div className="empty-state"><p>正在读取商品目录…</p></div> : null}
      {!loadingProducts && !failure && !visible.length ? <div className="empty-state"><h2>没有找到商品。</h2><p>换一个名称试试，或新增商品。</p></div> : null}
      {visible.length ?
      <div className="table-scroll">
        <table className="data-table data-table--responsive">
          <thead>
            <tr>
              <th>商品</th>
              <th>售价</th>
              <th>SKU</th>
              <th>库存</th>
              <th>状态</th>
              <th>动作</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((product) => (
              <tr key={product.id}>
                <td data-label="商品">
                  <div>
                    <strong>{product.name}</strong>
                    <small>{product.description}</small>
                  </div>
                </td>
                <td data-label="售价">{currency(product.minSalePrice)}</td>
                <td data-label="SKU">{product.skuCount}</td>
                <td data-label="库存">{product.totalAvailableStock}</td>
                <td data-label="状态">
                  <StatusPill status={product.status} />
                </td>
                <td data-label="动作">
                  <button
                    disabled={demo || busy === product.id}
                    onClick={() => void toggle(product)}
                    type="button"
                  >
                    {busy === product.id ? "处理中…" : product.status === "ON_SALE" ? "下架" : "上架"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      : null}
      {page > 1 && failure ? <p className="form-error" role="alert">后续商品暂时无法读取。请重试。</p> : null}
      {!demo && hasMore ? <div className="merchant-toolbar"><button className="button" disabled={loadingProducts} onClick={() => failure ? setRetry((value) => value + 1) : setPage((value) => value + 1)} type="button">{loadingProducts ? "加载中…" : failure ? "重试加载更多" : "加载更多商品"}</button></div> : null}
      </> : null}
    </MerchantShell>
  );
}
