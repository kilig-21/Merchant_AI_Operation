"use client";
import { apiClient } from "@/lib/client-api";
import { readDemoOrders } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import Link from "next/link";
import { useEffect, useState } from "react";
import { StatusPill } from "./StatusPill";
import { DemoNotice } from "./DemoNotice";
import { useSession } from "./SessionProvider";
const date = (value: string) =>
  value
    ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
    : "时间待同步";
export function OrderListClient() {
  const [orders, setOrders] = useState<OrderDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [demo, setDemo] = useState(false);
  const { user, loading: sessionLoading } = useSession();
  const demoSession = (user?.id ?? 0) >= 99000;
  async function load() {
    setLoading(true);
    setError("");
    if (demoSession) {
      setOrders(readDemoOrders());
      setDemo(true);
      setLoading(false);
      return;
    }
    try {
      setOrders(await apiClient<OrderDetail[]>("/api/backend/orders"));
      setDemo(false);
    } catch {
      setOrders(readDemoOrders());
      setDemo(true);
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    if (sessionLoading) return;
    if (demoSession) {
      setOrders(readDemoOrders());
      setDemo(true);
      setLoading(false);
      return;
    }
    apiClient<OrderDetail[]>("/api/backend/orders")
      .then((result) => { setOrders(result); setDemo(false); })
      .catch(() => { setOrders(readDemoOrders()); setDemo(true); })
      .finally(() => setLoading(false));
  }, [demoSession, sessionLoading]);
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
      {demo && <DemoNotice>当前显示保存在本机浏览器中的演示订单，不代表真实支付或履约状态。</DemoNotice>}
      {loading ? (
        <div className="empty-state">
          <p>正在读取订单…</p>
        </div>
      ) : error && !demo ? (
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
                {"storeName" in order ? <p>{String(order.storeName)}</p> : null}
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
          <Link className="button primary" href="/stores">
            去选购
          </Link>
        </div>
      )}
    </main>
  );
}
