import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, stockApi } from './api'
import type { StockItem, StockTransaction } from './api'

type ActionMode = 'IN' | 'ADJUSTMENT'

function quantity(value: number) {
  return new Intl.NumberFormat('en', { maximumFractionDigits: 3 }).format(value)
}

function dateTime(value: string) {
  return new Intl.DateTimeFormat('en', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export default function StockPage() {
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
      <div><p className="admin-eyebrow">Inventory / Control</p><h1>Stock overview</h1><p className="admin-subtitle">On-hand quantities and traceable stock movements.</p></div>
      <button className="admin-secondary-button" onClick={() => void loadData()} disabled={loading}>Refresh</button>
    </div>
    <div className="admin-metrics">
      <div><span>Tracked items</span><strong>{items.length}</strong></div>
      <div><span>Total quantity</span><strong>{quantity(totalUnits)} <small>units</small></strong></div>
      <div className={lowCount > 0 ? 'metric-warning' : ''}><span>At or below threshold</span><strong>{lowCount}</strong></div>
    </div>
    {error && <p className="admin-error" role="alert">{error}</p>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>Inventory</h2><span>{loading ? 'Loading...' : `${items.length} items`}</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>Item</th><th>SKU</th><th>On hand</th><th>Reorder at</th><th>Updated</th><th>Movement</th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.name}</strong><span className="table-secondary">{item.unit}</span></td>
          <td className="table-code">{item.sku}</td>
          <td><span className={item.quantity <= item.lowStockThreshold ? 'stock-quantity stock-low' : 'stock-quantity'}>{quantity(item.quantity)} {item.unit}</span></td>
          <td>{quantity(item.lowStockThreshold)} {item.unit}</td>
          <td>{dateTime(item.updatedAt)}</td>
          <td><div className="stock-actions"><button title={`Receive ${item.name}`} onClick={() => { setSelected(item); setMode('IN'); setAmount(''); setReason('') }}>Stock in</button><button title={`Adjust ${item.name}`} onClick={() => { setSelected(item); setMode('ADJUSTMENT'); setAmount(''); setReason('') }}>Adjust</button></div></td>
        </tr>)}
          {!loading && items.length === 0 && <tr><td colSpan={6} className="admin-empty">No stock items are configured.</td></tr>}
        </tbody>
      </table></div>
    </section>
    {selected && <section className="admin-action-panel">
      <div className="admin-section-heading"><div><p className="admin-eyebrow">{selected.sku} / {selected.name}</p><h2>{mode === 'IN' ? 'Receive stock' : 'Adjust balance'}</h2></div><button className="admin-icon-button" title="Close" aria-label="Close" onClick={() => setSelected(null)}>×</button></div>
      <div className="admin-mode-switch" role="group" aria-label="Stock movement type">
        <button className={mode === 'IN' ? 'selected' : ''} onClick={() => setMode('IN')}>Stock in</button>
        <button className={mode === 'ADJUSTMENT' ? 'selected' : ''} onClick={() => setMode('ADJUSTMENT')}>Adjustment</button>
      </div>
      <form className="stock-entry-form" onSubmit={submitChange}>
        <label>{mode === 'IN' ? 'Quantity received' : `Quantity change (${selected.unit})`}<input autoFocus type="number" step="0.001" min={mode === 'IN' ? '0.001' : undefined} required value={amount} onChange={(event) => setAmount(event.target.value)} placeholder={mode === 'IN' ? 'e.g. 12.5' : 'e.g. -1.25'} /></label>
        <label>Reason<input maxLength={255} required value={reason} onChange={(event) => setReason(event.target.value)} placeholder={mode === 'IN' ? 'Supplier delivery...' : 'Count correction, spoilage...'} /></label>
        <button className="admin-primary-button" disabled={saving}>{saving ? 'Saving...' : 'Save movement'}</button>
      </form>
    </section>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>Movement history</h2><span>Newest first</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>Time</th><th>Item</th><th>Type</th><th>Change</th><th>Balance after</th><th>Reason / staff</th></tr></thead>
        <tbody>{history.map((entry) => <tr key={entry.id}>
          <td>{dateTime(entry.createdAt)}</td><td><strong>{entry.itemName}</strong></td>
          <td><span className={`movement-tag movement-${entry.transactionType.toLowerCase()}`}>{entry.transactionType}</span></td>
          <td className={entry.quantityDelta < 0 ? 'delta-negative' : 'delta-positive'}>{entry.quantityDelta > 0 ? '+' : ''}{quantity(entry.quantityDelta)}</td>
          <td>{quantity(entry.balanceAfter)}</td><td><strong>{entry.reason}</strong><span className="table-secondary">by {entry.actorUsername}</span></td>
        </tr>)}
          {!loading && history.length === 0 && <tr><td colSpan={6} className="admin-empty">No stock movements recorded.</td></tr>}
        </tbody>
      </table></div>
    </section>
  </section>
}