import type {
  MarketplaceProduct,
  MerchantProduct,
  ProductDetail,
  ProductSummary,
  StoreSummary,
} from "./types";

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
  return productVisuals.find((item) => item.id === id) ?? productVisuals[(Math.abs(id) - 1) % productVisuals.length];
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

export const demoStores: StoreSummary[] = [
  {
    id: 1001,
    name: "Morrow 日常选物",
    englishName: "MORROW OBJECTS",
    tagline: "把喜欢的日常，留在明天之前。",
    description: "从声音、光线与手边器物开始，为每一个普通日子留下更从容的选择。",
    location: "杭州",
    categories: ["听觉", "日常", "空间", "出行"],
    productCount: 6,
    heroProductId: 1,
    tone: "#dfe7ed",
    accent: "#315f14",
    badge: "编辑精选",
  },
  {
    id: 1002,
    name: "留白声场",
    englishName: "PAUSE AUDIO",
    tagline: "把声音放回刚刚好的位置。",
    description: "为通勤、工作与独处挑选克制耐用的听觉设备，不追逐多余的响亮。",
    location: "上海",
    categories: ["听觉", "桌面", "通勤"],
    productCount: 4,
    heroProductId: 2,
    tone: "#e9e2d8",
    accent: "#69563d",
    badge: "声音专门店",
  },
  {
    id: 1003,
    name: "小满器物",
    englishName: "GENTLE FORMS",
    tagline: "不必太满，刚好够生活使用。",
    description: "围绕饮水、照明和气味收集朴素器物，让房间多一点温度与呼吸。",
    location: "景德镇",
    categories: ["日常", "空间", "香氛"],
    productCount: 4,
    heroProductId: 3,
    tone: "#e8e5df",
    accent: "#766342",
    badge: "器物商店",
  },
  {
    id: 1004,
    name: "轻行研究所",
    englishName: "LIGHT MILES",
    tagline: "少带一点，也能走得更远。",
    description: "为日常通勤与短途出发整理可靠装备，减少负担，也减少临时决定。",
    location: "深圳",
    categories: ["出行", "充电", "收纳"],
    productCount: 4,
    heroProductId: 5,
    tone: "#e2e7e7",
    accent: "#365f5d",
    badge: "出行好物",
  },
];

const demoStoreProducts: Record<number, ProductSummary[]> = {
  1001: demoProducts,
  1002: [
    { ...demoProducts[0], name: "云层头戴耳机", description: "长时间佩戴也保持轻盈的安静声场。", minSalePrice: 799 },
    { ...demoProducts[1], name: "留白桌面音箱", description: "适合小空间的近场声音，饱满但不喧闹。", minSalePrice: 359 },
    { ...demoProducts[4], name: "口袋音频转接器", description: "为通勤设备保留稳定连接。", minSalePrice: 129 },
    { ...demoProducts[5], name: "夜航白噪音机", description: "把睡前环境调到更柔和的频率。", minSalePrice: 229 },
  ],
  1003: [
    { ...demoProducts[2], name: "小满随行杯", description: "不烫手的柔和轮廓，陪伴从清晨到傍晚。", minSalePrice: 118 },
    { ...demoProducts[3], name: "纸月台灯", description: "让阅读与晚归拥有一小块温暖亮度。", minSalePrice: 249 },
    { ...demoProducts[5], name: "雨后木质香", description: "干净木香与很轻的潮湿空气。", minSalePrice: 169 },
    { ...demoProducts[1], name: "掌心计时器", description: "不用手机，也能给专注留一段完整时间。", minSalePrice: 89 },
  ],
  1004: [
    { ...demoProducts[4], name: "轻行折叠充电器", description: "一枚掌心大小的可靠电源，收好就出发。", minSalePrice: 219 },
    { ...demoProducts[2], name: "周末保温瓶", description: "为短途步行准备的轻量补水。", minSalePrice: 139 },
    { ...demoProducts[0], name: "通勤降噪耳塞", description: "把车厢的嘈杂留在耳朵之外。", minSalePrice: 499 },
    { ...demoProducts[5], name: "衣物整理喷雾", description: "抵达之后，让衣物快速恢复清新。", minSalePrice: 79 },
  ],
};

export function storeFor(storeId: number) {
  return demoStores.find((store) => store.id === storeId) ?? demoStores[0];
}

export function demoProductsForStore(storeId: number) {
  return demoStoreProducts[storeId] ?? demoStoreProducts[1001];
}

export function demoDetailForStore(storeId: number, productId: number): ProductDetail {
  const product =
    demoProductsForStore(storeId).find((item) => item.id === productId) ?? demoProductsForStore(storeId)[0];
  return {
    id: product.id,
    name: product.name,
    description: product.description,
    updatedAt: product.updatedAt,
    skus: [
      {
        id: storeId * 1000 + product.id * 10 + 1,
        skuName: "浅色款",
        salePrice: product.minSalePrice ?? 0,
        availableStock: product.totalAvailableStock,
      },
      {
        id: storeId * 1000 + product.id * 10 + 2,
        skuName: "深色款",
        salePrice: product.minSalePrice ?? 0,
        availableStock: Math.max(0, product.totalAvailableStock - 8),
      },
    ],
  };
}

export const demoMarketplaceProducts: MarketplaceProduct[] = demoStores.flatMap((store) =>
  demoProductsForStore(store.id).map((product) => ({
    ...product,
    storeId: store.id,
    storeName: store.name,
  })),
);

export const currency = (value: number | null | undefined) =>
  value == null ? "—" : `¥${value.toLocaleString("zh-CN")}`;
