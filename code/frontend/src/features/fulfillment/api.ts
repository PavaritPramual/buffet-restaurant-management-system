import { apiClient } from '../../api/client'
import type { OrderFulfillmentContext, OrderStatus } from '../../contracts/shared'

export type FulfillmentOrder = OrderFulfillmentContext

export function getApiError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }
  return 'เชื่อมต่อระบบไม่สำเร็จ กรุณาลองใหม่อีกครั้ง'
}

export async function getIncomingOrders() {
  return (await apiClient.get<FulfillmentOrder[]>('/orders/incoming')).data
}

export async function getReadyOrders() {
  return (await apiClient.get<FulfillmentOrder[]>('/orders/ready')).data
}

export async function updateOrderStatus(orderId: number, status: OrderStatus) {
  return (await apiClient.patch<FulfillmentOrder>(`/orders/${orderId}/status`, { status })).data
}
