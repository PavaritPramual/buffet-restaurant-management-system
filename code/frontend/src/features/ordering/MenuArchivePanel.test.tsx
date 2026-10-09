// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { MenuArchivePanel } from './MenuArchivePanel'
import type { MenuRemovalGateway } from './MenuArchivePanel'
import type { MenuItem, PageResult } from './api'

const category = { id: 3, name: 'ของทอด' }
const item: MenuItem = { id: 10, categoryId: 3, categoryName: 'ของทอด', name: 'ไก่ทอด', description: null, available: false, packageIds: [7], imageUrl: null }
const pageOf = (content: MenuItem[], page = 0, totalPages = 1): PageResult<MenuItem> => ({ content, page, size: 10, totalElements: content.length, totalPages })
function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (cause: unknown) => void
  const promise = new Promise<T>((yes, no) => { resolve = yes; reject = no })
  return { promise, resolve, reject }
}
function gateway(): MenuRemovalGateway {
  return {
    listItems: vi.fn().mockResolvedValue(pageOf([item])), listCategories: vi.fn().mockResolvedValue([category]),
    removeItem: vi.fn(), removeCategory: vi.fn(),
    restoreItem: vi.fn().mockResolvedValue(item), restoreCategory: vi.fn().mockResolvedValue(category),
  }
}
afterEach(() => { cleanup(); vi.clearAllMocks() })

