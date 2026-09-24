"use client";

import { apiClient } from "@/lib/client-api";
import { currency, demoMerchantProducts } from "@/lib/demo-data";
import type {
  AfterSaleRate,
  MerchantDashboardTrendPoint,
  MerchantOperatingSummary,
  MerchantProduct,
  PromotionPerformance,
  TopProduct,
} from "@/lib/types";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantCharts } from "./MerchantCharts";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

type DateRange = { startDate: string; endDate: string };
const dashboardSections = ["商品目录", "经营汇总", "趋势图", "热销商品", "促销表现", "售后申请率"] as const;

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
  const [metrics, setMetrics] = useState<MerchantOperatingSummary | null>(null);
  const [trends, setTrends] = useState<MerchantDashboardTrendPoint[]>([]);
  const [topProducts, setTopProducts] = useState<TopProduct[]>([]);
  const [promotions, setPromotions] = useState<PromotionPerformance[]>([]);
  const [afterSale, setAfterSale] = useState<AfterSaleRate | null>(null);
  const [demo, setDemo] = useState(false);
  const [loadingDashboard, setLoadingDashboard] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const [unavailableSections, setUnavailableSections] = useState<string[]>([]);
  const { user, loading } = useSession();

  const loadDashboard = useCallback(async () => {
    if (loading) return;

    setFailure(null);
    setUnavailableSections([]);
    setLoadingDashboard(true);
    setProducts([]);
    setMetrics(null);
    setTrends([]);
    setTopProducts([]);
    setPromotions([]);
    setAfterSale(null);

    if (user?.isDemo === true) {
      setProducts(demoMerchantProducts);
      setDemo(true);
      setLoadingDashboard(false);
      return;
    }

    try {
      const query = new URLSearchParams(activeRange).toString();
      const results = await Promise.allSettled([
        apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=8"),
        apiClient<MerchantOperatingSummary>(`/api/backend/merchant/analytics/summary?${query}`),
        apiClient<MerchantDashboardTrendPoint[]>(`/api/backend/merchant/dashboard/trends?${query}`),
        apiClient<TopProduct[]>(`/api/backend/merchant/analytics/top-products?${query}&limit=5`),
        apiClient<PromotionPerformance[]>(`/api/backend/merchant/analytics/promotions?${query}&limit=5`),
        apiClient<AfterSaleRate>(`/api/backend/merchant/analytics/after-sale-rate?${query}`),
      ]);
      const [nextProducts, nextMetrics, nextTrends, nextTopProducts, nextPromotions, nextAfterSale] = results;
      setProducts(nextProducts.status === "fulfilled" ? nextProducts.value : []);
      setMetrics(nextMetrics.status === "fulfilled" ? nextMetrics.value : null);
      setTrends(nextTrends.status === "fulfilled" ? nextTrends.value : []);
      setTopProducts(nextTopProducts.status === "fulfilled" ? nextTopProducts.value : []);
      setPromotions(nextPromotions.status === "fulfilled" ? nextPromotions.value : []);
      setAfterSale(nextAfterSale.status === "fulfilled" ? nextAfterSale.value : null);
      const unavailable = results.flatMap((result, index) => result.status === "rejected" ? [dashboardSections[index]] : []);
      setUnavailableSections(unavailable);
      if (unavailable.length === results.length) setFailure(nextProducts.status === "rejected" ? nextProducts.reason : new Error("经营数据暂时无法读取。"));
      setDemo(false);
    } catch (caught) {
      setProducts([]);
      setMetrics(null);
      setTrends([]);
      setTopProducts([]);
      setPromotions([]);
      setAfterSale(null);
      setUnavailableSections([]);
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
      {!failure && unavailableSections.length > 0 ? (
        <div className="dashboard-partial-warning" role="alert">
          <span>{unavailableSections.join("、")}暂时无法读取；其余区域仍显示真实数据。</span>
          <button className="button" disabled={loadingDashboard} onClick={retry} type="button">{loadingDashboard ? "读取中…" : "重试缺失数据"}</button>
        </div>
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

          <section className="metrics metrics--analytics" aria-busy={loadingDashboard} aria-label="真实经营汇总">
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
              <span>客单价</span>
              <strong>{metrics ? currency(metrics.averageOrderValue) : "—"}</strong>
              <small>{metrics ? `${metrics.paidOrderCount} 笔已支付订单` : "已支付营业额 ÷ 订单数"}</small>
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

          <MerchantCharts
            demo={demo}
            loading={loadingDashboard}
            products={products}
            productsUnavailable={unavailableSections.includes("商品目录")}
            rangeLabel={period}
            trends={trends}
            trendsUnavailable={unavailableSections.includes("趋势图")}
          />

          <section className="analytics-insights" aria-label="经营洞察">
            <article className="panel surface analytics-panel">
              <span className="eyebrow">TOP PRODUCTS</span>
              <h2>热销 SKU</h2>
              {!loadingDashboard && unavailableSections.includes("热销商品") ? <p className="chart-empty">热销商品暂时无法读取。</p> : null}
              {!loadingDashboard && !unavailableSections.includes("热销商品") && !topProducts.length ? <p className="chart-empty">{demo ? "演示会话不提供真实热销数据。" : "该范围内暂无已支付商品。"}</p> : null}
              {topProducts.map((product, index) => (
                <div className="merchant-row" key={product.skuId}>
                  <div>
                    <strong>{String(index + 1).padStart(2, "0")} · {product.skuName}</strong>
                    <small>{product.soldQuantity} 件 · {currency(product.paidRevenue)}</small>
                  </div>
                </div>
              ))}
            </article>

            <article className="panel surface analytics-panel">
              <span className="eyebrow">CAMPAIGN SIGNAL</span>
              <h2>促销表现</h2>
              {!loadingDashboard && unavailableSections.includes("促销表现") ? <p className="chart-empty">促销表现暂时无法读取。</p> : null}
              {!loadingDashboard && !unavailableSections.includes("促销表现") && !promotions.length ? <p className="chart-empty">{demo ? "演示会话不提供真实促销数据。" : "该范围内暂无促销资格记录。"}</p> : null}
              {promotions.map((promotion) => (
                <div className="merchant-row" key={promotion.activityId}>
                  <div>
                    <strong>{promotion.activityName}</strong>
                    <small>{promotion.successfulQuantity} 件 · {currency(promotion.promotionRevenue)}</small>
                  </div>
                  <span className="analytics-rate">{Math.round(promotion.orderConversionRate * 100)}%</span>
                </div>
              ))}
            </article>

            <article className="panel surface analytics-panel analytics-panel--rate">
              <span className="eyebrow">AFTER-SALE PULSE</span>
              <h2>售后申请率</h2>
              <strong className="analytics-rate-hero">{afterSale ? `${Math.round(afterSale.afterSaleRate * 100)}%` : "—"}</strong>
              <p>
                {afterSale
                  ? `${afterSale.afterSaleOrderItemCount} / ${afterSale.paidOrderItemCount} 个已支付订单明细曾发起售后`
                  : loadingDashboard ? "正在读取售后数据…" : demo ? "演示会话不提供真实售后数据。" : "售后申请率暂时无法读取或暂无数据。"}
              </p>
              <small>申请率不等于退款率或退款完成率</small>
            </article>
          </section>

          <section className="merchant-grid">
            <article className="panel surface">
              <span className="eyebrow">CATALOG PULSE</span>
              <h2>最近商品</h2>
              {loadingDashboard ? <p className="chart-empty">正在读取真实商品目录…</p> : null}
              {!loadingDashboard && unavailableSections.includes("商品目录") ? <p className="chart-empty">商品目录暂时无法读取。</p> : null}
              {!loadingDashboard && !unavailableSections.includes("商品目录") && !products.length ? <p className="chart-empty">当前店铺还没有商品。</p> : null}
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
                <span>{demo ? "演示会话不展示真实库存待办" : metrics ? metrics.lowStockProductCount ? `处理 ${metrics.lowStockProductCount} 个低库存商品` : "当前没有低库存商品" : "库存汇总暂时无法读取"}</span>
              </div>
              <div className="merchant-row">
                <span>{demo ? "演示会话不展示真实订单待办" : metrics ? metrics.pendingPaymentCount ? `跟进 ${metrics.pendingPaymentCount} 笔待支付订单` : "当前没有待支付订单" : "订单汇总暂时无法读取"}</span>
              </div>
              <Link className="merchant-row merchant-row--link" href="/merchant/orders">
                <span>查看本店真实订单</span>
                <span aria-hidden="true">↗</span>
              </Link>
            </article>
          </section>

          <section className="analytics-reference">
            <article className="panel surface metric-guide">
              <span className="eyebrow">METRIC NOTES</span>
              <h2>这些数字如何计算</h2>
              <div className="metric-guide-grid">
                <p><strong>客单价</strong><span>已支付营业额 ÷ 已支付订单数，按订单创建日归因。</span></p>
                <p><strong>热销商品</strong><span>仅统计已支付订单，按销量、销售额、SKU ID 稳定排序。</span></p>
                <p><strong>促销表现</strong><span>统计资格成功创建订单的金额，不代表订单已经支付。</span></p>
                <p><strong>售后申请率</strong><span>曾发起售后的明细数 ÷ 已支付订单明细数。</span></p>
              </div>
            </article>

            <article className="panel surface common-questions">
              <span className="eyebrow">AI ASSISTANT / A3</span>
              <h2>常问问题</h2>
              <p>真实商家账号可在明确日期范围内查询本店经营汇总；助手不会执行店铺操作。演示账号仅可预览界面。</p>
              <ul>
                <li>帮我写一段新品上架公告</li>
                <li>给我三条通用的店铺经营建议</li>
                <li>如何更清晰地回复顾客售后问题？</li>
              </ul>
              <Link className="button" href="/merchant/ai">进入 AI 经营助手 ↗</Link>
            </article>
          </section>
        </>
      ) : null}
    </MerchantShell>
  );
}
