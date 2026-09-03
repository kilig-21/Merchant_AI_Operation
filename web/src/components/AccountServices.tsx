"use client";

import {
  createDemoAfterSale,
  type DemoAddress,
  readDemoAddresses,
  readDemoAfterSales,
  readDemoFavorites,
  saveDemoAddresses,
  saveDemoFavorites,
} from "@/lib/demo-account";
import { apiClient } from "@/lib/client-api";
import { readDemoOrders } from "@/lib/demo-commerce";
import { currency, visualFor } from "@/lib/demo-data";
import type { ConsumerAddress } from "@/lib/types";
import Image from "next/image";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { type FormEvent, useCallback, useEffect, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { RequestFailure } from "./RequestFailure";
import { StatusPill } from "./StatusPill";
import { useSession } from "./SessionProvider";

const emptyAddress: Omit<DemoAddress, "id"> = { name: "", phone: "", city: "", detail: "", isDefault: false };

export function AddressBookClient() {
  const { user, loading } = useSession();
  if (loading) return <ServiceLoading />;
  return user?.isDemo ? <DemoAddressBook /> : <LiveAddressBook />;
}

function DemoAddressBook() {
  const [addresses, setAddresses] = useState<DemoAddress[]>([]);
  const [draft, setDraft] = useState(emptyAddress);
  const [open, setOpen] = useState(false);
  useEffect(() => {
    setAddresses(readDemoAddresses());
  }, []);
  function submit(event: FormEvent) {
    event.preventDefault();
    const next = [...addresses.map((item) => ({ ...item, isDefault: draft.isDefault ? false : item.isDefault })), { ...draft, id: Date.now() }];
    setAddresses(saveDemoAddresses(next));
    setDraft(emptyAddress);
    setOpen(false);
  }
  function makeDefault(id: number) {
    setAddresses(saveDemoAddresses(addresses.map((item) => ({ ...item, isDefault: item.id === id }))));
  }
  function remove(id: number) {
    const rest = addresses.filter((item) => item.id !== id);
    if (rest.length && !rest.some((item) => item.isDefault)) rest[0].isDefault = true;
    setAddresses(saveDemoAddresses(rest));
  }
  return (
    <main className="page-shell account-service-shell">
      <Link className="eyebrow" href="/account">← 返回账户</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">DELIVERY / ADDRESS BOOK</span><h1>收货信息</h1></div><p>在结算之前，把常用地址整理好。演示数据只保存在当前浏览器。</p></header>
      <DemoNotice>这是可操作的演示地址簿，新增、设为默认和删除都会保存在本机。</DemoNotice>
      <div className="account-service-actions"><button className="button primary" type="button" onClick={() => setOpen((value) => !value)}>{open ? "收起表单" : "新增地址"}</button></div>
      {open && (
        <form className="account-form surface" onSubmit={submit}>
          <label><span>收货人</span><input required value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} /></label>
          <label><span>手机号码</span><input required value={draft.phone} onChange={(event) => setDraft({ ...draft, phone: event.target.value })} /></label>
          <label><span>省市区</span><input required value={draft.city} onChange={(event) => setDraft({ ...draft, city: event.target.value })} /></label>
          <label className="wide"><span>详细地址</span><input required value={draft.detail} onChange={(event) => setDraft({ ...draft, detail: event.target.value })} /></label>
          <label className="account-check"><input type="checkbox" checked={draft.isDefault} onChange={(event) => setDraft({ ...draft, isDefault: event.target.checked })} /><span>设为默认地址</span></label>
          <button className="button primary" type="submit">保存地址</button>
        </form>
      )}
      <section className="address-grid">
        {addresses.map((address, index) => (
          <article className="address-card surface" key={address.id}>
            <header><span className="eyebrow">ADDRESS / {String(index + 1).padStart(2, "0")}</span>{address.isDefault && <b>默认</b>}</header>
            <h2>{address.name}</h2><p>{address.phone}</p><p>{address.city}<br />{address.detail}</p>
            <footer>{!address.isDefault && <button type="button" onClick={() => makeDefault(address.id)}>设为默认</button>}<button type="button" onClick={() => remove(address.id)}>删除</button></footer>
          </article>
        ))}
      </section>
    </main>
  );
}

