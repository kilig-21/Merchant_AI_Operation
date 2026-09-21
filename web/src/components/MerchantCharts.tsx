"use client";

import type { MerchantDashboardTrendPoint, MerchantProduct } from "@/lib/types";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

export interface InteractiveTrendPoint {
  date: string;
  value: number;
  displayValue: string;
}

type ChartUnit = "orders" | "cny";

interface InteractiveLineChartProps {
  data: InteractiveTrendPoint[];
  unit: ChartUnit;
  demo: boolean;
  animateOnFirstView?: boolean;
  showYAxis?: boolean;
  area?: boolean;
  smooth?: boolean;
  onFirstView?: () => void;
}

interface InventoryPoint {
  productId: number;
  name: string;
  stock: number;
}

interface MerchantChartsProps {
  products: MerchantProduct[];
  trends: MerchantDashboardTrendPoint[];
  rangeLabel: string;
  demo: boolean;
  loading: boolean;
}

const demoOrders: InteractiveTrendPoint[] = [18, 22, 16, 27, 31, 24, 36].map((value, index) => ({
  date: `08.${String(index + 1).padStart(2, "0")}`,
  value,
  displayValue: `${value} 单`,
}));

const demoRevenue: InteractiveTrendPoint[] = [1680, 2290, 1120, 3190, 4380, 2560, 4890].map((value, index) => ({
  date: `08.${String(index + 1).padStart(2, "0")}`,
  value,
  displayValue: `¥${value.toLocaleString("zh-CN")}`,
}));

const chartWidth = 400;
const chartHeight = 210;
const chartPadding = { top: 16, right: 16, bottom: 32, left: 46 };

function formatAxis(value: number, unit: ChartUnit) {
  if (unit === "orders") return String(Math.round(value));
  if (value === 0) return "¥0";
  if (value >= 1000) return `¥${(value / 1000).toFixed(value % 1000 === 0 ? 0 : 1)}k`;
  return `¥${Math.round(value)}`;
}

function formatCurrency(value: number) {
  return `¥${value.toLocaleString("zh-CN", { maximumFractionDigits: 2 })}`;
}

function chartGeometry(data: InteractiveTrendPoint[], unit: ChartUnit) {
  if (!data.length) {
    return { points: [], ticks: [], plotWidth: 1, plotHeight: 1 };
  }

  const rawMaximum = Math.max(...data.map((point) => point.value), 0);
  const baseStep = unit === "orders" ? Math.ceil(rawMaximum / 4) : Math.ceil(rawMaximum / 400) * 100;
  const step = Math.max(unit === "orders" ? 1 : 100, baseStep || 1);
  const maximum = Math.max(Math.ceil(rawMaximum / step) * step, step);
  const plotWidth = chartWidth - chartPadding.left - chartPadding.right;
  const plotHeight = chartHeight - chartPadding.top - chartPadding.bottom;
  const points = data.map((point, index) => ({
    ...point,
    x: chartPadding.left + (index / Math.max(data.length - 1, 1)) * plotWidth,
    y: chartPadding.top + (1 - point.value / maximum) * plotHeight,
  }));
  const ticks = Array.from({ length: 5 }, (_, index) => {
    const value = (maximum / 4) * index;
    return {
      value,
      y: chartPadding.top + (1 - index / 4) * plotHeight,
    };
  }).reverse();
  return { points, ticks, plotWidth, plotHeight };
}

function pathFromPoints(points: Array<{ x: number; y: number }>, smooth: boolean) {
  if (!points.length) return "";
  if (!smooth) return points.map((point, index) => `${index ? "L" : "M"}${point.x},${point.y}`).join(" ");
  return points.reduce((path, point, index) => {
    if (index === 0) return `M${point.x},${point.y}`;
    const previous = points[index - 1];
    const middle = (previous.x + point.x) / 2;
    return `${path} C${middle},${previous.y} ${middle},${point.y} ${point.x},${point.y}`;
  }, "");
}

