# PR19 review fixes — 5 October 2026

These results describe the working tree after merge commit 025d667, not a pushed GitHub commit. No reviewer comments were posted and no shared migration was applied.

## Review response map

| Reviewer concern | Change / result |
|---|---|
| Normal npm test failed | jsdom file directives added; npm test now passes 96 cases without environment override |
| Billing routes lost during merge | Both Billing routes restored under StaffShell; ManagerRoute retained |
| Order-after-close regression | Accepts UnauthorizedException or ResourceNotFoundException and still verifies zero orders; both PostgreSQL concurrency cases ran |
| Recovery stuck after no Payment | Successful explicit GET with no record resets request guard/attempted; manual confirmation required; GET failure or existing Payment still blocks POST |
| Missing runtime setup | Provider settings added to .env.example/README; fail-closed application defaults retained |
| Public/internal DTO mix | Internal BillCalculation retains exact arithmetic. Public BillSummary only sessionId/subtotalAmount/discountAmount/totalAmount; display subtotal/discount reconcile to rounded payable total |
| Strategy/design mismatch | Standard pricing delegates to injected immutable ChildRateCalculationStrategy; BillingEngine delegates discounts to DiscountCalculationStrategy/PromotionDiscountStrategy. Updated composition diagram is a proposed design delta for owner review |
| Recorded amount missing | PaymentResult and shared fixture include persisted amount; confirmation uses it even if preview differs |
| Obsolete diagrams | Sequence/activity now use sessionId + method, backend calculation and separate close after matching PAID |
| DB role / GRANT / sequence | Preserve V9 checksum. Proposed PostgreSQL V12 uses current_user (same Flyway/JDBC datasource) and pg_get_serial_sequence, explicit table/sequence grants, client revocations and RLS policy |
| PostgreSQL / concurrent duplicates | Guarded disposable DB tests added, including inherited payment acceptance cases, invalid constraints, role privileges and simultaneous POST → exactly 201/409 with one stored row |

## Verification

- Backend: 275 discovered, 273 passed, 2 Docker Stock tests skipped, no failures/errors. All environment-enabled Menu/Dining/Payment PostgreSQL tests ran on new marked databases.
- PostgreSQL Payment: 14 cases passed using PostgreSQL 18.6 and a non-superuser database owner/JDBC role. RLS policy role and privileges checked; invalid data/FK checks rejected; concurrent requests left one payment.
- Frontend: normal npm test 96/96 in 13 files; production build passed; lint 0 errors, 4 baseline warnings.
- Native HTTP smoke: 61 checks passed on a separate disposable PostgreSQL runtime, including health, CORS credentials, session Auth, CASH/QR/CARD, unpaid close, duplicate payment, same record after changing current package price, separate close and Swagger DTO schemas.
- Browser: logged in as sample SERVICE_STAFF; opened session 4, preview 997.50, confirmed CASH (payment 4), refreshed/reloaded preview and still saw payment 4/recorded 997.50; explicit close returned the table to AVAILABLE.
- Network-error manual retry is verified by component tests. No browser network fault injection was performed.
- PostgreSQL tests passed through V9/V10/V11/proposed V12 from an empty DB; this does not establish shared Supabase history compatibility.

## Evidence

- [Aggregate test results](../../test/evidence/billing-pr19-2026-10-05/test-results.json)
- [HTTP smoke results](../../test/evidence/billing-pr19-2026-10-05/runtime-smoke.json)
- [Payment after refresh](../../test/evidence/billing-pr19-2026-10-05/payment-after-refresh.png)
- [Table after close](../../test/evidence/billing-pr19-2026-10-05/table-after-close.png)

Evidence contains only sample IDs/amounts and test UI; cookies/passwords/QR grants and central secrets are not recorded.

## Reproduce normal checks

```bash
cd /home/koji/CS3-1/Prinsible_software/buffet-restaurant-management-system/code/frontend
npm test
npm run build
npm run lint

cd /home/koji/CS3-1/Prinsible_software/buffet-restaurant-management-system/code/backend
./mvnw test
```

Default backend tests use H2; PostgreSQL cases require explicit setup. The agent used an explicit Mockito javaagent because attach is restricted locally, without changing project dependencies.

## PostgreSQL test setup

Use a new disposable local PostgreSQL server only. Create NOLOGIN anon/authenticated roles, a non-superuser login buffet_backend_test, and three new buffet_test_ databases. Payment DB must be owned by buffet_backend_test. Mark each database with comment `buffet-disposable-test-only` before enabling destructive tests. Use a fresh Menu DB for every run because its test deliberately checks upgrade from V2.

Set ALLOW_DESTRUCTIVE_DB_TESTS=true and strict loopback JDBC URLs (no URL options) under MENU_TEST_PG_URL, DINING_TEST_PG_URL and PAYMENT_TEST_PG_URL; set PAYMENT_TEST_PG_USER=buffet_backend_test and corresponding local test passwords. Never point these variables at Supabase or shared databases. CI now creates/marks a separate Payment database and enables these cases automatically; CI has not been run on these unpushed changes.

## Remaining owner / deployment gates

1. Methus confirms V12 is available and central history. Keep applied V9 unchanged. If V10/V11 were applied with V9 absent, this V12 privilege migration cannot create the missing payments table; agree a forward creation plan before deployment. No outOfOrder setting was enabled.
2. Verify actual shared JDBC/Flyway role with current_user/session_user. Separate migration/runtime roles require explicit reviewed runtime grants; do not infer service_role from Supabase branding. Spring session Auth does not use auth.uid() policies.
3. Sarun/Pavarit/Sirapat review the compact response contract and composition design delta before merge.
4. Docker Compose + shared Supabase runtime remains unverified. Docker socket access and privilege elevation are unavailable locally; native PostgreSQL HTTP/browser smoke is different evidence.
5. Refresh PR validation after commit/push and request re-review. This agent did not commit, push, merge, or post comments.
