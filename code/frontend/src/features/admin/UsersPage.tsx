import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage, usersApi } from './api'
import { roleLabels } from './api'
import type { UserRecord, UserRole, UserContext } from './api'
import { useOutletContext } from 'react-router-dom'

const roles: UserRole[] = ['SERVICE_STAFF', 'KITCHEN_STAFF', 'SUPERVISOR', 'MANAGER']

export default function UsersPage() {
  useOutletContext<UserContext>()
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
      await usersApi.create({ username, password, displayName, email: email.trim() || null, role })
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
    <div className="admin-page-heading"><div><p className="admin-eyebrow">พนักงาน / สิทธิ์ใช้งาน</p><h1>พนักงาน</h1><p className="admin-subtitle">บัญชีพนักงานและบทบาทการทำงาน</p></div><span className="admin-count">{users.length} บัญชี</span></div>
    {error && <p className="admin-error" role="alert">{error}</p>}
    <section className="admin-section">
      <div className="admin-section-heading"><h2>รายชื่อพนักงาน</h2><span>{loading ? 'กำลังโหลด...' : `${users.length} คน`}</span></div>
      <div className="admin-table-wrap"><table className="admin-table">
        <thead><tr><th>ชื่อที่แสดง</th><th>ชื่อผู้ใช้</th><th>อีเมล</th><th>บทบาท</th></tr></thead>
        <tbody>{users.map((user) => <tr key={user.id}><td><strong>{user.displayName}</strong></td><td className="table-code">{user.username}</td><td>{user.email || '—'}</td><td><span className="role-tag">{roleLabels[user.role]}</span></td></tr>)}
          {!loading && users.length === 0 && <tr><td colSpan={4} className="admin-empty">ไม่พบข้อมูลพนักงาน</td></tr>}
        </tbody>
      </table></div>
    </section>
    <section className="admin-section admin-create-user">
      <div className="admin-section-heading"><div><p className="admin-eyebrow">บัญชีใหม่</p><h2>เพิ่มพนักงาน</h2></div></div>
      <form className="user-create-form" onSubmit={createUser}>
        <label>ชื่อที่แสดง<input required maxLength={120} value={displayName} onChange={(event) => setDisplayName(event.target.value)} /></label>
        <label>ชื่อผู้ใช้<input required maxLength={80} autoComplete="off" value={username} onChange={(event) => setUsername(event.target.value)} /></label>
        <label>รหัสผ่านเริ่มต้น<input required type="password" minLength={8} maxLength={72} autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
        <label>อีเมล<input type="email" maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} /></label>
        <label>บทบาท<select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>{roles.map((option) => <option key={option} value={option}>{roleLabels[option]}</option>)}</select></label>
        <button className="admin-primary-button" disabled={saving}>{saving ? 'กำลังสร้าง...' : 'สร้างบัญชี'}</button>
      </form>
    </section>
  </section>
}