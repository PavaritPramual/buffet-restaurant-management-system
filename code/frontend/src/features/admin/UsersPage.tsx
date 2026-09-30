import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, usersApi } from './api'
import type { UserRecord, UserRole } from './api'

const roles: UserRole[] = ['SERVICE_STAFF', 'KITCHEN_STAFF', 'SUPERVISOR', 'MANAGER']

export default function UsersPage() {
  const [users, setUsers] = useState<UserRecord[]>([])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<UserRole>('SERVICE_STAFF')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  async function loadUsers() {
    setLoading(true)
    try {
      setUsers(await usersApi.list())
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void loadUsers() }, [])

  async function createUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      await usersApi.create({ username, password, displayName, email, role })
      setUsername('')
      setPassword('')
      setDisplayName('')
      setEmail('')
      setRole('SERVICE_STAFF')
      await loadUsers()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading"><div><p className="admin-eyebrow">People / Access</p><h1>Users</h1><p className="admin-subtitle">Staff accounts and their assigned operating role.</p></div><span className="admin-count">{users.length} accounts</span></div>
    {error && <p className="admin-error" role="alert">{error}</p>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>Staff directory</h2><span>{loading ? 'Loading...' : `${users.length} users`}</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>Name</th><th>Username</th><th>Email</th><th>Role</th></tr></thead>
        <tbody>{users.map((user) => <tr key={user.id}><td><strong>{user.displayName}</strong></td><td className="table-code">{user.username}</td><td>{user.email || '—'}</td><td><span className="role-tag">{user.role.replaceAll('_', ' ')}</span></td></tr>)}
          {!loading && users.length === 0 && <tr><td colSpan={4} className="admin-empty">No staff accounts found.</td></tr>}
        </tbody>
      </table></div>
    </section>
    <section className="admin-section admin-create-user">
      <div className="admin-section-heading"><div><p className="admin-eyebrow">New account</p><h2>Add staff member</h2></div></div>
      <form className="user-create-form" onSubmit={createUser}>
        <label>Display name<input required maxLength={120} value={displayName} onChange={(event) => setDisplayName(event.target.value)} /></label>
        <label>Username<input required maxLength={80} autoComplete="off" value={username} onChange={(event) => setUsername(event.target.value)} /></label>
        <label>Temporary password<input required type="password" minLength={8} maxLength={72} autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>Email<input type="email" maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>Role<select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>{roles.map((option) => <option key={option} value={option}>{option.replaceAll('_', ' ')}</option>)}</select></label>
        <button className="admin-primary-button" disabled={saving}>{saving ? 'Creating...' : 'Create user'}</button>
      </form>
    </section>
  </section>
}