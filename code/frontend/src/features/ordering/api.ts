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
interface QrScan { token: string; promise: Promise<SessionContext>; settled: boolean; failed: boolean }
// Coordination is limited to this tab. The customer cookie is shared across tabs;
// use one ordering tab per browser (see doc/testing/test-plan.md for recovery).
let latestQrScan: QrScan | null = null
let previousQrExchange: Promise<unknown> = Promise.resolve()

export function redeemQr(token: string): Promise<SessionContext> {
  // Coalesce duplicate subscriptions only for the latest scan, including StrictMode.
  if (latestQrScan?.token === token && !latestQrScan.settled) return latestQrScan.promise

  // A new scan must finish after the previous exchange so its cookie wins.
  const exchange = previousQrExchange.catch(() => undefined).then(async () =>
    (await customerApiClient.post<SessionContext>('/dining-sessions/qr-exchange', { token })).data)
  const scan: QrScan = { token, promise: exchange, settled: false, failed: false }
  latestQrScan = scan
  previousQrExchange = exchange.then(() => undefined, () => undefined)
  void exchange.then(() => { scan.settled = true }, () => { scan.settled = true; scan.failed = true })
  return exchange
}
export async function getCustomerContext({ retryFailedQr = false }: { retryFailedQr?: boolean } = {}): Promise<SessionContext> {
  // Keep the latest scan across route remounts; a failed scan cannot restore an older cookie.
  if (retryFailedQr && latestQrScan?.failed) void redeemQr(latestQrScan.token)
  while (true) {
    const scan = latestQrScan
    try {
      if (scan) await scan.promise
      if (scan !== latestQrScan) continue
      const context = (await customerApiClient.get<SessionContext>('/dining-sessions/customer-context')).data
      if (scan === latestQrScan) return context
    } catch (cause) { if (scan === latestQrScan) throw cause }
  }
}
export async function getMenu(sessionId: number) { return (await customerApiClient.get<MenuItem[]>(`/dining-sessions/${sessionId}/menu`)).data }
export async function getCustomerPackage(sessionId: number) { return (await customerApiClient.get<BuffetPackage>(`/dining-sessions/${sessionId}/package`)).data }
export async function getCategories() { return (await apiClient.get<Category[]>('/menu-categories')).data }
export async function getBuffetPackages(active = true) { return (await apiClient.get<BuffetPackage[]>('/buffet-packages', { params: { active } })).data }
export async function saveCategory(id: number | null, name: string) { return (id === null ? await apiClient.post<Category>('/menu-categories', { name }) : await apiClient.put<Category>(`/menu-categories/${id}`, { name })).data }
export async function deleteCategory(id: number) { await apiClient.delete(`/menu-categories/${id}`) }
export async function getMenuItems(page = 0, size = 10, sort = 'id,asc') { return (await apiClient.get<PageResult<MenuItem>>('/menu-items', { params: { page, size, sort } })).data }
export async function saveMenuItem(id: number | null, input: MenuItemInput) { return (id === null ? await apiClient.post<MenuItem>('/menu-items', input) : await apiClient.put<MenuItem>(`/menu-items/${id}`, input)).data }
export async function deleteMenuItem(id: number) { await apiClient.delete(`/menu-items/${id}`) }
export async function getOrders(sessionId: number) { return (await customerApiClient.get<Order[]>(`/dining-sessions/${sessionId}/orders`)).data }
export async function placeOrder(sessionId: number, items: { menuItemId: number; quantity: number }[]) { return (await customerApiClient.post<Order>(`/dining-sessions/${sessionId}/orders`, { items })).data }
