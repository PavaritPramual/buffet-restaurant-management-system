import { apiClient } from '../../api/client'

export interface ManagedSession {
  sessionId: number; tableId: number; tableNumber: string; sessionStatus: 'ACTIVE' | 'CANCELLED' | 'COMPLETED'
  adultCount: number; childCount: number; startTime: string; endTime: string | null
}
export interface ManagerOperation {
  id: number; action: 'FORCE_CLOSE_SESSION' | 'FORCE_DELETE_MENU'; resourceId: number
  resourceLabel: string; reason: string; actorUsername: string; createdAt: string
}
export async function getManagedSessions() { return (await apiClient.get<ManagedSession[]>('/manager/dining-sessions/active')).data }
export async function getManagerOperations() { return (await apiClient.get<ManagerOperation[]>('/manager/operations')).data }
export async function forceCloseSession(id: number, reason: string) {
  return (await apiClient.post<ManagedSession>(`/manager/dining-sessions/${id}/force-close`, { reason })).data
}
export async function forceDeleteMenu(id: number, reason: string) {
  await apiClient.post(`/manager/menu-items/${id}/force-delete`, { reason })
}
