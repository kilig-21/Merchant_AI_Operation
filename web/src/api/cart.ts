import http, { type ApiResponse } from './http'

export interface CartItem { id: number; skuId: number; quantity: number }
export async function getCartItems() { return (await http.get<ApiResponse<CartItem[]>>('/cart/items')).data.data }
export async function addCartItem(skuId: number, quantity: number) { return (await http.post<ApiResponse<CartItem>>('/cart/items', { skuId, quantity })).data.data }
export async function updateCartQuantity(id: number, quantity: number) { return (await http.put<ApiResponse<CartItem>>(`/cart/items/${id}`, { quantity })).data.data }
export async function deleteCartItem(id: number) { await http.delete(`/cart/items/${id}`) }
