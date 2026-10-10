# Final requirement to test traceability

Owner: ศิระพัทธ์. [Test plan](test-plan.md). Latest integrated baseline: developd84f071, 9 October 2026. [Current public report](sirapat-public-regression-2026-10-09.md): real API12 groups and browser12 groups passed, Stock/Profile integrated, public Secure-cookie/logout/close verified. Independent customers are API cookie jars; the browser uses one shared context. Submitted production11edeb0 passes frontend125/125 with Thai QR recovery; not deployed. Live SHA/runtime/schema, timed TTL and reviewed release acceptance remain pending. Candidatecc72fa0 and [8 October follow-up](sirapat-step3-followup-2026-10-08.md) are historical. The following coverage inventory retains historical test names; current execution/gates take precedence below.

## Current execution mapping — 9 October

| Requirement | Current evidence | Remaining limit |
|---|---|---|
| Catalog / pagination / roles | Public Manager create/edit/reload/sort and delete confirmation/cancel; API role matrix and invalid sort; baseline CI | No permanent public deletion; image behavior remains automated coverage |
| QR / Order / State / Bill / Close | API two independent cookie jars, one-use/revocation/validation/12 State denials; browser cart/rescan/status/payment/close | One browser context; no physical-device or timed-delay claim |
| Many-menu customer QR | User-requested30 available foods/5categories +2 unavailable; filters6/category,32available cards, cart preserved; real UI order3/6lines/8pieces, Kitchen receipt and customer reload; bill499/session4 closed | Supplementary real public check; main12/12group counts unchanged; QR link redemption, no physical camera/device claim |
| AUTH / Secure cookie | Real HTTPS attributes, invalid login401, four-role login/logout protected401 | Elapsed customer8h/staffTTL not executed |
| STOCK / PROFILE | Public active/inactive/targets/history/role/validation API; browser confirmation/movements/profile update persisted after reload | Final feature-owner and migration/release attestation remain |
| Responsive / feedback | Public32 main +3 volume images,360/768/1280 in report; local Thai QR screenshot; changed frontend125 tests | Loading/remount/race fixtures are component/historical evidence, not fresh public outage simulation |
| QR Thai recovery | CustomerOrderingPage.test.tsx missing/expired/inactive/used-QR retry; local real API missing-session browser | Review/merge/redeploy and changed public rerun |
| Migrations / PG contention | d84f071 CI backend352/352,0 skips; local322 passed/28 skipped | Local Docker unavailable; CI and public schema attestation are separate |
| Owner QA presentation | Separate3-slide editable PPTX/PDF, full notes, package/font/geometry/import and every-slide/PDF-page inspection | User will insert into Canva; team approval/export/rehearsal separate |

## Coverage inventory and historical gates

