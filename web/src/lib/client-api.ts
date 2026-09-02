import { type ApiEnvelope, ApiError } from "./types";

function messageForStatus(status: number) {
  if (status === 401) return "登录已失效，请重新登录。";
  if (status === 403) return "你没有访问此内容或执行此操作的权限。";
  if (status === 409) return "当前操作与最新业务状态冲突，请刷新后重试。";
  if (status === 503) return "服务暂时无法连接，请稍后重试。";
  return "操作未完成，请稍后重试。";
}

export async function apiClient<T>(path: string, init: RequestInit = {}) {
  let response: Response;
  try {
    response = await fetch(path, {
      ...init,
      headers: { "Content-Type": "application/json", ...init.headers },
    });
  } catch {
    throw new ApiError("网络连接不稳定，请稍后再试。", 503);
  }
  const body = (await response.json().catch(() => null)) as ApiEnvelope<T> | null;
  if (!response.ok || !body || body.code !== 0) {
    throw new ApiError(
      body?.message || messageForStatus(response.status),
      response.status,
    );
  }
  return body.data;
}
