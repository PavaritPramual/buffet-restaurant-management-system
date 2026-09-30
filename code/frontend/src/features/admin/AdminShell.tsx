import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { authApi, getErrorMessage } from './api'
import type { UserContext } from './api'
import './admin.css'

export default function AdminShell() {
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

  if (checking) return <div className="admin-loading">Connecting to the staff desk...</div>

  if (!user) {
    return <main className="admin-login-wrap">
      <section className="admin-login">
        <div className="admin-mark">BR<span> / </span>OPS</div>
        <p className="admin-eyebrow">Buffet Restaurant / Staff access</p>
        <h1>Sign in</h1>
        <form className="admin-form" onSubmit={handleLogin}>
          <label>Username<input autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} /></label>
          <label>Password<input autoComplete="current-password" type="password" required value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          {error && <p className="admin-error" role="alert">{error}</p>}
          <button className="admin-primary-button" disabled={submitting}>{submitting ? 'Signing in...' : 'Sign in'}</button>
        </form>
      </section>
    </main>
  }

  return <div className="admin-shell">
    <aside className="admin-sidebar">
      <div className="admin-brand"><span className="admin-brand-mark">BR</span><span>BUFFET<br /><b>OPERATIONS</b></span></div>
      <p className="admin-eyebrow">Workspace</p>
      <nav className="admin-nav" aria-label="Admin navigation">
        <NavLink to="/admin/stock">Stock</NavLink>
        <NavLink to="/admin/users">Users</NavLink>
        <NavLink to="/admin/menu">Menu catalog</NavLink>
      </nav>
      <div className="admin-sidebar-user">
        <div className="admin-avatar">{user.displayName.slice(0, 1).toUpperCase()}</div>
        <div><strong>{user.displayName}</strong><span>{user.role.replaceAll('_', ' ')}</span></div>
        <button className="admin-logout" title="Sign out" aria-label="Sign out" onClick={handleLogout}>↗</button>
      </div>
    </aside>
    <main className="admin-main">
      <header className="admin-topbar"><span>Restaurant management</span><span>{user.username}</span></header>
      <Outlet />
    </main>
  </div>
}