"use client";

import { apiClient } from "@/lib/client-api";
import { createDemoOrders, type DemoCartLine, readDemoCart } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import { checkoutKey, getIdempotencyKey } from "@/lib/idempotency";
import type { CartItem, ConsumerAddress, CreateCheckoutGroupResult } from "@/lib/types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { RequestFailure } from "./RequestFailure";
import { useSession } from "./SessionProvider";

type CheckoutMode = "loading" | "live" | "demo" | "error";

type LiveStoreGroup = {
  storeId: number | null;
  storeName: string | null;
  items: CartItem[];
};

function requestedCartItemIds(value: string | null) {
  if (!value) return [];
  return [...new Set(value.split(",").map(Number).filter((id) => Number.isSafeInteger(id) && id > 0))];
}

function groupLiveItems(items: CartItem[]) {
  const groups = new Map<string, LiveStoreGroup>();
  for (const item of items) {
    const key = item.storeId === null ? `unknown-${item.id}` : String(item.storeId);
    const group = groups.get(key) ?? { storeId: item.storeId, storeName: item.storeName, items: [] };
    group.items.push(item);
    groups.set(key, group);
  }
  return [...groups.values()];
}

export function CheckoutClient() {
  const [mode, setMode] = useState<CheckoutMode>("loading");
  const [demoLines, setDemoLines] = useState<DemoCartLine[]>([]);
  const [liveItems, setLiveItems] = useState<CartItem[]>([]);
  const [addresses, setAddresses] = useState<ConsumerAddress[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [name, setName] = useState("林默");
  const [phone, setPhone] = useState("138 0000 2026");
  const [address, setAddress] = useState("浙江省杭州市西湖区明日路 26 号");
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);
  const [failure, setFailure] = useState<unknown>(null);
  const [message, setMessage] = useState("");
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, loading: sessionLoading } = useSession();
  const demoSession = user?.isDemo === true;
  const requestedIds = useMemo(() => requestedCartItemIds(searchParams.get("cartItemIds")), [searchParams]);

  useEffect(() => {
    if (sessionLoading) return;
    let cancelled = false;

    async function load() {
      setMode("loading");
      setFailure(null);
      setMessage("");
      if (demoSession) {
        if (!cancelled) {
          setDemoLines(readDemoCart());
          setMode("demo");
        }
        return;
      }

      try {
        const [cart, savedAddresses] = await Promise.all([
          apiClient<CartItem[]>("/api/backend/cart/items"),
          apiClient<ConsumerAddress[]>("/api/backend/addresses"),
        ]);
        if (!cancelled) {
          setLiveItems(cart);
          setAddresses(savedAddresses);
          setSelectedAddressId((current) => current ?? savedAddresses.find((entry) => entry.isDefault)?.id ?? savedAddresses[0]?.id ?? null);
          setMode("live");
        }
      } catch (caught) {
        if (!cancelled) {
          setFailure(caught);
          setMode("error");
        }
      }
    }

    void load();
    return () => {
      cancelled = true;
    };
  }, [demoSession, sessionLoading]);

  const chosenLiveItems = useMemo(() => {
    if (!requestedIds.length) return liveItems;
    return requestedIds.map((id) => liveItems.find((item) => item.id === id)).filter((item): item is CartItem => Boolean(item));
  }, [liveItems, requestedIds]);
  const selectedItemsMissing = requestedIds.length > 0 && chosenLiveItems.length !== requestedIds.length;
  const liveGroups = useMemo(() => groupLiveItems(chosenLiveItems), [chosenLiveItems]);
  const demoGroups = useMemo(() => {
    const groups = new Map<number, DemoCartLine[]>();
    for (const line of demoLines) groups.set(line.storeId, [...(groups.get(line.storeId) ?? []), line]);
    return [...groups.entries()];
  }, [demoLines]);
  const lines = demoSession ? demoLines : chosenLiveItems;
  const total = lines.reduce((sum, line) => sum + (line.salePrice ?? 0) * line.quantity, 0);
  const storeCount = demoSession ? demoGroups.length : liveGroups.length;
  const liveBlocked = selectedItemsMissing || chosenLiveItems.some((item) => !item.purchasable || item.salePrice === null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy || !lines.length) return;
    setMessage("");

    if (demoSession) {
      if (!name.trim() || !phone.trim() || !address.trim()) return;
      const orders = createDemoOrders(demoLines, { name: name.trim(), phone: phone.trim(), address: address.trim() });
      router.push(`/checkout/success?ids=${orders.map((order) => order.id).join(",")}`);
      return;
    }

    if (liveBlocked) {
      setMessage("所选商品的库存或上架状态已变化，请返回购物袋处理后再结算。");
      return;
    }
    if (selectedAddressId === null) {
      setMessage("请先选择收货地址。");
      return;
    }

    const cartItemIds = chosenLiveItems.map((item) => item.id);
    const idempotencyKey = getIdempotencyKey(cartItemIds);
    setBusy(true);
    try {
      const result = await apiClient<CreateCheckoutGroupResult>("/api/backend/checkouts", {
        method: "POST",
        headers: { "Idempotency-Key": idempotencyKey },
        body: JSON.stringify({ cartItemIds, addressId: selectedAddressId }),
      });
      sessionStorage.removeItem(checkoutKey(cartItemIds));
      router.replace(`/checkout/success?checkoutGroupId=${result.checkoutGroupId}`);
    } catch (caught) {
      setMessage(caught instanceof Error ? caught.message : "结算暂未完成，请重试。");
    } finally {
      setBusy(false);
    }
  }

  if (mode === "loading") {
    return <main className="page-shell checkout-page-shell"><div className="empty-state"><p>正在整理结算信息…</p></div></main>;
  }

  if (mode === "error") {
    return <main className="page-shell checkout-page-shell"><RequestFailure error={failure} loginHref="/consumer/login?redirect=/checkout" onRetry={() => window.location.reload()} title="结算信息暂时无法读取" /></main>;
  }

  if (!lines.length) {
    return (
      <main className="page-shell checkout-page-shell">
        <div className="empty-state">
          <span className="eyebrow">CHECKOUT / EMPTY</span>
          <h2>{selectedItemsMissing ? "购物袋内容已变化。" : "购物袋已经空了。"}</h2>
          <p>{selectedItemsMissing ? "请返回购物袋确认最新商品状态。" : "回到市集挑选商品后，再继续结算。"}</p>
          <Link className="button primary" href="/cart">返回购物袋</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="page-shell checkout-page-shell">
      <Link className="market-back-link" href="/cart">← 返回购物袋</Link>
      <header className="page-intro checkout-intro">
        <div><span className="eyebrow">CHECKOUT / {storeCount} STORES</span><h1>确认结算</h1></div>
        <p>只选择一次收货地址，系统会按店铺创建 {storeCount} 笔独立订单，并汇总为一笔结算记录。</p>
      </header>
      {demoSession && <DemoNotice>演示结算不会扣款、锁定库存或向商家发送订单。</DemoNotice>}
      <form className="checkout-layout" onSubmit={(event) => void submit(event)}>
        <div className="checkout-sections">
          {demoSession ? (
            <section className="checkout-section surface">
              <div className="checkout-section__head"><span className="eyebrow">01 / DELIVERY</span><h2>收货信息</h2></div>
              <div className="checkout-fields">
                <label><span>收货人</span><input value={name} onChange={(event) => setName(event.target.value)} required /></label>
                <label><span>联系电话</span><input value={phone} onChange={(event) => setPhone(event.target.value)} required /></label>
                <label className="wide"><span>详细地址</span><input value={address} onChange={(event) => setAddress(event.target.value)} required /></label>
                <label className="wide"><span>订单备注（选填）</span><textarea value={note} onChange={(event) => setNote(event.target.value)} placeholder="给店铺的补充说明" /></label>
              </div>
            </section>
          ) : (
            <section className="checkout-section surface">
              <div className="checkout-section__head"><span className="eyebrow">01 / DELIVERY</span><h2>选择收货地址</h2></div>
              {addresses.length ? (
                <div className="checkout-address-list">
                  {addresses.map((entry) => (
                    <label className={`checkout-address ${selectedAddressId === entry.id ? "is-selected" : ""}`} key={entry.id}>
                      <input checked={selectedAddressId === entry.id} name="address" onChange={() => setSelectedAddressId(entry.id)} type="radio" value={entry.id} />
                      <span><strong>{entry.receiverName} · {entry.receiverPhone}</strong>{entry.isDefault && <b>默认</b>}<small>{entry.province}{entry.city}{entry.district}{entry.detailAddress}</small></span>
                    </label>
                  ))}
                </div>
              ) : (
                <div className="checkout-address-empty"><p>还没有可用于结算的收货地址。</p><Link className="button" href="/account/addresses">新增收货地址</Link></div>
              )}
            </section>
          )}
          <section className="checkout-section surface">
            <div className="checkout-section__head"><span className="eyebrow">02 / SPLIT ORDERS</span><h2>按店铺确认</h2></div>
            {demoSession
              ? demoGroups.map(([storeId, storeLines]) => (
                  <article className="checkout-store" key={storeId}>
                    <header><div><span className="eyebrow">STORE {storeId}</span><h3>{storeLines[0].storeName}</h3></div><strong>{currency(storeLines.reduce((sum, line) => sum + line.salePrice * line.quantity, 0))}</strong></header>
                    {storeLines.map((line) => <div className="checkout-line" key={line.id}><span>{line.productName} · {line.skuName}</span><span>× {line.quantity}</span><strong>{currency(line.salePrice * line.quantity)}</strong></div>)}
                  </article>
                ))
              : liveGroups.map((group) => (
                  <article className="checkout-store" key={group.storeId ?? group.items[0].id}>
                    <header><div><span className="eyebrow">{group.storeId === null ? "STORE / UNAVAILABLE" : `STORE ${group.storeId}`}</span><h3>{group.storeName ?? "商品信息暂不可用"}</h3></div><strong>{currency(group.items.reduce((sum, item) => sum + (item.salePrice ?? 0) * item.quantity, 0))}</strong></header>
                    {group.items.map((item) => <div className="checkout-line" key={item.id}><span>{item.productName ?? "商品已不可用"} · {item.skuName ?? `SKU ${item.skuId}`}{!item.purchasable && "（不可购买）"}</span><span>× {item.quantity}</span><strong>{currency((item.salePrice ?? 0) * item.quantity)}</strong></div>)}
                  </article>
                ))}
          </section>
        </div>
        <aside className="checkout-submit surface">
          <span className="eyebrow">PAYMENT SUMMARY</span>
          <h2>{currency(total)}</h2>
          <div><span>商品</span><strong>{lines.reduce((sum, line) => sum + line.quantity, 0)} 件</strong></div>
          <div><span>配送</span><strong>免运费</strong></div>
          <div><span>生成订单</span><strong>{storeCount} 笔</strong></div>
          <p>{demoSession ? "提交后仍处于“待支付”，你可以分别查看每家店铺的订单。" : "提交时由服务端再次核验地址、库存、商品状态与拆单结果。"}</p>
          <button className="button primary" disabled={busy || (!demoSession && (!addresses.length || liveBlocked))} type="submit">
            {busy ? "正在创建结算…" : !demoSession && liveBlocked ? "请先处理不可购买商品" : demoSession ? `提交 ${storeCount} 笔演示订单` : `提交 ${storeCount} 笔真实订单`}
          </button>
          {message && <p className="form-error" role="alert">{message}</p>}
        </aside>
      </form>
    </main>
  );
}
