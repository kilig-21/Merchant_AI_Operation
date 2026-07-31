import type { PublicProductDetail, PublicProductItem, PublicSku } from '../api/product'

export interface ConsumerProduct extends PublicProductItem {
  category: string
  tagline: string
  image: string
  detailImage: string
  imageAlt: string
  tone: string
  isNew?: boolean
}

const imageUrl = (id: string, width = 1600) =>
  `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${width}&q=88`

export const demoProducts: ConsumerProduct[] = [
  {
    id: 1,
    name: '澄澈降噪耳机',
    description: '轻盈贴耳，把需要专注的时刻留给自己。',
    minSalePrice: 699,
    totalAvailableStock: 32,
    updatedAt: '',
    category: '听觉',
    tagline: '安静，也可以很有力量。',
    image: imageUrl('photo-1505740420928-5e560c06d30e'),
    detailImage: imageUrl('photo-1505740420928-5e560c06d30e', 2200),
    imageAlt: '一副耳机',
    tone: '#dfe7ed',
    isNew: true,
  },
  {
    id: 2,
    name: '回声桌面音箱',
    description: '为一张桌子准备的饱满声场。',
    minSalePrice: 289,
    totalAvailableStock: 126,
    updatedAt: '',
    category: '听觉',
    tagline: '刚刚好的声音，落在刚刚好的地方。',
    image: imageUrl('photo-1520175480921-4edfa2983e0f'),
    detailImage: imageUrl('photo-1520175480921-4edfa2983e0f', 2200),
    imageAlt: '桌面音响设备',
    tone: '#e9e2d8',
  },
  {
    id: 3,
    name: '晨雾保温杯',
    description: '从清晨到傍晚，陪你把温度带在身边。',
    minSalePrice: 99,
    totalAvailableStock: 80,
    updatedAt: '',
    category: '日常',
    tagline: '温热的日常，轻轻握住。',
    image: imageUrl('photo-1544145945-f90425340c7e'),
    detailImage: imageUrl('photo-1544145945-f90425340c7e', 2200),
    imageAlt: '一只简约水杯',
    tone: '#e8e5df',
  },
  {
    id: 4,
    name: '微光氛围灯',
    description: '把夜晚调成适合慢下来的亮度。',
    minSalePrice: 159,
    totalAvailableStock: 21,
    updatedAt: '',
    category: '空间',
    tagline: '一盏灯，给房间一点呼吸。',
    image: imageUrl('photo-1507473885765-e6ed057f782c'),
    detailImage: imageUrl('photo-1507473885765-e6ed057f782c', 2200),
    imageAlt: '柔和光线的台灯',
    tone: '#e9dfc9',
  },
  {
    id: 5,
    name: '轻旅充电器',
    description: '小巧收纳，为每一次出发留出余量。',
    minSalePrice: 199,
    totalAvailableStock: 46,
    updatedAt: '',
    category: '出行',
    tagline: '出发时，少带一点负担。',
    image: imageUrl('photo-1512428559087-560fa5ceab42'),
    detailImage: imageUrl('photo-1512428559087-560fa5ceab42', 2200),
    imageAlt: '充电设备',
    tone: '#e2e7e7',
    isNew: true,
  },
  {
    id: 6,
    name: '星期日香氛',
    description: '把一段不被打扰的午后，留在空气里。',
    minSalePrice: 129,
    totalAvailableStock: 36,
    updatedAt: '',
    category: '空间',
    tagline: '闻起来像刚晒过的被子。',
    image: imageUrl('photo-1603006905003-be475563bc59'),
    detailImage: imageUrl('photo-1603006905003-be475563bc59', 2200),
    imageAlt: '香氛蜡烛',
    tone: '#eee5dd',
  },
]

export const heroMedia = imageUrl('photo-1494438639946-1ebd1d20bf85', 2400)
export const journalMedia = imageUrl('photo-1497215842964-222b430dc094', 1800)

export function enrichProducts(products: PublicProductItem[]): ConsumerProduct[] {
  return products.map((product, index) => {
    const visual = demoProducts.find((item) => item.id === product.id) ?? demoProducts[index % demoProducts.length]
    return { ...visual, ...product, image: visual.image, detailImage: visual.detailImage }
  })
}

export function findDemoProduct(id: number) {
  return demoProducts.find((item) => item.id === id) ?? demoProducts[0]
}

export function findDemoProductBySku(skuId: number) {
  return findDemoProduct(Math.floor(skuId / 100))
}

export function makeDemoDetail(id: number): PublicProductDetail {
  const item = findDemoProduct(id)
  const skus: PublicSku[] = [
    { id: item.id * 100 + 1, skuName: '浅色款', salePrice: item.minSalePrice ?? 0, availableStock: item.totalAvailableStock },
    { id: item.id * 100 + 2, skuName: '深色款', salePrice: item.minSalePrice ?? 0, availableStock: Math.max(0, item.totalAvailableStock - 8) },
  ]
  return { id: item.id, name: item.name, description: item.description, updatedAt: item.updatedAt, skus }
}

export function currency(value: number | null | undefined) {
  return value == null ? '—' : `¥${value.toLocaleString('zh-CN')}`
}
