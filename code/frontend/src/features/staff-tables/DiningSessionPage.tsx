import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { QRCodeSVG } from 'qrcode.react'
import { Button, Card, ConfirmDialog, ErrorAlert, LoadingState, PageHeader, StatusBadge } from '../../components/common'
import { getApiError, getDiningSession, closeDiningSession } from './api'
import type { SessionContext } from '../ordering/api'
import './staff-tables.css'

export default function DiningSessionPage() {
  const { sessionId: rawId = '' } = useParams()
  const sessionId = Number(rawId)
  const invalidSessionId = !Number.isSafeInteger(sessionId) || sessionId < 1
  const navigate = useNavigate()
  const [session, setSession] = useState<SessionContext | null>(null)
  const [loading, setLoading] = useState(!invalidSessionId)
  const [error, setError] = useState('')
  const [confirming, setConfirming] = useState(false)
  const [closing, setClosing] = useState(false)

  useEffect(() => {
    if (invalidSessionId) return
    let active = true
    getDiningSession(sessionId)
      .then((result) => { if (active) setSession(result) })
      .catch((cause) => { if (active) setError(getApiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [sessionId, invalidSessionId])

  async function closeSession() {
    if (closing) return
    setClosing(true); setError('')
    try {
      await closeDiningSession(sessionId)
      navigate('/staff/tables', { replace: true })
    } catch (cause) { setError(getApiError(cause)); setConfirming(false) } finally { setClosing(false) }
  }

  const customerUrl = session?.sessionStatus === 'ACTIVE'
    ? new URL(`/customer/qr/${encodeURIComponent(session.sessionToken)}`, window.location.origin).toString()
    : ''

  return <main className="staff-session-page">
    <PageHeader eyebrow="พนักงานบริการ" title="รายละเอียดรอบกิน" description={session ? `โต๊ะ ${session.tableNumber}` : `รอบกิน #${rawId}`} action={<Button variant="secondary" onClick={() => navigate('/staff/tables')}>กลับไปหน้าโต๊ะ</Button>} />
    {(error || invalidSessionId) && <ErrorAlert message={error || 'รอบกินไม่ถูกต้อง'} />}
    {loading ? <LoadingState label="กำลังโหลดรายละเอียดรอบกิน…" /> : session && <>
      <section className="staff-session-grid">
        <Card className="staff-session-summary">
          <div className="staff-table-card-heading"><div><small>โต๊ะ</small><h2>{session.tableNumber}</h2></div><StatusBadge tone={session.sessionStatus === 'ACTIVE' ? 'info' : 'neutral'}>{session.sessionStatus === 'ACTIVE' ? 'กำลังใช้งาน' : 'ปิดรอบแล้ว'}</StatusBadge></div>
          <dl><div><dt>ผู้ใหญ่</dt><dd>{session.adultCount} คน</dd></div><div><dt>เด็ก</dt><dd>{session.childCount} คน</dd></div><div><dt>เริ่มรอบ</dt><dd>{new Date(session.startTime).toLocaleString('th-TH')}</dd></div></dl>
          <p>แพ็กเกจ #{session.packageId} · น้ำซุป #{session.soupId}</p>
          {session.sessionStatus === 'ACTIVE' && <Button variant="danger" onClick={() => setConfirming(true)}>ปิดรอบกิน</Button>}
        </Card>
        <Card className="staff-session-qr-card">
          <h2>QR สำหรับลูกค้า</h2>
          {customerUrl ? <>
            <p>ให้ลูกค้าสแกนเพื่อยืนยันรอบกินและดูเมนู</p>
            <QRCodeSVG value={customerUrl} size={220} level="M" title={`QR โต๊ะ ${session.tableNumber}`} />
            <a href={customerUrl}>{customerUrl}</a>
          </> : <p>รอบกินนี้ปิดแล้ว QR จึงใช้ไม่ได้</p>}
        </Card>
      </section>
    </>}
    <ConfirmDialog open={confirming} title="ยืนยันปิดรอบกิน" description="ระบบจะปิดรอบได้เมื่อยืนยันการชำระเงินแล้วเท่านั้น" busy={closing} onCancel={() => setConfirming(false)} onConfirm={() => void closeSession()} />
  </main>
}
