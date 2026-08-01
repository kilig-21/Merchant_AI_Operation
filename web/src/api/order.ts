import http, { apiErrorMessage, type ApiResponse } from './http'

export interface CreateOrderResult {
  orderId: number
  orderNo: string
  status: OrderStatus
  totalAmount: number
  expireAt: string
}

export type OrderStatus = 'PENDING_PAYMENT' | 'PAID' | 'CLOSED' | string

export interface OrderItem {
  id: number
  skuId: number
  skuNameSnapshot: string
  salePrice: number
  quantity: number
}

export interface OrderDetail {
  id: number
  orderNo: string
  tenantId: number
  status: OrderStatus
  totalAmount: number
  expireAt: string
  createdAt: string
  items: OrderItem[]
}

export async function createOrder(cartItemIds: number[]) {
  const response = await http.post<ApiResponse<CreateOrderResult>>('/orders', { cartItemIds })
  return response.data.data
}

export async function getOrders() {
  const response = await http.get<ApiResponse<OrderDetail[]>>('/orders')
  return response.data.data
}

export async function getOrderDetail(id: number) {
  const response = await http.get<ApiResponse<OrderDetail>>(`/orders/${id}`)
  return response.data.data
}

export async function mockPayOrder(id: number) {
  const response = await http.post<ApiResponse<null>>(`/orders/${id}/mock-pay`)
  return response.data.data
}

export const orderErrorMessage = apiErrorMessage
