import type { MerchantProduct, ProductDetail, ProductSummary } from "./types";

export const productVisuals = [
  {
    id: 1,
    category: "听觉",
    tagline: "安静，也可以很有力量。",
    image: "/media/product-1.svg",
    tone: "#dfe7ed",
  },
  {
    id: 2,
    category: "听觉",
    tagline: "声音落在刚刚好的地方。",
    image: "/media/product-2.svg",
    tone: "#e9e2d8",
  },
  {
    id: 3,
    category: "日常",
    tagline: "温热的日常，轻轻握住。",
    image: "/media/product-3.svg",
    tone: "#e8e5df",
  },
  {
    id: 4,
    category: "空间",
    tagline: "给房间一点呼吸。",
    image: "/media/product-4.svg",
    tone: "#e9dfc9",
  },
  {
    id: 5,
    category: "出行",
    tagline: "出发时，少带一点负担。",
    image: "/media/product-5.svg",
    tone: "#e2e7e7",
  },
  {
    id: 6,
    category: "空间",
    tagline: "像刚晒过的被子。",
    image: "/media/product-6.svg",
    tone: "#eee5dd",
  },
] as const;

export const heroPoster = "/media/hero-poster.svg";

export const demoProducts: ProductSummary[] = [
  {
    id: 1,
    name: "澄澈降噪耳机",
    description: "轻盈贴耳，把需要专注的时刻留给自己。",
    minSalePrice: 699,
    totalAvailableStock: 32,
    updatedAt: "",
  },
  {
    id: 2,
    name: "回声桌面音箱",
    description: "为一张桌子准备的饱满声场。",
    minSalePrice: 289,
    totalAvailableStock: 126,
    updatedAt: "",
  },
  {
    id: 3,
    name: "晨雾保温杯",
    description: "从清晨到傍晚，陪你把温度带在身边。",
    minSalePrice: 99,
    totalAvailableStock: 80,
    updatedAt: "",
  },
  {
    id: 4,
    name: "微光氛围灯",
    description: "把夜晚调成适合慢下来的亮度。",
    minSalePrice: 159,
    totalAvailableStock: 21,
    updatedAt: "",
  },
  {
    id: 5,
    name: "轻旅充电器",
    description: "小巧收纳，为每一次出发留出余量。",
    minSalePrice: 199,
    totalAvailableStock: 46,
    updatedAt: "",
  },
  {
    id: 6,
    name: "星期日香氛",
    description: "把一段不被打扰的午后留在空气里。",
    minSalePrice: 129,
    totalAvailableStock: 36,
    updatedAt: "",
  },
];

export function visualFor(id: number) {
  return productVisuals.find((item) => item.id === id) ?? productVisuals[0];
}

export function demoDetail(id: number): ProductDetail {
  const product = demoProducts.find((item) => item.id === id) ?? demoProducts[0];
  return {
    id: product.id,
    name: product.name,
    description: product.description,
    updatedAt: product.updatedAt,
    skus: [
      {
        id: product.id * 100 + 1,
        skuName: "浅色款",
        salePrice: product.minSalePrice ?? 0,
        availableStock: product.totalAvailableStock,
      },
      {
        id: product.id * 100 + 2,
        skuName: "深色款",
        salePrice: product.minSalePrice ?? 0,
        availableStock: Math.max(0, product.totalAvailableStock - 8),
      },
    ],
  };
}

export const demoMerchantProducts: MerchantProduct[] = demoProducts.slice(0, 4).map((item, index) => ({
  ...item,
  description: item.description,
  status: index < 2 ? "ON_SALE" : index === 2 ? "DRAFT" : "OFF_SALE",
  skuCount: 2,
}));

export const currency = (value: number | null | undefined) =>
  value == null ? "—" : `¥${value.toLocaleString("zh-CN")}`;