function useReducedMotion() {
  const [reduced, setReduced] = useState(false);
  useEffect(() => {
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const update = () => setReduced(query.matches);
    update();
    query.addEventListener("change", update);
    return () => query.removeEventListener("change", update);
  }, []);
  return reduced;
}

function InteractiveLineChart({
  data,
  unit,
  demo,
  animateOnFirstView = true,
  showYAxis = true,
  area = false,
  smooth = false,
  onFirstView,
}: InteractiveLineChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const announcedRef = useRef(false);
  const [entered, setEntered] = useState(false);
  const [activeIndex, setActiveIndex] = useState<number | null>(null);
  const reducedMotion = useReducedMotion();
  const geometry = useMemo(() => chartGeometry(data, unit), [data, unit]);
  const path = useMemo(() => pathFromPoints(geometry.points, smooth), [geometry.points, smooth]);
  const baseline = chartHeight - chartPadding.bottom;
  const areaPath = geometry.points.length
    ? `${path} L${geometry.points[geometry.points.length - 1].x},${baseline} L${geometry.points[0].x},${baseline} Z`
    : "";
  const active = activeIndex === null ? null : geometry.points[activeIndex];

  useEffect(() => {
    const container = containerRef.current;
    if (!container || !data.length) return;
    if (!animateOnFirstView || reducedMotion) {
      setEntered(true);
      if (!announcedRef.current) {
        announcedRef.current = true;
        onFirstView?.();
      }
      return;
    }
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting) return;
        setEntered(true);
        if (!announcedRef.current) {
          announcedRef.current = true;
          onFirstView?.();
        }
        observer.disconnect();
      },
      { threshold: 0.28 },
    );
    observer.observe(container);
    return () => observer.disconnect();
  }, [animateOnFirstView, data.length, onFirstView, reducedMotion]);

  const selectFromPointer = (clientX: number) => {
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect || !data.length) return;
    const relative = ((clientX - rect.left) / rect.width) * chartWidth;
    const raw = ((relative - chartPadding.left) / geometry.plotWidth) * Math.max(data.length - 1, 1);
    setActiveIndex(Math.max(0, Math.min(Math.round(raw), data.length - 1)));
  };

  if (!data.length) {
    return <div className="chart-empty chart-empty--panel">该日期范围内暂无真实趋势数据。</div>;
  }

  return (
    <div
      ref={containerRef}
      className={`interactive-line-chart ${entered ? "is-entered" : ""} ${area ? "has-area" : ""}`}
      role="slider"
      aria-label={`${unit === "orders" ? "每日有效订单" : "每日已支付营业额"}趋势图，${demo ? "演示数据" : "真实数据"}`}
      aria-valuemin={0}
      aria-valuemax={Math.max(data.length - 1, 0)}
      aria-valuenow={activeIndex ?? 0}
      aria-valuetext={active ? `${active.date}，${active.displayValue}` : "使用左右方向键浏览数据"}
      tabIndex={0}
      onPointerMove={(event) => selectFromPointer(event.clientX)}
      onPointerDown={(event) => selectFromPointer(event.clientX)}
      onPointerLeave={(event) => {
        if (event.pointerType === "mouse") setActiveIndex(null);
      }}
      onKeyDown={(event) => {
        if (event.key !== "ArrowLeft" && event.key !== "ArrowRight") return;
        event.preventDefault();
        const direction = event.key === "ArrowRight" ? 1 : -1;
        setActiveIndex((current) => Math.max(0, Math.min((current ?? 0) + direction, data.length - 1)));
      }}
    >
      <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} aria-hidden="true">
        {showYAxis &&
          geometry.ticks.map((tick) => (
            <g key={tick.value}>
              <line
                x1={chartPadding.left}
                x2={chartWidth - chartPadding.right}
                y1={tick.y}
                y2={tick.y}
                className="chart-grid-line chart-grid-line--horizontal"
              />
              <text x={chartPadding.left - 8} y={tick.y + 3} className="chart-axis-label" textAnchor="end">
                {formatAxis(tick.value, unit)}
              </text>
            </g>
          ))}
        {unit === "orders" &&
          geometry.points.map((point, index) => (
            <line
              key={`floor-${point.date}`}
              x1={point.x}
              x2={point.x}
              y1={baseline}
              y2={baseline - 7}
              className="chart-calendar-tick"
              style={{ animationDelay: `${index * 12}ms` }}
            />
          ))}
        {area ? <path d={areaPath} className="chart-area-path" /> : null}
        <path d={path} className="chart-hairline-path" pathLength="1" />
        {geometry.points.map((point, index) => (
          <circle
            key={point.date}
            cx={point.x}
            cy={point.y}
            r={index === activeIndex ? 4.8 : unit === "orders" ? 2.8 : 2.5}
            className={`chart-data-dot ${index === activeIndex ? "is-active" : ""}`}
            style={{ animationDelay: `${450 + index * 20}ms` }}
          />
        ))}
        {active ? <line x1={active.x} x2={active.x} y1={chartPadding.top} y2={baseline} className="chart-hover-line" /> : null}
        <text x={chartPadding.left} y={chartHeight - 9} className="chart-axis-label">{data[0]?.date}</text>
        <text x={chartWidth / 2} y={chartHeight - 9} className="chart-axis-label" textAnchor="middle">
          {data[Math.floor(data.length / 2)]?.date}
        </text>
        <text x={chartWidth - chartPadding.right} y={chartHeight - 9} className="chart-axis-label" textAnchor="end">
          {data[data.length - 1]?.date}
        </text>
        <text x="10" y="13" className="chart-axis-title">{unit === "orders" ? "订单 / 单" : "营收 / 元"}</text>
      </svg>
      {active ? (
        <div
          className="chart-tooltip"
          style={{ left: `${(active.x / chartWidth) * 100}%`, top: `${(active.y / chartHeight) * 100}%` }}
          aria-live="polite"
        >
          <span>{active.date}</span>
          <strong>{active.displayValue}</strong>
          {demo ? <small>演示数据</small> : <small>服务端真实数据</small>}
        </div>
      ) : null}
    </div>
  );
}

