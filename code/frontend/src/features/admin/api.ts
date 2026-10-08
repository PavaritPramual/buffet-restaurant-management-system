import { apiClient } from '../../api/client'

export type UserRole = 'SERVICE_STAFF' | 'KITCHEN_STAFF' | 'SUPERVISOR' | 'MANAGER'

export const roleLabels: Record<UserRole, string> = {
  SERVICE_STAFF: 'พนักงานบริการ',
  KITCHEN_STAFF: 'พนักงานครัว',
  SUPERVISOR: 'หัวหน้างาน',
  MANAGER: 'ผู้จัดการ',
}

export type UserContext = {
  userId: number
  username: string
  displayName: string
  role: UserRole
}

export type StockItem = {
  id: number
  sku: string
  name: string
  unit: string
  quantity: number
  lowStockThreshold: number
  openingTargetStock: number
  shortfall: number
  active: boolean
  updatedAt: string
}

export type StockTransaction = {
  id: number
  stockItemId: number
  itemName: string
  transactionType: 'IN' | 'ADJUSTMENT'
  quantityDelta: number
  balanceAfter: number
  reason: string
  actorUsername: string
  createdAt: string
}

export type UserRecord = {
  id: number
  username: string
  displayName: string
  email: string | null
  role: UserRole
  firstName: string | null
  lastName: string | null
  phoneNumber: string | null
}

export type UserProfileInput = { firstName: string; lastName: string; phoneNumber: string }

export const authApi = {
  async current() {
    return (await apiClient.get<UserContext>('/auth/me')).data
  },
  async login(username: string, password: string) {
    return (await apiClient.post<UserContext>('/auth/login', { username, password })).data
  },
  async logout() {
    await apiClient.post('/auth/logout')
  },
}

export const stockApi = {
  async overview() {
    return (await apiClient.get<StockItem[]>('/stock')).data
  },
  async history(itemId?: number) {
    return (await apiClient.get<StockTransaction[]>('/stock/transactions', {
      params: itemId ? { itemId } : undefined,
    })).data
  },
  async stockIn(itemId: number, quantity: number, reason: string) {
    return (await apiClient.post<StockTransaction>(`/stock/${itemId}/in`, { quantity, reason })).data
  },
  async adjust(itemId: number, quantityDelta: number, reason: string) {
    return (await apiClient.post<StockTransaction>(`/stock/${itemId}/adjustments`, { quantityDelta, reason })).data
  },
}

export const usersApi = {
  async list() {
    return (await apiClient.get<UserRecord[]>('/admin/users')).data
  },
  async create(user: { username: string; password: string; displayName: string; email: string | null; role: UserRole } & UserProfileInput) {
    return (await apiClient.post<UserRecord>('/admin/users', user)).data
  },
  async updateProfile(id: number, profile: UserProfileInput) {
    return (await apiClient.put<UserRecord>(`/admin/users/${id}/profile`, profile)).data
  },
}

export function getErrorMessage(error: unknown) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = error.response
    if (typeof response === 'object' && response !== null && 'data' in response) {
      const data = response.data
      if (typeof data === 'object' && data !== null && 'message' in data && typeof data.message === 'string') {
        return data.message
      }
    }
  }
  return 'Could not complete the request. Check the backend connection and try again.'
}