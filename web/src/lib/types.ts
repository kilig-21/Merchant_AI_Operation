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
  /** 仅由 web 的会话路由写入；绝不根据用户 ID 推断演示身份。 */
  isDemo?: boolean;
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

export interface StoreSummary {
  id: number;
  name: string;
  englishName: string;
  tagline: string;
  description: string;
  location: string;
  categories: string[];
  productCount: number;
  heroProductId: number;
  tone: string;
  accent: string;
  badge: string;
}

/** 公开店铺目录的真实接口合同。视觉素材不属于这份业务数据。 */
export interface PublicStoreSummary {
  id: number;
  name: string;
  productCount: number;
}

export interface MarketplaceProduct extends ProductSummary {
  storeId: number;
  storeName: string;
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
  productId: number | null;
  productName: string | null;
  skuName: string | null;
  storeId: number | null;
  storeName: string | null;
  salePrice: number | null;
  availableStock: number | null;
  quantity: number;
  purchasable: boolean;
  unavailableReason: string | null;
}

/** 购物车写接口的轻量响应；完整展示信息统一由 GET /api/cart/items 读取。 */
export interface CartItemMutation {
  id: number;
  skuId: number;
  quantity: number;
}

export interface ConsumerAddress {
  id: number;
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

export type OrderStatus = "PENDING_PAYMENT" | "PAID" | "CANCELLED" | "CLOSED" | string;

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

export interface ShippingAddress {
  receiverName: string;
  receiverPhone: string;
  province: string;
  city: string;
  district: string;
  detailAddress: string;
}

export interface CheckoutOrder {
  id: number;
  checkoutGroupId: number;
  orderNo: string;
  tenantId: number;
  status: OrderStatus;
  totalAmount: number;
  expireAt: string;
  createdAt: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress | null;
}

export interface CheckoutGroup {
  checkoutGroupId: number;
  checkoutNo: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  orders: CheckoutOrder[];
}

export interface CreateCheckoutGroupResult {
  checkoutGroupId: number;
  checkoutNo: string;
  status: OrderStatus;
  totalAmount: number;
  orders: CreateOrderResult[];
}

export type AfterSaleStatus = "SUBMITTED" | "REVIEWING" | "APPROVED" | "REJECTED" | string;

/** 消费者与商家共用的售后读取模型；不含内部租户、消费者和审核人字段。 */
export interface AfterSaleRequest {
  id: number;
  requestNo: string;
  orderId: number;
  orderItemId: number;
  quantity: number;
  requestedAmount: number;
  reason: string;
  status: AfterSaleStatus;
  merchantRemark: string | null;
  decidedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

/** 消费者可申请售后的已支付订单项上下文。 */
export interface AfterSaleEligibleOrderItem {
  orderId: number;
  orderItemId: number;
  tenantId: number;
  consumerId: number;
  orderStatus: string;
  salePrice: number;
  purchasedQuantity: number;
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

/** 商家经营概览的区间汇总；金额单位为人民币元。 */
export interface MerchantDashboardMetrics {
  validOrderCount: number;
  paidRevenue: number;
  pendingPaymentCount: number;
  lowStockProductCount: number;
}

/** 商家 Dashboard 的单日真实趋势点；日期由后端按业务自然日返回。 */
export interface MerchantDashboardTrendPoint {
  date: string;
  orderCount: number;
  paidRevenue: number;
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
