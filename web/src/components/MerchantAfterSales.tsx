"use client";

import { apiClient } from "@/lib/client-api";
import { currency } from "@/lib/demo-data";
import type { AfterSaleRequest } from "@/lib/types";
import { useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { MerchantShell } from "./MerchantShell";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const date = (value: string | null) => value ? new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : "—";

export function MerchantAfterSales() {
  const { user, loading: sessionLoading } = useSession();
  const [items, setItems] = useState<AfterSaleRequest[]>([]);
  const [selected, setSelected] = useState<AfterSaleRequest | null>(null);
  const [failure, setFailure] = useState<unknown>(null);
  const [loading, setLoading] = useState(true);
  const [decision, setDecision] = useState<"APPROVED" | "REJECTED">("APPROVED");
  const [remark, setRemark] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const load = useCallback(() => {
    setLoading(true);
    setFailure(null);
    apiClient<AfterSaleRequest[]>("/api/backend/merchant/after-sales")
      .then(setItems)
      .catch(setFailure)
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { if (!sessionLoading && user?.isDemo !== true) void load(); }, [sessionLoading, user?.isDemo, load]);

  async function openDetail(id: number) {
    setMessage("");
    try {
      const detail = await apiClient<AfterSaleRequest>(`/api/backend/merchant/after-sales/${id}`);
      setSelected(detail);
      setRemark(detail.merchantRemark ?? "");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "售后详情暂时无法读取。");
    }
  }

  async function review() {
    if (!selected || saving) return;
    setSaving(true);
    setMessage("");
    try {
      const updated = await apiClient<AfterSaleRequest>(`/api/backend/merchant/after-sales/${selected.id}/decision`, { method: "POST", body: JSON.stringify({ decision, remark: remark.trim() }) });
      setSelected(updated);
      setItems((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "审核操作未完成。");
    } finally {
      setSaving(false);
    }
  }

  if (user?.isDemo) return <MerchantShell eyebrow="AFTER SALES / DEMO" title="售后审核"><DemoNotice>演示商家不会读取或审核真实售后申请。</DemoNotice></MerchantShell>;
  return <MerchantShell eyebrow="AFTER SALES / LIVE" title="售后审核">
    {failure ? <RequestFailure error={failure} loginHref="/merchant/login?redirect=/merchant/after-sales" onRetry={load} title="商家售后暂时无法读取" /> : null}
    {!failure && loading ? <div className="empty-state"><p>正在读取本店真实售后申请…</p></div> : null}
    {!failure && !loading && !items.length ? <div className="empty-state"><h2>目前没有待处理售后。</h2><p>这里只显示当前商家店铺的真实售后申请。</p></div> : null}
    {!failure && !loading && items.length ? <div className="table-scroll"><table className="data-table data-table--responsive"><thead><tr><th>售后单号</th><th>订单</th><th>金额</th><th>状态</th><th>操作</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td data-label="售后单号"><strong>{item.requestNo}</strong></td><td data-label="订单">#{item.orderId}</td><td data-label="金额">{currency(item.requestedAmount)}</td><td data-label="状态"><StatusPill status={item.status} /></td><td data-label="操作"><button onClick={() => void openDetail(item.id)} type="button">查看处理</button></td></tr>)}</tbody></table></div> : null}
    {selected ? <section className="after-sale-form surface" style={{ marginTop: 24 }}><header><span className="eyebrow">REQUEST / {selected.requestNo}</span><h2>订单 #{selected.orderId} · 申请 {currency(selected.requestedAmount)}</h2><p>原因：{selected.reason}；数量：{selected.quantity}。审核通过只表示审核结论，不代表已退款。</p></header><div><StatusPill status={selected.status} /></div>{selected.status === "SUBMITTED" ? <><label><span>审核结果</span><select value={decision} onChange={(event) => setDecision(event.target.value as "APPROVED" | "REJECTED")}><option value="APPROVED">审核通过</option><option value="REJECTED">审核拒绝</option></select></label><label><span>商家备注（选填）</span><textarea maxLength={500} value={remark} onChange={(event) => setRemark(event.target.value)} /></label><button className="button primary" disabled={saving} onClick={() => void review()} type="button">{saving ? "提交审核中…" : "提交审核结果"}</button></> : <p>该申请已完成处理，审核时间：{date(selected.decidedAt)}。</p>}{message ? <p className="form-error" role="alert">{message}</p> : null}</section> : null}
  </MerchantShell>;
}
