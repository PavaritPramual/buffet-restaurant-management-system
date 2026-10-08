// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import StockPage from './StockPage'
import UsersPage from './UsersPage'
import { stockApi, usersApi } from './api'
import type { StockItem, StockTransaction, UserContext, UserRecord } from './api'

vi.mock('./api', async () => {
  const actual = await vi.importActual<typeof import('./api')>('./api')
  return {
    ...actual,
    stockApi: { overview: vi.fn(), history: vi.fn(), stockIn: vi.fn(), adjust: vi.fn() },
    usersApi: { list: vi.fn(), create: vi.fn(), updateProfile: vi.fn() },
  }
})

const item: StockItem = {
  id: 4, sku: 'RICE-01', name: 'ข้าวหอมมะลิ', unit: 'กก.', quantity: 12,
  lowStockThreshold: 3, openingTargetStock: 20, shortfall: 8, active: true, updatedAt: '2026-09-30T10:00:00Z',
}
const history: StockTransaction[] = [{
  id: 8, stockItemId: 4, itemName: 'ข้าวหอมมะลิ', transactionType: 'IN', quantityDelta: 2,
  balanceAfter: 12, reason: 'รับจากผู้ขาย', actorUsername: 'manager', createdAt: '2026-09-30T10:00:00Z',
}]
const manager: UserContext = { userId: 1, username: 'manager', displayName: 'ผู้จัดการ', role: 'MANAGER' }
const account: UserRecord = { id: 2, username: 'staff', displayName: 'พนักงานบริการ', email: null, role: 'SERVICE_STAFF', firstName: null, lastName: null, phoneNumber: null }

afterEach(() => { cleanup(); vi.clearAllMocks() })

function renderInShell(page: React.ReactNode, user: UserContext) {
  function ContextOutlet() { return <Outlet context={user} /> }
  return render(
    <MemoryRouter initialEntries={['/admin']}>
      <Routes><Route path="/admin" element={<ContextOutlet />}><Route index element={page} /></Route></Routes>
    </MemoryRouter>,
  )
}

describe('Admin stock and user pages', () => {
  it('requires confirmation and prevents duplicate adjustment submissions', async () => {
    vi.mocked(stockApi.overview).mockResolvedValue([item])
    vi.mocked(stockApi.history).mockResolvedValue(history)
    let complete!: (value: StockTransaction) => void
    vi.mocked(stockApi.adjust).mockReturnValue(new Promise((resolve) => { complete = resolve }))
    renderInShell(<StockPage />, manager)
    fireEvent.click(await screen.findByRole('button', { name: 'ปรับยอด' }))
    fireEvent.change(screen.getByLabelText('ผลต่างที่ปรับ (กก.)'), { target: { value: '-1' } })
    fireEvent.change(screen.getByLabelText('เหตุผล'), { target: { value: 'ตรวจนับ' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกรายการ' }))
    expect(stockApi.adjust).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: 'ยกเลิก' }))
    expect(stockApi.adjust).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกรายการ' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    fireEvent.click(screen.getByRole('button', { name: 'กำลังดำเนินการ…' }))
    expect(stockApi.adjust).toHaveBeenCalledTimes(1)
    complete({ ...history[0], quantityDelta: -1, balanceAfter: 11 })
    await waitFor(() => expect(screen.queryByRole('dialog')).toBeNull())
  })

  it('shows stock history and permits supervisor stock-in with a required reason', async () => {
    vi.mocked(stockApi.overview).mockResolvedValue([item])
    vi.mocked(stockApi.history).mockResolvedValue(history)
    vi.mocked(stockApi.stockIn).mockResolvedValue(history[0])

    renderInShell(<StockPage />, { ...manager, role: 'SUPERVISOR' })

    expect(await screen.findByRole('heading', { name: 'ภาพรวมสต็อก' })).toBeTruthy()
    expect(screen.getByText('รับจากผู้ขาย')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'รับเข้า' }))
    fireEvent.change(screen.getByLabelText('จำนวนที่รับเข้า'), { target: { value: '2.5' } })
    fireEvent.change(screen.getByLabelText('เหตุผล'), { target: { value: 'รับจากผู้ขาย' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกรายการ' }))

    await waitFor(() => expect(stockApi.stockIn).toHaveBeenCalledWith(4, 2.5, 'รับจากผู้ขาย'))
  })

  it('shows target and shortfall and blocks stock-in/adjustment for inactive items', async () => {
    vi.mocked(stockApi.overview).mockResolvedValue([item, { ...item, id: 5, sku: 'OFF-01', name: 'ปิดอยู่', active: false }])
    vi.mocked(stockApi.history).mockResolvedValue(history)
    renderInShell(<StockPage />, manager)
    const activeRow = await screen.findByRole('row', { name: /RICE-01/ })
    expect(activeRow.textContent).toContain('20')
    expect(activeRow.textContent).toContain('8')
    const inactiveRow = screen.getByRole('row', { name: /OFF-01/ })
    const buttons = Array.from(inactiveRow.querySelectorAll('button'))
    expect(buttons).toHaveLength(2)
    expect(buttons.every((button) => (button as HTMLButtonElement).disabled)).toBe(true)
    expect(screen.getByText('รับจากผู้ขาย')).toBeTruthy()
  })

  it('lets the manager fill names for a legacy profile and keeps the old display name', async () => {
    vi.mocked(usersApi.list).mockResolvedValue([account])
    vi.mocked(usersApi.updateProfile).mockResolvedValue({ ...account, firstName: 'สมชาย', lastName: 'ใจดี' })
    renderInShell(<UsersPage />, manager)
    const row = await screen.findByRole('row', { name: /พนักงานบริการ/ })
    expect(row.textContent).toContain('ใช้ชื่อที่แสดงเดิม')
    fireEvent.click(screen.getByRole('button', { name: 'เติมข้อมูล' }))
    fireEvent.change(screen.getAllByLabelText('ชื่อ')[0], { target: { value: 'สมชาย' } })
    fireEvent.change(screen.getAllByLabelText('นามสกุล')[0], { target: { value: 'ใจดี' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกโปรไฟล์' }))
    await waitFor(() => expect(usersApi.updateProfile).toHaveBeenCalledWith(2, { firstName: 'สมชาย', lastName: 'ใจดี', phoneNumber: '' }))
  })

  it('renders the user list and offers account creation to the manager page', async () => {
    vi.mocked(usersApi.list).mockResolvedValue([account])
    vi.mocked(usersApi.create).mockResolvedValue(account)

    renderInShell(<UsersPage />, manager)

    expect(await screen.findByRole('heading', { name: 'พนักงาน' })).toBeTruthy()
    expect(screen.getByRole('row', { name: /พนักงานบริการ/ })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'สร้างบัญชี' })).toBeTruthy()
  })
})
