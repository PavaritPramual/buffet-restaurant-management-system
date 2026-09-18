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
}

export interface Category {
  id: number
  name: string
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

export async function getOrders(sessionId: number) {
  const response = await apiClient.get<Order[]>(`/dining-sessions/${sessionId}/orders`)
  return response.data
}

export async function placeOrder(sessionId: number, items: { menuItemId: number; quantity: number }[]) {
  const response = await apiClient.post<Order>(`/dining-sessions/${sessionId}/orders`, { items })
  return response.data
}
