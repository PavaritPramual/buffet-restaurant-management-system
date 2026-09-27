import { apiClient } from "./client";
import type { BillingContext } from "../contracts/shared";

export interface BillSummary {
  sessionId: number
  subtotalNoneDiscount: number
  discountAmount: number
  totalAmount: number
  totalBeforeRounding: number
  roundingAdjustment: number
}

export async function previewBill(
  context: BillingContext,
): Promise<BillSummary> {

    const response = await apiClient.post<BillSummary>('/billing/preview', context)
    
    return response.data
}