"use client";

import type { MerchantProduct } from "@/lib/types";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";

export interface InteractiveTrendPoint {
  date: string;
  value: number;
  displayValue: string;
}

export interface InteractiveLineChartProps {
  data: InteractiveTrendPoint[];
  unit: "orders" | "cny";
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
  source: "backend" | "demo";
}

interface MerchantChartsProps {
  products: MerchantProduct[];
  demo: boolean;
}

const paidOrders: InteractiveTrendPoint[] = [
  42, 46, 49, 53, 51, 58, 64, 61, 67, 72, 70, 76, 73, 79, 86, 81, 88, 92, 89, 96, 101, 98, 104, 109, 106, 114,
  118, 121, 126, 132,
].map((value, index) => ({
  date: `08/${String(index + 1).padStart(2, "0")}`,
  value,
  displayValue: `${value} 单`,
}));

const cumulativeRevenue: InteractiveTrendPoint[] = [
  680, 1120, 1640, 2380, 3040, 3810, 4490, 5270, 5940, 6710, 7420, 8280, 9040, 10180, 11350, 12480, 13720,
  15060, 16420, 17920, 19310, 20760, 22240, 23780, 25360, 27040, 28820, 30760, 32740, 34860,
].map((value, index) => ({
  date: `08/${String(index + 1).padStart(2, "0")}`,
  value,
  displayValue: `¥${value.toLocaleString("zh-CN")}`,
}));

const chartWidth = 400;
const chartHeight = 210;
const chartPadding = { top: 16, right: 16, bottom: 32, left: 46 };

function formatAxis(value: number, unit: InteractiveLineChartProps["unit"]) {
  if (unit === "orders") return String(value);
  if (value === 0) return "¥0";
  return `¥${Math.round(value / 1000)}k`;
}

function chartGeometry(data: InteractiveTrendPoint[], unit: InteractiveLineChartProps["unit"]) {
  const rawMinimum = Math.min(...data.map((point) => point.value));
  const rawMaximum = Math.max(...data.map((point) => point.value));
  const step = unit === "orders" ? 20 : 10000;
  const minimum = unit === "orders" ? Math.floor(rawMinimum / step) * step : 0;
  const maximum = Math.max(Math.ceil(rawMaximum / step) * step, step);
  const plotWidth = chartWidth - chartPadding.left - chartPadding.right;
  const plotHeight = chartHeight - chartPadding.top - chartPadding.bottom;
  const points = data.map((point, index) => ({
    ...point,
    x: chartPadding.left + (index / Math.max(data.length - 1, 1)) * plotWidth,
    y: chartPadding.top + (1 - (point.value - minimum) / Math.max(maximum - minimum, 1)) * plotHeight,
  }));
  const ticks = Array.from({ length: 5 }, (_, index) => {
    const value = minimum + ((maximum - minimum) / 4) * index;
    return {
      value,
      y: chartPadding.top + (1 - index / 4) * plotHeight,
    };
  }).reverse();
  return { points, ticks, minimum, maximum, plotHeight, plotWidth };
}

function pathFromPoints(points: ReturnType<typeof chartGeometry>["points"], smooth: boolean) {
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
    if (!container) return;
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
  }, [animateOnFirstView, onFirstView, reducedMotion]);

  const selectFromPointer = (clientX: number) => {
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect || !data.length) return;
    const relative = ((clientX - rect.left) / rect.width) * chartWidth;
    const raw = ((relative - chartPadding.left) / geometry.plotWidth) * Math.max(data.length - 1, 1);
    setActiveIndex(Math.max(0, Math.min(Math.round(raw), data.length - 1)));
  };

  return (
    <div
      ref={containerRef}
      className={`interactive-line-chart ${entered ? "is-entered" : ""} ${area ? "has-area" : ""}`}
      role="slider"
      aria-label={`${unit === "orders" ? "过去三十天订单" : "本月累计成交额"}趋势图，${demo ? "演示数据" : "实时数据"}`}
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
        {area && <path d={areaPath} className="chart-area-path" />}
        <path d={path} className="chart-hairline-path" pathLength="1" />
        {geometry.points.map((point, index) => (
          <circle
            key={point.date}
            cx={point.x}
            cy={point.y}
            r={
              index === activeIndex
                ? 4.8
                : unit === "orders" && (index % 7 === 5 || index % 7 === 6)
                  ? 3
                  : 2.4
            }
            className={`${unit === "orders" && (index % 7 === 5 || index % 7 === 6) ? "chart-weekend-dot" : "chart-data-dot"} ${index === activeIndex ? "is-active" : ""}`}
            style={{ animationDelay: `${450 + index * 20}ms` }}
          />
        ))}
        {active && (
          <line
            x1={active.x}
            x2={active.x}
            y1={chartPadding.top}
            y2={baseline}
            className="chart-hover-line"
          />
        )}
        <text x={chartPadding.left} y={chartHeight - 9} className="chart-axis-label">
          {data[0]?.date}
        </text>
        <text x={chartWidth / 2} y={chartHeight - 9} className="chart-axis-label" textAnchor="middle">
          {data[Math.floor(data.length / 2)]?.date}
        </text>
        <text
          x={chartWidth - chartPadding.right}
          y={chartHeight - 9}
          className="chart-axis-label"
          textAnchor="end"
        >
          {data[data.length - 1]?.date}
        </text>
        {unit === "orders" && (
          <text x="10" y="13" className="chart-axis-title">
            订单 / 单
          </text>
        )}
      </svg>
      {active && (
        <div
          className="chart-tooltip"
          style={{ left: `${(active.x / chartWidth) * 100}%`, top: `${(active.y / chartHeight) * 100}%` }}
          aria-live="polite"
        >
          <span>{active.date}</span>
          <strong>{active.displayValue}</strong>
          {demo && <small>演示数据</small>}
        </div>
      )}
    </div>
  );
}

