"use client";

import { apiClient } from "@/lib/client-api";
import { visualFor } from "@/lib/demo-data";
import { checkoutKey, getIdempotencyKey } from "@/lib/idempotency";
import type { CartItem, CreateOrderResult } from "@/lib/types";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

export function CartClient() {
  const [items, setItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const router = useRouter();
  const count = useMemo(() => items.reduce((sum, item) => sum + item.quantity, 0), [items]);
  async function load() {
    setLoading(true);
    setError("");
    try {
      setItems(await apiClient<CartItem[]>("/api/backend/cart/items"));
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "购物袋加载失败。");
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    apiClient<CartItem[]>("/api/backend/cart/items")
      .then(setItems)
      .catch((caught) => setError(caught instanceof Error ? caught.message : "购物袋加载失败。"))
      .finally(() => setLoading(false));
  }, []);
  async function quantity(item: CartItem, next: number) {
    const value = Math.max(1, next);
    const previous = item.quantity;
    setItems((current) =>
      current.map((entry) => (entry.id === item.id ? { ...entry, quantity: value } : entry)),
    );
    try {
      await apiClient<CartItem>(`/api/backend/cart/items/${item.id}`, {
        method: "PUT",
        body: JSON.stringify({ quantity: value }),
      });
    } catch (caught) {
      setItems((current) =>
        current.map((entry) => (entry.id === item.id ? { ...entry, quantity: previous } : entry)),
      );
      setError(caught instanceof Error ? caught.message : "更新失败。");
    }
  }
  async function remove(item: CartItem) {
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
    if (busy || !items.length) return;
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
  return (
    <main className="page-shell">
      <header className="page-intro">
        <div>
          <span className="eyebrow">YOUR BAG / {count}</span>
          <h1>购物袋</h1>
        </div>
        <p>价格和库存会在下单时由后端再次确认。</p>
      </header>
      {loading ? (
        <div className="empty-state">
          <p>正在读取购物袋…</p>
        </div>
      ) : error && !items.length ? (
        <div className="empty-state">
          <h2>购物袋暂时无法读取</h2>
          <p>{error}</p>
          <button className="button" onClick={() => void load()} type="button">
            重新加载
          </button>
        </div>
      ) : items.length ? (
        <div className="cart-layout">
          <section>
            {items.map((item) => {
              const visual = visualFor(Math.floor(item.skuId / 100));
              return (
                <article className="cart-item" key={item.id}>
                  <div className="cart-thumb">
                    <Image src={visual.image} alt="购物袋商品" fill sizes="100px" />
                  </div>
                  <div>
                    <span className="eyebrow">SKU {item.skuId}</span>
                    <h2>商品 SKU {item.skuId}</h2>
                    <p>价格将在下单时确认</p>
                    <button onClick={() => void remove(item)} type="button">
                      移除
                    </button>
                  </div>
                  <div className="stepper">
                    <button onClick={() => void quantity(item, item.quantity - 1)} type="button">
                      −
                    </button>
                    <span>{item.quantity}</span>
                    <button onClick={() => void quantity(item, item.quantity + 1)} type="button">
                      +
                    </button>
                  </div>
                </article>
              );
            })}
          </section>
          <aside className="cart-summary surface">
            <h2>订单摘要</h2>
            <div className="summary-row">
              <span>商品数量</span>
              <strong>{count} 件</strong>
            </div>
            <div className="summary-row">
              <span>配送</span>
              <strong>免运费</strong>
            </div>
            <div className="summary-row">
              <span>订单总额</span>
              <strong>提交后确认</strong>
            </div>
            <button className="button primary" disabled={busy} onClick={() => void checkout()} type="button">
              {busy ? "正在提交…" : "继续结算"}
            </button>
            {error && <p className="form-error">{error}</p>}
          </aside>
        </div>
      ) : (
        <div className="empty-state">
          <h2>这里还没有你的选择。</h2>
          <p>从一件能让今天变好的物件开始。</p>
          <a className="button primary" href="/stores/1001/products">
            去选购
          </a>
        </div>
      )}
    </main>
  );
}
