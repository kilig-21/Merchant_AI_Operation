import { type ApiEnvelope, ApiError } from "./types";

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
      body?.message || (response.status === 401 ? "登录已失效，请重新登录。" : "操作未完成，请稍后重试。"),
      response.status,
    );
  }
  return body.data;
}
