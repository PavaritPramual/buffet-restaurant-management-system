# Requirement to test traceability — Menu/Ordering

Verified on 4 October 2026; see [execution report](sirapat-step2-report.md).

| Requirement | Current evidence | Remaining gate |
|---|---|---|
| Category/Menu create/read/update/delete, description/image/package persistence | MenuCatalogIntegrationTest manager CRUD cases; real Manager browser create/update/delete | New PR review |
| Only MANAGER changes catalog; anonymous=401, other staff=403 | MenuCatalogIntegrationTest checks every mutation for all four access contexts; AuthStockIntegrationTest; browser direct URL login guard | Broader team role flow |
| Pagination/sorting, next/empty page, invalid queries | MenuCatalogIntegrationTest; OrderingIntegrationTest; MenuAdminPage sorting tests and real browser sort | New API review |
| Active session sees only available package menus | OrderingIntegrationTest; real QR browser menu filters | None for module behavior |
| Unavailable/out-of-package/duplicate/zero/negative order rejected without saving | OrderingIntegrationTest | None for module behavior |
| Valid order persisted as RECEIVED; session-scoped status/history | OrderingIntegrationTest; real browser order and duplicate click check | Full Billing flow |
| QR fragment exchanged once; rescans and late responses isolated | CustomerOrderingPage and API tests; DiningSessionIntegrationTest | Production deployment/browser policy |
| Cart quantity/removal, confirmation, loading/empty/error and retry | CustomerOrderingPage tests; seven visually inspected browser screenshots | Additional device coverage |
| Retry menu loading after consumed QR | CustomerOrderingPage cookie retry test; browser network-error recovery | None |
| 360px Customer UI / 44px controls / 48px primary action | Browser geometry: viewport=scrollWidth=360; control heights recorded in browser-results.json | Additional devices |
| No unauthorized catalog mutation during disabled integration | MenuAdminDisabledIntegrationTest | None |
| Order history survives menu/session removal | OrderingIntegrationTest; H2/PostgreSQL migration tests verify RESTRICT and order-item CASCADE | Canonical design extension review |
| Flyway upgrade from empty/V1/V2/V3, PK/FK/nullability/defaults/checks | MenuOrderingMigrationTest (4 cases); PostgresMenuOrderingMigrationTest (1); Hibernate validates in integration startup | Methus/Pavarit schema decision |
| PostgreSQL RLS and anon/authenticated table/sequence access | PostgresMenuOrderingMigrationTest | Shared deployment ordering; no shared database run |
| Ordering ↔ Fulfillment / Session ↔ Ordering | OrderFulfillmentIntegrationTest, OrderingIntegrationTest, PostgresOrderCloseConcurrencyTest | Staff/Fulfillment production Auth adapter |
| Live Swagger schema and UI | Browser smoke checks API paths and Swagger HTTP 200 | None for module docs |
| Customer request bill / bill status | Not delivered by the current Billing/Payment baseline | Teeramet contract/implementation and pairwise test |

Evidence: [browser results and screenshots](../../test/evidence/sirapat-step2-2026-10-04/), [schema delta](../database/menu-ordering-schema-delta.md). Tests and schema audit do not constitute another person's approval.
