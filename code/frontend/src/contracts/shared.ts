export const TABLE_STATUSES = ['AVAILABLE', 'OCCUPIED'] as const
export type TableStatus = (typeof TABLE_STATUSES)[number]

export const DINING_SESSION_STATUSES = ['ACTIVE', 'COMPLETED', 'CANCELLED'] as const
export type DiningSessionStatus = (typeof DINING_SESSION_STATUSES)[number]

export const ORDER_STATUSES = ['RECEIVED', 'PREPARING', 'READY', 'SERVED'] as const
export type OrderStatus = (typeof ORDER_STATUSES)[number]

export const PAYMENT_METHODS = ['CASH', 'QR', 'CARD'] as const
export type PaymentMethod = (typeof PAYMENT_METHODS)[number]

export const PAYMENT_STATUSES = ['PENDING', 'PAID', 'FAILED'] as const
export type PaymentStatus = (typeof PAYMENT_STATUSES)[number]

export const USER_ROLES = ['SERVICE_STAFF', 'KITCHEN_STAFF', 'SUPERVISOR', 'MANAGER'] as const
export type UserRole = (typeof USER_ROLES)[number]

export interface SessionContext {
  sessionId: number
  sessionToken: string
  packageId: number
  tableId: number
  tableNumber: string
  sessionStatus: DiningSessionStatus
  adultCount: number
  childCount: number
}

export interface OrderFulfillmentItem {
  menuItemId: number
  name: string
  quantity: number
}

export interface OrderFulfillmentContext {
  orderId: number
  sessionId: number
  tableNumber: string
  items: OrderFulfillmentItem[]
  status: OrderStatus
  createdAt: string
}

export interface BillingContext {
  sessionId: number
  packagePrice: number
  adultCount: number
  childCount: number
  discountContext: Record<string, unknown> | null
  sessionStatus: DiningSessionStatus
}

export interface PaymentResult {
  paymentId: number
  sessionId: number
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  paidAt: string
}

export interface UserContext {
  userId: number
  username: string
  role: UserRole
  active: boolean
}
