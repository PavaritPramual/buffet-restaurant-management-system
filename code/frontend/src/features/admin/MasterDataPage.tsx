import { useEffect, useRef, useState, type FormEvent } from 'react'
import { apiClient } from '../../api/client'
import { Button, Card, ConfirmDialog, EmptyState, ErrorAlert, LoadingState, PageHeader, RefreshIcon, StatusBadge, TextField } from '../../components/common'
import { getApiError } from '../../api/errors'

type Kind = 'tables' | 'buffet-packages' | 'soups' | 'stock'
type Row = { id: number; tableNumber?: string; capacity?: number; status?: string; name?: string; price?: number; description?: string; active?: boolean; sku?: string; unit?: string; quantity?: number; lowStockThreshold?: number; openingTargetStock?: number; shortfall?: number }
const titles: Record<Kind, string> = { tables: 'จัดการโต๊ะ', 'buffet-packages': 'จัดการแพ็กเกจ', soups: 'จัดการน้ำซุป', stock: 'จัดการรายการสต็อก' }
const blanks = { tableNumber: '', capacity: '4', name: '', price: '', description: '', sku: '', unit: '', lowStockThreshold: '0', openingTargetStock: '0' }

export default function MasterDataPage({ kind }: { kind: Kind }) {
  const [rows, setRows] = useState<Row[]>([])
  const [form, setForm] = useState(blanks)
  const [editing, setEditing] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [pending, setPending] = useState<Row | null>(null)
  const inflight = useRef(false)
  const endpoint = kind === 'stock' ? '/stock/items' : `/${kind}`
  async function load() { setRows((await apiClient.get<Row[]>(`/${kind}`)).data) }
  useEffect(() => {
    let current = true
    apiClient.get<Row[]>(`/${kind}`).then(r => { if (current) setRows(r.data) })
      .catch(e => { if (current) setError(getApiError(e)) }).finally(() => { if (current) setLoading(false) })
    return () => { current = false }
  }, [kind])
  async function refresh() {
    if (inflight.current) return
    inflight.current = true; setLoading(true); setError('')
    try { await load() } catch (e) { setError(getApiError(e)) }
    finally { inflight.current = false; setLoading(false) }
  }
  async function save(e: FormEvent) {
    e.preventDefault(); if (inflight.current) return
    inflight.current = true; setBusy(true); setError(''); setNotice('')
    const body = kind === 'tables' ? { tableNumber: form.tableNumber.trim(), capacity: Number(form.capacity) }
      : kind === 'buffet-packages' ? { name: form.name.trim(), price: Number(form.price), description: form.description }
      : kind === 'soups' ? { name: form.name.trim() }
      : { sku: form.sku.trim(), name: form.name.trim(), unit: form.unit.trim(), lowStockThreshold: Number(form.lowStockThreshold), openingTargetStock: Number(form.openingTargetStock) }
    try {
      if (editing === null) await apiClient.post(endpoint, body)
      else await apiClient.put(`${endpoint}/${editing}`, body)
      setEditing(null); setForm(blanks); setNotice('บันทึกข้อมูลแล้ว'); await load()
    } catch (cause) { setError(getApiError(cause)) }
    finally { inflight.current = false; setBusy(false) }
  }
  async function confirmAction() {
    if (!pending || inflight.current) return
    inflight.current = true; setBusy(true); setError(''); setNotice('')
    try {
      if (kind === 'tables') await apiClient.delete(`${endpoint}/${pending.id}`)
      else if (kind === 'stock') await apiClient.put(`${endpoint}/${pending.id}/active`, { active: !pending.active })
      else await apiClient.patch(`${endpoint}/${pending.id}/active`, { active: !pending.active })
      setPending(null); setNotice('อัปเดตข้อมูลแล้ว'); await load()
    } catch (cause) { setError(getApiError(cause)) }
    finally { inflight.current = false; setBusy(false) }
  }
  function edit(row: Row) {
    setEditing(row.id); setError(''); setNotice('')
    setForm({ tableNumber: row.tableNumber ?? '', capacity: String(row.capacity ?? 4), name: row.name ?? '', price: String(row.price ?? ''), description: row.description ?? '', sku: row.sku ?? '', unit: row.unit ?? '', lowStockThreshold: String(row.lowStockThreshold ?? 0), openingTargetStock: String(row.openingTargetStock ?? 0) })
  }
  const field = (key: keyof typeof blanks, label: string, extra = {}) => <TextField label={label} value={form[key]} onChange={e => setForm({ ...form, [key]: e.target.value })} disabled={busy} required {...extra} />
  return <div className="ordering-page">
    <PageHeader eyebrow="ผู้จัดการ" title={titles[kind]} action={<Button variant="secondary" className="ui-icon-button" aria-label="อัปเดตรายการ" disabled={busy || loading} onClick={() => void refresh()}><RefreshIcon /></Button>} />
    {error && <ErrorAlert message={error} />}{notice && <p role="status">{notice}</p>}
    <Card><h2>{editing === null ? 'เพิ่มรายการ' : 'แก้ไขรายการ'}</h2>
      <form className="admin-form" onSubmit={e => void save(e)}>
        {kind === 'tables' ? <>{field('tableNumber', 'หมายเลขโต๊ะ', { maxLength: 20 })}{field('capacity', 'ความจุ', { type: 'number', min: 1, step: 1 })}</> : <>
          {kind === 'stock' && field('sku', 'รหัสสต็อก', { maxLength: 40 })}
          {field('name', 'ชื่อรายการ', { maxLength: kind === 'stock' ? 120 : 100 })}
          {kind === 'buffet-packages' && <>{field('price', 'ราคา', { type: 'number', min: '0.01', step: '0.01' })}{field('description', 'รายละเอียด', { required: false })}</>}
          {kind === 'stock' && <>{field('unit', 'หน่วย', { maxLength: 24 })}{field('lowStockThreshold', 'ยอดแจ้งเตือนต่ำ', { type: 'number', min: 0, step: '0.001' })}{field('openingTargetStock', 'ยอดเป้าหมายก่อนเปิดร้าน', { type: 'number', min: 0, step: '0.001' })}<p>รายการใหม่เริ่มยอดศูนย์ เพิ่มยอดผ่านหน้าสต็อกเพื่อบันทึกประวัติ หลังมีประวัติแล้วจะเปลี่ยนรหัสและหน่วยไม่ได้</p></>}
        </>}
        <div><Button type="submit" disabled={loading} loading={busy}>บันทึก</Button>{editing !== null && <Button type="button" variant="ghost" disabled={busy} onClick={() => { setEditing(null); setForm(blanks) }}>ยกเลิกแก้ไข</Button>}</div>
      </form>
    </Card>
    {loading ? <LoadingState /> : rows.length === 0 ? <EmptyState title="ยังไม่มีรายการ" /> : <section className="order-list" aria-label={titles[kind]}>
      {rows.map(row => <Card key={row.id}><h3>{row.tableNumber ?? row.name}</h3>
        <p>{kind === 'tables' ? `รองรับ ${row.capacity} คน` : kind === 'stock' ? `${row.sku} · ${row.quantity} ${row.unit} · แจ้งเตือนต่ำ ${row.lowStockThreshold} · เป้าหมาย ${row.openingTargetStock ?? 0} · ขาด ${row.shortfall ?? 0}` : kind === 'buffet-packages' ? `฿${row.price}` : ''}</p>
        <StatusBadge tone={kind === 'tables' ? row.status === 'AVAILABLE' ? 'success' : 'info' : row.active ? 'success' : 'neutral'}>{kind === 'tables' ? row.status === 'AVAILABLE' ? 'ว่าง' : 'กำลังใช้งาน' : row.active ? 'เปิดใช้งาน' : 'ปิดใช้งาน'}</StatusBadge>
        <Button variant="secondary" disabled={busy || (kind === 'tables' && row.status === 'OCCUPIED')} onClick={() => edit(row)}>แก้ไข</Button>
        <Button variant={kind === 'tables' || row.active ? 'danger' : 'secondary'} disabled={busy || (kind === 'tables' && row.status === 'OCCUPIED')} onClick={() => setPending(row)}>{kind === 'tables' ? 'ลบโต๊ะ' : row.active ? 'ปิดใช้งาน' : 'เปิดใช้งาน'}</Button>
      </Card>)}
    </section>}
    <ConfirmDialog open={!!pending} title={kind === 'tables' ? 'ยืนยันลบโต๊ะ' : pending?.active ? 'ยืนยันปิดใช้งาน' : 'ยืนยันเปิดใช้งาน'} description={kind === 'tables' ? 'ลบได้เฉพาะโต๊ะที่ไม่มีประวัติรอบกิน' : kind === 'stock' ? 'รายการที่ปิดใช้งานยังดูประวัติได้ แต่รับเข้าและปรับยอดไม่ได้จนกว่าจะเปิดใช้งานอีกครั้ง' : 'การเปลี่ยนนี้ไม่ลบประวัติรอบกินเดิม'} busy={busy} onCancel={() => setPending(null)} onConfirm={() => void confirmAction()} />
  </div>
}
