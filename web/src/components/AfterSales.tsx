"use client";

import {
  DemoAfterSalesClient,
  DemoAfterSalesCreateClient,
  DemoAfterSalesDetailClient,
} from "@/components/AccountServices";
import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { AfterSaleEligibleOrderItem, AfterSaleRequest } from "@/lib/types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useCallback, useEffect, useState } from "react";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const date = (value: string | null) =>
  value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "—";

export function AfterSalesClient() {
  const { user, loading } = useSession();
  if (loading) return <Loading text="正在读取售后服务…" />;
  return user?.isDemo ? <DemoAfterSalesClient /> : <LiveAfterSalesList />;
}

export function AfterSalesCreateClient() {
  const { user, loading } = useSession();
  if (loading) return <Loading text="正在准备售后申请…" />;
  return user?.isDemo ? <DemoAfterSalesCreateClient /> : <LiveAfterSalesCreate />;
}

export function AfterSalesDetailClient({ id }: { id: number }) {
  const { user, loading } = useSession();
  if (loading) return <Loading text="正在读取售后详情…" />;
  return user?.isDemo ? <DemoAfterSalesDetailClient id={id} /> : <LiveAfterSalesDetail id={id} />;
}

function LiveAfterSalesList() {
  const [items, setItems] = useState<AfterSaleRequest[]>([]);
  const [failure, setFailure] = useState<unknown>(null);
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    setFailure(null);
    apiClient<AfterSaleRequest[]>("/api/backend/after-sales")
      .then(setItems)
      .catch((error) => setFailure(error))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  return (
    <main className="page-shell account-service-shell">
      <Link className="eyebrow" href="/account">← 返回账户</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">SERVICE / AFTER SALES</span><h1>售后服务</h1></div><p>这里显示真实售后申请和商家审核结果；审核通过不代表资金已退回。</p></header>
      <div className="account-service-actions"><Link className="button primary" href="/after-sales/new">申请售后</Link></div>
      {failure ? <RequestFailure error={failure} loginHref="/consumer/login?redirect=/after-sales" onRetry={load} title="售后服务暂时无法读取" /> : null}
      {!failure && loading ? <Loading text="正在读取真实售后记录…" /> : null}
      {!failure && !loading && !items.length ? <div className="empty-state"><h2>目前没有售后申请。</h2><p>只有已支付订单项才会出现在可申请列表中。</p><Link className="button" href="/orders">查看订单</Link></div> : null}
      {!failure && !loading && items.length ? <section className="after-sale-list">{items.map((item) => <Link className="after-sale-row surface" href={`/after-sales/${item.id}`} key={item.id}><div><span className="eyebrow">REQUEST / {item.requestNo}</span><h2>订单 #{item.orderId}</h2><p>申请金额 {currency(item.requestedAmount)} · 数量 {item.quantity} · {item.reason}</p><small>提交于 {date(item.createdAt)}</small></div><StatusPill status={item.status} /></Link>)}</section> : null}
    </main>
  );
}

function LiveAfterSalesCreate() {
  const [eligible, setEligible] = useState<AfterSaleEligibleOrderItem[]>([]);
  const [orderItemId, setOrderItemId] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [reason, setReason] = useState("商品与预期不符");
  const [failure, setFailure] = useState<unknown>(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const router = useRouter();
  const selected = eligible.find((item) => item.orderItemId === Number(orderItemId));

  function load() {
    setLoading(true);
    setFailure(null);
    apiClient<AfterSaleEligibleOrderItem[]>("/api/backend/after-sales/eligible-orders")
      .then((items) => {
        setEligible(items);
        setOrderItemId((current) => current || String(items[0]?.orderItemId ?? ""));
        setQuantity("1");
      })
      .catch((error) => setFailure(error))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected || saving) return;
    setSaving(true);
    setMessage("");
    try {
      const created = await apiClient<AfterSaleRequest>("/api/backend/after-sales", {
        method: "POST",
        body: JSON.stringify({ orderItemId: selected.orderItemId, quantity: Number(quantity), reason: reason.trim() }),
      });
      router.push(`/after-sales/${created.id}`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "售后申请未能提交。");
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="page-shell account-service-shell narrow-service-shell">
      <Link className="eyebrow" href="/after-sales">← 返回售后服务</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">NEW REQUEST</span><h1>申请售后</h1></div><p>选择真实已支付订单项后提交。申请金额由服务端按历史成交价计算。</p></header>
      {failure ? <RequestFailure error={failure} loginHref="/consumer/login?redirect=/after-sales/new" onRetry={load} title="可申请订单项暂时无法读取" /> : null}
      {!failure && loading ? <Loading text="正在读取可申请订单项…" /> : null}
      {!failure && !loading && !eligible.length ? <div className="empty-state"><h2>没有可申请的订单项。</h2><p>已支付订单项才可能申请售后，且同一订单项需符合服务端规则。</p><Link className="button" href="/orders">查看订单</Link></div> : null}
      {!failure && !loading && eligible.length ? <form className="after-sale-form surface" onSubmit={(event) => void submit(event)}>
        <label><span>关联订单项</span><select value={orderItemId} onChange={(event) => { setOrderItemId(event.target.value); setQuantity("1"); }}>{eligible.map((item) => <option key={item.orderItemId} value={item.orderItemId}>订单 #{item.orderId} / 订单项 #{item.orderItemId} / {currency(item.salePrice)} × {item.purchasedQuantity}</option>)}</select></label>
        <label><span>申请数量</span><input min="1" max={selected?.purchasedQuantity ?? 1} onChange={(event) => setQuantity(event.target.value)} required type="number" value={quantity} /></label>
        <label><span>申请原因</span><textarea maxLength={255} minLength={1} onChange={(event) => setReason(event.target.value)} required value={reason} /></label>
        <p>服务端将以订单历史单价重新计算申请金额，不以页面展示金额为准。</p>
        <button className="button primary" disabled={saving} type="submit">{saving ? "提交中…" : "提交真实售后申请"}</button>
        {message ? <p className="form-error" role="alert">{message}</p> : null}
      </form> : null}
    </main>
  );
}

function LiveAfterSalesDetail({ id }: { id: number }) {
  const [item, setItem] = useState<AfterSaleRequest | null>(null);
  const [failure, setFailure] = useState<unknown>(null);
  const [loading, setLoading] = useState(true);
  const load = useCallback(() => {
    setLoading(true);
    setFailure(null);
    apiClient<AfterSaleRequest>(`/api/backend/after-sales/${id}`).then(setItem).catch(setFailure).finally(() => setLoading(false));
  }, [id]);
  useEffect(() => { void load(); }, [load]);
  if (failure) return <main className="page-shell account-service-shell"><RequestFailure error={failure} loginHref="/consumer/login?redirect=/after-sales" onRetry={load} title="售后详情暂时无法读取" /></main>;
  if (loading || !item) return <Loading text="正在读取真实售后详情…" />;
  return <AfterSaleDetail item={item} backHref="/after-sales" />;
}

export function AfterSaleDetail({ item, backHref }: { item: AfterSaleRequest; backHref: string }) {
  return <main className="page-shell account-service-shell narrow-service-shell"><Link className="eyebrow" href={backHref}>← 返回售后列表</Link><header className="page-intro compact-intro"><div><span className="eyebrow">REQUEST / {item.requestNo}</span><h1>售后详情</h1></div><StatusPill status={item.status} /></header><section className="service-detail surface"><p>当前状态以商家审核结果为准；审核通过不表示资金已退回。</p><dl><div><dt>关联订单</dt><dd>订单 #{item.orderId} / 订单项 #{item.orderItemId}</dd></div><div><dt>申请数量</dt><dd>{item.quantity}</dd></div><div><dt>申请金额</dt><dd>{currency(item.requestedAmount)}</dd></div><div><dt>申请原因</dt><dd>{item.reason}</dd></div><div><dt>商家备注</dt><dd>{item.merchantRemark || "商家暂未填写备注"}</dd></div><div><dt>提交时间</dt><dd>{date(item.createdAt)}</dd></div><div><dt>审核时间</dt><dd>{date(item.decidedAt)}</dd></div></dl></section></main>;
}

function Loading({ text }: { text: string }) {
  return <main className="page-shell"><div className="empty-state"><p>{text}</p></div></main>;
}
