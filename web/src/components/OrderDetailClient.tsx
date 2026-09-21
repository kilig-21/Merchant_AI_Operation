"use client";

import { apiClient } from "@/lib/client-api";
import { readDemoOrders, updateDemoOrderStatus } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import type { OrderDetail } from "@/lib/types";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { RequestFailure } from "./RequestFailure";
import { useSession } from "./SessionProvider";
import { StatusPill } from "./StatusPill";

const date = (value: string) => value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "时间待同步";

export function OrderDetailClient({ id }: { id: number }) {
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const [busy, setBusy] = useState("");
  const { user, loading: sessionLoading } = useSession();
  const demoSession = user?.isDemo === true;

  const load = useCallback(async () => {
    setLoading(true);
    setFailure(null);
    if (demoSession) {
      const local = readDemoOrders().find((entry) => entry.id === id) ?? null;
      setOrder(local);
      if (!local) setFailure(new Error("没有找到这笔演示订单。"));
      setLoading(false);
      return;
    }
    try {
      setOrder(await apiClient<OrderDetail>(`/api/backend/orders/${id}`));
    } catch (caught) {
      setOrder(null);
      setFailure(caught);
    } finally {
      setLoading(false);
    }
  }, [demoSession, id]);

  useEffect(() => {
    if (!sessionLoading) void load();
  }, [load, sessionLoading]);

  async function action(kind: "mock-pay" | "cancel") {
    if (!order) return;
    setBusy(kind);
    setFailure(null);
    try {
      if (demoSession) {
        const updated = updateDemoOrderStatus(id, kind === "mock-pay" ? "PAID" : "CLOSED");
        if (updated) setOrder(updated);
        return;
      }
      await apiClient<null>(`/api/backend/orders/${id}/${kind}`, { method: "POST" });
      setOrder({ ...order, status: kind === "mock-pay" ? "PAID" : "CLOSED" });
    } catch (caught) {
      setFailure(caught);
    } finally {
      setBusy("");
    }
  }

  if (loading) return <main className="page-shell"><div className="empty-state"><p>正在读取订单…</p></div></main>;
  if (failure && !order) return <main className="page-shell"><RequestFailure error={failure} loginHref={`/consumer/login?redirect=/orders/${id}`} onRetry={() => void load()} title="订单暂时无法读取" /></main>;
  if (!order) return null;

  const pending = order.status === "PENDING_PAYMENT";
  return (
    <main className="page-shell">
      <Link className="eyebrow" href="/orders">← 返回订单</Link>
      <header className="page-intro"><div><span className="eyebrow">ORDER / {order.orderNo}</span><h1>{order.status === "PAID" ? "支付完成" : order.status === "CLOSED" ? "订单已关闭" : "等待支付"}</h1></div><StatusPill status={order.status} /></header>
      {demoSession ? <DemoNotice>这是一笔本机演示订单；支付和取消只更新演示状态。</DemoNotice> : null}
      <section className="order-total surface"><span>订单合计</span><strong>{currency(order.totalAmount)}</strong><p>{pending ? "库存已为你保留，请在到期前完成支付。" : order.status === "PAID" ? "感谢你把它带进生活。" : "这笔订单当前无法支付。"}</p></section>
      {order.items.length ? <section><span className="eyebrow">ORDER ITEMS</span>{order.items.map((item) => <article className="order-items" key={item.id}><div><strong>{item.skuNameSnapshot}</strong><p>数量 {item.quantity}</p></div><strong>{currency(item.salePrice * item.quantity)}</strong></article>)}</section> : null}
      <section style={{ marginTop: 40 }}><span className="eyebrow">ORDER INFO</span><div className="info-row"><span>创建时间</span><strong>{date(order.createdAt)}</strong></div>{pending ? <div className="info-row"><span>支付截止</span><strong>{date(order.expireAt)}</strong></div> : null}</section>
      {pending ? <div className="order-actions"><button className="button primary" disabled={!!busy} onClick={() => void action("mock-pay")} type="button">{busy === "mock-pay" ? "确认中…" : `模拟支付 ${currency(order.totalAmount)}`}</button><button className="button" disabled={!!busy} onClick={() => void action("cancel")} type="button">{busy === "cancel" ? "取消中…" : "取消订单"}</button></div> : null}
      {order.status === "PAID" && demoSession ? <div className="order-actions"><Link className="button" href={`/after-sales/new?orderId=${id}`}>申请售后</Link></div> : null}
      {failure ? <p className="form-error">{failure instanceof Error ? failure.message : "操作失败。"}</p> : null}
    </main>
  );
}
