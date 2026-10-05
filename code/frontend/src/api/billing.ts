import { apiClient } from './client'

export interface BillingPreviewRequest {
  sessionId: number
}

export interface BillSummary {
  sessionId: number
  subtotalAmount: number
  discountAmount: number
  totalAmount: number
}

export async function previewBill(sessionId: number): Promise<BillSummary> {
  const request: BillingPreviewRequest = { sessionId }
  const response = await apiClient.post<BillSummary>('/billing/preview', request)
  return response.data
}
