// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import FoundationApp from '../../FoundationApp'
import DashboardPage from './DashboardPage'
import { authApi, dashboardApi } from './api'
import type { DashboardSummary } from './api'

vi.mock('./api', async () => ({
  ...await vi.importActual<typeof import('./api')>('./api'),
  dashboardApi: { summary: vi.fn() }, authApi: { current: vi.fn(), login: vi.fn(), logout: vi.fn() },
}))

const summary: DashboardSummary = {
  startDate: '2026-10-01', endDate: '2026-10-02', timeZone: 'Asia/Bangkok', generatedAt: '2026-10-02T10:00:00Z',
  sales: { revenue: 1234.5, paidBills: 2, averageBill: 617.25,
    daily: [{ date: '2026-10-01', bills: 2, revenue: 1234.5 }, { date: '2026-10-02', bills: 0, revenue: 0 }],
    byMethod: [{ method: 'CASH', bills: 1, revenue: 1000 }, { method: 'QR', bills: 1, revenue: 234.5 }, { method: 'CARD', bills: 0, revenue: 0 }] },
  visits: { openedSessions: 3, adults: 8, children: 2, orders: 5 },
  operations: { availableTables: 4, occupiedTables: 3, activeSessions: 3, activeGuests: 10, unpaidBillRequests: 1,
    paidAwaitingClose: 1, receivedOrders: 2, preparingOrders: 3, readyOrders: 1 },
  stock: { activeItems: 4, lowStockItems: 1, outOfStockItems: 0, belowTargetItems: 1,
    lowItems: [{ id: 1, name: 'ข้าว', unit: 'กก.', quantity: 1, threshold: 2, target: 5, shortfall: 4 }] },
  popularItems: [{ menuItemId: 1, name: 'หมูสไลซ์', servings: 5 }],
}
afterEach(() => { cleanup(); vi.resetAllMocks() })
function page() { return render(<MemoryRouter><DashboardPage /></MemoryRouter>) }

describe('Dashboard', () => {
  it.each(['MANAGER', 'SUPERVISOR'] as const)('opens the dashboard from /admin for %s with role-specific navigation', async (role) => {
    vi.mocked(authApi.current).mockResolvedValue({ userId: 1, username: role, displayName: 'Demo', role })
    vi.mocked(dashboardApi.summary).mockResolvedValue(summary)
    render(<MemoryRouter initialEntries={['/admin']}><FoundationApp /></MemoryRouter>)
    expect(await screen.findByRole('heading', { name: 'ภาพรวมร้าน' })).toBeTruthy()
    expect(await screen.findByText('หมูสไลซ์')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'ภาพรวมร้าน' }).getAttribute('aria-current')).toBe('page')
    expect(Boolean(screen.queryByRole('link', { name: 'พนักงาน' }))).toBe(role === 'MANAGER')
    expect(screen.queryByRole('link', { name: 'งานครัว' })).toBeNull()
  })

  it('renders zero states and distinguishes period totals from current snapshots', async () => {
    vi.mocked(dashboardApi.summary).mockResolvedValue({ ...summary,
      sales: { ...summary.sales, revenue: 0, paidBills: 0, averageBill: 0 }, popularItems: [],
      stock: { ...summary.stock, lowItems: [] } })
    page()
    expect(await screen.findByText('ยังไม่มีบิลที่ชำระแล้วในช่วงวันที่นี้')).toBeTruthy()
    expect(screen.getByText('ยังไม่มีรายการสั่งอาหารในช่วงวันที่นี้')).toBeTruthy()
    expect(screen.getByText('ไม่มีสต็อกที่ถึงจุดเตือน')).toBeTruthy()
    expect(screen.getByText(/ส่วนนี้แสดงสถานะล่าสุดของร้านทุกช่วงวันที่/)).toBeTruthy()
    expect(screen.getByText('ยอดสุทธิจากบิล PAID')).toBeTruthy()
  })

  it('shows errors and retries without displaying stale totals', async () => {
    vi.mocked(dashboardApi.summary).mockRejectedValueOnce({ response: { data: { message: 'โหลดสรุปไม่สำเร็จ' } } })
      .mockResolvedValueOnce(summary).mockRejectedValueOnce({ response: { data: { message: 'หมดสิทธิ์อ่านข้อมูล' } } })
    page()
    expect(await screen.findByRole('alert')).toHaveProperty('textContent', expect.stringContaining('โหลดสรุปไม่สำเร็จ'))
    fireEvent.click(screen.getByRole('button', { name: 'ลองอีกครั้ง' }))
    expect(await screen.findByText('หมูสไลซ์')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'รีเฟรชข้อมูล' }))
    expect(await screen.findByRole('alert')).toHaveProperty('textContent', expect.stringContaining('หมดสิทธิ์อ่านข้อมูล'))
    expect(screen.queryByText('หมูสไลซ์')).toBeNull()
  })

  it('validates date ranges and submits explicit dates', async () => {
    vi.mocked(dashboardApi.summary).mockResolvedValue(summary)
    page(); await screen.findByText('หมูสไลซ์')
    fireEvent.change(screen.getByLabelText('ตั้งแต่วันที่'), { target: { value: '2026-10-03' } })
    fireEvent.change(screen.getByLabelText('ถึงวันที่'), { target: { value: '2026-10-01' } })
    fireEvent.submit(screen.getByRole('button', { name: 'ดูสรุป' }).closest('form')!)
    expect(screen.getByRole('alert').textContent).toContain('เลือกช่วงวันที่ตามลำดับ')
    expect(dashboardApi.summary).toHaveBeenCalledTimes(1)
    fireEvent.change(screen.getByLabelText('ตั้งแต่วันที่'), { target: { value: '2026-10-01' } })
    fireEvent.change(screen.getByLabelText('ถึงวันที่'), { target: { value: '2026-10-02' } })
    fireEvent.submit(screen.getByRole('button', { name: 'ดูสรุป' }).closest('form')!)
    await waitFor(() => expect(dashboardApi.summary).toHaveBeenLastCalledWith('2026-10-01', '2026-10-02'))
  })

  it('ignores a late response from a previously selected range', async () => {
    let resolve!: (data: DashboardSummary) => void
    vi.mocked(dashboardApi.summary).mockReturnValueOnce(new Promise((done) => { resolve = done })).mockResolvedValueOnce(summary)
    page(); expect(screen.getByRole('status').textContent).toContain('กำลังโหลด')
    fireEvent.click(screen.getByRole('button', { name: 'วันนี้' }))
    expect(await screen.findByText('หมูสไลซ์')).toBeTruthy()
    await act(async () => resolve({ ...summary, popularItems: [{ menuItemId: 99, name: 'ข้อมูลช่วงเก่า', servings: 99 }] }))
    expect(screen.queryByText('ข้อมูลช่วงเก่า')).toBeNull()
    expect(screen.getByText('หมูสไลซ์')).toBeTruthy()
  })
})
