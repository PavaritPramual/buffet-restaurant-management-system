import { apiClient } from './client'
import { isAxiosError } from 'axios'
import type { PaymentMethod, PaymentResult } from '../contracts/shared'

export interface CreatePaymentRequest {
  sessionId: number
  paymentMethod: PaymentMethod
}

export async function findPayment(sessionId: number): Promise<PaymentResult | null> {
  try {
    const response = await apiClient.get<PaymentResult>(`/payments/sessions/${sessionId}`)
    return response.data
  } catch (error) {
    // 404 means no record; network/auth errors must not be treated as unpaid.
    if (isAxiosError(error) && error.response?.status === 404) return null
    throw error
  }
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
