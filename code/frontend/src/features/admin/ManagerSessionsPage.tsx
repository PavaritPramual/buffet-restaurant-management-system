import { useCallback, useEffect, useRef, useState } from 'react'
import { Button, Card, DataTable, EmptyState, ErrorAlert, LoadingState, PageHeader } from '../../components/common'
import { getErrorMessage } from './api'
import { forceCloseSession, getManagedSessions, getManagerOperations } from './manager-operations-api'
import type { ManagedSession, ManagerOperation } from './manager-operations-api'
import ForceActionDialog from './ForceActionDialog'
import './manager-operations.css'

export default function ManagerSessionsPage() {
  const [sessions, setSessions] = useState<ManagedSession[]>([])
  const [history, setHistory] = useState<ManagerOperation[]>([])
  const [selected, setSelected] = useState<ManagedSession | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [actionError, setActionError] = useState('')
  const [notice, setNotice] = useState('')
  const revision = useRef(0)
  const invalidate = useCallback(() => { ++revision.current }, [])
  const load = useCallback(async () => {
    const request = ++revision.current
    setLoading(true); setLoadError('')
    try {
      const [nextSessions, nextHistory] = await Promise.all([getManagedSessions(), getManagerOperations()])
      if (request === revision.current) { setSessions(nextSessions); setHistory(nextHistory) }
    } catch (cause) { if (request === revision.current) setLoadError(getErrorMessage(cause)) }
    finally { if (request === revision.current) setLoading(false) }
  }, [])
  // Synchronize the initial server snapshot; revisions discard requests after unmount.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { void load(); return invalidate }, [load, invalidate])

  async function close(reason: string) {
    if (!selected || busy) return
    setBusy(true); setActionError(''); setNotice('')
    try {
      await forceCloseSession(selected.sessionId, reason)
      setSelected(null); setNotice(`บังคับปิดโต๊ะ ${selected.tableNumber} แล้ว โต๊ะพร้อมเปิดรอบใหม่`)
      await load()
    } catch (cause) { setActionError(getErrorMessage(cause)) }
    finally { setBusy(false) }
  }
  return <div className="admin-page manager-operations-page">
    <PageHeader eyebrow="ผู้จัดการ" title="จัดการรอบกิน" description="บังคับปิดรอบที่ค้าง พร้อมเก็บเหตุผลและประวัติการดำเนินการ" action={<Button variant="secondary" disabled={loading || busy} onClick={() => void load()}>โหลดข้อมูลใหม่</Button>} />
    {loadError && <ErrorAlert message={loadError} />}
    {notice && <p role="status">{notice}</p>}
    {loading ? <LoadingState /> : <>
      <h2>รอบกินที่ยังเปิดอยู่</h2>
      {!sessions.length ? <EmptyState title="ไม่มีรอบกินที่เปิดอยู่" /> : <div className="manager-session-cards">{sessions.map((session) => <Card key={session.sessionId}>
        <h3>โต๊ะ {session.tableNumber}</h3>
        <p>รอบ #{session.sessionId} · ผู้ใหญ่ {session.adultCount} / เด็ก {session.childCount}</p>
        <p>เปิดเมื่อ {new Date(session.startTime).toLocaleString('th-TH')}</p>
        <Button variant="danger" aria-label={`บังคับปิดโต๊ะ ${session.tableNumber}`} disabled={busy} onClick={() => { setActionError(''); setSelected(session) }}>บังคับปิดโต๊ะ</Button>
      </Card>)}</div>}
      <Card><h2>ประวัติการบังคับจัดการล่าสุด</h2><p>แสดง 50 รายการล่าสุด ทั้งการปิดโต๊ะและลบเมนู</p>
        {!history.length ? <EmptyState title="ยังไม่มีประวัติการบังคับจัดการ" /> : <DataTable headers={['เวลา', 'การดำเนินการ', 'รายการ', 'เหตุผล', 'ผู้ดำเนินการ']}>{history.map((entry) => <tr key={entry.id}>
          <td>{new Date(entry.createdAt).toLocaleString('th-TH')}</td><td>{entry.action === 'FORCE_CLOSE_SESSION' ? 'บังคับปิดโต๊ะ' : 'บังคับลบเมนู'}</td>
          <td>{entry.resourceLabel} (#{entry.resourceId})</td><td>{entry.reason}</td><td>{entry.actorUsername}</td>
        </tr>)}</DataTable>}
      </Card>
    </>}
    {selected && <ForceActionDialog key={selected.sessionId} title={`บังคับปิดโต๊ะ ${selected.tableNumber}`} description="จะยุติรอบกินทันที แม้ยังไม่ชำระเงิน โต๊ะจะว่างและ QR เดิมใช้ต่อไม่ได้ ออเดอร์จะหยุดแสดงในงานครัวและงานเสิร์ฟ โดยเก็บประวัติและยอดชำระเดิมไว้ การปิดนี้ไม่ถือว่าได้รับเงินแล้ว" confirmLabel="ยืนยันบังคับปิดโต๊ะ" busy={busy} error={actionError} onConfirm={close} onCancel={() => setSelected(null)} />}
  </div>
}
