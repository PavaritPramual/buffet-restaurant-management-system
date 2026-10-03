import { useEffect, useState } from 'react'
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { authApi, getErrorMessage, roleLabels } from '../admin/api'
import type { UserContext } from '../admin/api'
import './staff-shell.css'

export default function StaffShell() {
  const location = useLocation()
  const navigate = useNavigate()
  const [user, setUser] = useState<UserContext | null>(null)
  const [checking, setChecking] = useState(true)
  const [loggingOut, setLoggingOut] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    const handleSessionExpired = () => {
      setUser(null)
      navigate('/admin', { replace: true })
    }
    window.addEventListener('auth:session-expired', handleSessionExpired)
    authApi.current()
      .then((currentUser) => { if (active) setUser(currentUser) })
      .catch(() => { if (active) navigate('/admin', { replace: true }) })
      .finally(() => { if (active) setChecking(false) })
    return () => {
      active = false
      window.removeEventListener('auth:session-expired', handleSessionExpired)
    }
  }, [navigate])

  async function handleLogout() {
    setLoggingOut(true)
    setError('')
    try {
      await authApi.logout()
      setUser(null)
      navigate('/admin', { replace: true })
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoggingOut(false)
    }
  }

  if (checking) return <div className="admin-loading">กำลังตรวจสอบการเข้าสู่ระบบ...</div>
  if (!user) return null

  if (user.role !== 'SERVICE_STAFF' && user.role !== 'KITCHEN_STAFF') {
    return <Navigate to="/admin/stock" replace />
  }

  const isKitchenRoute = location.pathname === '/kitchen'
  if (isKitchenRoute && user.role !== 'KITCHEN_STAFF') return <Navigate to="/staff/tables" replace />
  if (!isKitchenRoute && user.role === 'KITCHEN_STAFF') return <Navigate to="/kitchen" replace />

  return <div className="staff-shell">
    <header className="staff-shell-header">
      <div className="staff-shell-brand"><span className="admin-brand-mark">BR</span><strong>BUFFET <span>ระบบจัดการร้าน</span></strong></div>
      <div className="staff-shell-user">
        <div><strong>{user.displayName}</strong><span>{roleLabels[user.role]}</span></div>
        <button className="admin-logout" title="ออกจากระบบ" aria-label="ออกจากระบบ" disabled={loggingOut} onClick={() => void handleLogout()}>↗</button>
      </div>
    </header>
    {error && <p className="admin-error staff-shell-error" role="alert">ออกจากระบบไม่สำเร็จ: {error}</p>}
    <Outlet />
  </div>
}