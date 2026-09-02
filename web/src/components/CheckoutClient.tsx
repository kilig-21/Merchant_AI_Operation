"use client";

import { createDemoOrders, type DemoCartLine, readDemoCart } from "@/lib/demo-commerce";
import { currency } from "@/lib/demo-data";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import { DemoNotice } from "./DemoNotice";
import { useSession } from "./SessionProvider";

export function CheckoutClient() {
  const [lines, setLines] = useState<DemoCartLine[]>([]);
  const [ready, setReady] = useState(false);
  const [name, setName] = useState("林默");
  const [phone, setPhone] = useState("138 0000 2026");
  const [address, setAddress] = useState("浙江省杭州市西湖区明日路 26 号");
  const [note, setNote] = useState("");
  const router = useRouter();
  const { user, loading: sessionLoading } = useSession();
  const demoSession = user?.isDemo === true;

  useEffect(() => {
    if (sessionLoading) return;
    setLines(demoSession ? readDemoCart() : []);
    setReady(true);
  }, [demoSession, sessionLoading]);

  const grouped = useMemo(() => {
    const groups = new Map<number, typeof lines>();
    for (const line of lines) groups.set(line.storeId, [...(groups.get(line.storeId) ?? []), line]);
    return [...groups.entries()];
  }, [lines]);
  const total = lines.reduce((sum, line) => sum + line.salePrice * line.quantity, 0);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!lines.length || !name.trim() || !phone.trim() || !address.trim()) return;
    const orders = createDemoOrders(lines, { name: name.trim(), phone: phone.trim(), address: address.trim() });
    router.push(`/checkout/success?ids=${orders.map((order) => order.id).join(",")}`);
  }

  if (!ready) {
    return (
      <main className="page-shell checkout-page-shell">
        <div className="empty-state" aria-live="polite">
          <span className="eyebrow">CHECKOUT / PREPARING</span>
          <h2>正在整理结算信息。</h2>
          <p>马上就好。</p>
        </div>
      </main>
    );
  }

  if (!demoSession) {
    return (
      <main className="page-shell checkout-page-shell">
        <div className="empty-state">
          <span className="eyebrow">CHECKOUT / LIVE PENDING</span>
          <h2>真实结算正在接入。</h2>
          <p>当前不会读取或创建演示订单。请先返回购物袋，真实结算会在购物车详情接口完成后接入。</p>
          <Link className="button primary" href="/cart">返回购物袋</Link>
        </div>
      </main>
    );
  }

  if (!lines.length) {
    return (
      <main className="page-shell checkout-page-shell">
        <div className="empty-state">
          <span className="eyebrow">CHECKOUT / EMPTY</span>
          <h2>购物袋已经空了。</h2>
          <p>回到市集挑选商品后，再继续结算。</p>
          <Link className="button primary" href="/stores">浏览店铺</Link>
        </div>
      </main>
    );
  }

  return (
    <main className="page-shell checkout-page-shell">
      <Link className="market-back-link" href="/cart">← 返回购物袋</Link>
      <header className="page-intro checkout-intro">
        <div><span className="eyebrow">CHECKOUT / {grouped.length} STORES</span><h1>确认结算</h1></div>
        <p>只填写一次收货信息，系统会按店铺生成 {grouped.length} 笔独立订单。</p>
      </header>
      <DemoNotice>演示结算不会扣款、锁定库存或向商家发送订单。</DemoNotice>
      <form className="checkout-layout" onSubmit={submit}>
        <div className="checkout-sections">
          <section className="checkout-section surface">
            <div className="checkout-section__head"><span className="eyebrow">01 / DELIVERY</span><h2>收货信息</h2></div>
            <div className="checkout-fields">
              <label><span>收货人</span><input value={name} onChange={(event) => setName(event.target.value)} required /></label>
              <label><span>联系电话</span><input value={phone} onChange={(event) => setPhone(event.target.value)} required /></label>
              <label className="wide"><span>详细地址</span><input value={address} onChange={(event) => setAddress(event.target.value)} required /></label>
              <label className="wide"><span>订单备注（选填）</span><textarea value={note} onChange={(event) => setNote(event.target.value)} placeholder="给店铺的补充说明" /></label>
            </div>
          </section>
          <section className="checkout-section surface">
            <div className="checkout-section__head"><span className="eyebrow">02 / SPLIT ORDERS</span><h2>按店铺确认</h2></div>
            {grouped.map(([storeId, storeLines]) => (
              <article className="checkout-store" key={storeId}>
                <header><div><span className="eyebrow">STORE {storeId}</span><h3>{storeLines[0].storeName}</h3></div><strong>{currency(storeLines.reduce((sum, line) => sum + line.salePrice * line.quantity, 0))}</strong></header>
                {storeLines.map((line) => <div className="checkout-line" key={line.id}><span>{line.productName} · {line.skuName}</span><span>× {line.quantity}</span><strong>{currency(line.salePrice * line.quantity)}</strong></div>)}
              </article>
            ))}
          </section>
        </div>
        <aside className="checkout-submit surface">
          <span className="eyebrow">PAYMENT SUMMARY</span>
          <h2>{currency(total)}</h2>
          <div><span>商品</span><strong>{lines.reduce((sum, line) => sum + line.quantity, 0)} 件</strong></div>
          <div><span>配送</span><strong>免运费</strong></div>
          <div><span>生成订单</span><strong>{grouped.length} 笔</strong></div>
          <p>提交后仍处于“待支付”，你可以分别查看每家店铺的订单。</p>
          <button className="button primary" type="submit">提交 {grouped.length} 笔演示订单</button>
        </aside>
      </form>
    </main>
  );
}
