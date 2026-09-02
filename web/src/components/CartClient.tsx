"use client";

import { apiClient } from "@/lib/client-api";
import {
  type DemoCartLine,
  readDemoCart,
  removeDemoCartLine,
  updateDemoCartLine,
} from "@/lib/demo-commerce";
import { currency, visualFor } from "@/lib/demo-data";
import { checkoutKey, getIdempotencyKey } from "@/lib/idempotency";
import type { CartItem, CreateOrderResult } from "@/lib/types";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { RequestFailure } from "./RequestFailure";
import { useSession } from "./SessionProvider";

type CartMode = "loading" | "live" | "demo" | "error";

export function CartClient() {
  const [mode, setMode] = useState<CartMode>("loading");
  const [items, setItems] = useState<CartItem[]>([]);
  const [demoItems, setDemoItems] = useState<DemoCartLine[]>([]);
  const [error, setError] = useState("");
  const [failure, setFailure] = useState<unknown>(null);
  const [busy, setBusy] = useState(false);
  const router = useRouter();
  const { user, loading: sessionLoading } = useSession();
  const demoSession = user?.isDemo === true;
  const count = useMemo(
    () =>
      mode === "demo"
        ? demoItems.reduce((sum, item) => sum + item.quantity, 0)
        : items.reduce((sum, item) => sum + item.quantity, 0),
    [demoItems, items, mode],
  );
  const demoTotal = useMemo(
    () => demoItems.reduce((sum, item) => sum + item.salePrice * item.quantity, 0),
    [demoItems],
  );
  const groupedDemo = useMemo(() => {
    const groups = new Map<number, DemoCartLine[]>();
    for (const item of demoItems) groups.set(item.storeId, [...(groups.get(item.storeId) ?? []), item]);
    return [...groups.entries()];
  }, [demoItems]);

  useEffect(() => {
    if (sessionLoading) return;
    let cancelled = false;
    async function load() {
      setMode("loading");
      setError("");
      setFailure(null);
      if (demoSession) {
        setDemoItems(readDemoCart());
        setMode("demo");
        return;
      }
      try {
        const nextItems = await apiClient<CartItem[]>("/api/backend/cart/items");
        if (!cancelled) {
          setItems(nextItems);
          setMode("live");
        }
      } catch (caught) {
        if (!cancelled) {
          setItems([]);
          setFailure(caught);
          setMode("error");
        }
      }
    }
    void load();
    return () => {
      cancelled = true;
    };
  }, [sessionLoading, demoSession]);

  async function liveQuantity(item: CartItem, next: number) {
    const value = Math.max(1, next);
    const previous = item.quantity;
    setItems((current) => current.map((entry) => (entry.id === item.id ? { ...entry, quantity: value } : entry)));
    try {
      await apiClient<CartItem>(`/api/backend/cart/items/${item.id}`, {
        method: "PUT",
        body: JSON.stringify({ quantity: value }),
      });
    } catch (caught) {
      setItems((current) => current.map((entry) => (entry.id === item.id ? { ...entry, quantity: previous } : entry)));
      setError(caught instanceof Error ? caught.message : "更新失败。");
    }
  }

  async function liveRemove(item: CartItem) {
    const previous = items;
    setItems((current) => current.filter((entry) => entry.id !== item.id));
    try {
      await apiClient<null>(`/api/backend/cart/items/${item.id}`, { method: "DELETE" });
    } catch (caught) {
      setItems(previous);
      setError(caught instanceof Error ? caught.message : "移除失败。");
    }
  }

  async function checkout() {
    if (busy || !count) return;
    if (mode === "demo") {
      router.push("/checkout");
      return;
    }
    setBusy(true);
    setError("");
    const ids = items.map((item) => item.id);
    const idempotency = getIdempotencyKey(ids);
    try {
      const order = await apiClient<CreateOrderResult>("/api/backend/orders", {
        method: "POST",
        headers: { "Idempotency-Key": idempotency },
        body: JSON.stringify({ cartItemIds: ids }),
      });
      sessionStorage.removeItem(checkoutKey(ids));
      sessionStorage.setItem(`morrow_created_order_${order.orderId}`, JSON.stringify(order));
      router.push(`/orders/${order.orderId}?created=1`);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "下单失败。");
    } finally {
      setBusy(false);
    }
  }

  const hasItems = mode === "demo" ? demoItems.length > 0 : items.length > 0;

  return (
    <main className="page-shell cart-page-shell">
      <header className="page-intro">
        <div>
          <span className="eyebrow">YOUR BAG / {count}</span>
          <h1>购物袋</h1>
        </div>
        <p>{mode === "demo" ? "不同店铺的商品会分别结算并生成独立订单。" : "价格和库存会在下单时由后端再次确认。"}</p>
      </header>
      {mode === "demo" && <DemoNotice>当前使用演示购物袋；数据仅保存在本机浏览器，不会提交真实订单。</DemoNotice>}
      {mode === "loading" ? (
        <div className="empty-state"><p>正在读取购物袋…</p></div>
      ) : mode === "error" ? (
        <RequestFailure error={failure} loginHref="/consumer/login?redirect=/cart" onRetry={() => window.location.reload()} title="购物袋暂时无法读取" />
      ) : hasItems ? (
        <div className="cart-layout">
          <section className="cart-groups">
            {mode === "demo"
              ? groupedDemo.map(([storeId, lines]) => (
                  <section className="cart-store-group" key={storeId}>
                    <header>
                      <div><span className="eyebrow">STORE {storeId}</span><h2>{lines[0].storeName}</h2></div>
                      <Link href={`/stores/${storeId}`}>查看店铺 ↗</Link>
                    </header>
                    {lines.map((item) => {
                      const visual = visualFor(item.productId);
                      return (
                        <article className="cart-item" key={item.id}>
                          <div className="cart-thumb" style={{ background: visual.tone }}>
                            <Image src={visual.image} alt={item.productName} fill sizes="100px" />
                          </div>
                          <div>
                            <span className="eyebrow">{item.skuName}</span>
                            <h2>{item.productName}</h2>
                            <p>{currency(item.salePrice)} / 件</p>
                            <button onClick={() => setDemoItems(removeDemoCartLine(item.id))} type="button">移除</button>
                          </div>
                          <div className="cart-line-price">
                            <strong>{currency(item.salePrice * item.quantity)}</strong>
                            <div className="stepper">
                              <button aria-label={`减少 ${item.productName} 数量`} onClick={() => setDemoItems(updateDemoCartLine(item.id, item.quantity - 1))} type="button">−</button>
                              <span>{item.quantity}</span>
                              <button aria-label={`增加 ${item.productName} 数量`} onClick={() => setDemoItems(updateDemoCartLine(item.id, item.quantity + 1))} type="button">+</button>
                            </div>
                          </div>
                        </article>
                      );
                    })}
                    <footer><span>店铺小计</span><strong>{currency(lines.reduce((sum, item) => sum + item.salePrice * item.quantity, 0))}</strong></footer>
                  </section>
                ))
              : items.map((item) => {
                  const visual = visualFor(Math.floor(item.skuId / 100));
                  return (
                    <article className="cart-item" key={item.id}>
                      <div className="cart-thumb"><Image src={visual.image} alt="购物袋商品" fill sizes="100px" /></div>
                      <div>
                        <span className="eyebrow">SKU {item.skuId}</span>
                        <h2>商品 SKU {item.skuId}</h2>
                        <p>价格将在下单时确认</p>
                        <button onClick={() => void liveRemove(item)} type="button">移除</button>
                      </div>
                      <div className="stepper">
                        <button aria-label={`减少 SKU ${item.skuId} 数量`} onClick={() => void liveQuantity(item, item.quantity - 1)} type="button">−</button>
                        <span>{item.quantity}</span>
                        <button aria-label={`增加 SKU ${item.skuId} 数量`} onClick={() => void liveQuantity(item, item.quantity + 1)} type="button">+</button>
                      </div>
                    </article>
                  );
                })}
          </section>
          <aside className="cart-summary surface">
            <span className="eyebrow">ORDER SUMMARY</span>
            <h2>订单摘要</h2>
            <div className="summary-row"><span>商品数量</span><strong>{count} 件</strong></div>
            <div className="summary-row"><span>店铺数量</span><strong>{mode === "demo" ? groupedDemo.length : 1} 家</strong></div>
            <div className="summary-row"><span>配送</span><strong>免运费</strong></div>
            <div className="summary-row total"><span>订单总额</span><strong>{mode === "demo" ? currency(demoTotal) : "提交后确认"}</strong></div>
            <button className="button primary" disabled={busy} onClick={() => void checkout()} type="button">
              {busy ? "正在提交…" : "继续结算"}
            </button>
            <p>跨店商品将在提交后拆分为 {mode === "demo" ? groupedDemo.length : 1} 笔订单。</p>
            {error && <p className="form-error">{error}</p>}
          </aside>
        </div>
      ) : (
        <div className="empty-state">
          <h2>这里还没有你的选择。</h2>
          <p>从一间喜欢的店铺开始，挑一件能让今天变好的物件。</p>
          <Link className="button primary" href="/stores">去逛店铺</Link>
        </div>
      )}
    </main>
  );
}
