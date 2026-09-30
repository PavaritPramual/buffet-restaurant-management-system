import { Navigate, Route, Routes } from 'react-router-dom'
import DesignSystemPage from './DesignSystemPage'
import AdminShell from './features/admin/AdminShell'
import StockPage from './features/admin/StockPage'
import UsersPage from './features/admin/UsersPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'

export default function FoundationApp() {
  return <Routes>
    <Route path="/" element={<DesignSystemPage />} />
    <Route path="/customer/sessions/:sessionId" element={<CustomerOrderingPage />} />
    <Route path="/admin" element={<AdminShell />}>
      <Route index element={<Navigate to="stock" replace />} />
      <Route path="stock" element={<StockPage />} />
      <Route path="users" element={<UsersPage />} />
      <Route path="menu" element={<MenuAdminPage />} />
    </Route>
    <Route path="*" element={<DesignSystemPage />} />
  </Routes>
}
