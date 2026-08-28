import type { OrderDetail, OrderItem, Sku } from "./types";

const CART_KEY = "morrow_demo_cart_v1";
const ORDERS_KEY = "morrow_demo_orders_v1";

export interface DemoCartLine {
  id: number;
  storeId: number;
  storeName: string;
  productId: number;
  productName: string;
  skuId: number;
  skuName: string;
  salePrice: number;
  quantity: number;
}

export interface DemoOrder extends OrderDetail {
  storeName: string;
  deliveryName: string;
  deliveryPhone: string;
  deliveryAddress: string;
}

function read<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  try {
    return JSON.parse(window.localStorage.getItem(key) || "") as T;
  } catch {
    return fallback;
  }
}

function write<T>(key: string, value: T) {
  if (typeof window !== "undefined") window.localStorage.setItem(key, JSON.stringify(value));
}

export function readDemoCart() {
  return read<DemoCartLine[]>(CART_KEY, []);
}

export function addDemoCartLine(input: {
  storeId: number;
  storeName: string;
  productId: number;
  productName: string;
  sku: Sku;
  quantity: number;
}) {
  const cart = readDemoCart();
  const existing = cart.find((line) => line.storeId === input.storeId && line.skuId === input.sku.id);
  const next = existing
    ? cart.map((line) =>
        line.id === existing.id
          ? { ...line, quantity: Math.min(input.sku.availableStock, line.quantity + input.quantity) }
          : line,
      )
    : [
        ...cart,
        {
          id: Date.now(),
          storeId: input.storeId,
          storeName: input.storeName,
          productId: input.productId,
          productName: input.productName,
          skuId: input.sku.id,
          skuName: input.sku.skuName,
          salePrice: input.sku.salePrice,
          quantity: input.quantity,
        },
      ];
  write(CART_KEY, next);
  window.dispatchEvent(new Event("morrow-cart-change"));
  return next;
}

export function updateDemoCartLine(id: number, quantity: number) {
  const next = readDemoCart().map((line) => (line.id === id ? { ...line, quantity: Math.max(1, quantity) } : line));
  write(CART_KEY, next);
  return next;
}

export function removeDemoCartLine(id: number) {
  const next = readDemoCart().filter((line) => line.id !== id);
  write(CART_KEY, next);
  return next;
}

export function clearDemoCart() {
  write(CART_KEY, []);
  window.dispatchEvent(new Event("morrow-cart-change"));
}

export function readDemoOrders() {
  return read<DemoOrder[]>(ORDERS_KEY, []);
}

export function createDemoOrders(
  lines: DemoCartLine[],
  delivery: { name: string; phone: string; address: string },
) {
  const grouped = new Map<number, DemoCartLine[]>();
  for (const line of lines) grouped.set(line.storeId, [...(grouped.get(line.storeId) ?? []), line]);
  const now = new Date();
  const created = [...grouped.entries()].map(([storeId, storeLines], index): DemoOrder => {
    const id = Date.now() + index;
    const items: OrderItem[] = storeLines.map((line) => ({
      id: line.id,
      skuId: line.skuId,
      skuNameSnapshot: `${line.productName} · ${line.skuName}`,
      salePrice: line.salePrice,
      quantity: line.quantity,
    }));
    return {
      id,
      orderNo: `DEMO-${now.toISOString().slice(0, 10).replaceAll("-", "")}-${String(id).slice(-6)}`,
      tenantId: storeId,
      storeName: storeLines[0].storeName,
      status: "PENDING_PAYMENT",
      totalAmount: items.reduce((sum, item) => sum + item.salePrice * item.quantity, 0),
      expireAt: new Date(now.getTime() + 30 * 60 * 1000).toISOString(),
      createdAt: now.toISOString(),
      items,
      deliveryName: delivery.name,
      deliveryPhone: delivery.phone,
      deliveryAddress: delivery.address,
    };
  });
  write(ORDERS_KEY, [...created, ...readDemoOrders()]);
  clearDemoCart();
  return created;
}

export function updateDemoOrderStatus(id: number, status: "PAID" | "CLOSED") {
  const next = readDemoOrders().map((order) => (order.id === id ? { ...order, status } : order));
  write(ORDERS_KEY, next);
  return next.find((order) => order.id === id) ?? null;
}
