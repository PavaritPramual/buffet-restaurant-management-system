import { apiClient } from '../../api/client'
import { getApiError } from '../../api/errors'

export interface StaffSession {
  sessionId: number; sessionToken: string; packageId: number; tableId: number; tableNumber: string; soupId: number
  sessionStatus: 'ACTIVE' | 'COMPLETED' | 'CANCELLED'; adultCount: number; childCount: number
  startTime: string; endTime: string | null; billRequestedAt?: string | null
}

export { getApiError }
export interface RestaurantTable { id: number; tableNumber: string; capacity: number; status: 'AVAILABLE' | 'OCCUPIED' }
export interface CatalogOption { id: number; name: string; active: boolean; price?: number; description?: string | null }
export interface OpenSessionInput { tableId: number; packageId: number; soupId: number; adultCount: number; childCount: number }

export async function getTables() { return (await apiClient.get<RestaurantTable[]>('/tables')).data }
export async function getPackages() { return (await apiClient.get<CatalogOption[]>('/buffet-packages', { params: { active: true } })).data }
export async function getSoups() { return (await apiClient.get<CatalogOption[]>('/soups', { params: { active: true } })).data }
export async function getActiveSessions() { return (await apiClient.get<StaffSession[]>('/dining-sessions/active')).data }
export async function getDiningSession(id: number) { return (await apiClient.get<StaffSession>(`/dining-sessions/${id}`)).data }
export async function openDiningSession(input: OpenSessionInput) { return (await apiClient.post<StaffSession>('/dining-sessions', input)).data }
export async function closeDiningSession(id: number) { return (await apiClient.post<StaffSession>(`/dining-sessions/${id}/close`)).data }
