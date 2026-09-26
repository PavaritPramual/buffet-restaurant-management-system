import { apiClient } from '../../api/client'
import { getApiError } from '../../api/errors'
import type { OrderStatus } from '../../contracts/shared'

export interface MenuItem { id: number; categoryId: number; categoryName: string; name: string; description: string | null; available: boolean; packageIds: number[]; imageUrl: string | null }
export interface Category { id: number; name: string }
export interface BuffetPackage { id: number; name: string; price: number; description: string | null; active: boolean }
export interface SessionContext { sessionId: number; sessionToken: string; packageId: number; tableId: number; tableNumber: string; soupId: number; sessionStatus: 'ACTIVE' | 'COMPLETED' | 'CANCELLED'; adultCount: number; childCount: number; startTime: string; endTime: string | null }
export interface MenuItemInput { categoryId: number; name: string; description: string | null; available: boolean; packageIds: number[]; imageUrl: string | null }
export interface Order { orderId: number; sessionId: number; tableNumber: string; items: { menuItemId: number; name: string; quantity: number }[]; status: OrderStatus; createdAt: string }
export interface PageResult<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }

export { getApiError }
const sessionHeaders = (sessionToken: string) => ({ headers: { 'X-Session-Token': sessionToken } })
export async function getSessionByToken(token: string) { return (await apiClient.get<SessionContext>(`/dining-sessions/token/${encodeURIComponent(token)}`)).data }
export async function getMenu(sessionId: number, token: string) { return (await apiClient.get<MenuItem[]>(`/dining-sessions/${sessionId}/menu`, sessionHeaders(token))).data }
export async function getBuffetPackage(id: number) { return (await apiClient.get<BuffetPackage>(`/buffet-packages/${id}`)).data }
export async function getCategories() { return (await apiClient.get<Category[]>('/menu-categories')).data }
export async function getBuffetPackages(active = true) { return (await apiClient.get<BuffetPackage[]>('/buffet-packages', { params: { active } })).data }
export async function saveCategory(id: number | null, name: string) { return (id === null ? await apiClient.post<Category>('/menu-categories', { name }) : await apiClient.put<Category>(`/menu-categories/${id}`, { name })).data }
export async function deleteCategory(id: number) { await apiClient.delete(`/menu-categories/${id}`) }
export async function getMenuItems(page = 0, size = 10, sort = 'id,asc') { return (await apiClient.get<PageResult<MenuItem>>('/menu-items', { params: { page, size, sort } })).data }
export async function saveMenuItem(id: number | null, input: MenuItemInput) { return (id === null ? await apiClient.post<MenuItem>('/menu-items', input) : await apiClient.put<MenuItem>(`/menu-items/${id}`, input)).data }
export async function deleteMenuItem(id: number) { await apiClient.delete(`/menu-items/${id}`) }
export async function getOrders(sessionId: number, token: string) { return (await apiClient.get<Order[]>(`/dining-sessions/${sessionId}/orders`, sessionHeaders(token))).data }
export async function placeOrder(sessionId: number, token: string, items: { menuItemId: number; quantity: number }[]) { return (await apiClient.post<Order>(`/dining-sessions/${sessionId}/orders`, { items }, sessionHeaders(token))).data }
