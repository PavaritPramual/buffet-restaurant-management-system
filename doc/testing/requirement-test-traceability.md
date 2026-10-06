# Final requirement to test traceability

Owner: ศิระพัทธ์. [Test plan](test-plan.md). Baseline: integrated develop 54e3538, 6 October 2026. Test names identify coverage, not proof of release execution. [Step 2](pavarit-step2-close-report.md) is historical; [Step 3 report](sirapat-step3-report.md) records current runs. Public acceptance is pending.

| ID / requirement | Executable coverage / evidence | Final gate / owner |
|---|---|---|
| Category/Menu CRUD, description/image/packages | MenuCatalogIntegrationTest; MenuAdminPage.test.tsx; historical Manager browser | Current PR + public Manager review |
| MANAGER-only catalog writes; anonymous/wrong role | MenuCatalogIntegrationTest; MenuAdminDisabledIntegrationTest; AuthStockIntegrationTest; OrderingConcurrency | Public four-role cookies; ศรัณย์ API |
| Pagination/sort/query/late responses | MenuCatalogIntegrationTest; OrderingIntegrationTest; MenuAdminPage/OrderingConcurrency | Current run; public API/sorting |
| QR-01 one-use fragment/StrictMode | DiningSessionIntegrationTest; CustomerOrderingPage/OrderingConcurrency/CustomerOrderingConcurrency; ordering api tests | Real public HTTPS/cookie/Origin |
| QR-02 independent phones, invalid credential/session | DiningSessionIntegrationTest; Step2CompletionIntegrationTest; step3-core-flow.cjs | Public runtime/accounts; two customer contexts |
| QR-03 same-tab rescan/remount/latest-response | CustomerOrderingPage/CustomerOrderingConcurrency/OrderingConcurrency; historical concurrency browser | Rerun controlled-delay browser; label fixtures |
| ORDER-01 valid RECEIVED, invalid order unchanged | OrderingIntegrationTest; CustomerOrderingPage; step3-core-flow.cjs | Full real public flow; API review |
| ORDER-02 status/history/acknowledgement races | CustomerOrderingConcurrency; OrderFulfillmentIntegrationTest; SessionFulfillmentIntegrationTest | Public Kitchen/Staff transitions and denial |
| BILL-01 request stops every phone's orders | Step2CompletionIntegrationTest; PostgresOrderCloseConcurrencyTest; CustomerOrderingPage; step3-core-flow.cjs | Public two-phone polling/badge/409 |
| BILL-02 request before payment, total/due/paid | PaymentIntegrationTest; Step2CompletionIntegrationTest; BillingPreviewPage/PaymentPanel/CustomerOrderingPage | Public PAID/no duplicate; ธีรเมธ/ศรัณย์ |
| CLOSE-01 unpaid rejection/close/revocation/locks | DiningSessionIntegrationTest; PaymentIntegrationTest; PostgresOrderCloseConcurrencyTest; step3-core-flow.cjs | Actual PG concurrency + public close |
| ROLE-01 four roles/direct URL/case/slash/spoofing | AdminShell/OrderingConcurrency; SessionFulfillmentIntegrationTest; PaymentAuthorizationTest; step3-core-flow.cjs | Independent public login contexts |
| AUTH-01 logout/employee expiry | AdminShell.test.tsx: actual Axios401 Manager/Supervisor, stale restore, Staff expiry/logout; step3-core-flow.cjs | Public cookie expiry; protected UI removed |
| Customer states/confirmation/duplicate action | CustomerOrderingPage/CustomerOrderingConcurrency; state scripts; step3-core-flow.cjs | Current360px images; separate fixture report |
| Staff/Kitchen/Manager responsive | StaffTablesPage/KitchenBoardPage/StaffServingPage/AdminPages/MasterDataPage; step3-core-flow.cjs | Inspect768/1280px and loading/empty/error |
| STOCK-01 target/default/validation/shortage | Planned owner API/H2/frontend tests; existing stock tests cover Step2 only | **Pending เมธัส implementation**: DECIMAL(12,3)≥0 default0, active=true; ศรัณย์ DTO; ศิระพัทธ์ UI |
| STOCK-02 inactive history/blocked writes/reactivate | Planned role/API + StockPage/MasterDataPage cases after contract review | **Pending เมธัส**; historical stock tests do not cover lifecycle |
| PROFILE-01 first/last/phone, legacy fallback | Planned owner Auth/API/frontend/migration tests | **Pending เมธัส**; names≤100, phone≤20, new names required; no guessed legacy split |
| MIGRATION-01 fresh/upgrade/grants/preserve history | MenuOrderingMigrationTest/PostgresMenuOrderingMigrationTest; DiningSessionMigrationTest; PostgresStockSecurityIntegrationTest cover baseline | Final migrations เมธัส; integration ปวริศช์; rerunPG |
| DEPLOY-01 HTTPS/Origin/deep links/providers/Swagger | Existing CI/local smoke; configurable step3-core-flow.cjs | **Pending public deployment ธีรเมธ**; commit/schema confirmation + release rerun |
| Automated counts/warnings/skipped/provenance | Surefire/Vitest/lint/build logs; [Step3 report](sirapat-step3-report.md) | Latest release rerun; no reuse of Step2 counts |
| Menu SOLID/cascade/fetch contribution | [Module notes](../architecture/sirapat-menu-ordering-solid-jpa.md) | ปวริศช์ integrates; reviewers confirm |
| Test strategy/QR/frontend slides | doc/slide/sirapat-step3-quality.pptx and notes source | Final numbers/URLs + merged deck/PDF + five-person rehearsal |

Requirements: [Step3](https://app.notion.com/p/37e90b8ff964834fad3701e2d8115de2), [Regression](https://app.notion.com/p/1cc90b8ff96482af882481112591accd), [Stock](https://app.notion.com/p/4cc90b8ff96483a8abec0159c87caf26), [Profile](https://app.notion.com/p/15c90b8ff96483d0b384813b365e4c09). Tests and documents do not constitute another person's approval.