| ID / requirement | Executable coverage / evidence | Final gate / owner |
|---|---|---|
| Category/Menu CRUD, description/image/packages | MenuCatalogIntegrationTest; MenuAdminPage.test.tsx; step3-core-flow real Manager UI category/menu create, update, reload/read and confirmed unused-data delete with persistence checks | Public Manager rerun; browser covers description/packages, image behavior remains component/API coverage |
| MANAGER-only catalog writes; anonymous/wrong role | MenuCatalogIntegrationTest; MenuAdminDisabledIntegrationTest; AuthStockIntegrationTest; OrderingConcurrency | Public four-role cookies; ศรัณย์ API |
| Pagination/sort/query/late responses | MenuCatalogIntegrationTest; OrderingIntegrationTest; MenuAdminPage/OrderingConcurrency | Current run; public API/sorting |
| QR-01 one-use fragment/StrictMode | DiningSessionIntegrationTest; CustomerOrderingPage/OrderingConcurrency/CustomerOrderingConcurrency; ordering api tests | Real public HTTPS/cookie/Origin |
| QR-02 independent phones, invalid credential/session | DiningSessionIntegrationTest; Step2CompletionIntegrationTest; step3-core-flow.cjs | Public runtime/accounts; two customer contexts |
| QR-03 same-tab rescan/remount/latest-response | CustomerOrderingPage/CustomerOrderingConcurrency/OrderingConcurrency; step3-core-flow real same-document rescan; step3-concurrency current controlled-delay/remount fixtures | Public release rerun; controlled delay/503 fixtures stay separate from real Core Flow |
| ORDER-01 valid RECEIVED, invalid order unchanged | OrderingIntegrationTest; CustomerOrderingPage; step3-core-flow.cjs | Full real public flow; API review |
| ORDER-02 status/history/acknowledgement races | CustomerOrderingConcurrency; OrderFulfillmentIntegrationTest; SessionFulfillmentIntegrationTest; step3-core-flow.cjs: real HTTP401/403/400/404, skipped/reversed/terminal/unknown-enum transitions, ErrorResponse and unchanged persisted status | Public Kitchen/Staff transitions and denial; latest local evidence in API/State regression report |
| BILL-01 request stops every phone's orders | Step2CompletionIntegrationTest; PostgresOrderCloseConcurrencyTest; CustomerOrderingPage; step3-core-flow.cjs | Public two-phone polling/badge/409 |
| BILL-02 request before payment, total/due/paid | PaymentIntegrationTest; Step2CompletionIntegrationTest; BillingPreviewPage/PaymentPanel/CustomerOrderingPage | Public PAID/no duplicate; ธีรเมธ/ศรัณย์ |
| CLOSE-01 unpaid rejection/close/revocation/locks | DiningSessionIntegrationTest; PaymentIntegrationTest; PostgresOrderCloseConcurrencyTest; step3-core-flow.cjs | Actual PG concurrency + public close |
| ROLE-01 four roles/direct URL/case/slash/spoofing | AdminShell/OrderingConcurrency; SessionFulfillmentIntegrationTest; PaymentAuthorizationTest; step3-core-flow.cjs | Independent public login contexts |
| AUTH-01 logout/employee expiry | AdminShell.test.tsx: actual Axios401 Manager/Supervisor, stale restore/login/logout and pending duplicate actions; step3-core-flow.cjs: mounted-shell protected API401 for all four roles after server-side invalidation, plus separate Staff/Kitchen UI logout | Public timed TTL/cookie expiry; protected UI removed |
| Customer states/confirmation/duplicate action | CustomerOrderingPage/CustomerOrderingConcurrency; state scripts; step3-core-flow.cjs | Current360px images; separate fixture report |
| Staff/Kitchen/Manager responsive | StaffTablesPage/KitchenBoardPage/StaffServingPage/AdminPages/MasterDataPage; step3-core-flow.cjs | Inspect768/1280px and loading/empty/error |
| STOCK-01 target/default/validation/shortage | PR #28 StockProfileFinalIntegrationTest, migration/frontend tests; step3-stock-profile.cjs real defaults/decimal UI edit/negative400/persisted quantity/shortfall; candidate8/8 in current report | Candidatecc72fa0 passed before merge; reviewed integration/public rerun still required |
| STOCK-02 inactive history/blocked writes/reactivate | step3-stock-profile.cjs real confirmation, disabled actions/409/preserved history/reactivation and four-role denial; candidate automated tests | เมธัส feature/migration gate; ปวริศช์ integration; ศิระพัทธ์ rerun after merge/public |
| PROFILE-01 first/last/phone, legacy fallback | Candidate AuthService/StockProfileMigration/FinalIntegration/frontend tests; real UI create/update/reload/length/role400/403/401; controlled legacy fallback labelled separately | Candidate pass; names≤100 required for new/update, phone≤20 optional/no regex; final integrated/public review |
| MIGRATION-01 fresh/upgrade/grants/preserve history | MenuOrderingMigrationTest/PostgresMenuOrderingMigrationTest; DiningSessionMigrationTest; PostgresStockSecurityIntegrationTest cover baseline | Final migrations เมธัส; integration ปวริศช์; rerunPG |
| DEPLOY-01 HTTPS/Origin/deep links/providers/Swagger | Existing CI/local smoke; step3-core-flow public same-origin HTTPS guard before startup; step3-runtime-config.test.cjs (6 URL guard cases in CI) | **Pending public deployment ธีรเมธ**; commit/schema confirmation + release rerun |
| Automated counts/warnings/skipped/provenance | Current report: develop local310/27skips, CI339/339 +frontend118; candidate local318/28skips, CI348/348 +frontend121; guards6; canonical Git blob manifests. Separate revision/environment/time for each | Latest release rerun; candidate/CI/fixtures do not substitute for public acceptance |
| Menu SOLID/cascade/fetch contribution | [Module notes](../architecture/sirapat-menu-ordering-solid-jpa.md) | ปวริศช์ integrates; reviewers confirm |
| Test strategy/QR/frontend slides | [8 October personal content/notes draft](../slide/sirapat-step3-quality-2026-10-08-draft.md): develop339/118 and candidate348/121 explicitly separated; public pending. Older303/116 PPTX/PDF remain historical; team Canva remains draft | Final revision numbers/URLs + reviewed team deck/PDF + five-person rehearsal |

Requirements: [Step3](https://app.notion.com/p/a9e90b8ff9648363a6ab81b48fd70816), [Regression](https://app.notion.com/p/85290b8ff96482c59ba201e5d9a767fd), [Stock](https://app.notion.com/p/80d90b8ff964820d8ad38171aeed722c), [Profile](https://app.notion.com/p/5e690b8ff96483c9ab05015e38d972c6). Tests and documents do not constitute another person's approval.

R01-A (9 October 2026): current implementation/test mapping and separate local browser/API evidence are in [Sirapat R01-A report](sirapat-r01-a-2026-10-09.md). Combined V16/V17/V18 validation passed on `ad977e8`, and PostgreSQL CI passed again for the index-review patch `9ab3312`; final CI after the QR test scheduling correction is recorded on PR47. Actual shared FK/Flyway/RLS/grants and backend UPDATE audit, current peer approval, and public deployed-SHA acceptance remain release gates.

## R02 optional recipe and stock consumption

| Behavior | Evidence | Limits |
|---|---|---|
| Atomic recipe configuration, validation, role denial, frozen recipe, aggregation, rollback, unit/archive protection | MenuStockConsumptionIntegrationTest / shared MenuStockConsumptionScenarios | H2 does not prove PostgreSQL locking |
| Same-order concurrent start and different orders sharing Stock | PostgresMenuStockConsumptionIntegrationTest | Disposable marked PostgreSQL; not shared/public runtime |
| V18 upgrade defaults, FK/CHECK/unique, PostgreSQL RLS/client grants | MenuStockMigrationTest + PostgreSQL scenarios | No new central apply |
| Manager recipe / customer order / kitchen shortage and retry / audit / Payment-close / Swagger | test/browser/menu-stock-consumption.cjs | Local isolated real HTTP; prior public UAT is historical |
