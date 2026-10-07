# Contract, State and Delivery Audit

**Audit date:** 7 October 2026  
**Initial audit baseline:** `origin/develop` at `0dbbe1b`
**Rechecked PR baseline:** `develop` at `cb612d9`; PR #26 review head `af82e3d`
**Scope:** Stock/Profile DTO and API contracts, OpenAPI, JSON serialization, Kitchen/Serving fulfillment, JPA rationale and slide source notes.

This is a local source-and-test audit, not a release certification. The checked-in implementation was compared across backend DTOs/controllers, frontend API/types/forms, tests and documentation. A public deployment and the externally maintained Canva/export files were not available for verification.

## Checklist result

| Checklist item | Result | Evidence / limits |
|---|---|---|
| Stock/Profile DTO/API agreement | **PASS — local contract** | Stock quantity, threshold, transaction deltas and package price are JSON numbers backed by `BigDecimal`; frontend form strings are converted before sending. Create-user email is optional/null and validated when supplied. `UserContext` now matches `userId`, `username`, `displayName`, `role`; staff profile responses expose only `id`, `username`, `displayName`, nullable `email`, `role`. The actual Profile surface is list/create only; no edit/delete endpoint is claimed. Validation, role and HTTP outcomes are documented in `shared-contracts.md` and `api-conventions.md`. |
| OpenAPI cookies, bill/payment/close and credential exposure | **PASS — schema tests** | OpenAPI describes staff `JSESSIONID` and customer `customer_session` cookie schemes; the QR exchange token is a write-only POST-body field. Bill `dueAmount`/`paidAmount`, payment input/result, session close and error schemas/examples are present. Payment amount is server-calculated and omitted from the request schema. Examples use synthetic values; no live QR token, cookie, password or session credential is included. `OpenApiContractIntegrationTest` verifies cookie schemes, endpoint security, numeric bill amount fields, QR token write-only behavior, fulfillment success schemas, shared error schemas and the intentionally empty `/auth/me` 401 response. |
| Timezone, enums and numeric JSON | **PASS — source/tests** | API timestamp examples and documented output use UTC `Z`; session timestamps are mapped from the UTC clock. Shared enum spelling is uppercase and case-sensitive. Backend JSON tests verify enum rejection and error response shape; Payment and Step 2 integration tests cover numeric amount serialization and UTC assertions. Frontend build and tests cover numeric form serialization. |
| Kitchen/Serving State diagrams and real roles | **PASS — source and previews regenerated** | State, class and sequence sources describe `RECEIVED → PREPARING → READY → SERVED`, with Kitchen responsible through READY and Service Staff for SERVED. Rechecked against the post-PR #25 implementation at PR base `cb612d9` and review head `af82e3d`; current sources and speaker-note examples use `OrderStateResolver.resolve()` / `RegistryOrderStateResolver`. Regenerated `class-order-state.svg` and `sequence-ordering-kitchen.svg` locally with PlantUML 1.2025.0 and visually reviewed both. Canva/PPTX/PDF are still separate and not updated. |
| State Pattern explanation and allowed/rejected transitions | **PASS — docs/tests** | Documentation explains the context (`OrderFulfillmentServiceImpl`), persisted enum, injected `OrderStateResolver`, validated `RegistryOrderStateResolver` and concrete states. Tests cover allowed progression, skipped/reversed/terminal transitions, unauthorized roles, anonymous requests and unknown orders. |
| Public deployment flow and role enforcement | **BLOCKED — no public URL** | Local session-backed integration tests exercise login cookies and role enforcement, including attempts to spoof `X-User-Role`; they do not prove behavior on a public deployment. The repository README says there is no confirmed public URL. |
| Order/OrderItem cascade/fetch rationale, SOLID examples and slides | **SOURCE UPDATED; external delivery pending** | The architecture note explains aggregate-owned `Order.items` cascade/orphan removal, lazy loading, no reverse cascade, FK behavior and the query caveat; it records concrete SOLID examples and limits. Speaker-note source was updated for State, cascade/fetch and UTC/number serialization. Canva was not synchronized and PPTX/PDF exports were not created or checked. |

## Validation performed

| Validation | Result |
|---|---|
| Frontend `npm run build` | Passed |
| Frontend full `npm test` | 14 files, 117 tests passed |
| Backend focused contract, billing, State and integration tests | 55 tests passed; includes Stock/Profile auth, Payment, order fulfillment, session-backed roles, Step 2 bill flow, OpenAPI and State tests |
| Backend enum/error contract tests | 5 tests passed |
| Backend full local `./mvnw test` on reviewer-fix working tree based on PR head `af82e3d` | 337 tests, 0 failures/errors, 27 skipped |
| GitHub Actions CI for PR head `af82e3d` | Backend and PostgreSQL: 338/338 passed, 0 skipped; Frontend tests/lint/build passed |
| Reviewer OpenAPI evidence at PR head `af82e3d` | `/v3/api-docs` on H2 exposed missing fulfillment 200 schemas, incorrect ErrorResponse schemas and inferred `/auth/me` 401 body; the contract integration test was extended to cover these cases |
| `OpenApiContractIntegrationTest` after reviewer fixes | 2 tests passed; checks fulfillment 200 array/item and transition schemas, ErrorResponse refs across annotated endpoint errors, and empty `/auth/me` 401 response documentation and runtime body |
| Updated PlantUML previews | Both changed SVGs rendered locally with PlantUML 1.2025.0 and visually reviewed |
| `git diff --check` | Passed |

The local full-suite result and CI result are recorded separately because the CI job uses the full PostgreSQL-enabled test configuration. This is not a deployed-browser test.

## Follow-up needed to close remaining items

1. Provide the confirmed public deployment URL and arrange role test accounts through a secure channel. Verify the end-to-end order progression, wrong-role denials, payment, close and public Swagger against the deployed build/deployed SHA.
2. Sync the approved slide source into the team's Canva deck, then export and review the matching PPTX and PDF. The checked-in speaker notes and repository SVGs are not evidence that Canva/export artifacts changed.
3. Have the relevant API/feature owners review the contract changes before release.

No deployment, Canva edit, or export was performed as part of this audit.
