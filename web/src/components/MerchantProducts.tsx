"use client";
import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import type { MerchantProduct } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";
export function MerchantProducts() {
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [query, setQuery] = useState("");
  const [demo, setDemo] = useState(false);
  const [error, setError] = useState("");
  const [failure, setFailure] = useState<unknown>(null);
  const [busy, setBusy] = useState<number | null>(null);
  const { user, loading } = useSession();
  async function load() {
    setError("");
    try {
      setProducts(
        await apiClient<MerchantProduct[]>(
          `/api/backend/merchant/products?page=1&size=50${query ? `&keyword=${encodeURIComponent(query)}` : ""}`,
        ),
      );
      setDemo(false);
    } catch (caught) {
      setProducts([]);
      setDemo(false);
      setFailure(caught);
    }
  }
  useEffect(() => {
    if (loading) return;
    setFailure(null);
    if (user?.isDemo === true) {
      setProducts(demoMerchantProducts);
      setDemo(true);
      return;
    }
    apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=50")
      .then((result) => {
        setProducts(result);
        setDemo(false);
      })
      .catch((caught) => {
        setProducts([]);
        setDemo(false);
        setFailure(caught);
      });
  }, [loading, user?.isDemo]);
  const visible = useMemo(
    () => products.filter((p) => p.name.toLowerCase().includes(query.toLowerCase())),
    [products, query],
  );
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
      {failure ? <RequestFailure error={failure} loginHref="/merchant/login?redirect=/merchant/products" onRetry={() => window.location.reload()} title="商品目录暂时无法读取" /> : null}
      {!failure ? <>
      <div className="merchant-toolbar surface">
        <input aria-label="搜索商品名称" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="搜索商品名称" />
        <span className="eyebrow">{visible.length} RESULTS</span>
      </div>
      {error && <p className="form-error">{error}</p>}
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
      </> : null}
    </MerchantShell>
  );
}
