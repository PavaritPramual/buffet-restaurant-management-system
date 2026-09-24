import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Button, Card, ConfirmDialog, DataTable, EmptyState, ErrorAlert, LoadingState, PageHeader, SelectField, TextField } from '../../components/common'
import { deleteCategory, deleteMenuItem, getApiError, getCategories, getMenuItems, saveCategory, saveMenuItem } from './api'
import type { Category, MenuItem, MenuItemInput } from './api'
import './ordering.css'

const emptyItem = { categoryId: 0, name: '', available: true, packageIds: '1', imageUrl: '' }
type ItemDraft = typeof emptyItem

export default function MenuAdminPage() {
  const [categories, setCategories] = useState<Category[]>([]); const [items, setItems] = useState<MenuItem[]>([])
  const [page, setPage] = useState(0); const [totalPages, setTotalPages] = useState(0); const [total, setTotal] = useState(0)
  const [categoryName, setCategoryName] = useState(''); const [editingCategory, setEditingCategory] = useState<number | null>(null)
  const [itemDraft, setItemDraft] = useState<ItemDraft>(emptyItem); const [editingItem, setEditingItem] = useState<number | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ kind: 'category' | 'item'; id: number; label: string } | null>(null)
  const [error, setError] = useState(''); const [notice, setNotice] = useState(''); const [busy, setBusy] = useState(false); const [loading, setLoading] = useState(true)

  const load = useCallback(async (currentPage: number) => {
    const [nextCategories, nextItems] = await Promise.all([getCategories(), getMenuItems(currentPage)])
    setCategories(nextCategories); setItems(nextItems.content); setTotal(nextItems.totalElements); setTotalPages(nextItems.totalPages)
    setItemDraft((current) => current.categoryId || !nextCategories.length ? current : { ...current, categoryId: nextCategories[0].id })
  }, [])
  // Loading server data is the synchronization purpose of this effect.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { load(page).catch((cause) => setError(getApiError(cause))).finally(() => setLoading(false)) }, [load, page])
  function changePage(nextPage: number) { setLoading(true); setPage(nextPage) }
  async function run(action: () => Promise<void>) { if (busy) return; setBusy(true); setError(''); setNotice(''); try { await action() } catch (cause) { setError(getApiError(cause)) } finally { setBusy(false) } }
  function submitCategory(event: FormEvent) { event.preventDefault(); if (!categoryName.trim()) return; void run(async () => { await saveCategory(editingCategory, categoryName.trim()); await load(page); setCategoryName(''); setEditingCategory(null); setNotice('บันทึกหมวดหมู่แล้ว') }) }
  function submitItem(event: FormEvent) {
    event.preventDefault(); const raw = itemDraft.packageIds.split(',').map((entry) => entry.trim())
    if (raw.some((entry) => !/^[1-9]\d*$/.test(entry))) { setError('รหัสแพ็กเกจต้องเป็นเลขบวกและคั่นด้วยจุลภาค'); return }
    const input: MenuItemInput = { categoryId: itemDraft.categoryId, name: itemDraft.name.trim(), available: itemDraft.available, packageIds: [...new Set(raw.map(Number))], imageUrl: itemDraft.imageUrl.trim() || null }
    void run(async () => { await saveMenuItem(editingItem, input); await load(page); setEditingItem(null); setItemDraft({ ...emptyItem, categoryId: categories[0]?.id ?? 0 }); setNotice('บันทึกเมนูแล้ว') })
  }
  function editItem(item: MenuItem) { setEditingItem(item.id); setItemDraft({ categoryId: item.categoryId, name: item.name, available: item.available, packageIds: item.packageIds.join(','), imageUrl: item.imageUrl ?? '' }); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  async function confirmDelete() { if (!deleteTarget) return; await run(async () => { if (deleteTarget.kind === 'category') await deleteCategory(deleteTarget.id); else await deleteMenuItem(deleteTarget.id); setDeleteTarget(null); await load(page); setNotice('ลบข้อมูลแล้ว') }) }

  return <main className="ordering-page admin-page"><PageHeader eyebrow="Admin · Menu Catalog" title="จัดการเมนูอาหาร" description="ข้อมูลถูกบันทึกในฐานข้อมูลและนำไปใช้กับหน้าสั่งอาหารของลูกค้า" />
    {error && <ErrorAlert message={error} />}{notice && <div className="ordering-notice" role="status">{notice}</div>}
    <div className="admin-forms"><Card><h2>หมวดหมู่</h2><form className="admin-form" onSubmit={submitCategory}><TextField label="ชื่อหมวดหมู่" value={categoryName} maxLength={100} required onChange={(event) => setCategoryName(event.target.value)} /><div className="form-actions"><Button loading={busy} type="submit">{editingCategory ? 'บันทึกการแก้ไข' : 'เพิ่มหมวดหมู่'}</Button>{editingCategory && <Button variant="secondary" type="button" onClick={() => { setEditingCategory(null); setCategoryName('') }}>ยกเลิก</Button>}</div></form>
      {categories.length === 0 ? <EmptyState title="ยังไม่มีหมวดหมู่" /> : <ul className="category-list">{categories.map((entry) => <li key={entry.id}><span>{entry.name}</span><span><Button variant="ghost" size="sm" onClick={() => { setEditingCategory(entry.id); setCategoryName(entry.name) }}>แก้ไข</Button><Button variant="ghost" size="sm" onClick={() => setDeleteTarget({ kind: 'category', id: entry.id, label: entry.name })}>ลบ</Button></span></li>)}</ul>}</Card>
      <Card><h2>{editingItem ? 'แก้ไขเมนู' : 'เพิ่มเมนู'}</h2><form className="admin-form" onSubmit={submitItem}><TextField label="ชื่อเมนู" value={itemDraft.name} maxLength={120} required onChange={(event) => setItemDraft({ ...itemDraft, name: event.target.value })} /><SelectField label="หมวดหมู่" value={itemDraft.categoryId} required onChange={(event) => setItemDraft({ ...itemDraft, categoryId: Number(event.target.value) })}>{categories.map((entry) => <option key={entry.id} value={entry.id}>{entry.name}</option>)}</SelectField><TextField label="รหัสแพ็กเกจที่สั่งได้" value={itemDraft.packageIds} required onChange={(event) => setItemDraft({ ...itemDraft, packageIds: event.target.value })} /><TextField label="URL ภาพเมนู (ถ้ามี)" value={itemDraft.imageUrl} onChange={(event) => setItemDraft({ ...itemDraft, imageUrl: event.target.value })} /><label className="checkbox-field"><input type="checkbox" checked={itemDraft.available} onChange={(event) => setItemDraft({ ...itemDraft, available: event.target.checked })} /> พร้อมให้สั่ง</label><div className="form-actions"><Button loading={busy} disabled={!categories.length}>บันทึกเมนู</Button>{editingItem && <Button variant="secondary" type="button" onClick={() => { setEditingItem(null); setItemDraft({ ...emptyItem, categoryId: categories[0]?.id ?? 0 }) }}>ยกเลิก</Button>}</div></form></Card></div>
    <Card className="menu-table"><div className="section-title"><div><h2>รายการเมนู</h2><p>ทั้งหมด {total} รายการ</p></div></div>{loading ? <LoadingState /> : items.length === 0 ? <EmptyState title="ยังไม่มีเมนู" description="เพิ่มหมวดหมู่และเมนูแรกได้จากแบบฟอร์มด้านบน" /> : <DataTable headers={['เมนู', 'หมวดหมู่', 'แพ็กเกจ', 'สถานะ', 'จัดการ']}>{items.map((item) => <tr key={item.id}><td><span className="table-menu-name">{item.imageUrl && <img src={item.imageUrl} alt="" />}{item.name}</span></td><td>{item.categoryName}</td><td>{item.packageIds.join(', ')}</td><td>{item.available ? 'พร้อมสั่ง' : 'ปิดขาย'}</td><td><div className="table-actions"><Button size="sm" variant="secondary" onClick={() => editItem(item)}>แก้ไข</Button><Button size="sm" variant="ghost" onClick={() => setDeleteTarget({ kind: 'item', id: item.id, label: item.name })}>ลบ</Button></div></td></tr>)}</DataTable>}<div className="pagination"><Button variant="secondary" disabled={page === 0} onClick={() => changePage(page - 1)}>ก่อนหน้า</Button><span>หน้า {page + 1} / {Math.max(1, totalPages)}</span><Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => changePage(page + 1)}>ถัดไป</Button></div></Card>
    <ConfirmDialog open={Boolean(deleteTarget)} title="ยืนยันการลบ" description={`ต้องการลบ “${deleteTarget?.label ?? ''}” ใช่หรือไม่`} busy={busy} onCancel={() => setDeleteTarget(null)} onConfirm={() => void confirmDelete()} />
  </main>
}
