import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  deleteCategory, deleteMenuItem, getApiError, getCategories, getMenuItems,
  saveCategory, saveMenuItem,
} from './api'
import type { Category, MenuItem, MenuItemInput } from './api'
import './ordering.css'

const emptyItem = { categoryId: 0, name: '', available: true, packageIds: '1', imageUrl: '' }
type ItemDraft = typeof emptyItem

export default function MenuAdminPage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [items, setItems] = useState<MenuItem[]>([])
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)
  const [categoryName, setCategoryName] = useState('')
  const [editingCategory, setEditingCategory] = useState<number | null>(null)
  const [itemDraft, setItemDraft] = useState<ItemDraft>(emptyItem)
  const [editingItem, setEditingItem] = useState<number | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async (currentPage: number) => {
    const [nextCategories, nextItems] = await Promise.all([getCategories(), getMenuItems(currentPage)])
    setCategories(nextCategories)
    setItems(nextItems.content)
    setTotal(nextItems.totalElements)
    setItemDraft((current) => current.categoryId || !nextCategories.length
      ? current : { ...current, categoryId: nextCategories[0].id })
  }, [])

  useEffect(() => {
    let active = true
    Promise.all([getCategories(), getMenuItems(page)])
      .then(([nextCategories, nextItems]) => {
        if (!active) return
        setCategories(nextCategories)
        setItems(nextItems.content)
        setTotal(nextItems.totalElements)
        setItemDraft((current) => current.categoryId || !nextCategories.length
          ? current : { ...current, categoryId: nextCategories[0].id })
        setError('')
      })
      .catch((cause) => { if (active) setError(getApiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [page])

  async function run(action: () => Promise<void>) {
    if (busy) return
    setBusy(true)
    setError('')
    setNotice('')
    try { await action() }
    catch (cause) { setError(getApiError(cause)) }
    finally { setBusy(false) }
  }

  function resetItem(firstCategory = categories[0]?.id ?? 0) {
    setEditingItem(null)
    setItemDraft({ ...emptyItem, categoryId: firstCategory })
  }

  function submitCategory(event: FormEvent) {
    event.preventDefault()
    if (!categoryName.trim()) { setError('กรุณากรอกชื่อหมวดหมู่'); return }
    void run(async () => {
      await saveCategory(editingCategory, categoryName.trim())
      await load(page)
      setCategoryName('')
      setEditingCategory(null)
      setNotice('บันทึกหมวดหมู่แล้ว')
    })
  }

  function submitItem(event: FormEvent) {
    event.preventDefault()
    const rawPackages = itemDraft.packageIds.split(',').map((entry) => entry.trim())
    if (rawPackages.some((entry) => !/^[1-9]\d*$/.test(entry))) {
      setError('รหัสแพ็กเกจต้องเป็นเลขบวก คั่นด้วยจุลภาค'); return
    }
    const packageIds = [...new Set(rawPackages.map(Number))]
    const input: MenuItemInput = {
      categoryId: itemDraft.categoryId,
      name: itemDraft.name.trim(),
      available: itemDraft.available,
      packageIds,
      imageUrl: itemDraft.imageUrl.trim() || null,
    }
    if (!input.categoryId || !input.name) { setError('กรุณากรอกหมวดหมู่และชื่อเมนู'); return }
    void run(async () => {
      await saveMenuItem(editingItem, input)
      await load(page)
      resetItem()
      setNotice('บันทึกเมนูแล้ว')
    })
  }

  function startEdit(item: MenuItem) {
    setEditingItem(item.id)
    setItemDraft({ categoryId: item.categoryId, name: item.name,
      available: item.available, packageIds: item.packageIds.join(','), imageUrl: item.imageUrl ?? '' })
    setError('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function remove(kind: 'category' | 'item', id: number) {
    const key = `${kind}:${id}`
    if (deleteTarget !== key) { setDeleteTarget(key); return }
    void run(async () => {
      if (kind === 'category') await deleteCategory(id)
      else await deleteMenuItem(id)
      setDeleteTarget(null)
      await load(page)
      setNotice('ลบแล้ว')
    })
  }

  return <main className="ordering-page menu-admin-page">
    <header className="ordering-header"><span className="ordering-eyebrow">MENU CATALOG</span>
      <h1>จัดการเมนู</h1><p>ข้อมูลตัวอย่างสำหรับพัฒนา ข้อมูลจะเริ่มใหม่เมื่อปิด backend</p></header>
    {error && <div role="alert" className="ordering-alert">{error}</div>}
    {notice && <div role="status" className="ordering-notice">{notice}</div>}
    <div className="menu-admin-layout">
      <section className="menu-admin-panel">
        <h2>หมวดหมู่</h2>
        <form onSubmit={submitCategory} className="menu-admin-form">
          <label>ชื่อหมวดหมู่<input value={categoryName} maxLength={100}
            onChange={(event) => setCategoryName(event.target.value)} required /></label>
          <div className="menu-admin-actions"><button disabled={busy} type="submit">{editingCategory ? 'บันทึกการแก้ไข' : 'เพิ่มหมวดหมู่'}</button>
            {editingCategory && <button type="button" className="secondary" onClick={() => { setEditingCategory(null); setCategoryName('') }}>ยกเลิก</button>}</div>
        </form>
        <ul className="menu-admin-list">{categories.map((entry) => <li key={entry.id}>
          <span>{entry.name}</span><span className="menu-admin-row-actions">
            <button type="button" onClick={() => { setEditingCategory(entry.id); setCategoryName(entry.name) }}>แก้ไข</button>
            <button type="button" onClick={() => remove('category', entry.id)} disabled={busy}>
              {deleteTarget === `category:${entry.id}` ? 'ยืนยันลบ' : 'ลบ'}</button>
          </span></li>)}</ul>
      </section>
      <section className="menu-admin-panel">
        <h2>{editingItem ? `แก้ไขเมนู #${editingItem}` : 'เพิ่มเมนู'}</h2>
        <form onSubmit={submitItem} className="menu-admin-form">
          <label>ชื่อเมนู<input value={itemDraft.name} maxLength={120} required
            onChange={(event) => setItemDraft({ ...itemDraft, name: event.target.value })} /></label>
          <label>หมวดหมู่<select value={itemDraft.categoryId} required
            onChange={(event) => setItemDraft({ ...itemDraft, categoryId: Number(event.target.value) })}>
            {categories.map((entry) => <option key={entry.id} value={entry.id}>{entry.name}</option>)}
          </select></label>
          <label>รหัสแพ็กเกจที่สั่งได้<input value={itemDraft.packageIds} placeholder="1,2" required
            onChange={(event) => setItemDraft({ ...itemDraft, packageIds: event.target.value })} /></label>
          <label>URL ภาพเมนู (ถ้ามี)<input type="text" inputMode="url" value={itemDraft.imageUrl} placeholder="https://example.com/menu.jpg หรือ /images/menu.jpg"
            onChange={(event) => setItemDraft({ ...itemDraft, imageUrl: event.target.value })} /></label>
          <label className="menu-admin-check"><input type="checkbox" checked={itemDraft.available}
            onChange={(event) => setItemDraft({ ...itemDraft, available: event.target.checked })} /> พร้อมให้สั่ง</label>
          <div className="menu-admin-actions"><button disabled={busy || !categories.length} type="submit">บันทึกเมนู</button>
            {editingItem && <button type="button" className="secondary" onClick={() => resetItem()}>ยกเลิก</button>}</div>
        </form>
      </section>
    </div>
    <section className="menu-admin-panel menu-admin-items">
      <h2>รายการเมนู <small>ทั้งหมด {total} รายการ</small></h2>
      {loading ? <p>กำลังโหลด...</p> : items.length === 0 ? <p>ยังไม่มีเมนูในหน้านี้</p> :
        <ul className="menu-admin-list">{items.map((item) => <li key={item.id}>
          <span className="menu-admin-item-summary">
            {item.imageUrl && <img src={item.imageUrl} alt="" loading="lazy" />}
            <span><strong>{item.name}</strong><small>#{item.id} · {categories.find((entry) => entry.id === item.categoryId)?.name ?? 'ไม่พบหมวดหมู่'} · {item.available ? 'พร้อมสั่ง' : 'ปิดขาย'}</small></span>
          </span><span className="menu-admin-row-actions">
            <button type="button" onClick={() => startEdit(item)}>แก้ไข</button>
            <button type="button" disabled={busy} onClick={() => remove('item', item.id)}>
              {deleteTarget === `item:${item.id}` ? 'ยืนยันลบ' : 'ลบ'}</button>
          </span></li>)}</ul>}
      <div className="menu-admin-pagination"><button disabled={page === 0} onClick={() => setPage(page - 1)}>ก่อนหน้า</button>
        <span>หน้า {page + 1} / {Math.max(1, Math.ceil(total / 10))}</span>
        <button disabled={(page + 1) * 10 >= total} onClick={() => setPage(page + 1)}>ถัดไป</button></div>
    </section>
  </main>
}
