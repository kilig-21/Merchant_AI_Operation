"use client";
import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import type { MerchantProduct } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { StatusPill } from "./StatusPill";
export function MerchantProducts() {
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [query, setQuery] = useState("");
  const [demo, setDemo] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState<number | null>(null);
  async function load() {
    setError("");
    try {
      setProducts(
        await apiClient<MerchantProduct[]>(
          `/api/backend/merchant/products?page=1&size=50${query ? `&keyword=${encodeURIComponent(query)}` : ""}`,
        ),
      );
      setDemo(false);
    } catch {
      setProducts(demoMerchantProducts);
      setDemo(true);
    }
  }
  useEffect(() => {
    apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=50")
      .then((result) => {
        setProducts(result);
        setDemo(false);
      })
      .catch(() => {
        setProducts(demoMerchantProducts);
        setDemo(true);
      });
  }, []);
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
      <div className="merchant-toolbar surface">
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="搜索商品名称" />
        <span className="eyebrow">{visible.length} RESULTS</span>
      </div>
      {error && <p className="form-error">{error}</p>}
      <div className="table-scroll">
        <table className="data-table">
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
                <td>
                  <strong>{product.name}</strong>
                  <br />
                  <small>{product.description}</small>
                </td>
                <td>{currency(product.minSalePrice)}</td>
                <td>{product.skuCount}</td>
                <td>{product.totalAvailableStock}</td>
                <td>
                  <StatusPill status={product.status} />
                </td>
                <td>
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
    </MerchantShell>
  );
}
