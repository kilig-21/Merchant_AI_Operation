"use client";

import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import type { MerchantDashboardMetrics, MerchantDashboardTrendPoint, MerchantProduct } from "@/lib/types";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantCharts } from "./MerchantCharts";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

type DateRange = { startDate: string; endDate: string };

function toDateInputValue(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function createDefaultRange(): DateRange {
  const end = new Date();
  const start = new Date(end);
  start.setDate(end.getDate() - 6);
  return { startDate: toDateInputValue(start), endDate: toDateInputValue(end) };
}

function rangeLabel(range: DateRange) {
  return `${range.startDate.replaceAll("-", ".")} — ${range.endDate.replaceAll("-", ".")}`;
}

export function MerchantDashboard() {
  const [range, setRange] = useState<DateRange>(createDefaultRange);
  const [activeRange, setActiveRange] = useState<DateRange>(createDefaultRange);
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [metrics, setMetrics] = useState<MerchantDashboardMetrics | null>(null);
  const [trends, setTrends] = useState<MerchantDashboardTrendPoint[]>([]);
  const [demo, setDemo] = useState(false);
  const [loadingDashboard, setLoadingDashboard] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const { user, loading } = useSession();

  const loadDashboard = useCallback(async () => {
    if (loading) return;

    setFailure(null);
    setLoadingDashboard(true);

    if (user?.isDemo === true) {
      setProducts(demoMerchantProducts);
      setMetrics(null);
      setTrends([]);
      setDemo(true);
      setLoadingDashboard(false);
      return;
    }

    try {
      const query = new URLSearchParams(activeRange).toString();
      const [nextProducts, nextMetrics, nextTrends] = await Promise.all([
        apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=8"),
        apiClient<MerchantDashboardMetrics>(`/api/backend/merchant/dashboard/metrics?${query}`),
        apiClient<MerchantDashboardTrendPoint[]>(`/api/backend/merchant/dashboard/trends?${query}`),
      ]);
      setProducts(nextProducts);
      setMetrics(nextMetrics);
      setTrends(nextTrends);
      setDemo(false);
    } catch (caught) {
      setProducts([]);
      setMetrics(null);
      setTrends([]);
      setDemo(false);
      setFailure(caught);
    } finally {
      setLoadingDashboard(false);
    }
  }, [activeRange, loading, user?.isDemo]);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const applyRange = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setActiveRange({ ...range });
  };

  const retry = () => setActiveRange({ ...activeRange });
  const period = rangeLabel(activeRange);

  return (
    <MerchantShell
      title="经营概览"
      eyebrow="OPERATIONS / LIVE PULSE"
      actions={
        <Link className="button primary" href="/merchant/products/new">
          ＋ 新增商品
        </Link>
      }
    >
      {demo ? <DemoNotice>当前为显式演示会话；经营数字与趋势仅用于展示，不能视为真实数据。</DemoNotice> : null}
      {failure ? (
        <RequestFailure
          error={failure}
          loginHref="/merchant/login?redirect=/merchant/dashboard"
          onRetry={retry}
          title="经营概览暂时无法读取"
        />
      ) : null}
      {!failure ? (
        <>
          <form className="dashboard-range surface" onSubmit={applyRange}>
            <div>
              <span className="eyebrow">REPORTING WINDOW</span>
              <strong>{period}</strong>
            </div>
            <label>
              <span>开始日期</span>
              <input
                aria-label="经营数据开始日期"
                max={range.endDate}
                onChange={(event) => setRange((current) => ({ ...current, startDate: event.target.value }))}
                type="date"
                value={range.startDate}
              />
            </label>
            <label>
              <span>结束日期</span>
              <input
                aria-label="经营数据结束日期"
                min={range.startDate}
                onChange={(event) => setRange((current) => ({ ...current, endDate: event.target.value }))}
                type="date"
                value={range.endDate}
              />
            </label>
            <button className="button" disabled={loadingDashboard} type="submit">
              {loadingDashboard ? "读取中…" : "更新数据"}
            </button>
          </form>

          <section className="metrics" aria-busy={loadingDashboard} aria-label="真实经营汇总">
            <article className="metric">
              <span>有效订单</span>
              <strong>{metrics ? metrics.validOrderCount : "—"}</strong>
              <small>{period}</small>
            </article>
            <article className="metric">
              <span>已支付营业额</span>
              <strong>{metrics ? currency(metrics.paidRevenue) : "—"}</strong>
              <small>按下单日归因</small>
            </article>
            <article className="metric">
              <span>待支付订单</span>
              <strong>{metrics ? metrics.pendingPaymentCount : "—"}</strong>
              <small>当前待处理</small>
            </article>
            <article className="metric">
              <span>低库存商品</span>
              <strong>{metrics ? metrics.lowStockProductCount : "—"}</strong>
              <small>当前库存快照</small>
            </article>
          </section>

          <MerchantCharts demo={demo} loading={loadingDashboard} products={products} rangeLabel={period} trends={trends} />

          <section className="merchant-grid">
            <article className="panel surface">
              <span className="eyebrow">CATALOG PULSE</span>
              <h2>最近商品</h2>
              {loadingDashboard ? <p className="chart-empty">正在读取真实商品目录…</p> : null}
              {!loadingDashboard && !products.length ? <p className="chart-empty">当前店铺还没有商品。</p> : null}
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
            <article className="panel surface dashboard-actions">
              <span className="eyebrow">NEXT ACTION</span>
              <h2>经营待办</h2>
              <div className="merchant-row">
                <span>{metrics?.lowStockProductCount ? `处理 ${metrics.lowStockProductCount} 个低库存商品` : "当前没有低库存商品"}</span>
              </div>
              <div className="merchant-row">
                <span>{metrics?.pendingPaymentCount ? `跟进 ${metrics.pendingPaymentCount} 笔待支付订单` : "当前没有待支付订单"}</span>
              </div>
              <Link className="merchant-row merchant-row--link" href="/merchant/orders">
                <span>查看本店真实订单</span>
                <span aria-hidden="true">↗</span>
              </Link>
            </article>
          </section>
        </>
      ) : null}
    </MerchantShell>
  );
}
