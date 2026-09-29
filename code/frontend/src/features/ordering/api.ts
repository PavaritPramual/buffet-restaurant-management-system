import { apiClient, customerApiClient } from '../../api/client'
import { getApiError } from '../../api/errors'
import type { OrderStatus } from '../../contracts/shared'

export interface MenuItem { id: number; categoryId: number; categoryName: string; name: string; description: string | null; available: boolean; packageIds: number[]; imageUrl: string | null }
export interface Category { id: number; name: string }
export interface BuffetPackage { id: number; name: string; price: number; description: string | null; active: boolean }
export interface SessionContext { sessionId: number; packageId: number; tableNumber: string; sessionStatus: 'ACTIVE' | 'COMPLETED' | 'CANCELLED' }
export interface MenuItemInput { categoryId: number; name: string; description: string | null; available: boolean; packageIds: number[]; imageUrl: string | null }
export interface Order { orderId: number; sessionId: number; tableNumber: string; items: { menuItemId: number; name: string; quantity: number }[]; status: OrderStatus; createdAt: string }
export interface PageResult<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }

export { getApiError }
const pendingQrExchanges = new Map<string, Promise<SessionContext>>()
let previousQrExchange: Promise<unknown> = Promise.resolve()

export function redeemQr(token: string): Promise<SessionContext> {
  const pending = pendingQrExchanges.get(token)
  if (pending) return pending

  // A new scan must finish after the previous exchange so its cookie wins.
  const exchange = previousQrExchange.catch(() => undefined).then(async () =>
    (await customerApiClient.post<SessionContext>('/dining-sessions/qr-exchange', { token })).data)
  pendingQrExchanges.set(token, exchange)
  previousQrExchange = exchange.then(() => undefined, () => undefined)
  const clear = () => { if (pendingQrExchanges.get(token) === exchange) pendingQrExchanges.delete(token) }
  void exchange.then(clear, clear)
  return exchange
}
export async function getCustomerContext() { return (await customerApiClient.get<SessionContext>('/dining-sessions/customer-context')).data }
export async function getMenu(sessionId: number) { return (await customerApiClient.get<MenuItem[]>(`/dining-sessions/${sessionId}/menu`)).data }
export async function getBuffetPackage(id: number) { return (await apiClient.get<BuffetPackage>(`/buffet-packages/${id}`)).data }
export async function getCategories() { return (await apiClient.get<Category[]>('/menu-categories')).data }
export async function getBuffetPackages(active = true) { return (await apiClient.get<BuffetPackage[]>('/buffet-packages', { params: { active } })).data }
export async function saveCategory(id: number | null, name: string) { return (id === null ? await apiClient.post<Category>('/menu-categories', { name }) : await apiClient.put<Category>(`/menu-categories/${id}`, { name })).data }
export async function deleteCategory(id: number) { await apiClient.delete(`/menu-categories/${id}`) }
export async function getMenuItems(page = 0, size = 10, sort = 'id,asc') { return (await apiClient.get<PageResult<MenuItem>>('/menu-items', { params: { page, size, sort } })).data }
export async function saveMenuItem(id: number | null, input: MenuItemInput) { return (id === null ? await apiClient.post<MenuItem>('/menu-items', input) : await apiClient.put<MenuItem>(`/menu-items/${id}`, input)).data }
export async function deleteMenuItem(id: number) { await apiClient.delete(`/menu-items/${id}`) }
export async function getOrders(sessionId: number) { return (await customerApiClient.get<Order[]>(`/dining-sessions/${sessionId}/orders`)).data }
export async function placeOrder(sessionId: number, items: { menuItemId: number; quantity: number }[]) { return (await customerApiClient.post<Order>(`/dining-sessions/${sessionId}/orders`, { items })).data }
