import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, NavLink, Outlet, useOutletContext } from 'react-router-dom'
import { authApi, getErrorMessage, roleLabels } from './api'
import type { UserContext } from './api'
import './admin.css'

export default function AdminShell() {
  const [user, setUser] = useState<UserContext | null>(null)
  const [checking, setChecking] = useState(true)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [logoutError, setLogoutError] = useState('')
  const [loggingOut, setLoggingOut] = useState(false)

  useEffect(() => {
    authApi.current()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setChecking(false))
  }, [])

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      setUser(await authApi.login(username, password))
      setPassword('')
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    setLoggingOut(true)
    setLogoutError('')
    try {
      await authApi.logout()
      setUser(null)
    } catch (requestError) {
      setLogoutError(getErrorMessage(requestError))
    } finally {
      setLoggingOut(false)
    }
  }

  if (checking) return <div className="admin-loading">กำลังตรวจสอบการเข้าสู่ระบบ...</div>

  if (!user) {
    return <main className="admin-login-wrap">
      <section className="admin-login">
        <div className="admin-mark">BR<span> / </span>OPS</div>
        <p className="admin-eyebrow">บุฟเฟต์เรสเตอรองต์ / สำหรับพนักงาน</p>
        <h1>เข้าสู่ระบบ</h1>
        <form className="admin-form" onSubmit={handleLogin}>
          <label>ชื่อผู้ใช้<input autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} /></label>
          <label>รหัสผ่าน<input autoComplete="current-password" type="password" required value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          {error && <p className="admin-error" role="alert">{error}</p>}
          <button className="admin-primary-button" disabled={submitting}>{submitting ? 'กำลังเข้าสู่ระบบ...' : 'เข้าสู่ระบบ'}</button>
        </form>
      </section>
    </main>
  }

  if (user.role === 'SERVICE_STAFF') return <Navigate to="/staff/tables" replace />
  if (user.role === 'KITCHEN_STAFF') return <Navigate to="/kitchen" replace />

  return <div className="admin-shell">
    <aside className="admin-sidebar">
      <div className="admin-brand"><span className="admin-brand-mark">BR</span><span>BUFFET<br /><b>ระบบจัดการร้าน</b></span></div>
      <p className="admin-eyebrow">เมนูหลัก</p>
      <nav className="admin-nav" aria-label="เมนูผู้ดูแล">
        {(user.role === 'MANAGER' || user.role === 'SUPERVISOR') && <NavLink to="/admin/stock">สต็อก</NavLink>}
        {user.role === 'MANAGER' && <><NavLink to="/admin/tables">โต๊ะ</NavLink><NavLink to="/admin/packages">แพ็กเกจ</NavLink><NavLink to="/admin/soups">น้ำซุป</NavLink><NavLink to="/admin/stock-items">รายการสต็อก</NavLink></>}
        {user.role === 'MANAGER' && <NavLink to="/admin/users">พนักงาน</NavLink>}
        {user.role === 'MANAGER' && <NavLink to="/admin/menu">เมนูอาหาร</NavLink>}
      </nav>
      {logoutError && <p className="admin-error" role="alert">ออกจากระบบไม่สำเร็จ: {logoutError}</p>}
      <div className="admin-sidebar-user">
        <div className="admin-avatar">{user.displayName.slice(0, 1).toUpperCase()}</div>
        <div><strong>{user.displayName}</strong><span>{roleLabels[user.role]}</span></div>
        <button className="admin-logout" title="ออกจากระบบ" aria-label="ออกจากระบบ" disabled={loggingOut} onClick={handleLogout}>↗</button>
      </div>
    </aside>
    <main className="admin-main">
      <header className="admin-topbar"><span>ระบบจัดการร้านอาหาร</span><span>{user.username}</span></header>
      <Outlet context={user} />
    </main>
  </div>
}

export function ManagerRoute() {
  const user = useOutletContext<UserContext>()
  return user.role === 'MANAGER' ? <Outlet context={user} /> : <Navigate to="/admin/stock" replace />
}
