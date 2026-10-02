import { Navigate, Route, Routes } from 'react-router-dom'
import DesignSystemPage from './DesignSystemPage'
import AdminShell from './features/admin/AdminShell'
import StockPage from './features/admin/StockPage'
import UsersPage from './features/admin/UsersPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'
import StaffTablesPage from './features/staff/StaffTablesPage'
import DiningSessionPage from './features/staff/DiningSessionPage'
import KitchenBoardPage from './features/kitchen/KitchenBoardPage'
import StaffServingPage from './features/staff/StaffServingPage'

export default function FoundationApp() {
  return <Routes>
    <Route path="/" element={<DesignSystemPage />} />
    <Route path="/customer/qr" element={<CustomerOrderingPage />} />
    <Route path="/staff/tables" element={<StaffTablesPage />} />
    <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
    <Route path="/admin" element={<AdminShell />}>
      <Route index element={<Navigate to="stock" replace />} />
      <Route path="stock" element={<StockPage />} />
      <Route path="users" element={<UsersPage />} />
      <Route path="menu" element={<MenuAdminPage />} />
    </Route>
    <Route path="/admin/menu" element={<MenuAdminPage />} />
    <Route path="/kitchen" element={<KitchenBoardPage />} />
    <Route path="/staff/serving" element={<StaffServingPage />} />
    <Route path="*" element={<DesignSystemPage />} />
  </Routes>
}