type AddressDraft = Pick<
  ConsumerAddress,
  "receiverName" | "receiverPhone" | "province" | "city" | "district" | "detailAddress" | "isDefault"
>;

const emptyLiveAddress: AddressDraft = {
  receiverName: "",
  receiverPhone: "",
  province: "",
  city: "",
  district: "",
  detailAddress: "",
  isDefault: false,
};

function draftFromAddress(address: ConsumerAddress): AddressDraft {
  return {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    province: address.province,
    city: address.city,
    district: address.district,
    detailAddress: address.detailAddress,
    isDefault: address.isDefault,
  };
}

function LiveAddressBook() {
  const [addresses, setAddresses] = useState<ConsumerAddress[]>([]);
  const [draft, setDraft] = useState<AddressDraft>(emptyLiveAddress);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<"loading" | "ready" | "error">("loading");
  const [failure, setFailure] = useState<unknown>(null);
  const [message, setMessage] = useState("");
  const [saving, setSaving] = useState(false);
  const [actionId, setActionId] = useState<number | null>(null);

  const reload = useCallback(async () => {
    setMode("loading");
    setFailure(null);
    try {
      const next = await apiClient<ConsumerAddress[]>("/api/backend/addresses");
      setAddresses(next);
      setMode("ready");
    } catch (caught) {
      setFailure(caught);
      setMode("error");
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  function startCreate() {
    setDraft(emptyLiveAddress);
    setEditingId(null);
    setMessage("");
    setOpen(true);
  }

  function startEdit(address: ConsumerAddress) {
    setDraft(draftFromAddress(address));
    setEditingId(address.id);
    setMessage("");
    setOpen(true);
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    try {
      if (editingId === null) {
        await apiClient<null>("/api/backend/addresses", {
          method: "POST",
          body: JSON.stringify(draft),
        });
      } else {
        await apiClient<null>(`/api/backend/addresses/${editingId}`, {
          method: "PUT",
          body: JSON.stringify(draft),
        });
      }
      await reload();
      setDraft(emptyLiveAddress);
      setEditingId(null);
      setOpen(false);
    } catch (caught) {
      setMessage(caught instanceof Error ? caught.message : "地址未能保存，请稍后重试。");
    } finally {
      setSaving(false);
    }
  }

  async function makeDefault(address: ConsumerAddress) {
    setActionId(address.id);
    setMessage("");
    try {
      await apiClient<null>(`/api/backend/addresses/${address.id}`, {
        method: "PUT",
        body: JSON.stringify({ ...draftFromAddress(address), isDefault: true }),
      });
      await reload();
    } catch (caught) {
      setMessage(caught instanceof Error ? caught.message : "默认地址切换失败，请稍后重试。");
    } finally {
      setActionId(null);
    }
  }

  async function remove(address: ConsumerAddress) {
    setActionId(address.id);
    setMessage("");
    try {
      await apiClient<null>(`/api/backend/addresses/${address.id}`, { method: "DELETE" });
      await reload();
    } catch (caught) {
      setMessage(caught instanceof Error ? caught.message : "地址删除失败，请稍后重试。");
    } finally {
      setActionId(null);
    }
  }

  return (
    <main className="page-shell account-service-shell">
      <Link className="eyebrow" href="/account">← 返回账户</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">DELIVERY / ADDRESS BOOK</span><h1>收货信息</h1></div><p>保存的地址来自你的真实账户，可在刷新或其他浏览器会话中继续使用。</p></header>
      {mode === "error" ? (
        <RequestFailure error={failure} loginHref="/consumer/login?redirect=/account/addresses" onRetry={() => void reload()} title="地址簿暂时无法读取" />
      ) : (
        <>
          <div className="account-service-actions"><button className="button primary" type="button" onClick={startCreate}>{open ? "新增另一地址" : "新增地址"}</button></div>
          {open && (
            <form className="account-form surface" onSubmit={(event) => void submit(event)}>
              <label><span>收货人</span><input required value={draft.receiverName} onChange={(event) => setDraft({ ...draft, receiverName: event.target.value })} /></label>
              <label><span>手机号码</span><input required value={draft.receiverPhone} onChange={(event) => setDraft({ ...draft, receiverPhone: event.target.value })} /></label>
              <label><span>省份</span><input required value={draft.province} onChange={(event) => setDraft({ ...draft, province: event.target.value })} /></label>
              <label><span>城市</span><input required value={draft.city} onChange={(event) => setDraft({ ...draft, city: event.target.value })} /></label>
              <label><span>区县</span><input required value={draft.district} onChange={(event) => setDraft({ ...draft, district: event.target.value })} /></label>
              <label className="wide"><span>详细地址</span><input required value={draft.detailAddress} onChange={(event) => setDraft({ ...draft, detailAddress: event.target.value })} /></label>
              <label className="account-check"><input type="checkbox" checked={draft.isDefault} onChange={(event) => setDraft({ ...draft, isDefault: event.target.checked })} /><span>设为默认地址</span></label>
              <button className="button primary" disabled={saving} type="submit">{saving ? "保存中…" : editingId === null ? "保存地址" : "保存修改"}</button>
              {message && <p className="form-error" role="alert">{message}</p>}
            </form>
          )}
          {mode === "loading" ? (
            <div className="empty-state"><p>正在读取真实地址簿…</p></div>
          ) : addresses.length ? (
            <section className="address-grid">
              {addresses.map((address, index) => (
                <article className="address-card surface" key={address.id}>
                  <header><span className="eyebrow">ADDRESS / {String(index + 1).padStart(2, "0")}</span>{address.isDefault && <b>默认</b>}</header>
                  <h2>{address.receiverName}</h2><p>{address.receiverPhone}</p><p>{address.province}{address.city}{address.district}<br />{address.detailAddress}</p>
                  <footer>
                    <button disabled={actionId === address.id} type="button" onClick={() => startEdit(address)}>编辑</button>
                    {!address.isDefault && <button disabled={actionId === address.id} type="button" onClick={() => void makeDefault(address)}>设为默认</button>}
                    <button disabled={actionId === address.id} type="button" onClick={() => void remove(address)}>{actionId === address.id ? "处理中…" : "删除"}</button>
                  </footer>
                </article>
              ))}
            </section>
          ) : (
            <div className="empty-state"><h2>还没有保存收货地址。</h2><p>新增后，地址会保存到你的真实账户。</p><button className="button primary" type="button" onClick={startCreate}>新增地址</button></div>
          )}
          {!open && message && <p className="form-error" role="alert">{message}</p>}
        </>
      )}
    </main>
  );
}

export function FavoritesClient() {
  const [favorites, setFavorites] = useState<ReturnType<typeof readDemoFavorites>>([]);
  const { user, loading } = useSession();
  useEffect(() => {
    if (user?.isDemo === true) setFavorites(readDemoFavorites());
  }, [user?.isDemo]);
  if (loading) return <ServiceLoading />;
  if (user?.isDemo !== true) return <LiveFeaturePending title="真实喜欢清单尚未接入" detail="当前不会读取或修改本机演示收藏。" backHref="/account" />;
  return (
    <main className="page-shell account-service-shell">
      <Link className="eyebrow" href="/account">← 返回账户</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">SAVED / OBJECTS</span><h1>喜欢清单</h1></div><p>先收藏，晚一点再决定。这里放的是跨店铺的演示选物。</p></header>
      <DemoNotice>收藏数据为本机演示；移除操作会立即更新当前浏览器中的清单。</DemoNotice>
      {favorites.length ? <section className="favorite-grid">{favorites.map((item) => { const visual = visualFor(item.productId); return (
        <article className="favorite-card surface" key={item.id}><Link className="favorite-card__media" href={`/stores/${item.storeId}/products/${item.productId}`} style={{ background: visual.tone }}><Image src={visual.image} alt={item.productName} fill sizes="(max-width: 680px) 100vw, 33vw" /></Link><div><span className="eyebrow">{item.storeName}</span><h2>{item.productName}</h2><p>{item.note}</p><footer><Link href={`/stores/${item.storeId}/products/${item.productId}`}>查看商品 ↗</Link><button type="button" onClick={() => { const next = favorites.filter((favorite) => favorite.id !== item.id); setFavorites(saveDemoFavorites(next)); }}>移除</button></footer></div></article>
      ); })}</section> : <div className="empty-state"><h2>还没有收藏的物件。</h2><Link className="button primary" href="/stores">去逛店铺</Link></div>}
    </main>
  );
}

export function AfterSalesClient() {
  const [items, setItems] = useState<ReturnType<typeof readDemoAfterSales>>([]);
  const { user, loading } = useSession();
  useEffect(() => {
    if (user?.isDemo === true) setItems(readDemoAfterSales());
  }, [user?.isDemo]);
  if (loading) return <ServiceLoading />;
  if (user?.isDemo !== true) return <LiveFeaturePending title="真实售后服务正在接入" detail="当前不会读取或创建本机演示售后申请。" backHref="/account" />;
  return (
    <main className="page-shell account-service-shell">
      <Link className="eyebrow" href="/account">← 返回账户</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">SERVICE / AFTER SALES</span><h1>售后服务</h1></div><p>退货与退款申请会按店铺分别处理，进度集中保留在这里。</p></header>
      <DemoNotice>当前为演示售后流程，不会影响真实订单或发起真实退款。</DemoNotice>
      <div className="account-service-actions"><Link className="button primary" href="/after-sales/new">申请售后</Link></div>
      {items.length ? <section className="after-sale-list">{items.map((item) => <Link className="after-sale-row surface" href={`/after-sales/${item.id}`} key={item.id}><div><span className="eyebrow">SERVICE / {item.id}</span><h2>{item.storeName}</h2><p>关联订单 {item.orderNo} · {item.reason}</p></div><StatusPill status={item.status} /></Link>)}</section> : <div className="empty-state"><h2>目前没有售后申请。</h2><p>如果已支付的演示订单需要退货或退款，可以从这里开始。</p><Link className="button" href="/orders">查看订单</Link></div>}
    </main>
  );
}

export function AfterSalesCreateClient() {
  const router = useRouter();
  const search = useSearchParams();
  const [orders, setOrders] = useState<ReturnType<typeof readDemoOrders>>([]);
  const [orderId, setOrderId] = useState("");
  const [type, setType] = useState<"RETURN" | "REFUND">("RETURN");
  const [reason, setReason] = useState("商品与预期不符");
  const [description, setDescription] = useState("");
  const { user, loading } = useSession();
  useEffect(() => {
    if (user?.isDemo !== true) return;
    const paid = readDemoOrders().filter((item) => item.status === "PAID");
    setOrders(paid);
    setOrderId(search.get("orderId") ?? String(paid[0]?.id ?? ""));
  }, [search, user?.isDemo]);
  if (loading) return <ServiceLoading />;
  if (user?.isDemo !== true) return <LiveFeaturePending title="真实售后申请正在接入" detail="当前不会使用演示订单创建售后申请。" backHref="/after-sales" />;
  function submit(event: FormEvent) {
    event.preventDefault();
    const order = orders.find((item) => item.id === Number(orderId));
    if (!order) return;
    const item = createDemoAfterSale({ orderId: order.id, orderNo: order.orderNo, storeName: String(order.storeName ?? "演示店铺"), type, reason, description });
    router.push(`/after-sales/${item.id}`);
  }
  return (
    <main className="page-shell account-service-shell narrow-service-shell">
      <Link className="eyebrow" href="/after-sales">← 返回售后服务</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">NEW REQUEST</span><h1>申请售后</h1></div><p>一个申请只关联一家店铺的一笔订单，便于商家独立处理。</p></header>
      {orders.length ? <form className="after-sale-form surface" onSubmit={submit}>
        <label><span>关联订单</span><select value={orderId} onChange={(event) => setOrderId(event.target.value)}>{orders.map((order) => <option value={order.id} key={order.id}>{order.storeName} / {order.orderNo} / {currency(order.totalAmount)}</option>)}</select></label>
        <label><span>售后类型</span><select value={type} onChange={(event) => setType(event.target.value as "RETURN" | "REFUND")}><option value="RETURN">退货退款</option><option value="REFUND">仅退款</option></select></label>
        <label><span>申请原因</span><select value={reason} onChange={(event) => setReason(event.target.value)}><option>商品与预期不符</option><option>商品有损坏</option><option>发错商品</option><option>其他原因</option></select></label>
        <label><span>补充说明</span><textarea required minLength={6} value={description} onChange={(event) => setDescription(event.target.value)} placeholder="请简要说明商品情况和你的诉求" /></label>
        <button className="button primary" type="submit">提交演示申请</button>
      </form> : <div className="empty-state"><h2>没有可申请售后的订单。</h2><p>请先完成一笔演示订单的支付，再回到这里。</p><Link className="button primary" href="/orders">查看订单</Link></div>}
    </main>
  );
}

export function AfterSalesDetailClient({ id }: { id: number }) {
  const [item, setItem] = useState<ReturnType<typeof readDemoAfterSales>[number] | null | undefined>(undefined);
  const { user, loading } = useSession();
  useEffect(() => {
    if (user?.isDemo === true) setItem(readDemoAfterSales().find((entry) => entry.id === id) ?? null);
  }, [id, user?.isDemo]);
  if (loading) return <ServiceLoading />;
  if (user?.isDemo !== true) return <LiveFeaturePending title="真实售后详情正在接入" detail="当前不会读取本机演示售后记录。" backHref="/after-sales" />;
  if (item === undefined) return <main className="page-shell"><div className="empty-state"><p>正在读取售后申请…</p></div></main>;
  if (!item) return <main className="page-shell"><div className="empty-state"><h2>没有找到这笔售后申请。</h2><Link className="button" href="/after-sales">返回售后服务</Link></div></main>;
  return (
    <main className="page-shell account-service-shell narrow-service-shell">
      <Link className="eyebrow" href="/after-sales">← 返回售后服务</Link>
      <header className="page-intro compact-intro"><div><span className="eyebrow">SERVICE / {item.id}</span><h1>申请已提交</h1></div><StatusPill status={item.status} /></header>
      <section className="service-detail surface"><span className="eyebrow">CURRENT STEP / 01</span><h2>等待商家审核</h2><p>商家会在演示流程中查看申请信息。真实系统接入后，这里将展示协商、寄回与退款节点。</p><dl><div><dt>所属店铺</dt><dd>{item.storeName}</dd></div><div><dt>关联订单</dt><dd>{item.orderNo}</dd></div><div><dt>申请类型</dt><dd>{item.type === "RETURN" ? "退货退款" : "仅退款"}</dd></div><div><dt>申请原因</dt><dd>{item.reason}</dd></div><div><dt>补充说明</dt><dd>{item.description}</dd></div></dl></section>
    </main>
  );
}

function ServiceLoading() {
  return <main className="page-shell"><div className="empty-state"><p>正在确认会话状态…</p></div></main>;
}

function LiveFeaturePending({ title, detail, backHref }: { title: string; detail: string; backHref: string }) {
  return (
    <main className="page-shell account-service-shell">
      <div className="empty-state">
        <span className="eyebrow">LIVE SERVICE / PENDING</span>
        <h2>{title}</h2>
        <p>{detail}</p>
        <Link className="button primary" href={backHref}>返回</Link>
      </div>
    </main>
  );
}
