import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, EmptyState, ErrorAlert, LoadingState, PageHeader, SelectField, StatusBadge, TextField } from '../../components/common'
import { getActiveSessions, getApiError, getPackages, getSoups, getTables, openDiningSession } from './api'
import type { CatalogOption, OpenSessionInput, RestaurantTable } from './api'
import type { SessionContext } from '../ordering/api'
import './staff-tables.css'

const labels = { AVAILABLE: 'ว่าง', OCCUPIED: 'กำลังใช้งาน' }

export default function StaffTablesPage() {
  const navigate = useNavigate()
  const [tables, setTables] = useState<RestaurantTable[]>([])
  const [sessions, setSessions] = useState<SessionContext[]>([])
  const [packages, setPackages] = useState<CatalogOption[]>([])
  const [soups, setSoups] = useState<CatalogOption[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedTable, setSelectedTable] = useState('')
  const [adultCount, setAdultCount] = useState('1')
  const [childCount, setChildCount] = useState('0')
  const [packageId, setPackageId] = useState('')
  const [soupId, setSoupId] = useState('')
  const [saving, setSaving] = useState(false)

  function applyData([nextTables, nextSessions, nextPackages, nextSoups]: [RestaurantTable[], SessionContext[], CatalogOption[], CatalogOption[]]) {
      setTables(nextTables); setSessions(nextSessions); setPackages(nextPackages); setSoups(nextSoups)
      setPackageId((current) => current || String(nextPackages[0]?.id ?? ''))
      setSoupId((current) => current || String(nextSoups[0]?.id ?? ''))
  }

  useEffect(() => {
    let active = true
    Promise.all([getTables(), getActiveSessions(), getPackages(), getSoups()])
      .then((data) => { if (active) applyData(data) })
      .catch((cause) => { if (active) setError(getApiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  async function refresh() {
    setLoading(true); setError('')
    try { applyData(await Promise.all([getTables(), getActiveSessions(), getPackages(), getSoups()])) }
    catch (cause) { setError(getApiError(cause)) }
    finally { setLoading(false) }
  }
  const activeByTable = useMemo(() => new Map(sessions.map((session) => [session.tableId, session])), [sessions])

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (saving) return
    const input: OpenSessionInput = {
      tableId: Number(selectedTable), packageId: Number(packageId), soupId: Number(soupId),
      adultCount: Number(adultCount), childCount: Number(childCount),
    }
    setSaving(true); setError('')
    try {
      const session = await openDiningSession(input)
      navigate(`/staff/sessions/${session.sessionId}`)
    } catch (cause) { setError(getApiError(cause)) } finally { setSaving(false) }
  }

  return <main className="staff-tables-page">
    <PageHeader eyebrow="พนักงานบริการ" title="โต๊ะในร้าน" description="เลือกโต๊ะเพื่อเปิดรอบกิน หรือดูรอบที่กำลังใช้งาน" action={<Button variant="secondary" onClick={() => void refresh()}>อัปเดต</Button>} />
    {error && <ErrorAlert message={error} />}
    {loading ? <LoadingState label="กำลังโหลดโต๊ะและรอบกิน…" /> : tables.length === 0 ? <EmptyState title="ยังไม่มีโต๊ะ" description="เพิ่มโต๊ะผ่านหน้าจัดการโต๊ะก่อนเริ่มบริการ" /> : <section className="staff-table-grid" aria-label="รายการโต๊ะ">
      {tables.map((table) => {
        const session = activeByTable.get(table.id)
        return <Card key={table.id} className="staff-table-card">
          <div className="staff-table-card-heading"><div><small>โต๊ะ</small><h2>{table.tableNumber}</h2></div>
            <StatusBadge tone={table.status === 'AVAILABLE' ? 'success' : 'info'}>{labels[table.status]}</StatusBadge>
          </div>
          <p>รองรับ {table.capacity} คน</p>
          {session && <p className="staff-table-session-meta">ผู้ใหญ่ {session.adultCount} · เด็ก {session.childCount}<br />เริ่ม {new Date(session.startTime).toLocaleTimeString('th-TH', { hour: '2-digit', minute: '2-digit' })}</p>}
          {table.status === 'AVAILABLE'
            ? <Button className="staff-table-action" onClick={() => setSelectedTable(String(table.id))}>เปิดโต๊ะ</Button>
            : session ? <Button className="staff-table-action" variant="secondary" onClick={() => navigate(`/staff/sessions/${session.sessionId}`)}>ดูรอบกิน</Button>
              : <Button className="staff-table-action" variant="secondary" disabled>กำลังตรวจสอบรอบกิน</Button>}
        </Card>
      })}
    </section>}

    {selectedTable && <Card className="staff-open-session-card">
      <div className="staff-table-card-heading"><div><small>เริ่มต้นรอบกิน</small><h2>{tables.find((table) => table.id === Number(selectedTable))?.tableNumber}</h2></div>
        <Button variant="ghost" onClick={() => setSelectedTable('')} disabled={saving}>ยกเลิก</Button>
      </div>
      {(packages.length === 0 || soups.length === 0) && <ErrorAlert message="ต้องมีแพ็กเกจและน้ำซุปที่เปิดใช้งานอย่างน้อยหนึ่งรายการก่อนเปิดโต๊ะ" />}
      <form className="staff-open-session-form" onSubmit={(event) => void submit(event)}>
        <TextField label="จำนวนผู้ใหญ่" type="number" min="0" value={adultCount} onChange={(event) => setAdultCount(event.target.value)} required />
        <TextField label="จำนวนเด็ก" type="number" min="0" value={childCount} onChange={(event) => setChildCount(event.target.value)} required />
        <SelectField label="แพ็กเกจ" value={packageId} onChange={(event) => setPackageId(event.target.value)} required>
          {packages.map((item) => <option key={item.id} value={item.id}>{item.name} · ฿{item.price}</option>)}
        </SelectField>
        <SelectField label="น้ำซุป" value={soupId} onChange={(event) => setSoupId(event.target.value)} required>
          {soups.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
        </SelectField>
        <Button type="submit" loading={saving} disabled={!packages.length || !soups.length}>ยืนยันเปิดโต๊ะ</Button>
      </form>
    </Card>}
  </main>
}
