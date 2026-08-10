export interface ApiEnvelope<T> {
  code: number;
  message: string;
  data: T;
}

export type UserRole = "CONSUMER" | "MERCHANT_ADMIN" | "MERCHANT_OPERATOR";

export interface SessionUser {
  id: number;
  username: string;
  userType: UserRole;
  tenantId: number | null;
}

export interface LoginResult {
  accessToken: string;
  user: SessionUser;
}

export interface ProductSummary {
  id: number;
  name: string;
  description: string | null;
  minSalePrice: number | null;
  totalAvailableStock: number;
  updatedAt: string;
}

export interface Sku {
  id: number;
  skuName: string;
  salePrice: number;
  availableStock: number;
}

export interface ProductDetail {
  id: number;
  name: string;
  description: string | null;
  updatedAt: string;
  skus: Sku[];
}

export interface CartItem {
  id: number;
  skuId: number;
  quantity: number;
}

export type OrderStatus = "PENDING_PAYMENT" | "PAID" | "CLOSED" | string;

export interface OrderItem {
  id: number;
  skuId: number;
  skuNameSnapshot: string;
  salePrice: number;
  quantity: number;
}

export interface OrderDetail {
  id: number;
  orderNo: string;
  tenantId: number;
  status: OrderStatus;
  totalAmount: number;
  expireAt: string;
  createdAt: string;
  items: OrderItem[];
}

export interface CreateOrderResult {
  orderId: number;
  orderNo: string;
  status: OrderStatus;
  totalAmount: number;
  expireAt: string;
}

export interface MerchantProduct {
  id: number;
  name: string;
  description: string | null;
  status: "DRAFT" | "ON_SALE" | "OFF_SALE";
  skuCount: number;
  minSalePrice: number | null;
  totalAvailableStock: number;
  updatedAt: string;
}

export interface JournalMetadata {
  slug: string;
  title: string;
  summary: string;
  publishedAt: string;
  cover: string;
  relatedProductIds: number[];
  sponsored: boolean;
  sponsorName?: string;
  ctaLabel?: string;
  ctaHref?: string;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status = 500,
  ) {
    super(message);
  }
}
