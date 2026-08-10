import Link from "next/link";
export default function ForbiddenPage() {
  return (
    <main className="service-state">
      <span className="eyebrow">403 / FORBIDDEN</span>
      <h1>
        这不是当前
        <br />
        账户的入口。
      </h1>
      <p>消费者与商家工作台使用不同权限，请切换到对应账户。</p>
      <Link className="button primary" href="/">
        返回首页
      </Link>
    </main>
  );
}
