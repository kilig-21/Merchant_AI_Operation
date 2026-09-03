"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { CheckoutGroup } from "@/lib/types";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";

const date = (value: string) =>
  value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "时间待同步";

export function CheckoutSuccessClient({ checkoutGroupId }: { checkoutGroupId: number }) {
  const [group, setGroup] = useState<CheckoutGroup | null>(null);
  const [loading, setLoading] = useState(true);
  const [failure, setFailure] = useState<unknown>(null);
  const [busy, setBusy] = useState<"mock-pay" | "cancel" | "" >("");

  const load = useCallback(async () => {
    setLoading(true);
    setFailure(null);
    try {
      setGroup(await apiClient<CheckoutGroup>(`/api/backend/checkouts/${checkoutGroupId}`));
    } catch (caught) {
      setGroup(null);
      setFailure(caught);
    } finally {
      setLoading(false);
    }
  }, [checkoutGroupId]);

  useEffect(() => {
    void load();
  }, [load]);

  async function action(kind: "mock-pay" | "cancel") {
    setBusy(kind);
    setFailure(null);
    try {
      await apiClient<null>(`/api/backend/checkouts/${checkoutGroupId}/${kind}`, { method: "POST" });
      await load();
    } catch (caught) {
      setFailure(caught);
    } finally {
      setBusy("");
    }
  }

  if (loading && !group) {
    return <main className="page-shell checkout-success"><p>正在同步结算状态…</p></main>;
  }

  if (failure && !group) {
    return <main className="page-shell checkout-success"><RequestFailure error={failure} loginHref={`/consumer/login?redirect=/checkout/success?checkoutGroupId=${checkoutGroupId}`} onRetry={() => void load()} title="结算结果暂时无法读取" /></main>;
  }

  if (!group) return null;

  const pending = group.status === "PENDING_PAYMENT";
  const heading = group.status === "PAID" ? "支付完成" : group.status === "CANCELLED" ? "订单已取消" : group.status === "CLOSED" ? "订单已关闭" : "订单已经准备好了";

  return (
    <main className="page-shell checkout-page-shell">
      <Link className="market-back-link" href="/orders">← 查看全部订单</Link>
      <header className="page-intro checkout-result-intro">
        <div><span className="eyebrow">CHECKOUT / {group.checkoutNo}</span><h1>{heading}</h1></div>
        <StatusPill status={group.status} />
      </header>
      <section className="checkout-result-summary surface">
        <div><span className="eyebrow">TOTAL</span><strong>{currency(group.totalAmount)}</strong></div>
        <p>本次结算包含 {group.orders.length} 家店铺的独立订单。状态由服务端实时确认。</p>
        <button className="button" disabled={loading || !!busy} onClick={() => void load()} type="button">{loading ? "刷新中…" : "刷新状态"}</button>
      </section>
      <section className="checkout-result-orders">
        {group.orders.map((order) => (
          <article className="checkout-result-order surface" key={order.id}>
            <header><div><span className="eyebrow">STORE {order.tenantId} / {order.orderNo}</span><h2>{currency(order.totalAmount)}</h2></div><StatusPill status={order.status} /></header>
            <div className="checkout-result-order__items">
              {order.items.map((item) => <div key={item.id}><span>{item.skuNameSnapshot} × {item.quantity}</span><strong>{currency(item.salePrice * item.quantity)}</strong></div>)}
            </div>
            {order.shippingAddress && <p className="checkout-result-address">配送至：{order.shippingAddress.receiverName} · {order.shippingAddress.receiverPhone}<br />{order.shippingAddress.province}{order.shippingAddress.city}{order.shippingAddress.district}{order.shippingAddress.detailAddress}</p>}
            <footer><span>下单时间 {date(order.createdAt)}</span>{order.status === "PENDING_PAYMENT" && <span>支付截止 {date(order.expireAt)}</span>}</footer>
          </article>
        ))}
      </section>
      {pending && (
        <div className="checkout-result-actions">
          <button className="button primary" disabled={!!busy} onClick={() => void action("mock-pay")} type="button">{busy === "mock-pay" ? "支付确认中…" : `模拟支付 ${currency(group.totalAmount)}`}</button>
          <button className="button" disabled={!!busy} onClick={() => void action("cancel")} type="button">{busy === "cancel" ? "取消中…" : "取消本次结算"}</button>
        </div>
      )}
      {failure !== null && <p className="form-error checkout-result-error" role="alert">{failure instanceof Error ? failure.message : "操作未完成，请稍后重试。"}</p>}
    </main>
  );
}
