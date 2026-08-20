import Link from "next/link";

export default async function CheckoutSuccessPage({ searchParams }: { searchParams: Promise<{ ids?: string }> }) {
  const ids = ((await searchParams).ids ?? "").split(",").filter(Boolean);
  return (
    <main className="page-shell checkout-success">
      <span className="eyebrow">ORDER SPLIT / COMPLETE</span>
      <div className="checkout-success__mark">✓</div>
      <h1>订单已经按店铺<br />分别准备好了。</h1>
      <p>本次共生成 {ids.length || 0} 笔演示订单。每家店铺拥有独立订单号、金额和后续状态。</p>
      <div className="checkout-success__actions">
        <Link className="button primary" href="/orders">查看全部订单</Link>
        {ids[0] ? <Link className="button" href={`/orders/${ids[0]}`}>查看第一笔订单</Link> : null}
      </div>
    </main>
  );
}
