//api/auth.ts 只负责调用后端接口

import http, { type ApiResponse } from './http'

export interface CurrentUser {
  id: number
  username: string
  userType: 'CONSUMER' | 'MERCHANT_ADMIN' | 'MERCHANT_OPERATOR'
  tenantId: number | null
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  user: CurrentUser
}

export async function login(request: LoginRequest) {
  const response = await http.post<ApiResponse<LoginResult>>('/auth/login', request)
  return response.data.data
}

export async function getCurrentUser() {
  const response = await http.get<ApiResponse<CurrentUser>>('/auth/me')
  return response.data.data
}