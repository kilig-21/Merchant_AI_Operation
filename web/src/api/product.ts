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

export interface PublicProductItem { id: number; name: string; description: string | null; minSalePrice: number | null; totalAvailableStock: number; updatedAt: string }
export interface PublicSku { id: number; skuName: string; salePrice: number; availableStock: number }
export interface PublicProductDetail { id: number; name: string; description: string | null; updatedAt: string; skus: PublicSku[] }

export async function getStoreProducts(storeId: number) {
  const response = await http.get<ApiResponse<PublicProductItem[]>>(`/public/stores/${storeId}/products`, { params: { page: 1, size: 24 } })
  return response.data.data
}

export async function getProductDetail(spuId: number) {
  const response = await http.get<ApiResponse<PublicProductDetail>>(`/public/products/${spuId}`)
  return response.data.data
}
