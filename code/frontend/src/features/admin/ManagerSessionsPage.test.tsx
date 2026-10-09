// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { afterEach, expect, it, vi } from 'vitest'
import ManagerSessionsPage from './ManagerSessionsPage'
import * as api from './manager-operations-api'
vi.mock('./manager-operations-api', () => ({ getManagedSessions: vi.fn(), getManagerOperations: vi.fn(), forceCloseSession: vi.fn() }))

const session = { sessionId: 42, tableId: 3, tableNumber: 'T03', sessionStatus: 'ACTIVE' as const, adultCount: 1, childCount: 0, startTime: '2026-10-09T03:00:00Z', endTime: null }
afterEach(() => { cleanup(); vi.resetAllMocks() })

it('confirms a force close with reason, prevents duplicate submission and reloads the active list/audit', async () => {
  vi.mocked(api.getManagedSessions).mockResolvedValueOnce([session]).mockResolvedValue([])
  vi.mocked(api.getManagerOperations).mockResolvedValueOnce([]).mockResolvedValue([{ id: 1, action: 'FORCE_CLOSE_SESSION', resourceId: 42, resourceLabel: 'T03', reason: 'รอบทดสอบค้าง', actorUsername: 'Manager', createdAt: '2026-10-09T04:00:00Z' }])
  let finish!: (value: typeof session) => void
  vi.mocked(api.forceCloseSession).mockImplementation(() => new Promise((resolve) => { finish = resolve }))
  render(<ManagerSessionsPage />)
  fireEvent.click(await screen.findByRole('button', { name: 'บังคับปิดโต๊ะ T03' }))
  const dialog = screen.getByRole('dialog')
  expect(dialog.textContent).toContain('ไม่ถือว่าได้รับเงินแล้ว')
  expect((within(dialog).getByRole('button', { name: 'ยืนยันบังคับปิดโต๊ะ' }) as HTMLButtonElement).disabled).toBe(true)
  fireEvent.change(screen.getByLabelText('เหตุผลในการบังคับดำเนินการ'), { target: { value: 'รอบทดสอบค้าง' } })
  const form = within(dialog).getByRole('button', { name: 'ยืนยันบังคับปิดโต๊ะ' }).closest('form')!
  fireEvent.click(within(dialog).getByRole('button', { name: 'ยืนยันบังคับปิดโต๊ะ' }))
  fireEvent.submit(form)
  expect(api.forceCloseSession).toHaveBeenCalledExactlyOnceWith(42, 'รอบทดสอบค้าง')
  finish(session)
  await screen.findByText('ไม่มีรอบกินที่เปิดอยู่')
  expect(screen.getByText('รอบทดสอบค้าง')).toBeTruthy()
  expect(screen.queryByRole('dialog')).toBeNull()
})

it('retains a rejected force-close target and reason without showing success', async () => {
  vi.mocked(api.getManagedSessions).mockResolvedValue([session])
  vi.mocked(api.getManagerOperations).mockResolvedValue([])
  vi.mocked(api.forceCloseSession).mockRejectedValue({ response: { data: { message: 'รอบกินนี้ปิดแล้ว กรุณาโหลดข้อมูลใหม่' } } })
  render(<ManagerSessionsPage />)
  fireEvent.click(await screen.findByRole('button', { name: 'บังคับปิดโต๊ะ T03' }))
  fireEvent.change(screen.getByLabelText('เหตุผลในการบังคับดำเนินการ'), { target: { value: 'ตรวจสถานะค้าง' } })
  fireEvent.click(screen.getByRole('button', { name: 'ยืนยันบังคับปิดโต๊ะ' }))
  await waitFor(() => expect(within(screen.getByRole('dialog')).getByRole('alert').textContent).toContain('ปิดแล้ว'))
  expect(screen.queryByText('บังคับปิดโต๊ะ T03 แล้ว โต๊ะพร้อมเปิดรอบใหม่')).toBeNull()
  expect((screen.getByLabelText('เหตุผลในการบังคับดำเนินการ') as HTMLInputElement).value).toBe('ตรวจสถานะค้าง')
})

it('retries a failed list load without performing an override', async () => {
  vi.mocked(api.getManagedSessions).mockRejectedValueOnce(new Error('offline')).mockResolvedValue([session])
  vi.mocked(api.getManagerOperations).mockResolvedValue([])
  render(<ManagerSessionsPage />)
  await screen.findByRole('alert')
  fireEvent.click(screen.getByRole('button', { name: 'โหลดข้อมูลใหม่' }))
  await screen.findByRole('button', { name: 'บังคับปิดโต๊ะ T03' })
  expect(screen.queryByRole('alert')).toBeNull()
  expect(api.forceCloseSession).not.toHaveBeenCalled()
})
