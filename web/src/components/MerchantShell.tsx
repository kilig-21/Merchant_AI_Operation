"use client";
import { ThemeSwitcher } from "@once-ui-system/core";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "./SessionProvider";
const nav = [
  { href: "/merchant/dashboard", label: "经营概览" },
  { href: "/merchant/products", label: "商品管理" },
  { href: "/merchant/orders", label: "订单管理" },
  { href: "/merchant/marketing", label: "营销活动" },
  { href: "/merchant/customers", label: "顾客洞察" },
  { href: "/merchant/settings", label: "店铺设置" },
];
export function MerchantShell({
  title,
  eyebrow = "MORROW / OPERATIONS",
  actions,
  children,
}: { title: string; eyebrow?: string; actions?: React.ReactNode; children: React.ReactNode }) {
  const path = usePathname();
  const { user, signOut } = useSession();
  return (
    <main className="merchant-shell">
      <aside className="merchant-sidebar">
        <Link className="merchant-logo" href="/merchant/dashboard">
          MORROW<span>OS</span>
        </Link>
        <nav>
          {nav.map((item) => (
            <Link className={path.startsWith(item.href) ? "active" : ""} href={item.href} key={item.href}>
              {item.label}
              <span>↗</span>
            </Link>
          ))}
        </nav>
        <div className="merchant-account">
          <span>{user?.username || "merchant"}</span>
          <small>{user?.tenantId ? `Tenant ${user.tenantId}` : "商家账户"}</small>
          <ThemeSwitcher />
          <button type="button" onClick={() => void signOut()}>
            退出登录
          </button>
        </div>
      </aside>
      <section className="merchant-main">
        <header className="merchant-head">
          <div>
            <span className="eyebrow">{eyebrow}</span>
            <h1>{title}</h1>
          </div>
          {actions}
        </header>
        {children}
      </section>
    </main>
  );
}
