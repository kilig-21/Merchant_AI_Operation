"use client";

import { ThemeSwitcher } from "@once-ui-system/core";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSession } from "./SessionProvider";
import GlassSurface from "./ui/GlassSurface";

export type SiteNavStoryMode = "default" | "hero" | "content";

const items = [
  { href: "/", label: "首页", short: "首页" },
  { href: "/stores", label: "店铺", short: "店铺" },
  { href: "/search", label: "搜索", short: "搜索" },
  { href: "/journal", label: "选物志", short: "选物" },
  { href: "/orders", label: "订单", short: "订单" },
];

const homeItems = [
  { href: "/", label: "首页", short: "首页" },
  { href: "/stores", label: "选购", short: "选购" },
  { href: "/journal", label: "选物志", short: "选物" },
  { href: "/orders", label: "订单", short: "订单" },
];

export function SiteNav({ storyMode = "default" }: { storyMode?: SiteNavStoryMode }) {
  const pathname = usePathname();
  const { user } = useSession();
  const navItems = pathname === "/" ? homeItems : items;
  return (
    <>
      <header className="site-header">
        <Link className="wordmark" href="/">
          MORROW<sup>©26</sup>
        </Link>
        <GlassSurface
          className={`nav-glass nav-glass--${storyMode}`}
          height={58}
          borderRadius={22}
          distortionScale={-8}
          redOffset={0}
          greenOffset={1}
          blueOffset={2}
        >
          <nav className="nav-capsule" aria-label="消费者主导航">
            {navItems.map((item) => (
              <Link
                key={item.href}
                className={
                  pathname === item.href || (item.href !== "/" && pathname.startsWith(item.href))
                    ? "active"
                    : ""
                }
                href={item.href}
              >
                {item.label}
              </Link>
            ))}
            <span className="nav-divider" />
            <ThemeSwitcher />
          </nav>
        </GlassSurface>
        <div className="nav-actions">
          <Link href={user ? "/account" : "/consumer/login"}>{user ? user.username : "登录"}</Link>
          <Link className="bag-action" href="/cart">
            购物袋 <span>↗</span>
          </Link>
        </div>
      </header>
      <nav className="mobile-dock" aria-label="移动端主导航">
        {navItems.slice(0, 3).map((item, index) => (
          <Link
            key={item.href}
            className={
              pathname === item.href || (item.href !== "/" && pathname.startsWith(item.href)) ? "active" : ""
            }
            href={item.href}
          >
            <span>{["⌂", "◇", pathname === "/" ? "≡" : "⌕"][index]}</span>
            {item.short}
          </Link>
        ))}
        <Link
          className={pathname.startsWith("/cart") || pathname.startsWith("/account") ? "active" : ""}
          href="/cart"
        >
          <span>○</span>购物袋
        </Link>
      </nav>
    </>
  );
}
