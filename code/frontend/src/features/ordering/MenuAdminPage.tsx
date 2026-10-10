import { useCallback, useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Button, Card, ConfirmDialog, DataTable, EmptyState, ErrorAlert, LoadingState, PageHeader, SelectField, TextField } from '../../components/common'
import { deleteCategory, deleteMenuItem, getApiError, getBuffetPackages, getCategories, getMenuItems, getMenuStockUsage, getRecipeStocks, saveCategory, saveMenuItem } from './api'
import type { BuffetPackage, Category, MenuItem, MenuItemInput, RecipeStockOption, StockUsageInput } from './api'
import MenuStockUsageFields from './MenuStockUsageFields'
import { MenuArchivePanel } from './MenuArchivePanel'
import type { MenuRemovalGateway } from './MenuArchivePanel'
import './ordering.css'

interface ItemDraft { categoryId: number; name: string; description: string; available: boolean; packageIds: number[]; imageUrl: string; automaticStockDeduction: boolean; stockUsage: StockUsageInput[] }
const emptyItem: ItemDraft = { categoryId: 0, name: '', description: '', available: true, packageIds: [], imageUrl: '', automaticStockDeduction: false, stockUsage: [] }

export default function MenuAdminPage({ removalGateway }: { removalGateway?: MenuRemovalGateway }) {
  const [catalogView, setCatalogView] = useState<'working' | 'archived'>('working')
  const [categories, setCategories] = useState<Category[]>([]); const [items, setItems] = useState<MenuItem[]>([])
  const [packages, setPackages] = useState<BuffetPackage[]>([])
  const [recipeStocks, setRecipeStocks] = useState<RecipeStockOption[]>([])
  const [recipeStockError, setRecipeStockError] = useState('')
  const [recipeStockLoading, setRecipeStockLoading] = useState(true)
  const [page, setPage] = useState(0); const [totalPages, setTotalPages] = useState(0); const [total, setTotal] = useState(0)
  const [sort, setSort] = useState('id,asc')
  const [categoryName, setCategoryName] = useState(''); const [editingCategory, setEditingCategory] = useState<number | null>(null)
  const [itemDraft, setItemDraft] = useState<ItemDraft>(emptyItem); const [editingItem, setEditingItem] = useState<number | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ kind: 'category' | 'item'; id: number; label: string } | null>(null)
  const [loadError, setLoadError] = useState(''); const [mutationError, setMutationError] = useState('')
  const [notice, setNotice] = useState(''); const [busy, setBusy] = useState(false); const [loading, setLoading] = useState(true)
  const catalogRequest = useRef(0)
  const recipeStockRequest = useRef(0)
  const mutationInFlight = useRef(false)
  const originalRecipe = useRef('')
  const invalidateCatalog = useCallback(() => { ++catalogRequest.current }, [])
  const loadRecipeStocks = useCallback(async () => {
    const request = ++recipeStockRequest.current
    setRecipeStockLoading(true)
    setRecipeStockError('')
    try {
      const stocks = await getRecipeStocks()
      if (request === recipeStockRequest.current) setRecipeStocks(stocks)
    } catch (cause) {
      if (request === recipeStockRequest.current) setRecipeStockError(getApiError(cause))
    } finally {
      if (request === recipeStockRequest.current) setRecipeStockLoading(false)
    }
  }, [])

  const load = useCallback(async (currentPage: number) => {
    const request = ++catalogRequest.current
    setLoading(true)
    setLoadError('')
    try {
      const [nextCategories, nextPackages, nextItems] = await Promise.all([getCategories(), getBuffetPackages(true), getMenuItems(currentPage, 10, sort)])
      if (request !== catalogRequest.current) return
      setCategories(nextCategories); setPackages(nextPackages); setItems(nextItems.content); setTotal(nextItems.totalElements); setTotalPages(nextItems.totalPages)
      setItemDraft((current) => nextCategories.some((category) => category.id === current.categoryId)
        ? current
        : { ...current, categoryId: nextCategories[0]?.id ?? 0 })
    } catch (cause) {
      if (request === catalogRequest.current) setLoadError(getApiError(cause))
    } finally { if (request === catalogRequest.current) setLoading(false) }
  }, [sort])
  useEffect(() => {
    // Loading server data is the synchronization purpose of this effect.
    // oxlint-disable-next-line react/set-state-in-effect
    void load(page)
    return invalidateCatalog
  }, [invalidateCatalog, load, page])
  useEffect(() => {
    // Stock choices are independent from catalog availability.
    // oxlint-disable-next-line react/set-state-in-effect
    void loadRecipeStocks()
    return () => { ++recipeStockRequest.current }
  }, [loadRecipeStocks])
  function changePage(nextPage: number) { if (nextPage === page) return; invalidateCatalog(); setLoading(true); setPage(nextPage) }
  function changeSort(nextSort: string) { if (nextSort === sort) return; invalidateCatalog(); setLoading(true); setPage(0); setSort(nextSort) }
  function retryLoad() { void load(page) }
  async function run(action: () => Promise<void>) { if (mutationInFlight.current) return; mutationInFlight.current = true; setBusy(true); setMutationError(''); setNotice(''); try { await action() } catch (cause) { setMutationError(getApiError(cause)) } finally { mutationInFlight.current = false; setBusy(false) } }
  function submitCategory(event: FormEvent) { event.preventDefault(); if (!categoryName.trim()) return; void run(async () => { await saveCategory(editingCategory, categoryName.trim()); await load(page); setCategoryName(''); setEditingCategory(null); setNotice('บันทึกหมวดหมู่แล้ว') }) }
  function submitItem(event: FormEvent) {
    event.preventDefault()
    if (!itemDraft.packageIds.length) { setMutationError('กรุณาเลือกแพ็กเกจอย่างน้อย 1 รายการ'); return }
    const recipeSignature = (enabled: boolean, usage: StockUsageInput[]) => JSON.stringify({ enabled, usage: [...usage].sort((a, b) => a.stockItemId - b.stockItemId) })
    const unchangedRecipe = editingItem !== null && recipeSignature(itemDraft.automaticStockDeduction, itemDraft.stockUsage) === originalRecipe.current
    if (itemDraft.automaticStockDeduction && (!itemDraft.stockUsage.length || itemDraft.stockUsage.some(entry => (!recipeStocks.some(stock => stock.id === entry.stockItemId && stock.active) && !unchangedRecipe) || entry.quantityPerServing <= 0))) {
      setMutationError('กรุณาเลือกวัตถุดิบที่ใช้งานได้และปริมาณต่อเสิร์ฟอย่างน้อย 1 รายการ'); return
    }
    const input: MenuItemInput = { categoryId: itemDraft.categoryId, name: itemDraft.name.trim(), description: itemDraft.description.trim() || null, available: itemDraft.available, packageIds: itemDraft.packageIds, imageUrl: itemDraft.imageUrl.trim() || null }
    input.automaticStockDeduction = itemDraft.automaticStockDeduction
    input.stockUsage = itemDraft.automaticStockDeduction ? itemDraft.stockUsage : []
    void run(async () => { await saveMenuItem(editingItem, input); await load(page); setEditingItem(null); setItemDraft({ ...emptyItem, categoryId: categories[0]?.id ?? 0 }); setNotice('บันทึกเมนูแล้ว') })
  }
  function editItem(item: MenuItem) { void run(async () => {
    const recipe = await getMenuStockUsage(item.id)
    setRecipeStocks(current => [...current, ...recipe.stockUsage.filter(entry => !current.some(stock => stock.id === entry.stockItemId)).map(entry => ({ id: entry.stockItemId, name: entry.stockItemName, unit: entry.unit, active: entry.active }))])
    setEditingItem(item.id)
    const stockUsage = recipe.stockUsage.map(({ stockItemId, quantityPerServing }) => ({ stockItemId, quantityPerServing }))
    originalRecipe.current = JSON.stringify({ enabled: recipe.automaticStockDeduction, usage: [...stockUsage].sort((a, b) => a.stockItemId - b.stockItemId) })
    setItemDraft({ categoryId: item.categoryId, name: item.name, description: item.description ?? '', available: item.available, packageIds: item.packageIds, imageUrl: item.imageUrl ?? '', automaticStockDeduction: recipe.automaticStockDeduction, stockUsage })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }) }
  function togglePackage(packageId: number) { setItemDraft((current) => ({ ...current, packageIds: current.packageIds.includes(packageId) ? current.packageIds.filter((id) => id !== packageId) : [...current.packageIds, packageId] })) }
  async function confirmDelete() { if (!deleteTarget) return; await run(async () => {
    const target = deleteTarget
    const deletingLastItemOnPage = target.kind === 'item' && items.length === 1 && page > 0
    if (target.kind === 'category') {
      await (removalGateway ? removalGateway.removeCategory(target.id) : deleteCategory(target.id))
      if (editingCategory === target.id) { setEditingCategory(null); setCategoryName('') }
    } else {
      await (removalGateway ? removalGateway.removeItem(target.id) : deleteMenuItem(target.id))
      if (editingItem === target.id) { setEditingItem(null); setItemDraft({ ...emptyItem, categoryId: categories[0]?.id ?? 0 }) }
    }
    setDeleteTarget(null)
    if (deletingLastItemOnPage) { setLoading(true); setPage(page - 1) } else await load(page)
    setNotice(removalGateway ? 'นำรายการออกจากรายการใช้งานแล้ว หากมีประวัติ ระบบจะเก็บไว้ในรายการเก็บออก' : 'ลบข้อมูลแล้ว')
  }) }

  return <main className="ordering-page admin-page"><PageHeader eyebrow="ผู้จัดการ · รายการเมนู" title="จัดการเมนูอาหาร" description="ข้อมูลถูกบันทึกในฐานข้อมูลและนำไปใช้กับหน้าสั่งอาหารของลูกค้า" />
    {removalGateway && <div className="form-actions menu-archive-navigation"><Button variant={catalogView === 'working' ? 'primary' : 'secondary'} aria-pressed={catalogView === 'working'} disabled={busy} onClick={() => { setCatalogView('working'); void load(page) }}>รายการใช้งาน</Button><Button variant={catalogView === 'archived' ? 'primary' : 'secondary'} aria-pressed={catalogView === 'archived'} disabled={busy} onClick={() => { invalidateCatalog(); setMutationError(''); setNotice(''); setDeleteTarget(null); setCatalogView('archived') }}>รายการเก็บออก</Button></div>}
    {catalogView === 'archived' && removalGateway ? <MenuArchivePanel gateway={removalGateway} onBusyChange={setBusy} /> : <>
    {loadError && <><ErrorAlert message={loadError} /><Button variant="secondary" disabled={loading || busy} onClick={retryLoad}>โหลดข้อมูลใหม่</Button></>}
    {recipeStockError && <div className="menu-stock-load-error"><ErrorAlert message={`โหลดรายการวัตถุดิบไม่สำเร็จ: ${recipeStockError}`} /><Button variant="secondary" disabled={recipeStockLoading || busy} onClick={() => void loadRecipeStocks()}>โหลดรายการวัตถุดิบใหม่</Button></div>}
    {mutationError && <ErrorAlert message={mutationError} />}{notice && <div className="ordering-notice" role="status">{notice}</div>}
    <div className="admin-forms"><Card><h2>หมวดหมู่</h2><form className="admin-form" onSubmit={submitCategory}><TextField label="ชื่อหมวดหมู่" value={categoryName} maxLength={100} required onChange={(event) => setCategoryName(event.target.value)} /><div className="form-actions"><Button loading={busy} type="submit">{editingCategory ? 'บันทึกการแก้ไข' : 'เพิ่มหมวดหมู่'}</Button>{editingCategory && <Button variant="secondary" type="button" onClick={() => { setEditingCategory(null); setCategoryName('') }}>ยกเลิก</Button>}</div></form>
      {categories.length === 0 ? <EmptyState title="ยังไม่มีหมวดหมู่" /> : <ul className="category-list">{categories.map((entry) => <li key={entry.id}><span>{entry.name}</span><span><Button variant="ghost" size="sm" onClick={() => { setEditingCategory(entry.id); setCategoryName(entry.name) }}>แก้ไข</Button><Button variant="ghost" size="sm" onClick={() => setDeleteTarget({ kind: 'category', id: entry.id, label: entry.name })}>ลบ</Button></span></li>)}</ul>}</Card>
      <Card><h2>{editingItem ? 'แก้ไขเมนู' : 'เพิ่มเมนู'}</h2><form className="admin-form" onSubmit={submitItem}><TextField label="ชื่อเมนู" value={itemDraft.name} maxLength={100} required onChange={(event) => setItemDraft({ ...itemDraft, name: event.target.value })} /><TextField label="รายละเอียดเมนู (ถ้ามี)" value={itemDraft.description} onChange={(event) => setItemDraft({ ...itemDraft, description: event.target.value })} /><SelectField label="หมวดหมู่" value={itemDraft.categoryId} required onChange={(event) => setItemDraft({ ...itemDraft, categoryId: Number(event.target.value) })}>{categories.map((entry) => <option key={entry.id} value={entry.id}>{entry.name}</option>)}</SelectField><fieldset className="package-options"><legend>แพ็กเกจที่สั่งเมนูนี้ได้</legend>{packages.length ? packages.map((entry) => <label key={entry.id} className="checkbox-field"><input type="checkbox" checked={itemDraft.packageIds.includes(entry.id)} onChange={() => togglePackage(entry.id)} /> {entry.name}</label>) : <p>ยังไม่มีแพ็กเกจที่เปิดใช้งาน</p>}</fieldset><MenuStockUsageFields enabled={itemDraft.automaticStockDeduction} usage={itemDraft.stockUsage} stocks={recipeStocks} disabled={busy || loading} onChange={(automaticStockDeduction, stockUsage) => setItemDraft({ ...itemDraft, automaticStockDeduction, stockUsage })} /><TextField label="URL ภาพเมนู (ถ้ามี)" value={itemDraft.imageUrl} onChange={(event) => setItemDraft({ ...itemDraft, imageUrl: event.target.value })} /><label className="checkbox-field"><input type="checkbox" checked={itemDraft.available} onChange={(event) => setItemDraft({ ...itemDraft, available: event.target.checked })} /> พร้อมให้สั่ง</label><div className="form-actions"><Button loading={busy} disabled={loading || Boolean(loadError) || !categories.length || !packages.length}>บันทึกเมนู</Button>{editingItem && <Button variant="secondary" type="button" onClick={() => { setEditingItem(null); setItemDraft({ ...emptyItem, categoryId: categories[0]?.id ?? 0 }) }}>ยกเลิก</Button>}</div></form></Card></div>
    <Card className="menu-table"><div className="section-title"><div><h2>รายการเมนู</h2><p>ทั้งหมด {total} รายการ</p></div><SelectField label="เรียงเมนู" value={sort} disabled={busy} onChange={(event) => changeSort(event.target.value)}><option value="id,asc">เพิ่มก่อน → หลัง</option><option value="name,asc">ชื่อ A → Z</option><option value="name,desc">ชื่อ Z → A</option><option value="available,desc">พร้อมสั่งก่อน</option></SelectField></div>{loading ? <LoadingState /> : items.length === 0 ? <EmptyState title="ยังไม่มีเมนู" description="เพิ่มหมวดหมู่และเมนูแรกได้จากแบบฟอร์มด้านบน" /> : <DataTable headers={['เมนู', 'หมวดหมู่', 'แพ็กเกจ', 'สถานะ', 'จัดการ']}>{items.map((item) => <tr key={item.id}><td><span className="table-menu-name">{item.imageUrl && <img src={item.imageUrl} alt="" />}{item.name}</span></td><td>{item.categoryName}</td><td>{item.packageIds.map((id) => packages.find((entry) => entry.id === id)?.name ?? `แพ็กเกจ #${id}`).join(', ')}</td><td>{item.available ? 'พร้อมสั่ง' : 'ปิดขาย'}</td><td><div className="table-actions"><Button size="sm" variant="secondary" onClick={() => editItem(item)}>แก้ไข</Button><Button size="sm" variant="ghost" onClick={() => setDeleteTarget({ kind: 'item', id: item.id, label: item.name })}>ลบ</Button></div></td></tr>)}</DataTable>}<div className="pagination"><Button variant="secondary" disabled={busy || page === 0} onClick={() => changePage(page - 1)}>ก่อนหน้า</Button><span>หน้า {page + 1} / {Math.max(1, totalPages)}</span><Button variant="secondary" disabled={busy || page + 1 >= totalPages} onClick={() => changePage(page + 1)}>ถัดไป</Button></div></Card>
    <ConfirmDialog open={Boolean(deleteTarget)} title={removalGateway ? 'ยืนยันการนำรายการออก' : 'ยืนยันการลบ'} description={removalGateway ? `นำ “${deleteTarget?.label ?? ''}” ออกจากรายการใช้งานใช่หรือไม่? รายการที่ไม่เคยใช้และไม่มีข้อมูลอ้างอิงจะถูกลบถาวร หากมีประวัติระบบจะเก็บรายการไว้และลูกค้าสั่งใหม่ไม่ได้ หมวดหมู่ที่ยังมีเมนูใช้งานอยู่ต้องย้ายหรือเก็บเมนูออกก่อน` : `ต้องการลบ “${deleteTarget?.label ?? ''}” ใช่หรือไม่`} busy={busy} onCancel={() => setDeleteTarget(null)} onConfirm={() => void confirmDelete()} />
    </>}
  </main>
}
