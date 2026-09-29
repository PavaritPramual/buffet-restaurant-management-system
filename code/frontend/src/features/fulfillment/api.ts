import { apiClient } from '../../api/client'
import type { OrderFulfillmentContext, OrderStatus, UserRole } from '../../contracts/shared'

export type FulfillmentOrder = OrderFulfillmentContext

export function getApiError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  return 'เชื่อมต่อระบบไม่สำเร็จ กรุณาลองใหม่อีกครั้ง'
}

// TEMPORARY: until the Authentication module ships real staff login, each board identifies
// itself with a fixed role via this header. The backend's fixture access provider reads it.
// Replace with the logged-in user's real role once login exists.
function roleHeaders(role: UserRole) {
  return { headers: { 'X-User-Role': role } }
}

export async function getIncomingOrders(role: UserRole = 'KITCHEN_STAFF') {
  return (await apiClient.get<FulfillmentOrder[]>('/orders/incoming', roleHeaders(role))).data
}

export async function getReadyOrders(role: UserRole = 'SERVICE_STAFF') {
  return (await apiClient.get<FulfillmentOrder[]>('/orders/ready', roleHeaders(role))).data
}

export async function updateOrderStatus(orderId: number, status: OrderStatus, role: UserRole) {
  return (await apiClient.patch<FulfillmentOrder>(`/orders/${orderId}/status`, { status }, roleHeaders(role))).data
}
