import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, NavLink, Outlet, useLocation } from 'react-router-dom'
import { authApi, getErrorMessage, roleLabels } from './api'
import type { UserContext } from './api'
import './admin.css'

export default function AdminShell() {
  const location = useLocation()
  const [user, setUser] = useState<UserContext | null>(null)
  const [checking, setChecking] = useState(true)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

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
    await authApi.logout().catch(() => undefined)
    setUser(null)
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

  if (user.role !== 'MANAGER' && ['/admin/users', '/admin/menu'].includes(location.pathname)) {
    return <Navigate to="/admin/stock" replace />
  }

  return <div className="admin-shell">
    <aside className="admin-sidebar">
      <div className="admin-brand"><span className="admin-brand-mark">BR</span><span>BUFFET<br /><b>ระบบจัดการร้าน</b></span></div>
      <p className="admin-eyebrow">เมนูหลัก</p>
      <nav className="admin-nav" aria-label="เมนูผู้ดูแล">
        <NavLink to="/admin/stock">สต็อก</NavLink>
        {user.role === 'MANAGER' && <NavLink to="/admin/users">พนักงาน</NavLink>}
        {user.role === 'MANAGER' && <NavLink to="/admin/menu">เมนูอาหาร</NavLink>}
      </nav>
      <div className="admin-sidebar-user">
        <div className="admin-avatar">{user.displayName.slice(0, 1).toUpperCase()}</div>
        <div><strong>{user.displayName}</strong><span>{roleLabels[user.role]}</span></div>
        <button className="admin-logout" title="ออกจากระบบ" aria-label="ออกจากระบบ" onClick={handleLogout}>↗</button>
      </div>
    </aside>
    <main className="admin-main">
      <header className="admin-topbar"><span>ระบบจัดการร้านอาหาร</span><span>{user.username}</span></header>
      <Outlet context={user} />
    </main>
  </div>
}