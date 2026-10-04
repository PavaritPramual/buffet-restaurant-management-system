# Test plan — Buffet Restaurant Management System

Owner of this plan and test process: ศิระพัทธ์. Each feature owner writes tests for their own module. The plan is updated when the integrated system and deployment target become available.

## Objectives and scope

Prove that the customer can use a valid session to see allowed menu items, place one valid order, and see its status; that invalid requests leave order data unchanged; and that staff can maintain categories and menu items. Confirm the shared JSON contract, isolated test data, and the basic frontend states (loading, empty, error, narrow screen).

Unit tests cover business rules without network or database. Controller tests cover validation, HTTP status, and ErrorResponse. Integration tests will cover the real Supabase/JPA adapter using a dedicated disposable database/schema. Browser checks cover the customer path and menu admin path. Session/Ordering and Ordering/Fulfillment pairwise tests are merged. Menu writes now use real Auth sessions with MANAGER-only authorization. Billing/Payment and production Staff/Fulfillment authorization remain integration dependencies.

## Environments and data

- Local module development: JPA/Flyway persistence with fixture SessionContext (active session 1, package 1; completed session 2).
- Automated backend tests: isolated H2 database in PostgreSQL mode with V1,V2,V3,V4,V6,V7,V8,V10, plus upgrade checks from empty/V1/V2/V3. Optional PostgreSQL tests use fresh local databases for V1–V8,V10,V11, constraints and client-role restrictions. V9 Payment is not in this checkout. No shared Supabase project.
- Pairwise persistence tests: dedicated disposable database/schema; never use the team's shared project for destructive automated tests.
- Cross-module payload examples: `test/fixtures/` and `doc/contracts/shared-contracts.md`.

## Customer browser-tab limitation

Customer QR/session coordination currently supports one ordering tab per browser profile. `latestQrScan` and exchange sequencing live in one tab, while the HttpOnly customer cookie is shared across tabs. A QR exchange in another tab can therefore leave an older tab displaying a previous session; requests with that old session ID and the newer cookie are rejected by the backend. The UI does not receive cross-tab session notifications. Close older ordering tabs and reload the remaining tab to read the current cookie/session, or use separate browser profiles for different sessions. No multi-tab synchronization is delivered or covered by the acceptance tests.

## Continuous integration and database safety

[CI workflow](../../.github/workflows/ci.yml) runs backend verification with disposable PostgreSQL services and frontend tests/lint/build for PRs into `develop`. See [database safety setup](../../test/README.md): all environment-driven Menu/Dining PostgreSQL tests require `ALLOW_DESTRUCTIVE_DB_TESTS=true`, an explicit loopback port and `buffet_test_` database name without URL options, plus a matching database comment. The checks run before migration/startup and again before concurrency cleanup. Docker Stock tests use a separate Testcontainers database. Browser execution and screenshot inspection remain local evidence. The workflow configuration alone is not a passing CI result; inspect the run for the current revision before merging.

## Entry and exit criteria

Entry: the module contract and fixture are agreed; the branch is based on current `develop`. Exit for a module PR: backend tests, frontend tests/lint/build, responsive browser checks, Swagger and migrations pass; acceptance criteria and limits are recorded; another team member reviews. Integration is complete only after the real Session adapter, authorization, Kitchen/Billing handoff, and pairwise integration tests pass.

## Responsibilities

ศิระพัทธ์ maintains this plan, traceability, regression checklist, Menu/Ordering tests, and frontend quality review. Other feature owners test their own rules. ศรัณย์ reviews API/JSON changes, ปวริศช์ reviews shared architecture/session integration, เมธัส reviews migrations and authentication boundary, and ธีรเมธ reviews deployment/browser connectivity.

## Latest execution

See [Sirapat Step 2 verification](sirapat-step2-report.md) and [final browser evidence](../../test/evidence/sirapat-step2-2026-10-04/final-review/) for actual results from 4 October 2026. Owner review and canonical schema approval remain separate acceptance gates.
