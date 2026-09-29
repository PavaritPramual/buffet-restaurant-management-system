import { Route, Routes } from 'react-router-dom'
import BillingPreviewPage from './BillingPreviewPage'
import DesignSystemPage from './DesignSystemPage'
import CustomerOrderingPage from './features/ordering/CustomerOrderingPage'
import MenuAdminPage from './features/ordering/MenuAdminPage'
import StaffTablesPage from './features/staff-tables/StaffTablesPage'
import DiningSessionPage from './features/staff-tables/DiningSessionPage'
import KitchenBoardPage from './features/fulfillment/KitchenBoardPage'
import StaffServingPage from './features/fulfillment/StaffServingPage'

export default function FoundationApp() {
  return (
    <Routes>
      <Route path="/" element={<DesignSystemPage />} />

      <Route path="/billing/preview" element={<BillingPreviewPage />} />
      <Route
        path="/staff/sessions/:sessionId/billing"
        element={<BillingPreviewPage />}
      />

      <Route path="/customer/qr" element={<CustomerOrderingPage />} />
      <Route path="/staff/tables" element={<StaffTablesPage />} />
      <Route
        path="/staff/sessions/:sessionId"
        element={<DiningSessionPage />}
      />

      <Route path="/admin/menu" element={<MenuAdminPage />} />
      <Route path="/kitchen" element={<KitchenBoardPage />} />
      <Route path="/staff/serving" element={<StaffServingPage />} />
      <Route path="*" element={<DesignSystemPage />} />
    </Routes>
  )
}