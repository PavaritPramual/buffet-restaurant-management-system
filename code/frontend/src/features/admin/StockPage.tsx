import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getErrorMessage, stockApi } from './api'
import type { StockItem, StockTransaction, UserContext } from './api'

type ActionMode = 'IN' | 'ADJUSTMENT'

function quantity(value: number) {
  return new Intl.NumberFormat('th-TH', { maximumFractionDigits: 3 }).format(value)
}

function dateTime(value: string) {
  return new Intl.DateTimeFormat('th-TH', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export default function StockPage() {
  const user = useOutletContext<UserContext>()
  const canMoveStock = user.role === 'MANAGER' || user.role === 'SUPERVISOR'
  const [items, setItems] = useState<StockItem[]>([])
  const [history, setHistory] = useState<StockTransaction[]>([])
  const [selected, setSelected] = useState<StockItem | null>(null)
  const [mode, setMode] = useState<ActionMode>('IN')
  const [amount, setAmount] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  async function loadData() {
    setLoading(true)
    try {
      const [stock, transactions] = await Promise.all([stockApi.overview(), stockApi.history()])
      setItems(stock)
      setHistory(transactions)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void loadData() }, [])

  async function submitChange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected) return
    setSaving(true)
    setError('')
    try {
      if (mode === 'IN') await stockApi.stockIn(selected.id, amount, reason)
      else await stockApi.adjust(selected.id, amount, reason)
      setSelected(null)
      setAmount('')
      setReason('')
      await loadData()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  const lowCount = items.filter((item) => item.quantity <= item.lowStockThreshold).length
  const totalUnits = items.reduce((sum, item) => sum + item.quantity, 0)

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><p className="admin-eyebrow">คลังวัตถุดิบ / ภาพรวม</p><h1>ภาพรวมสต็อก</h1><p className="admin-subtitle">ยอดคงเหลือและประวัติการเคลื่อนไหวของวัตถุดิบ</p></div>
      <button className="admin-secondary-button" onClick={() => void loadData()} disabled={loading}>โหลดข้อมูลใหม่</button>
    </div>
    <div className="admin-metrics">
      <div><span>รายการวัตถุดิบ</span><strong>{items.length}</strong></div>
      <div><span>ปริมาณรวม</span><strong>{quantity(totalUnits)} <small>หน่วย</small></strong></div>
      <div className={lowCount > 0 ? 'metric-warning' : ''}><span>ถึงหรือต่ำกว่าจุดเตือน</span><strong>{lowCount}</strong></div>
    </div>
    {error && <p className="admin-error" role="alert">{error}</p>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>วัตถุดิบคงเหลือ</h2><span>{loading ? 'กำลังโหลด...' : `${items.length} รายการ`}</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>วัตถุดิบ</th><th>รหัส</th><th>คงเหลือ</th><th>จุดเตือน</th><th>อัปเดตล่าสุด</th>{canMoveStock && <th>รายการ</th>}</tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.name}</strong><span className="table-secondary">{item.unit}</span></td>
          <td className="table-code">{item.sku}</td>
          <td><span className={item.quantity <= item.lowStockThreshold ? 'stock-quantity stock-low' : 'stock-quantity'}>{quantity(item.quantity)} {item.unit}</span></td>
          <td>{quantity(item.lowStockThreshold)} {item.unit}</td>
          <td>{dateTime(item.updatedAt)}</td>
          {canMoveStock && <td><div className="stock-actions"><button title={`รับเข้า ${item.name}`} onClick={() => { setSelected(item); setMode('IN'); setAmount(''); setReason('') }}>รับเข้า</button><button title={`ปรับยอด ${item.name}`} onClick={() => { setSelected(item); setMode('ADJUSTMENT'); setAmount(''); setReason('') }}>ปรับยอด</button></div></td>}
        </tr>)}
          {!loading && items.length === 0 && <tr><td colSpan={canMoveStock ? 6 : 5} className="admin-empty">ยังไม่มีรายการสต็อก</td></tr>}
        </tbody>
      </table></div>
    </section>
    {selected && <section className="admin-action-panel">
      <div className="admin-section-heading"><div><p className="admin-eyebrow">{selected.sku} / {selected.name}</p><h2>{mode === 'IN' ? 'รับวัตถุดิบเข้า' : 'ปรับยอดคงเหลือ'}</h2></div><button type="button" className="admin-icon-button" title="ปิด" aria-label="ปิด" onClick={() => setSelected(null)}>×</button></div>
      <div className="admin-mode-switch" role="group" aria-label="ประเภทการเคลื่อนไหวสต็อก">
        <button type="button" className={mode === 'IN' ? 'selected' : ''} onClick={() => setMode('IN')}>รับเข้า</button>
        <button type="button" className={mode === 'ADJUSTMENT' ? 'selected' : ''} onClick={() => setMode('ADJUSTMENT')}>ปรับยอด</button>
      </div>
      <form className="stock-entry-form" onSubmit={submitChange}>
        <label>{mode === 'IN' ? 'จำนวนที่รับเข้า' : `ผลต่างที่ปรับ (${selected.unit})`}<input autoFocus type="number" step="0.001" min={mode === 'IN' ? '0.001' : undefined} required value={amount} onChange={(event) => setAmount(event.target.value)} placeholder={mode === 'IN' ? 'เช่น 12.5' : 'เช่น -1.25'} /></label>
        <label>เหตุผล<input maxLength={255} required value={reason} onChange={(event) => setReason(event.target.value)} placeholder={mode === 'IN' ? 'รับสินค้าจากผู้ขาย' : 'ปรับยอดตรวจนับหรือของเสีย'} /></label>
        <button className="admin-primary-button" disabled={saving}>{saving ? 'กำลังบันทึก...' : 'บันทึกรายการ'}</button>
      </form>
    </section>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>ประวัติการเคลื่อนไหว</h2><span>รายการล่าสุดก่อน</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>เวลา</th><th>วัตถุดิบ</th><th>ประเภท</th><th>ผลต่าง</th><th>คงเหลือหลังทำรายการ</th><th>เหตุผล / ผู้ทำรายการ</th></tr></thead>
        <tbody>{history.map((entry) => <tr key={entry.id}>
          <td>{dateTime(entry.createdAt)}</td><td><strong>{entry.itemName}</strong></td>
          <td><span className={`movement-tag movement-${entry.transactionType.toLowerCase()}`}>{entry.transactionType === 'IN' ? 'รับเข้า' : 'ปรับยอด'}</span></td>
          <td className={entry.quantityDelta < 0 ? 'delta-negative' : 'delta-positive'}>{entry.quantityDelta > 0 ? '+' : ''}{quantity(entry.quantityDelta)}</td>
          <td>{quantity(entry.balanceAfter)}</td><td><strong>{entry.reason}</strong><span className="table-secondary">โดย {entry.actorUsername}</span></td>
        </tr>)}
          {!loading && history.length === 0 && <tr><td colSpan={6} className="admin-empty">ยังไม่มีประวัติการเคลื่อนไหว</td></tr>}
        </tbody>
      </table></div>
    </section>
  </section>
}