function InventoryTicks({ products }: { products: MerchantProduct[] }) {
  const inventory: InventoryPoint[] = useMemo(
    () =>
      products
        .slice()
        .sort((a, b) => b.totalAvailableStock - a.totalAvailableStock)
        .slice(0, 6)
        .map((product) => ({ productId: product.id, name: product.name, stock: product.totalAvailableStock })),
    [products],
  );
  const maximum = Math.max(...inventory.map((item) => item.stock), 1);

  return (
    <ul className="merchant-chart merchant-chart--inventory" aria-label="库存排名">
      {inventory.map((item, index) => (
        <li className="inventory-row" key={item.productId} title={`${item.name} · ${item.stock} 件`}>
          <div className="inventory-row__meta">
            <span>{String(index + 1).padStart(2, "0")}</span>
            <strong>{item.name}</strong>
            <b>{item.stock}</b>
          </div>
          <div className="inventory-track"><span style={{ width: `${Math.max((item.stock / maximum) * 100, 5)}%` }} /></div>
        </li>
      ))}
      {!inventory.length ? <li className="chart-empty">暂无商品库存数据</li> : null}
    </ul>
  );
}

function RevenueStroke({ data, demo }: { data: InteractiveTrendPoint[]; demo: boolean }) {
  const frameRef = useRef<number | null>(null);
  const [counter, setCounter] = useState(0);
  const total = useMemo(() => data.reduce((sum, point) => sum + point.value, 0), [data]);

  const startCounter = useCallback(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      setCounter(total);
      return;
    }
    const start = performance.now();
    const tick = (time: number) => {
      const progress = Math.min((time - start) / 1200, 1);
      setCounter(Math.round(total * (1 - (1 - progress) ** 3)));
      if (progress < 1) frameRef.current = requestAnimationFrame(tick);
    };
    frameRef.current = requestAnimationFrame(tick);
  }, [total]);

  useEffect(
    () => () => {
      if (frameRef.current !== null) cancelAnimationFrame(frameRef.current);
    },
    [],
  );

  return (
    <div className="merchant-chart merchant-chart--stroke">
      <div className="chart-counter">
        <strong>{formatCurrency(counter)}</strong>
        <span>区间已支付营业额 · {demo ? "演示数据" : "真实数据"}</span>
      </div>
      <InteractiveLineChart data={data} unit="cny" demo={demo} showYAxis area smooth onFirstView={startCounter} />
    </div>
  );
}

