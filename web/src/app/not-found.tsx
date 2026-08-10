import Link from "next/link";
export default function NotFound() {
  return (
    <main className="service-state">
      <span className="eyebrow">404 / NOT FOUND</span>
      <h1>
        这一页暂时
        <br />
        不在陈列中。
      </h1>
      <p>它可能换了位置，也可能还没有准备好与你见面。</p>
      <Link className="button primary" href="/">
        返回首页
      </Link>
    </main>
  );
}
