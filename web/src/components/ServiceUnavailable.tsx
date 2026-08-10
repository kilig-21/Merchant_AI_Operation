import Link from "next/link";
export function ServiceUnavailable() {
  return (
    <main className="service-state">
      <span className="eyebrow">SERVICE / OFFLINE</span>
      <h1>
        服务暂时
        <br />
        没有回应。
      </h1>
      <p>公共内容仍可浏览；登录、购物袋和订单需要后端服务恢复后使用。</p>
      <Link className="button primary" href="/">
        返回首页
      </Link>
    </main>
  );
}
