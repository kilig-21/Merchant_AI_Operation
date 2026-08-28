"use client";
import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import type { MerchantProduct } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantCharts } from "./MerchantCharts";
import { MerchantShell } from "./MerchantShell";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";
export function MerchantDashboard() {
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [demo, setDemo] = useState(false);
  const { user, loading } = useSession();
  useEffect(() => {
    if (loading) return;
    if ((user?.id ?? 0) >= 99000) {
      setProducts(demoMerchantProducts);
      setDemo(true);
      return;
    }
    apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=8")
      .then(setProducts)
      .catch(() => {
        setProducts(demoMerchantProducts);
        setDemo(true);
      });
  }, [loading, user?.id]);
  const onSale = useMemo(() => products.filter((p) => p.status === "ON_SALE").length, [products]);
  const stock = useMemo(() => products.reduce((sum, p) => sum + p.totalAvailableStock, 0), [products]);
  return (
    <MerchantShell
      title="经营概览"
      eyebrow="TODAY / OVERVIEW"
      actions={
        <Link className="button primary" href="/merchant/products/new">
          ＋ 新增商品
        </Link>
      }
    >
      {demo && <DemoNotice>服务未连接，经营概览显示演示数据。</DemoNotice>}
      <section className="metrics">
        <article className="metric">
          <span>商品总数</span>
          <strong>{products.length}</strong>
          <small>当前目录</small>
        </article>
        <article className="metric">
          <span>上架商品</span>
          <strong>{onSale}</strong>
          <small>消费者可见</small>
        </article>
        <article className="metric">
          <span>可售库存</span>
          <strong>{stock}</strong>
          <small>全部 SKU</small>
        </article>
        <article className="metric">
          <span>今日待办</span>
          <strong>03</strong>
          <small>静态预览</small>
        </article>
      </section>
      <MerchantCharts products={products} demo={demo} />
      <section className="merchant-grid">
        <article className="panel surface">
          <span className="eyebrow">CATALOG PULSE</span>
          <h2>最近商品</h2>
          {products.slice(0, 5).map((product) => (
            <div className="merchant-row" key={product.id}>
              <div>
                <strong>{product.name}</strong>
                <small>
                  {product.skuCount} SKU · {currency(product.minSalePrice)}
                </small>
              </div>
              <StatusPill status={product.status} />
            </div>
          ))}
        </article>
        <article className="panel surface">
          <span className="eyebrow">NEXT ACTION</span>
          <h2>经营待办</h2>
          {["补充低库存商品", "完善新品视觉素材", "回看本周订单趋势"].map((item) => (
            <label className="merchant-row" key={item}>
              <span>
                <input type="checkbox" /> {item}
              </span>
            </label>
          ))}
        </article>
      </section>
    </MerchantShell>
  );
}
