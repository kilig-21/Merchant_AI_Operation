import Link from "next/link";

const nav = [
  { href: "/platform", label: "平台总览" },
  { href: "/platform/merchants", label: "商家审核" },
  { href: "/platform/orders", label: "跨店订单" },
  { href: "/platform/governance", label: "内容治理" },
];

export function PlatformShell({ title, eyebrow, children }: { title: string; eyebrow: string; children: React.ReactNode }) {
  return (
    <main className="platform-shell">
      <aside className="platform-rail">
        <Link className="platform-logo" href="/platform">MORROW<span>PLATFORM</span></Link>
        <nav>{nav.map((item) => <Link href={item.href} key={item.href}>{item.label}<span>↗</span></Link>)}</nav>
        <div><span className="eyebrow">DEMO CONSOLE</span><p>平台接口与权限体系待接入</p><Link href="/">返回商城</Link></div>
      </aside>
      <section className="platform-main">
        <header className="platform-head"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1></div><span className="platform-mode">READ-ONLY DEMO</span></header>
        {children}
      </section>
    </main>
  );
}
