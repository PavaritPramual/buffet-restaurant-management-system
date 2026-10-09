// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import StaffTablesPage from './StaffTablesPage'
import DiningSessionPage from './DiningSessionPage'
import * as api from './api'

vi.mock('./api', () => ({
  getApiError: (error: unknown) => error instanceof Error ? error.message : 'Request failed',
  getTables: vi.fn(), getActiveSessions: vi.fn(), getPackages: vi.fn(), getSoups: vi.fn(),
  getDiningSession: vi.fn(), openDiningSession: vi.fn(), closeDiningSession: vi.fn(),
}))

afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderStaff() {
  return render(<MemoryRouter initialEntries={['/staff/tables']}><Routes>
    <Route path="/staff/tables" element={<StaffTablesPage />} />
    <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
  </Routes></MemoryRouter>)
}

const context = {
  sessionId: 32, sessionToken: 'demo-token', packageId: 5, tableId: 7, tableNumber: 'T07', soupId: 2,
  packageName: 'บุฟเฟต์มาตรฐาน', soupName: 'น้ำซุปต้มยำ',
  sessionStatus: 'ACTIVE' as const, adultCount: 2, childCount: 1, startTime: '2026-09-26T10:00:00+07:00', endTime: null,
}

describe('Staff table flow', () => {
  it('opens a table and displays a QR URL for the returned session token', async () => {
    vi.mocked(api.getTables).mockResolvedValue([{ id: 7, tableNumber: 'T07', capacity: 4, status: 'AVAILABLE' }])
    vi.mocked(api.getActiveSessions).mockResolvedValue([])
    vi.mocked(api.getPackages).mockResolvedValue([{ id: 5, name: 'Standard', active: true, price: 299 }])
    vi.mocked(api.getSoups).mockResolvedValue([{ id: 2, name: 'Tom Yum', active: true }])
    vi.mocked(api.openDiningSession).mockResolvedValue(context)
    vi.mocked(api.getDiningSession).mockResolvedValue(context)

    const { container } = renderStaff()
    fireEvent.click(await screen.findByRole('button', { name: 'เปิดโต๊ะ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันเปิดโต๊ะ' }))

    await waitFor(() => expect(api.openDiningSession).toHaveBeenCalledWith({
      tableId: 7, packageId: 5, soupId: 2, adultCount: 1, childCount: 0,
    }))
    expect(await screen.findByText('QR สำหรับลูกค้า')).toBeTruthy()
    expect(screen.getByText('แพ็กเกจ บุฟเฟต์มาตรฐาน · น้ำซุป น้ำซุปต้มยำ')).toBeTruthy()
    expect(screen.queryByText('แพ็กเกจ #5 · น้ำซุป #2')).toBeNull()
    expect(container.querySelector('svg title')?.textContent).toBe('QR โต๊ะ T07')
    expect(screen.getByRole('link').getAttribute('href')).toContain('/customer/qr#token=demo-token')
  })

  it('keeps the session open and reports the API error when payment is not ready', async () => {
    vi.mocked(api.getDiningSession).mockResolvedValue(context)
    vi.mocked(api.closeDiningSession).mockRejectedValue(new Error('Payment status verification is not configured'))
    render(<MemoryRouter initialEntries={['/staff/sessions/32']}><Routes>
      <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    </Routes></MemoryRouter>)

    fireEvent.click(await screen.findByRole('button', { name: 'ปิดรอบกิน' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))

    expect((await screen.findByRole('alert')).textContent).toContain('Payment status verification is not configured')
    expect(screen.getByText('กำลังใช้งาน')).toBeTruthy()
  })

  it('shows the payment instruction in Thai when an unpaid close is rejected', async () => {
    vi.mocked(api.getDiningSession).mockResolvedValue(context)
    vi.mocked(api.closeDiningSession).mockRejectedValue(new Error('กรุณาบันทึกการชำระเงินก่อนปิดรอบกิน'))
    render(<MemoryRouter initialEntries={['/staff/sessions/32']}><Routes>
      <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    </Routes></MemoryRouter>)

    fireEvent.click(await screen.findByRole('button', { name: 'ปิดรอบกิน' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))

    expect((await screen.findByRole('alert')).textContent).toContain('กรุณาบันทึกการชำระเงินก่อนปิดรอบกิน')
    expect(screen.getByText('กำลังใช้งาน')).toBeTruthy()
    expect(api.closeDiningSession).toHaveBeenCalledWith(32)
  })

  it('does not show a QR after the dining session has closed', async () => {
    vi.mocked(api.getDiningSession).mockResolvedValue({ ...context, sessionStatus: 'COMPLETED' })
    render(<MemoryRouter initialEntries={['/staff/sessions/32']}><Routes>
      <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    </Routes></MemoryRouter>)

    expect(await screen.findByText('รอบกินนี้ปิดแล้ว QR จึงใช้ไม่ได้')).toBeTruthy()
    expect(screen.getByText('แพ็กเกจ บุฟเฟต์มาตรฐาน · น้ำซุป น้ำซุปต้มยำ')).toBeTruthy()
    expect(api.getPackages).not.toHaveBeenCalled()
    expect(api.getSoups).not.toHaveBeenCalled()
    expect(screen.queryByRole('link')).toBeNull()
    expect(document.querySelector('svg title')).toBeNull()
  })

  it('shows loading then the detail error without fetching catalog names', async () => {
    let rejectRequest!: (error: Error) => void
    vi.mocked(api.getDiningSession).mockReturnValue(new Promise((_, reject) => { rejectRequest = reject }))
    render(<MemoryRouter initialEntries={['/staff/sessions/32']}><Routes>
      <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    </Routes></MemoryRouter>)

    expect(screen.getByText('กำลังโหลดรายละเอียดรอบกิน…')).toBeTruthy()
    rejectRequest(new Error('ไม่สามารถโหลดรายละเอียดรอบกินได้'))
    expect((await screen.findByRole('alert')).textContent).toContain('ไม่สามารถโหลดรายละเอียดรอบกินได้')
    expect(screen.queryByText('แพ็กเกจ บุฟเฟต์มาตรฐาน · น้ำซุป น้ำซุปต้มยำ')).toBeNull()
    expect(api.getPackages).not.toHaveBeenCalled()
    expect(api.getSoups).not.toHaveBeenCalled()
  })
})
