"use client";
import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { CreateOrderResult, OrderDetail } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { StatusPill } from "./StatusPill";
const date = (value: string) =>
  value
    ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
    : "时间待同步";
export function OrderDetailClient({ id }: { id: number }) {
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [created, setCreated] = useState<CreateOrderResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState("");
  async function load() {
    setLoading(true);
    setError("");
    try {
      const raw = sessionStorage.getItem(`morrow_created_order_${id}`);
      if (raw) setCreated(JSON.parse(raw));
      setOrder(await apiClient<OrderDetail>(`/api/backend/orders/${id}`));
    } catch (caught) {
      if (!sessionStorage.getItem(`morrow_created_order_${id}`))
        setError(caught instanceof Error ? caught.message : "订单读取失败。");
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    setLoading(true);
    setError("");
    const storageKey = `morrow_created_order_${id}`;
    const raw = sessionStorage.getItem(storageKey);
    if (raw) setCreated(JSON.parse(raw));
    apiClient<OrderDetail>(`/api/backend/orders/${id}`)
      .then(setOrder)
      .catch((caught) => {
        if (!raw) setError(caught instanceof Error ? caught.message : "订单读取失败。");
      })
      .finally(() => setLoading(false));
  }, [id]);
  const status = order?.status ?? created?.status ?? "";
  const amount = order?.totalAmount ?? created?.totalAmount ?? 0;
  const no = order?.orderNo ?? created?.orderNo ?? "";
  const pending = status === "PENDING_PAYMENT";
  async function action(kind: "mock-pay" | "cancel") {
    setBusy(kind);
    setError("");
    try {
      await apiClient<null>(`/api/backend/orders/${id}/${kind}`, { method: "POST" });
      if (order) setOrder({ ...order, status: kind === "mock-pay" ? "PAID" : "CLOSED" });
      if (created) setCreated({ ...created, status: kind === "mock-pay" ? "PAID" : "CLOSED" });
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "操作失败。");
    } finally {
      setBusy("");
    }
  }
  if (loading)
    return (
      <main className="page-shell">
        <div className="empty-state">
          <p>正在读取订单…</p>
        </div>
      </main>
    );
  if (error && !status)
    return (
      <main className="page-shell">
        <div className="empty-state">
          <h2>订单暂时无法读取</h2>
          <p>{error}</p>
        </div>
      </main>
    );
  return (
    <main className="page-shell">
      <Link className="eyebrow" href="/orders">
        ← 返回订单
      </Link>
      <header className="page-intro">
        <div>
          <span className="eyebrow">ORDER / {no}</span>
          <h1>{status === "PAID" ? "支付完成" : status === "CLOSED" ? "订单已关闭" : "等待支付"}</h1>
        </div>
        <StatusPill status={status} />
      </header>
      <section className="order-total surface">
        <span>订单合计</span>
        <strong>{currency(amount)}</strong>
        <p>
          {pending
            ? "库存已为你保留，请在到期前完成支付。"
            : status === "PAID"
              ? "感谢你把它带进生活。"
              : "这笔订单当前无法支付。"}
        </p>
      </section>
      {order?.items?.length ? (
        <section>
          <span className="eyebrow">ORDER ITEMS</span>
          {order.items.map((item) => (
            <article className="order-items" key={item.id}>
              <article>
                <div>
                  <strong>{item.skuNameSnapshot}</strong>
                  <p>数量 {item.quantity}</p>
                </div>
                <strong>{currency(item.salePrice * item.quantity)}</strong>
              </article>
            </article>
          ))}
        </section>
      ) : null}
      <section style={{ marginTop: 40 }}>
        <span className="eyebrow">ORDER INFO</span>
        <div className="info-row">
          <span>创建时间</span>
          <strong>{date(order?.createdAt || "")}</strong>
        </div>
        {pending && (
          <div className="info-row">
            <span>支付截止</span>
            <strong>{date(order?.expireAt || created?.expireAt || "")}</strong>
          </div>
        )}
      </section>
      {pending && (
        <div className="order-actions">
          <button
            className="button primary"
            disabled={!!busy}
            onClick={() => void action("mock-pay")}
            type="button"
          >
            {busy === "mock-pay" ? "确认中…" : `模拟支付 ${currency(amount)}`}
          </button>
          <button className="button" disabled={!!busy} onClick={() => void action("cancel")} type="button">
            {busy === "cancel" ? "取消中…" : "取消订单"}
          </button>
        </div>
      )}
      {error && <p className="form-error">{error}</p>}
    </main>
  );
}
