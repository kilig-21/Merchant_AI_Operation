"use client";
import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";
import { StatusPill } from "./StatusPill";
const date = (value: string) =>
  value
    ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
    : "时间待同步";
export function OrderListClient() {
  const [orders, setOrders] = useState<OrderDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  async function load() {
    setLoading(true);
    setError("");
    try {
      setOrders(await apiClient<OrderDetail[]>("/api/backend/orders"));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "订单读取失败。");
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    apiClient<OrderDetail[]>("/api/backend/orders")
      .then(setOrders)
      .catch((caught) => setError(caught instanceof Error ? caught.message : "订单读取失败。"))
      .finally(() => setLoading(false));
  }, []);
  return (
    <main className="page-shell">
      <header className="page-intro">
        <div>
          <span className="eyebrow">YOUR ORDERS</span>
          <h1>
            每一次选择，
            <br />
            都有清楚去处。
          </h1>
        </div>
        <p>查看待支付、已支付和已关闭订单。</p>
      </header>
      {loading ? (
        <div className="empty-state">
          <p>正在读取订单…</p>
        </div>
      ) : error ? (
        <div className="empty-state">
          <h2>订单暂时无法读取</h2>
          <p>{error}</p>
          <button className="button" onClick={() => void load()} type="button">
            重新加载
          </button>
        </div>
      ) : orders.length ? (
        <section className="order-list">
          {orders.map((order) => (
            <Link className="order-row" href={`/orders/${order.id}`} key={order.id}>
              <div>
                <StatusPill status={order.status} />
                <h2>{currency(order.totalAmount)}</h2>
              </div>
              <small>
                {order.orderNo}
                <br />
                {date(order.createdAt)}
              </small>
              <span>→</span>
            </Link>
          ))}
        </section>
      ) : (
        <div className="empty-state">
          <h2>还没有留下订单。</h2>
          <p>从一件适合今天的物品开始。</p>
          <Link className="button primary" href="/stores/1001/products">
            去选购
          </Link>
        </div>
      )}
    </main>
  );
}
