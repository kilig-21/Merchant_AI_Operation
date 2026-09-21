import {
  type ApiEnvelope,
  ApiError,
  type MarketplaceProduct,
  type ProductDetail,
  type ProductSummary,
  type PublicStoreSummary,
  type SessionUser,
} from "./types";

export const backendOrigin = () => (process.env.BACKEND_ORIGIN || "http://localhost:8080").replace(/\/$/, "");

export async function backendRequest<T>(path: string, init: RequestInit = {}, token?: string) {
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let response: Response;
  try {
    response = await fetch(`${backendOrigin()}${path}`, { ...init, headers, cache: "no-store" });
  } catch {
    throw new ApiError("服务暂时无法连接。", 503);
  }

  const body = (await response.json().catch(() => null)) as ApiEnvelope<T> | null;
  if (!response.ok || !body || body.code !== 0) {
    throw new ApiError(
      body?.message || (response.status === 401 ? "登录已失效。" : "操作未完成。"),
      response.status,
    );
  }
  return body.data;
}

export const getPublicProducts = (storeId: number) =>
  backendRequest<ProductSummary[]>(`/api/public/stores/${storeId}/products?page=1&size=48`);

export const getPublicStores = () => backendRequest<PublicStoreSummary[]>("/api/public/stores");

export const searchPublicProducts = (keyword = "", storeId?: number) => {
  const params = new URLSearchParams({ keyword, page: "1", size: "48" });
  if (storeId) params.set("storeId", String(storeId));
  return backendRequest<MarketplaceProduct[]>(`/api/public/stores/products/search?${params.toString()}`);
};

export const getPublicProduct = (storeId: number, productId: number) =>
  backendRequest<ProductDetail>(`/api/public/stores/${storeId}/products/${productId}`);

export const getCurrentUserWithToken = (token: string) =>
  backendRequest<SessionUser>("/api/auth/me", {}, token);
