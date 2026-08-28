import Link from "next/link";
export function SiteFooter() {
  return (
    <footer className="site-footer">
      <div>
        <span className="eyebrow">MORROW / QUIET COMMERCE</span>
        <h2>
          把更好的日常，
          <br />
          留在明天之前。
        </h2>
      </div>
      <nav>
        <Link href="/stores">浏览全部</Link>
        <Link href="/journal">选物志</Link>
        <Link href="/merchant/login">商家入口</Link>
      </nav>
      <small>© 2026 Morrow Store · Thoughtful objects for everyday life.</small>
    </footer>
  );
}
