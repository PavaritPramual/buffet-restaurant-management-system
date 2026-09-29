import { apiClient } from './client'

export interface BillingPreviewRequest {
  sessionId: number
}

export interface BillSummary {
  sessionId: number
  subtotalNoneDiscount: number
  discountAmount: number
  totalAmount: number
  totalBeforeRounding: number
  roundingAdjustment: number
}

export async function previewBill(sessionId: number): Promise<BillSummary> {
  const request: BillingPreviewRequest = { sessionId }
  const response = await apiClient.post<BillSummary>('/billing/preview', request)
  return response.data
}
