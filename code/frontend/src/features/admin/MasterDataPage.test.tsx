// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, expect, it, vi } from 'vitest'
import { apiClient } from '../../api/client'
import MasterDataPage from './MasterDataPage'
afterEach(() => { cleanup(); vi.restoreAllMocks() })
it('creates zero-balance stock metadata without sending a quantity', async () => {
  vi.spyOn(apiClient, 'get').mockResolvedValue({ data: [] })
  const post = vi.spyOn(apiClient, 'post').mockResolvedValue({ data: { id: 1 } })
  render(<MasterDataPage kind="stock" />)
  await screen.findByText('ยังไม่มีรายการ')
  for(const [label, value] of [['รหัสสต็อก','S01'],['ชื่อรายการ','เนื้อ'],['หน่วย','kg'],['ยอดแจ้งเตือนต่ำ','2.5'],['ยอดเป้าหมายก่อนเปิดร้าน','10']]) fireEvent.change(screen.getByLabelText(label), { target: { value } })
  fireEvent.click(screen.getByRole('button', { name: 'บันทึก' }))
  await waitFor(() => expect(post).toHaveBeenCalledWith('/stock/items', { sku: 'S01', name: 'เนื้อ', unit: 'kg', lowStockThreshold: '2.5', openingTargetStock: '10' }))
  expect(screen.queryByLabelText('ยอดคงเหลือ')).toBeNull()
})
it('lets the manager deactivate a stock item through the stock active endpoint', async () => {
  vi.spyOn(apiClient, 'get').mockResolvedValue({ data: [{ id: 7, sku: 'S07', name: 'หมู', unit: 'kg', quantity: 1, lowStockThreshold: 1, openingTargetStock: 5, shortfall: 4, active: true }] })
  const put = vi.spyOn(apiClient, 'put').mockResolvedValue({ data: {} })
  render(<MasterDataPage kind="stock" />)
  await screen.findByText('หมู')
  fireEvent.click(screen.getByRole('button', { name: 'ปิดใช้งาน' }))
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  await waitFor(() => expect(put).toHaveBeenCalledWith('/stock/items/7/active', { active: false }))
})
it('requires confirmation to reactivate a package and prevents duplicate actions', async () => {
  vi.spyOn(apiClient, 'get').mockResolvedValue({ data: [{ id: 1, name: 'Standard', price: 299, active: false }] })
  let resolve!: (v: unknown) => void
  const patch = vi.spyOn(apiClient, 'patch').mockReturnValue(new Promise(done => { resolve = done }))
  render(<MasterDataPage kind="buffet-packages" />)
  await screen.findByText('Standard')
  fireEvent.click(screen.getByRole('button', { name: 'เปิดใช้งาน' }))
  expect(patch).not.toHaveBeenCalled()
  const confirm=screen.getByRole('button', { name: 'ยืนยัน' })
  fireEvent.click(confirm); fireEvent.click(confirm)
  expect(patch).toHaveBeenCalledTimes(1)
  expect(patch).toHaveBeenCalledWith('/buffet-packages/1/active', { active: true })
  resolve({ data: {} }); await screen.findByText('อัปเดตข้อมูลแล้ว')
})
it('displays server rejection when a historical table cannot be deleted', async () => {
  vi.spyOn(apiClient, 'get').mockResolvedValue({ data: [{ id: 1, tableNumber: 'T01', capacity: 4, status: 'AVAILABLE' }] })
  vi.spyOn(apiClient, 'delete').mockRejectedValue({ response: { data: { message: 'โต๊ะมีประวัติรอบกิน' } } })
  render(<MasterDataPage kind="tables" />); await screen.findByText('T01')
  fireEvent.click(screen.getByRole('button', { name: 'ลบโต๊ะ' })); fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
  expect(await screen.findByRole('alert')).toHaveProperty('textContent', 'โต๊ะมีประวัติรอบกิน')
})
