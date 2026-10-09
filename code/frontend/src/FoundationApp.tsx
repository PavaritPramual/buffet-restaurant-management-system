
import BillingPreviewPage from './BillingPreviewPage'
import { Navigate, Route, Routes } from 'react-router-dom'
import DesignSystemPage from './DesignSystemPage'
import AdminShell, { ManagerRoute } from './features/admin/AdminShell'
import StockPage from './features/admin/StockPage'
import MasterDataPage from './features/admin/MasterDataPage'
import UsersPage from './features/admin/UsersPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'
import StaffTablesPage from './features/staff-tables/StaffTablesPage'
import DiningSessionPage from './features/staff-tables/DiningSessionPage'
import KitchenBoardPage from './features/fulfillment/KitchenBoardPage'
import StaffServingPage from './features/fulfillment/StaffServingPage'
import StaffShell from './features/auth/StaffShell'

export default function FoundationApp() {

  return <Routes>
    <Route path="/" element={<Navigate to="/admin" replace />} />
    {import.meta.env.DEV && <Route path="/dev/ui" element={<DesignSystemPage />} />}
    <Route path="/customer/qr" element={<CustomerOrderingPage />} />
    <Route element={<StaffShell />}>
      <Route path="/staff/tables" element={<StaffTablesPage />} />
      <Route path="/staff/sessions/:sessionId" element={<DiningSessionPage />} />
      <Route path="/kitchen" element={<KitchenBoardPage />} />
      <Route path="/staff/serving" element={<StaffServingPage />} />
      <Route path="/billing/preview" element={<BillingPreviewPage />} />
      <Route path="/staff/sessions/:sessionId/billing" element={<BillingPreviewPage />} />
    </Route>
    <Route path="/admin" element={<AdminShell />}>
      <Route index element={<Navigate to="stock" replace />} />
      <Route path="stock" element={<StockPage />} />
      <Route element={<ManagerRoute />}>
        <Route path="tables" element={<MasterDataPage key="tables" kind="tables" />} />
        <Route path="packages" element={<MasterDataPage key="packages" kind="buffet-packages" />} />
        <Route path="soups" element={<MasterDataPage key="soups" kind="soups" />} />
        <Route path="stock-items" element={<MasterDataPage key="stock" kind="stock" />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="menu" element={<MenuAdminPage />} />
      </Route>
    </Route>
    <Route path="*" element={<Navigate to="/admin" replace />} />
  </Routes>

}
