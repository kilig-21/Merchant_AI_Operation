export type DemoAddress = {
  id: number;
  name: string;
  phone: string;
  city: string;
  detail: string;
  isDefault: boolean;
};

export type DemoFavorite = {
  id: number;
  storeId: number;
  storeName: string;
  productId: number;
  productName: string;
  note: string;
};

export type DemoAfterSale = {
  id: number;
  orderId: number;
  orderNo: string;
  storeName: string;
  type: "RETURN" | "REFUND";
  reason: string;
  description: string;
  status: "SUBMITTED" | "REVIEWING" | "COMPLETED";
  createdAt: string;
};

const addressKey = "morrow_demo_addresses_v1";
const favoriteKey = "morrow_demo_favorites_v1";
const afterSaleKey = "morrow_demo_after_sales_v1";

const seedAddresses: DemoAddress[] = [
  { id: 1, name: "林默", phone: "138 0000 2026", city: "浙江省 杭州市", detail: "西湖区 Morrow 路 18 号", isDefault: true },
];

const seedFavorites: DemoFavorite[] = [
  { id: 1, storeId: 1001, storeName: "Morrow 日常选物", productId: 1, productName: "澄澈降噪耳机", note: "留给需要专注的下午" },
  { id: 2, storeId: 1003, storeName: "小满器物", productId: 4, productName: "纸月台灯", note: "适合床边的一小块光" },
  { id: 3, storeId: 1004, storeName: "轻行研究所", productId: 5, productName: "轻行折叠充电器", note: "下一次短途出发" },
];

function read<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  const raw = localStorage.getItem(key);
  if (!raw) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

function write<T>(key: string, value: T): T {
  if (typeof window !== "undefined") localStorage.setItem(key, JSON.stringify(value));
  return value;
}

export const readDemoAddresses = () => read<DemoAddress[]>(addressKey, seedAddresses);
export const saveDemoAddresses = (addresses: DemoAddress[]) => write(addressKey, addresses);
export const readDemoFavorites = () => read<DemoFavorite[]>(favoriteKey, seedFavorites);
export const saveDemoFavorites = (favorites: DemoFavorite[]) => write(favoriteKey, favorites);
export const readDemoAfterSales = () => read<DemoAfterSale[]>(afterSaleKey, []);

export function createDemoAfterSale(input: Omit<DemoAfterSale, "id" | "status" | "createdAt">) {
  const item: DemoAfterSale = {
    ...input,
    id: Date.now(),
    status: "SUBMITTED",
    createdAt: new Date().toISOString(),
  };
  write(afterSaleKey, [item, ...readDemoAfterSales()]);
  return item;
}
