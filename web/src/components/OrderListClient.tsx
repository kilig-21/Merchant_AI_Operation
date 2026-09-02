"use client";

import { apiClient } from "@/lib/client-api";
import { readDemoOrders } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { RequestFailure } from "./RequestFailure";
import { useSession } from "./SessionProvider";
import { StatusPill } from "./StatusPill";

const date = (value: string) =>
  value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "时间待同步";

export function OrderListClient() {
  const [orders, setOrders] = useState<OrderDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const [demo, setDemo] = useState(false);
  const { user, loading: sessionLoading } = useSession();
  const demoSession = user?.isDemo === true;

  const load = useCallback(async () => {
    setLoading(true);
    setFailure(null);
    if (demoSession) {
      setOrders(readDemoOrders());
      setDemo(true);
      setLoading(false);
      return;
    }
    try {
      setOrders(await apiClient<OrderDetail[]>("/api/backend/orders"));
      setDemo(false);
    } catch (caught) {
      setOrders([]);
      setDemo(false);
      setFailure(caught);
    } finally {
      setLoading(false);
    }
  }, [demoSession]);

  useEffect(() => {
    if (!sessionLoading) void load();
  }, [load, sessionLoading]);

  return (
    <main className="page-shell">
      <header className="page-intro">
        <div><span className="eyebrow">YOUR ORDERS</span><h1>每一次选择，<br />都有清楚去处。</h1></div>
        <p>查看待支付、已支付和已关闭订单。</p>
      </header>
      {demo ? <DemoNotice>当前显示保存在本机浏览器中的演示订单，不代表真实支付或履约状态。</DemoNotice> : null}
      {loading ? <div className="empty-state"><p>正在读取订单…</p></div> : null}
      {!loading && failure ? <RequestFailure error={failure} loginHref="/consumer/login?redirect=/orders" onRetry={() => void load()} title="订单暂时无法读取" /> : null}
      {!loading && !failure && orders.length ? (
        <section className="order-list">
          {orders.map((order) => (
            <Link className="order-row" href={`/orders/${order.id}`} key={order.id}>
              <div><StatusPill status={order.status} /><h2>{currency(order.totalAmount)}</h2>{"storeName" in order ? <p>{String(order.storeName)}</p> : null}</div>
              <small>{order.orderNo}<br />{date(order.createdAt)}</small><span>→</span>
            </Link>
          ))}
        </section>
      ) : null}
      {!loading && !failure && !orders.length ? <div className="empty-state"><h2>还没有留下订单。</h2><p>从一件适合今天的物品开始。</p><Link className="button primary" href="/stores">去选购</Link></div> : null}
    </main>
  );
}
