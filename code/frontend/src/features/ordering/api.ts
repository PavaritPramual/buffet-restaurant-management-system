import axios from 'axios'
import { API_BASE_URL } from '../../api/client'
import type { OrderStatus } from '../../contracts/shared'

// The development proxy lets this feature run before the shared CORS setup lands.
const apiClient = axios.create({ baseURL: import.meta.env.DEV ? '/api/v1' : API_BASE_URL })

export interface MenuItem {
  id: number
  categoryId: number
  name: string
  available: boolean
  packageIds: number[]
  imageUrl: string | null
}

export interface Category {
  id: number
  name: string
}

export interface MenuItemInput {
  categoryId: number
  name: string
  available: boolean
  packageIds: number[]
  imageUrl: string | null
}

export function getApiError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  return 'เชื่อมต่อไม่สำเร็จ กรุณาลองอีกครั้ง'
}

export interface Order {
  orderId: number
  sessionId: number
  tableNumber: string
  items: { menuItemId: number; name: string; quantity: number }[]
  status: OrderStatus
  createdAt: string
}

export async function getMenu(sessionId: number) {
  const response = await apiClient.get<MenuItem[]>(`/dining-sessions/${sessionId}/menu`)
  return response.data
}

export async function getCategories() {
  const response = await apiClient.get<Category[]>('/menu-categories')
  return response.data
}

export async function saveCategory(id: number | null, name: string) {
  const response = id === null
    ? await apiClient.post<Category>('/menu-categories', { name })
    : await apiClient.put<Category>(`/menu-categories/${id}`, { name })
  return response.data
}

export async function deleteCategory(id: number) {
  await apiClient.delete(`/menu-categories/${id}`)
}

export async function getMenuItems(page = 0, size = 10) {
  const response = await apiClient.get<{ content: MenuItem[]; page: number; size: number; totalElements: number }>(
    '/menu-items', { params: { page, size, sort: 'id,asc' } },
  )
  return response.data
}

export async function saveMenuItem(id: number | null, input: MenuItemInput) {
  const response = id === null
    ? await apiClient.post<MenuItem>('/menu-items', input)
    : await apiClient.put<MenuItem>(`/menu-items/${id}`, input)
  return response.data
}

export async function deleteMenuItem(id: number) {
  await apiClient.delete(`/menu-items/${id}`)
}

export async function getOrders(sessionId: number) {
  const response = await apiClient.get<Order[]>(`/dining-sessions/${sessionId}/orders`)
  return response.data
}

export async function placeOrder(sessionId: number, items: { menuItemId: number; quantity: number }[]) {
  const response = await apiClient.post<Order>(`/dining-sessions/${sessionId}/orders`, { items })
  return response.data
}
