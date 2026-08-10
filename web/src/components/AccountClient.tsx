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
        <article className="account-card surface">
          <span className="eyebrow">02 / PREVIEW</span>
          <h2>收货信息</h2>
          <p>后端地址簿尚未接入，此处保留未来入口。</p>
        </article>
        <article className="account-card surface">
          <span className="eyebrow">03 / PREVIEW</span>
          <h2>喜欢清单</h2>
          <p>收藏能力尚未接入，此处仅展示页面结构。</p>
        </article>
        <article className="account-card surface">
          <span className="eyebrow">04 / ACCOUNT</span>
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
