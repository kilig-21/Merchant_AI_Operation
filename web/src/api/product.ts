import http, { type ApiResponse } from './http'

export interface MerchantProductItem {
  id: number
  name: string
  description: string | null
  status: 'DRAFT' | 'ON_SALE' | 'OFF_SALE'
  skuCount: number
  minSalePrice: number | null
  totalAvailableStock: number
  updatedAt: string
}

export interface MerchantProductListParams {
  page: number
  size: number
  keyword?: string
}

export async function getMerchantProducts(params: MerchantProductListParams) {
  const response = await http.get<ApiResponse<MerchantProductItem[]>>('/merchant/products', {
    params,
  })

  return response.data.data
}

export interface CreateMerchantProductRequest { name: string; description?: string }
export interface CreateMerchantSkuRequest { skuName: string; salePrice: number; availableStock: number }

export async function createMerchantProduct(request: CreateMerchantProductRequest) {
  const response = await http.post<ApiResponse<{ id: number }>>('/merchant/products', request)
  return response.data.data
}

export async function createMerchantSku(spuId: number, request: CreateMerchantSkuRequest) {
  const response = await http.post<ApiResponse<{ id: number }>>(`/merchant/products/${spuId}/skus`, request)
  return response.data.data
}

export async function publishMerchantProduct(spuId: number) {
  await http.post(`/merchant/products/${spuId}/publish`)
}

export async function unpublishMerchantProduct(spuId: number) {
  await http.post(`/merchant/products/${spuId}/unpublish`)
}

export async function updateMerchantSkuPrice(skuId: number, salePrice: number) {
  await http.put(`/merchant/products/skus/${skuId}/price`, { salePrice })
}

export interface PublicProductItem { id: number; name: string; description: string | null; minSalePrice: number | null; totalAvailableStock: number; updatedAt: string }
export interface PublicSku { id: number; skuName: string; salePrice: number; availableStock: number }
export interface PublicProductDetail { id: number; name: string; description: string | null; updatedAt: string; skus: PublicSku[] }

export async function getStoreProducts(storeId: number, page = 1, size = 24) {
  const response = await http.get<ApiResponse<PublicProductItem[]>>(`/public/stores/${storeId}/products`, { params: { page, size } })
  return response.data.data
}

export async function getProductDetail(storeId: number, spuId: number) {
  const response = await http.get<ApiResponse<PublicProductDetail>>(`/public/stores/${storeId}/products/${spuId}`)
  return response.data.data
}

export async function getSkuAvailability(skuId: number) {
  const response = await http.get<ApiResponse<{ skuId: number; purchasable: boolean; availableStock: number; message: string }>>(`/public/skus/${skuId}/availability`)
  return response.data.data
}