function OrdersHairline({ data }: { data: InteractiveTrendPoint[] }) {
  return <InteractiveLineChart data={data} unit="orders" demo showYAxis />;
}

function InventoryTicks({ products, demo }: { products: MerchantProduct[]; demo: boolean }) {
  const inventory: InventoryPoint[] = useMemo(
    () =>
      products
        .slice()
        .sort((a, b) => b.totalAvailableStock - a.totalAvailableStock)
        .slice(0, 6)
        .map((product) => ({
          productId: product.id,
          name: product.name,
          stock: product.totalAvailableStock,
          source: demo ? "demo" : "backend",
        })),
    [products, demo],
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
          <div className="inventory-track">
            <span style={{ width: `${Math.max((item.stock / maximum) * 100, 5)}%` }} />
          </div>
        </li>
      ))}
      {!inventory.length && <li className="chart-empty">暂无商品库存数据</li>}
    </ul>
  );
}

function RevenueStroke({ data }: { data: InteractiveTrendPoint[] }) {
  const frameRef = useRef<number | null>(null);
  const [counter, setCounter] = useState(0);
  const total = data[data.length - 1]?.value ?? 0;

  const startCounter = useCallback(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      setCounter(total);
      return;
    }
    const start = performance.now();
    const tick = (time: number) => {
      const progress = Math.min((time - start) / 2200, 1);
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
        <strong>¥{counter.toLocaleString("zh-CN")}</strong>
        <span>累计成交额 · 演示数据</span>
      </div>
      <InteractiveLineChart data={data} unit="cny" demo showYAxis area smooth onFirstView={startCounter} />
    </div>
  );
}

export function MerchantCharts({ products, demo }: MerchantChartsProps) {
  return (
    <section className="merchant-analytics" aria-label="经营趋势">
      <div className="merchant-analytics-head">
        <div>
          <span className="eyebrow">OPERATIONS / PULSE</span>
          <h2>把每一次经营，放回清楚的节奏里。</h2>
        </div>
        <span className="demo-chart-label">DEMO / 接口待接入</span>
      </div>
      <div className="merchant-chart-grid">
        <article className="panel surface chart-panel chart-panel--wide">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">F2 / DAILY ORDERS</span>
              <h3>过去 30 天，订单在周末前后更集中</h3>
            </div>
            <span>08 / 2026</span>
          </div>
          <OrdersHairline data={paidOrders} />
          <div className="chart-source">HAIRLINE LINE · F2 · DEMO ORDERS</div>
        </article>
        <article className="panel surface chart-panel">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">G18 / REVENUE STROKE</span>
              <h3>本月成交额沿一条线累计</h3>
            </div>
            <span>¥ / CNY</span>
          </div>
          <RevenueStroke data={cumulativeRevenue} />
          <div className="chart-source">DRAW-IN + COUNTER · G18 · DEMO REVENUE</div>
        </article>
      </div>
      <div className="merchant-chart-grid merchant-chart-grid--lower">
        <article className="panel surface chart-panel chart-panel--wide">
          <div className="chart-heading">
            <div>
              <span className="eyebrow">F5 / INVENTORY TICKS</span>
              <h3>库存主要集中在这些商品</h3>
            </div>
            <span>{demo ? "DEMO CATALOG" : "LIVE CATALOG"}</span>
          </div>
          <InventoryTicks products={products} demo={demo} />
          <div className="chart-source">
            TICK ROWS · F5 · {demo ? "DEMO PRODUCT CATALOG" : "MERCHANT PRODUCT API"}
          </div>
        </article>
        <article className="panel surface chart-panel chart-note-panel">
          <span className="eyebrow">READING NOTES</span>
          <h3>图表是经营的第二层视线。</h3>
          <p>当前趋势图使用确定性演示数据，等经营分析接口接入后只替换数据源，不改变图形结构。</p>
          <span className="chart-note-mark">MORROW / OS</span>
        </article>
      </div>
    </section>
  );
}
