import axios, { type AxiosError } from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export class ApiRequestError extends Error {
  readonly status?: number

  constructor(message: string, status?: number) {
    super(message)
    this.status = status
  }
}

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as Partial<ApiResponse<unknown>>
    if (typeof body?.code === 'number' && body.code !== 0) {
      return Promise.reject(new ApiRequestError(body.message || '操作未完成，请稍后重试。', body.code))
    }
    return response
  },
  (error: AxiosError<ApiResponse<unknown>>) => {
    const status = error.response?.status
    if (status === 401) return Promise.reject(new ApiRequestError('登录已失效，请重新登录。', status))
    if (status === 403) return Promise.reject(new ApiRequestError('当前账号没有访问该内容的权限。', status))
    if (status && status >= 500) return Promise.reject(new ApiRequestError('服务暂时不可用，请稍后再试。', status))
    return Promise.reject(new ApiRequestError('网络连接不稳定，请稍后再试。', status))
  },
)

export function apiErrorMessage(error: unknown) {
  return error instanceof ApiRequestError ? error.message : '操作未完成，请稍后重试。'
}

export default http
