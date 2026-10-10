import type { PaymentMethod } from '../../contracts/shared'

export const paymentMethodLabels: Record<PaymentMethod, string> = {
  CASH: 'เงินสด',
  QR: 'คิวอาร์',
  CARD: 'บัตร',
}