"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const nav = [
  { href: "/platform", label: "平台总览" },
  { href: "/platform/merchants", label: "商家审核" },
  { href: "/platform/orders", label: "跨店订单" },
  { href: "/platform/governance", label: "内容治理" },
];

export function PlatformShell({ title, eyebrow, children }: { title: string; eyebrow: string; children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <main className="platform-shell">
      <aside className="platform-rail">
        <Link className="platform-logo" href="/platform">MORROW<span>PLATFORM</span></Link>
        <nav>{nav.map((item) => <Link aria-current={pathname === item.href ? "page" : undefined} className={pathname === item.href ? "active" : ""} href={item.href} key={item.href}>{item.label}<span>↗</span></Link>)}</nav>
        <div><span className="eyebrow">DEMO CONSOLE</span><p>平台接口与权限体系待接入</p><Link href="/">返回商城</Link></div>
      </aside>
      <section className="platform-main">
        <header className="platform-head"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1></div><span className="platform-mode">READ-ONLY DEMO</span></header>
        {children}
      </section>
    </main>
  );
}
