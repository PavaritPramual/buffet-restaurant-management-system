# Menu/Ordering independent development test report

Date: 18 September 2026. Scope: process-local demo adapter and independent Menu/Ordering module. This report is updated with new run results before review; it is not the final integrated system report.

| Check | Result | Evidence |
|---|---|---|
| Backend unit/controller suite | Passed, 14 tests with no failures | `mvn '-Dmaven.repo.local=.m2-cache' test -q`; Surefire reports in `code/backend/target/surefire-reports/` |
| Frontend lint/build | Passed | `npm run lint`, `npm run build` |
| Frontend component suite | Passed, 3 tests with no failures | `npm run test` (customer selection/submission, invalid session, admin item submission) |
| Customer browser flow | Passed | Session 1 menu loaded, item added, order submitted and displayed as `RECEIVED` |
| Admin item create/edit/delete | Passed | Created an item with `imageUrl`, changed its name, and removed it through `/admin/menu` |
| Customer image display | Passed | Created item's image appeared in `/customer/sessions/1` |
| Admin category create/delete | Passed | Created and removed a temporary category through `/admin/menu` |
| Customer mobile layout | Passed at 360px width | Menu cards stacked without horizontal overflow; desktop checked at default width |

Known limits: demo data resets on backend restart; real Supabase persistence, Session/QR, staff authorization, Kitchen/Billing integration, and final E2E tests depend on other owners' implementations.
