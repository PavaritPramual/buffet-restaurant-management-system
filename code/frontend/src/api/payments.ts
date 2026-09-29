import { apiClient } from './client'
import type { PaymentMethod, PaymentResult } from '../contracts/shared'

export interface CreatePaymentRequest {
  sessionId: number
  paymentMethod: PaymentMethod
}

export async function createPayment(
  request: CreatePaymentRequest,
): Promise<PaymentResult> {
  const response = await apiClient.post<PaymentResult>(
    '/payments',
    request,
  )

  return response.data
}