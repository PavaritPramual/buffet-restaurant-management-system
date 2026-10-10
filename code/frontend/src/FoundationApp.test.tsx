// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, expect, it, vi } from 'vitest'
import FoundationApp from './FoundationApp'
import { authApi } from './features/admin/api'
import type { UserContext } from './features/admin/api'

vi.mock('./features/admin/api', async () => ({
  ...await vi.importActual<typeof import('./features/admin/api')>('./features/admin/api'),
  authApi: { current: vi.fn(), login: vi.fn(), logout: vi.fn() },
}))
vi.mock('./DesignSystemPage', () => ({ default: () => <p>Design demo</p> }))
vi.mock('./features/admin/StockPage', () => ({ default: () => <p>Stock home</p> }))
vi.mock('./features/staff-tables/StaffTablesPage', () => ({ default: () => <p>Staff home</p> }))
vi.mock('./features/fulfillment/KitchenBoardPage', () => ({ default: () => <p>Kitchen home</p> }))
afterEach(() => { cleanup(); vi.resetAllMocks(); vi.unstubAllEnvs() })
const user: UserContext = { userId: 1, username: 'qa', displayName: 'QA', role: 'MANAGER' }
function show(path = '/') { render(<MemoryRouter initialEntries={[path]}><FoundationApp /></MemoryRouter>) }
it.each(['/', '/unknown', '/dev/ui'])('routes signed-out production visitors from %s to the real login', async path => {
  vi.stubEnv('DEV', false); vi.stubEnv('PROD', true)
  vi.mocked(authApi.current).mockRejectedValue({ response: { status: 401 } })
  show(path)
  await screen.findByRole('heading', { name: 'เข้าสู่ระบบ' })
  expect(screen.queryByText('Design demo')).toBeNull()
})
it.each([
  ['MANAGER', 'Stock home'], ['SUPERVISOR', 'Stock home'],
  ['SERVICE_STAFF', 'Staff home'], ['KITCHEN_STAFF', 'Kitchen home'],
] as const)('opens the %s role home from root and removes it on expiry', async (role, home) => {
  vi.mocked(authApi.current).mockResolvedValue({ ...user, role })
  show(); await screen.findByText(home)
  vi.mocked(authApi.current).mockRejectedValue({ response: { status: 401 } })
  act(() => window.dispatchEvent(new Event('auth:session-expired')))
  await screen.findByRole('heading', { name: 'เข้าสู่ระบบ' })
  expect(screen.queryByText(home)).toBeNull()
})
it.each(['MANAGER', 'SUPERVISOR', 'SERVICE_STAFF', 'KITCHEN_STAFF'] as const)('logs %s out from its root destination', async role => {
  vi.mocked(authApi.current).mockResolvedValue({ ...user, role })
  vi.mocked(authApi.logout).mockResolvedValue(undefined)
  show(); const logout = await screen.findByRole('button', { name: 'ออกจากระบบ' })
  vi.mocked(authApi.current).mockRejectedValue({ response: { status: 401 } })
  fireEvent.click(logout)
  await screen.findByRole('heading', { name: 'เข้าสู่ระบบ' })
  expect(screen.queryByText('Stock home')).toBeNull()
  expect(screen.queryByText('Staff home')).toBeNull()
  expect(screen.queryByText('Kitchen home')).toBeNull()
})
