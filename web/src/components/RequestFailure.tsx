import type { ApiError } from "@/lib/types";
import Link from "next/link";

export function RequestFailure({
  error,
  onRetry,
  loginHref,
  title = "内容暂时无法读取",
}: {
  error: unknown;
  onRetry?: () => void;
  loginHref?: string;
  title?: string;
}) {
  const status = error instanceof Error && "status" in error ? Number((error as ApiError).status) : 500;
  const message = error instanceof Error ? error.message : "请稍后重试。";
  const heading = status === 401 ? "登录已失效" : status === 403 ? "无权访问" : status === 409 ? "状态发生变化" : status === 503 ? "服务暂时不可用" : title;

  return (
    <div className="empty-state" role="alert">
      <h2>{heading}</h2>
      <p>{message}</p>
      <div className="checkout-success__actions">
        {status === 401 && loginHref ? <Link className="button primary" href={loginHref}>重新登录</Link> : null}
        {onRetry ? <button className="button" onClick={onRetry} type="button">重新加载</button> : null}
      </div>
    </div>
  );
}