describe('MenuArchivePanel UI draft with injected gateway', () => {
  it('shows empty archived lists without removal, edit or availability controls', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockResolvedValue(pageOf([]))
    vi.mocked(source.listCategories).mockResolvedValue([])
    render(<MenuArchivePanel gateway={source} />)
    await screen.findByText('ไม่มีเมนูที่เก็บออก')
    expect(screen.getByText('ไม่มีหมวดหมู่ที่เก็บออก')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'ลบ' })).toBeNull()
    expect(screen.queryByRole('checkbox')).toBeNull()
    expect(source.removeItem).not.toHaveBeenCalled()
  })

  it('requires confirmation and cancellation makes no restore request', async () => {
    const source = gateway()
    render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    expect(within(screen.getByRole('dialog')).getByText(/เมนูที่คืนครั้งแรกจะยังปิดขาย/)).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'ยกเลิก' }))
    expect(screen.queryByRole('dialog')).toBeNull()
    expect(source.restoreItem).not.toHaveBeenCalled()
  })

  it('submits one confirmed restore and waits before changing the list', async () => {
    const source = gateway()
    const pending = deferred<MenuItem>()
    vi.mocked(source.restoreItem).mockReturnValue(pending.promise)
    vi.mocked(source.listItems).mockResolvedValueOnce(pageOf([item])).mockResolvedValue(pageOf([]))
    render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    const confirm = screen.getByRole('button', { name: 'ยืนยัน' })
    fireEvent.click(confirm); fireEvent.click(confirm)
    expect(source.restoreItem).toHaveBeenCalledExactlyOnceWith(10)
    expect((screen.getByRole('button', { name: 'ยกเลิก' }) as HTMLButtonElement).disabled).toBe(true)
    expect(screen.getByRole('row', { name: /ไก่ทอด/ })).toBeTruthy()
    await act(async () => pending.resolve(item))
    await screen.findByText('ไม่มีเมนูที่เก็บออก')
    expect(screen.getByText(/คืนรายการแล้ว กรุณาตรวจสอบสถานะ/)).toBeTruthy()
    expect(screen.queryByRole('dialog')).toBeNull()
  })

  it('keeps a business conflict and the same row available for a deliberate retry', async () => {
    const source = gateway()
    vi.mocked(source.restoreItem).mockRejectedValueOnce({ response: { status: 409, data: { message: 'กรุณาคืนหมวดหมู่ก่อนคืนเมนู' } } }).mockResolvedValue(item)
    render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    expect((await screen.findByRole('alert')).textContent).toBe('กรุณาคืนหมวดหมู่ก่อนคืนเมนู')
    expect(screen.getByRole('row', { name: /ไก่ทอด/ })).toBeTruthy()
    expect(screen.getByRole('dialog')).toBeTruthy()
    expect(source.restoreItem).toHaveBeenCalledTimes(1)
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByText(/คืนรายการแล้ว กรุณาตรวจสอบสถานะ/)
    expect(source.restoreItem).toHaveBeenCalledTimes(2)
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('a category restore does not send restore requests for its children', async () => {
    const source = gateway()
    vi.mocked(source.listCategories).mockResolvedValueOnce([category]).mockResolvedValue([])
    render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนหมวดหมู่ ของทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByText('ไม่มีหมวดหมู่ที่เก็บออก')
    expect(source.restoreCategory).toHaveBeenCalledExactlyOnceWith(3)
    expect(source.restoreItem).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: 'คืนเมนู ไก่ทอด' })).toBeTruthy()
  })

  it('retries a failed read without repeating a successful restore', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockResolvedValueOnce(pageOf([item])).mockRejectedValueOnce(new Error('offline')).mockResolvedValue(pageOf([]))
    render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByRole('button', { name: 'โหลดรายการเก็บออกใหม่' })
    expect(screen.queryByRole('dialog')).toBeNull()
    expect(screen.queryByRole('button', { name: 'คืนเมนู ไก่ทอด' })).toBeNull()
    expect(screen.getByText(/คืนรายการแล้ว/)).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'โหลดรายการเก็บออกใหม่' }))
    await screen.findByText('ไม่มีเมนูที่เก็บออก')
    expect(source.restoreItem).toHaveBeenCalledTimes(1)
    expect(screen.queryByRole('alert')).toBeNull()
  })

  it('shows an error rather than presenting stale rows as a verified archive list', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockRejectedValueOnce({ response: { status: 403, data: { message: 'ไม่มีสิทธิ์ดูรายการเก็บออก' } } }).mockResolvedValue(pageOf([item]))
    render(<MenuArchivePanel gateway={source} />)
    expect((await screen.findByRole('alert')).textContent).toBe('ไม่มีสิทธิ์ดูรายการเก็บออก')
    expect(screen.queryByText('ไม่มีเมนูที่เก็บออก')).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'โหลดรายการเก็บออกใหม่' }))
    await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' })
  })

  it('returns from the last page after restoring its last item', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockResolvedValueOnce(pageOf([], 0, 2)).mockResolvedValueOnce(pageOf([item], 1, 2)).mockResolvedValue(pageOf([]))
    render(<MenuArchivePanel gateway={source} />)
    await screen.findByText('หน้า 1 / 2')
    fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    await screen.findByText('หน้า 1 / 1')
    expect(source.listItems).toHaveBeenLastCalledWith(0, 10, 'id,asc')
  })

  it('moves back to an existing page if another Manager emptied the selected page', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockResolvedValueOnce(pageOf([], 0, 2)).mockResolvedValueOnce(pageOf([], 1, 1)).mockResolvedValue(pageOf([item]))
    render(<MenuArchivePanel gateway={source} />)
    await screen.findByText('หน้า 1 / 2')
    fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
    await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' })
    expect(screen.getByText('หน้า 1 / 1')).toBeTruthy()
    expect(source.listItems).toHaveBeenLastCalledWith(0, 10, 'id,asc')
  })

  it('changes sorting from page two and starts the new sort on page one', async () => {
    const source = gateway()
    vi.mocked(source.listItems).mockResolvedValue(pageOf([item], 0, 2))
    render(<MenuArchivePanel gateway={source} />)
    await screen.findByText('หน้า 1 / 2')
    fireEvent.click(screen.getByRole('button', { name: 'ถัดไป' }))
    await screen.findByText('หน้า 2 / 2')
    fireEvent.change(screen.getByLabelText('เรียงรายการเก็บออก'), { target: { value: 'name,desc' } })
    await waitFor(() => expect(source.listItems).toHaveBeenLastCalledWith(0, 10, 'name,desc'))
    expect(screen.getByText('หน้า 1 / 2')).toBeTruthy()
  })

  it('ignores an older read after its data source has been replaced', async () => {
    const source = gateway()
    const old = deferred<PageResult<MenuItem>>()
    vi.mocked(source.listItems).mockReturnValue(old.promise)
    const rendered = render(<MenuArchivePanel gateway={source} />)
    const fresh = gateway()
    vi.mocked(fresh.listItems).mockResolvedValue(pageOf([{ ...item, name: 'ปลา' }]))
    rendered.rerender(<MenuArchivePanel gateway={fresh} />)
    await screen.findByRole('button', { name: 'คืนเมนู ปลา' })
    await act(async () => old.resolve(pageOf([{ ...item, name: 'คำตอบเก่า' }], 1, 2)))
    expect(screen.queryByText('คำตอบเก่า')).toBeNull()
  })

  it('a pending restore after unmount does not reload or contaminate a new panel', async () => {
    const source = gateway()
    const pending = deferred<MenuItem>()
    vi.mocked(source.restoreItem).mockReturnValue(pending.promise)
    const first = render(<MenuArchivePanel gateway={source} />)
    fireEvent.click(await screen.findByRole('button', { name: 'คืนเมนู ไก่ทอด' }))
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยัน' }))
    first.unmount()
    const secondSource = gateway()
    vi.mocked(secondSource.listItems).mockResolvedValue(pageOf([]))
    render(<MenuArchivePanel gateway={secondSource} />)
    await screen.findByText('ไม่มีเมนูที่เก็บออก')
    await act(async () => pending.resolve(item))
    expect(source.listItems).toHaveBeenCalledTimes(1)
    expect(screen.queryByText(/คืนรายการแล้ว/)).toBeNull()
  })
})