function toLineData(points: MerchantDashboardTrendPoint[], key: "orderCount" | "paidRevenue") {
  return points.map((point) => {
    const value = point[key];
    return {
      date: point.date.replaceAll("-", "."),
      value,
      displayValue: key === "orderCount" ? `${value} 单` : formatCurrency(value),
    };
  });
}

export function MerchantCharts({ products, trends, rangeLabel, demo, loading }: MerchantChartsProps) {
  const realOrders = useMemo(() => toLineData(trends, "orderCount"), [trends]);
  const realRevenue = useMemo(() => toLineData(trends, "paidRevenue"), [trends]);
  const orderData = demo ? demoOrders : realOrders;
  const revenueData = demo ? demoRevenue : realRevenue;
  const source = demo ? "DEMO / EXPLICIT SESSION" : "LIVE / SERVER DATA";

  return (
    <section className="merchant-analytics" aria-label="经营趋势">
      <div className="merchant-analytics-head">
        <div>
          <span className="eyebrow">OPERATIONS / PULSE</span>
          <h2>用真实数据，读清店铺的经营节奏。</h2>
        </div>
        <span className="data-source-label">{loading ? "LOADING / LIVE" : source}</span>
      </div>
      <div className="merchant-chart-grid">
        <article className="panel surface chart-panel chart-panel--wide">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">DAILY ORDERS</span>
              <h3>每天有效订单</h3>
            </div>
            <span>{rangeLabel}</span>
          </div>
          {loading ? <div className="chart-empty chart-empty--panel">正在读取真实订单趋势…</div> : <InteractiveLineChart data={orderData} unit="orders" demo={demo} showYAxis />}
          <div className="chart-source">HAIRLINE LINE · {source}</div>
        </article>
        <article className="panel surface chart-panel">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">DAILY REVENUE</span>
              <h3>每天已支付营业额</h3>
            </div>
            <span>人民币 / 元</span>
          </div>
          {loading ? <div className="chart-empty chart-empty--panel">正在读取真实营业额趋势…</div> : <RevenueStroke data={revenueData} demo={demo} />}
          <div className="chart-source">DRAW-IN + COUNTER · {source}</div>
        </article>
      </div>
      <div className="merchant-chart-grid merchant-chart-grid--lower">
        <article className="panel surface chart-panel chart-panel--wide">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">INVENTORY TICKS</span>
              <h3>商品库存分布</h3>
            </div>
            <span>{demo ? "DEMO CATALOG" : "LIVE CATALOG"}</span>
          </div>
          <InventoryTicks products={products} />
          <div className="chart-source">TICK ROWS · {demo ? "DEMO CATALOG" : "MERCHANT PRODUCT API"}</div>
        </article>
        <article className="panel surface chart-panel chart-note-panel">
          <span className="eyebrow">METRIC NOTE</span>
          <h3>图表呈现真实趋势，不替你补写故事。</h3>
          <p>订单与营业额按服务端业务日返回。没有订单的日期显示为零，不用浏览器演示数据填充。</p>
          <span className="chart-note-mark">{demo ? "DEMO MODE" : "SERVER-SOURCED"}</span>
        </article>
      </div>
    </section>
  );
}
