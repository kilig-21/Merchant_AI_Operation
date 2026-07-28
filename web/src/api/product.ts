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