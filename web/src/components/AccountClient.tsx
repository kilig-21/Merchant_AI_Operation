"use client";
import Link from "next/link";
import { useSession } from "./SessionProvider";
export function AccountClient() {
  const { user, signOut } = useSession();
  return (
    <main className="page-shell">
      <header className="page-intro">
        <div>
          <span className="eyebrow">MEMBER / {user?.id || "—"}</span>
          <h1>
            你好，
            <br />
            {user?.username || "Morrow member"}。
          </h1>
        </div>
        <p>订单、账户和售后入口，都收在这个安静的角落。</p>
      </header>
      <section className="account-grid">
        <Link className="account-card surface accent" href="/orders">
          <span className="eyebrow">01 / REAL SERVICE</span>
          <h2>我的订单</h2>
          <p>查看状态、支付或取消待支付订单。</p>
        </Link>
        <Link className="account-card surface" href="/account/addresses">
          <span className="eyebrow">02 / DELIVERY</span>
          <h2>收货信息</h2>
          <p>管理常用地址与默认收货信息。</p>
        </Link>
        <Link className="account-card surface" href="/account/favorites">
          <span className="eyebrow">03 / SAVED</span>
          <h2>喜欢清单</h2>
          <p>跨店铺保留想晚一点再决定的物件。</p>
        </Link>
        <Link className="account-card surface" href="/after-sales">
          <span className="eyebrow">04 / SERVICE</span>
          <h2>售后服务</h2>
          <p>发起退货或退款，并查看处理进度。</p>
        </Link>
        <article className="account-card surface">
          <span className="eyebrow">05 / ACCOUNT</span>
          <h2>安全与退出</h2>
          <p>{user?.userType}</p>
          <button className="button" type="button" onClick={() => void signOut()}>
            退出登录
          </button>
        </article>
      </section>
    </main>
  );
}
