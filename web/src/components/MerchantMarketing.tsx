"use client";

import { apiClient } from "@/lib/client-api";
import type { MerchantProduct, MerchantPromotionActivity, ProductDetail } from "@/lib/types";
import { useCallback, useEffect, useMemo, useState } from "react";
import { MerchantShell } from "./MerchantShell";
import { useSession } from "./SessionProvider";

type Filter = "ALL" | "ACTIVE" | "SCHEDULED" | "ENDED" | "CANCELLED";

const labels: Record<string, string> = {
  ACTIVE: "进行中",
  SCHEDULED: "已排期",
  ENDED: "已结束",
  CANCELLED: "已取消",
};

const formatTime = (value: string) =>
  new Intl.DateTimeFormat("zh-CN", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));

function datetimeAfter(minutes: number) {
  const value = new Date(Date.now() + minutes * 60_000);
  value.setMinutes(value.getMinutes() - value.getTimezoneOffset());
  return value.toISOString().slice(0, 16);
}

export function MerchantMarketing() {
  const { user, loading: sessionLoading } = useSession();
  const [activities, setActivities] = useState<MerchantPromotionActivity[]>([]);
  const [products, setProducts] = useState<MerchantProduct[]>([]);
  const [skus, setSkus] = useState<ProductDetail["skus"]>([]);
  const [filter, setFilter] = useState<Filter>("ALL");
  const [selectedProductId, setSelectedProductId] = useState("");
  const [selectedSkuId, setSelectedSkuId] = useState("");
  const [name, setName] = useState("");
  const [startAt, setStartAt] = useState(() => datetimeAfter(15));
  const [endAt, setEndAt] = useState(() => datetimeAfter(75));
  const [activityPrice, setActivityPrice] = useState("");
  const [stockTotal, setStockTotal] = useState("1");
  const [limitPerUser, setLimitPerUser] = useState("1");
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState<number | "create" | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [nextActivities, nextProducts] = await Promise.all([
        apiClient<MerchantPromotionActivity[]>("/api/backend/merchant/promotions"),
        apiClient<MerchantProduct[]>("/api/backend/merchant/products?page=1&size=50"),
      ]);
      setActivities(nextActivities);
      setProducts(nextProducts.filter((product) => product.status === "ON_SALE"));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "真实营销活动暂时无法读取。");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (sessionLoading || !user || user.isDemo) return;
    void load();
  }, [load, sessionLoading, user]);

  useEffect(() => {
    if (!selectedProductId || !user?.tenantId) {
      setSkus([]);
      setSelectedSkuId("");
      return;
    }
    let cancelled = false;
    apiClient<ProductDetail>(`/api/backend/public/stores/${user.tenantId}/products/${selectedProductId}`)
      .then((product) => {
        if (cancelled) return;
        setSkus(product.skus);
        const first = product.skus[0];
        setSelectedSkuId(String(first?.id ?? ""));
        setActivityPrice(String(first?.salePrice ?? ""));
      })
      .catch((caught) => !cancelled && setError(caught instanceof Error ? caught.message : "SKU 暂时无法读取。"));
    return () => {
      cancelled = true;
    };
  }, [selectedProductId, user?.tenantId]);

  const visible = useMemo(
    () => activities.filter((activity) => filter === "ALL" || activity.status === filter),
    [activities, filter],
  );
  const activeCount = useMemo(() => activities.filter((activity) => activity.status === "ACTIVE").length, [activities]);
  const activeStock = useMemo(
    () => activities
      .filter((activity) => activity.status === "ACTIVE")
      .reduce((sum, activity) => sum + activity.stockAvailable, 0),
    [activities],
  );

  async function create(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setMessage("");
    setBusy("create");
    try {
      await apiClient<number>("/api/backend/merchant/promotions", {
        method: "POST",
        body: JSON.stringify({
          name,
          startAt,
          endAt,
          skuId: Number(selectedSkuId),
          activityPrice: Number(activityPrice),
          stockTotal: Number(stockTotal),
          limitPerUser: Number(limitPerUser),
        }),
      });
      setMessage("活动已创建。开始前请在本页执行预热。");
      setOpen(false);
      setName("");
      await load();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "活动创建失败。");
    } finally {
      setBusy(null);
    }
  }

  async function runAction(activity: MerchantPromotionActivity, action: "preheat" | "cancel") {
    setError("");
    setMessage("");
    setBusy(activity.activityId);
    try {
      await apiClient<null>(`/api/backend/merchant/promotions/${activity.activityId}${action === "preheat" ? "/preheat" : ""}`, {
        method: action === "preheat" ? "POST" : "DELETE",
      });
      setMessage(action === "preheat" ? "活动已预热，抢购规则已写入 Redis。" : "活动已取消，未使用库存已归还。");
      await load();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "操作未完成。");
    } finally {
      setBusy(null);
    }
  }

  if (sessionLoading) {
    return <MerchantShell title="营销活动" eyebrow="GROWTH / CAMPAIGNS"><div className="empty-state"><p>正在确认会话状态…</p></div></MerchantShell>;
  }
  if (user?.isDemo) {
    return <MerchantShell title="营销活动" eyebrow="GROWTH / CAMPAIGNS"><div className="empty-state"><span className="eyebrow">DEMO / READ ONLY</span><h2>演示账户没有真实促销活动。</h2><p>此页面不会用演示活动替代真实营销数据。</p></div></MerchantShell>;
  }

  return (
    <MerchantShell
      title="营销活动"
      eyebrow="GROWTH / CAMPAIGNS"
      actions={<button className="button primary" onClick={() => setOpen((value) => !value)} type="button">{open ? "收起创建" : "＋ 创建活动"}</button>}
    >
      <section className="merchant-kicker-grid">
        <article><span>全部活动</span><strong>{activities.length}</strong><small>当前商家真实数据</small></article>
        <article><span>进行中</span><strong>{activeCount}</strong><small>状态以服务端为准</small></article>
        <article><span>活动可用库存</span><strong>{activeStock}</strong><small>仅统计进行中活动</small></article>
      </section>
      {open ? <form className="promotion-form surface" onSubmit={(event) => void create(event)}>
        <header><div><span className="eyebrow">NEW / LIMITED SALE</span><h2>创建限量促销</h2></div><p>活动库存会从普通可售库存中划拨；开始前需要手动预热。</p></header>
        <label className="form-field">活动名称<input onChange={(event) => setName(event.target.value)} required value={name} /></label>
        <label className="form-field">参与商品<select onChange={(event) => setSelectedProductId(event.target.value)} required value={selectedProductId}><option value="">选择已上架商品</option>{products.map((product) => <option key={product.id} value={product.id}>{product.name}</option>)}</select></label>
        <label className="form-field">SKU<select disabled={!skus.length} onChange={(event) => { const sku = skus.find((entry) => entry.id === Number(event.target.value)); setSelectedSkuId(event.target.value); if (sku) setActivityPrice(String(sku.salePrice)); }} required value={selectedSkuId}><option value="">选择 SKU</option>{skus.map((sku) => <option key={sku.id} value={sku.id}>{sku.skuName} · 可售 {sku.availableStock}</option>)}</select></label>
        <label className="form-field">活动价格（元）<input min="0.01" onChange={(event) => setActivityPrice(event.target.value)} required step="0.01" type="number" value={activityPrice} /></label>
        <label className="form-field">活动库存<input min="1" onChange={(event) => setStockTotal(event.target.value)} required type="number" value={stockTotal} /></label>
        <label className="form-field">每人限购<input min="1" onChange={(event) => setLimitPerUser(event.target.value)} required type="number" value={limitPerUser} /></label>
        <label className="form-field">开始时间<input onChange={(event) => setStartAt(event.target.value)} required type="datetime-local" value={startAt} /></label>
        <label className="form-field">结束时间<input onChange={(event) => setEndAt(event.target.value)} required type="datetime-local" value={endAt} /></label>
        <footer><button className="button primary" disabled={busy === "create" || !selectedSkuId} type="submit">{busy === "create" ? "创建中…" : "创建真实活动"}</button></footer>
      </form> : null}
      {error ? <p className="form-error">{error}</p> : null}
      {message ? <p className="form-success">{message}</p> : null}
      <div className="merchant-toolbar surface">
        <div className="merchant-tab-list">
          {([ ["ALL", "全部"], ["ACTIVE", "进行中"], ["SCHEDULED", "已排期"], ["ENDED", "已结束"], ["CANCELLED", "已取消"] ] as const).map(([value, label]) => <button className={filter === value ? "active" : ""} key={value} onClick={() => setFilter(value)} type="button">{label}</button>)}
        </div>
        <span className="eyebrow">{loading ? "LOADING…" : `${visible.length} CAMPAIGNS`}</span>
      </div>
      {!loading && !error && !visible.length ? <div className="empty-state"><h2>还没有匹配的真实活动。</h2><p>创建活动后，这里会显示服务端返回的状态和库存。</p></div> : null}
      <div className="table-scroll">
        <table className="data-table">
          <thead><tr><th>活动</th><th>商品 / SKU</th><th>活动价格</th><th>库存</th><th>周期</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>{visible.map((activity) => <tr key={activity.activityId}><td><strong>{activity.name}</strong><br /><small>ACTIVITY / {activity.activityId}</small></td><td>{activity.productName}<br /><small>{activity.skuName}</small></td><td>¥{Number(activity.activityPrice).toFixed(2)}<br /><small>每人限购 {activity.limitPerUser}</small></td><td>{activity.stockAvailable} / {activity.stockTotal}</td><td>{formatTime(activity.startAt)}<br /><small>至 {formatTime(activity.endAt)}</small></td><td><span className="status-pill">{labels[activity.status] ?? activity.status}</span></td><td><div className="promotion-actions">{activity.status === "SCHEDULED" ? <><button disabled={busy === activity.activityId} onClick={() => void runAction(activity, "preheat")} type="button">预热</button><button disabled={busy === activity.activityId} onClick={() => void runAction(activity, "cancel")} type="button">取消</button></> : <small>不可操作</small>}</div></td></tr>)}</tbody>
        </table>
      </div>
    </MerchantShell>
  );
}
