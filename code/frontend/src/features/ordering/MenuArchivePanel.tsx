import { useCallback, useEffect, useRef, useState } from 'react'
import { Button, Card, ConfirmDialog, DataTable, EmptyState, ErrorAlert, LoadingState, SelectField } from '../../components/common'
import { getApiError } from '../../api/errors'
import type { Category, MenuItem, PageResult } from './api'
import './ordering.css'

// Injected for UI state tests; the Manager route supplies the real HTTP adapter.
export interface MenuRemovalGateway {
  listItems: (page: number, size: number, sort: string) => Promise<PageResult<MenuItem>>
  listCategories: () => Promise<Category[]>
  removeItem: (id: number) => Promise<void>
  removeCategory: (id: number) => Promise<void>
  restoreItem: (id: number) => Promise<MenuItem>
  restoreCategory: (id: number) => Promise<Category>
}

type RestoreTarget = { kind: 'item' | 'category'; id: number; name: string }

export function MenuArchivePanel({ gateway, onBusyChange }: { gateway: MenuRemovalGateway; onBusyChange?: (busy: boolean) => void }) {
  const [items, setItems] = useState<MenuItem[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('id,asc')
  const [totalPages, setTotalPages] = useState(0)
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [mutationError, setMutationError] = useState('')
  const [notice, setNotice] = useState('')
  const [target, setTarget] = useState<RestoreTarget | null>(null)
  const [busy, setBusy] = useState(false)
  const revision = useRef(0)
  const inFlight = useRef(false)
  const mounted = useRef(false)
  const latestGateway = useRef(gateway)
  useEffect(() => { latestGateway.current = gateway }, [gateway])
  const invalidate = useCallback(() => { mounted.current = false; ++revision.current }, [])

  const load = useCallback(async (nextPage: number) => {
    const request = ++revision.current
    setLoading(true)
    setLoadError('')
    try {
      const [nextItems, nextCategories] = await Promise.all([
        gateway.listItems(nextPage, 10, sort), gateway.listCategories(),
      ])
      if (!mounted.current || request !== revision.current) return
      // Another Manager can restore the last item while this page is open.
      if (nextPage > 0 && nextPage >= Math.max(1, nextItems.totalPages)) {
        setPage(Math.max(0, nextItems.totalPages - 1))
        return
      }
      setItems(nextItems.content)
      setCategories(nextCategories)
      setTotal(nextItems.totalElements)
      setTotalPages(nextItems.totalPages)
    } catch (cause) {
      if (mounted.current && request === revision.current) setLoadError(getApiError(cause))
    } finally {
      if (mounted.current && request === revision.current) setLoading(false)
    }
  }, [gateway, sort])

  useEffect(() => {
    mounted.current = true
    // Fetching the selected archived page is this effect's synchronization work.
    // oxlint-disable-next-line react/set-state-in-effect
    void load(page)
    return invalidate
  }, [invalidate, load, page])

  function changePage(next: number) {
    ++revision.current
    setLoading(true)
    setPage(next)
  }
  function changeSort(next: string) {
    ++revision.current
    setLoading(true)
    setPage(0)
    setSort(next)
  }
  function chooseRestore(next: RestoreTarget) {
    setMutationError('')
    setNotice('')
    setTarget(next)
  }
  async function confirmRestore() {
    if (!target || inFlight.current) return
    inFlight.current = true
    setBusy(true)
    onBusyChange?.(true)
    setMutationError('')
    setNotice('')
    try {
      if (target.kind === 'item') await gateway.restoreItem(target.id)
      else await gateway.restoreCategory(target.id)
      if (!mounted.current || latestGateway.current !== gateway) return
      setTarget(null)
      // No optimistic state change: the confirmed server operation is the
      // boundary between a retryable restore and a read-only refresh retry.
      setNotice('คืนรายการแล้ว กรุณาตรวจสอบสถานะในรายการใช้งานก่อนเปิดขาย')
      if (target.kind === 'item' && items.length === 1 && page > 0) changePage(page - 1)
      else await load(page)
    } catch (cause) {
      if (mounted.current && latestGateway.current === gateway) setMutationError(getApiError(cause))
    } finally {
      inFlight.current = false
      onBusyChange?.(false)
      if (mounted.current) setBusy(false)
    }
  }

  return <section aria-label="รายการเมนูที่เก็บออก" className="menu-archive-panel">
    <p>รายการเก็บออกไม่แสดงให้ลูกค้าสั่ง ประวัติออเดอร์ยังคงอยู่ การคืนหมวดหมู่จะไม่คืนเมนูในหมวดให้อัตโนมัติ</p>
    {loadError && <><ErrorAlert message={loadError} /><Button variant="secondary" disabled={loading || busy} onClick={() => void load(page)}>โหลดรายการเก็บออกใหม่</Button></>}
    {mutationError && <ErrorAlert message={mutationError} />}
    {notice && <div className="ordering-notice" role="status">{notice}</div>}
    {loading ? <LoadingState label="กำลังโหลดรายการเก็บออก…" /> : loadError ? null : <>
      <Card><h2>หมวดหมู่ที่เก็บออก</h2>
        {categories.length === 0 ? <EmptyState title="ไม่มีหมวดหมู่ที่เก็บออก" /> : <ul className="category-list">{categories.map(category => <li key={category.id}><span>{category.name}</span><Button variant="secondary" size="sm" disabled={busy} onClick={() => chooseRestore({ kind: 'category', id: category.id, name: category.name })}>คืนหมวดหมู่ {category.name}</Button></li>)}</ul>}
      </Card>
      <Card className="menu-table"><div className="section-title"><div><h2>เมนูที่เก็บออก</h2><p>ทั้งหมด {total} รายการ</p></div><SelectField label="เรียงรายการเก็บออก" value={sort} disabled={busy} onChange={event => changeSort(event.target.value)}><option value="id,asc">เพิ่มก่อน → หลัง</option><option value="name,asc">ชื่อ A → Z</option><option value="name,desc">ชื่อ Z → A</option></SelectField></div>
        {items.length === 0 ? <EmptyState title="ไม่มีเมนูที่เก็บออก" /> : <DataTable headers={['เมนู', 'หมวดหมู่', 'จัดการ']}>{items.map(item => <tr key={item.id}><td>{item.name}</td><td>{item.categoryName}</td><td><Button variant="secondary" size="sm" disabled={busy} onClick={() => chooseRestore({ kind: 'item', id: item.id, name: item.name })}>คืนเมนู {item.name}</Button></td></tr>)}</DataTable>}
        <div className="pagination"><Button variant="secondary" disabled={busy || page === 0} onClick={() => changePage(page - 1)}>ก่อนหน้า</Button><span>หน้า {page + 1} / {Math.max(1, totalPages)}</span><Button variant="secondary" disabled={busy || page + 1 >= totalPages} onClick={() => changePage(page + 1)}>ถัดไป</Button></div>
      </Card>
    </>}
    <ConfirmDialog open={Boolean(target)} title="ยืนยันการคืนรายการ" description={`คืน “${target?.name ?? ''}” ไปยังรายการใช้งานใช่หรือไม่? เมนูที่คืนครั้งแรกจะยังปิดขาย กรุณาตรวจสอบหมวดหมู่และแพ็กเกจก่อนเปิดขาย`} busy={busy} onCancel={() => { setTarget(null); setMutationError('') }} onConfirm={() => void confirmRestore()} />
  </section>
}
