# ศิระพัทธ์ — Public regression, 9 October 2026

Owner: Sirapat Wongwiwatseree. Scope read from the current [responsibilities](https://app.notion.com/p/Menu-Catalog-Customer-Ordering-SQA-3ddcb2e9d47a81149b7bc5a464bc9ed9), [Step 3](https://app.notion.com/p/3f1cb2e9d47a81e28aa2dc642cd6ead6), and [Final regression task](https://app.notion.com/p/Final-Regression-Public-Core-Flow-role-3f1cb2e9d47a8118923ce11532cd3936). This closes the executable owner regression work in this round; final release approval remains open.

## Outcome and revision identity

Public HTTPS Core Flow, four roles, Stock/Profile and persisted browser operations passed. Customer missing/revoked QR errors exposed English backend messages; this branch adds Thai recovery guidance with component regressions. The public site still runs the old wording.

[PR #36](https://github.com/PavaritPramual/buffet-restaurant-management-system/pull/36) is open for ปวริศช์/ศรัณย์ review. CI at head9b9eddf passed Backend/PostgreSQL352/352 and changed Frontend125/125, URLguards6/6, lint0 errors/4 existing warnings and build; [run37876116678](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37876116678), [saved job-log excerpts](../../test/evidence/sirapat-public-2026-10-09/pr36-ci.json). This additional evidence commit changes documentation only; reviewed release and deploy remain separate. Canva browser access is at the login page, so direct QA content merge/export cannot currently be performed.

| Evidence | Revision / identity | Result and limit |
|---|---|---|
| Integrated source | develop d84f071ee03b738d6a6dd2a899c5f6e6cc230d3f; PR #28/#31/#32/#33/#34/#35 integrated | Clean fast-forward before work; historical candidate counts are not reused as current |
| Submitted production source | 11edeb0d39afd1519d047e625f4884ccf63f6405 | Thai customer error adapter + four tests; subsequent commits contain runner metadata/docs only |
| Public deployment | 2f8bc4b from [runtime owner's record](../deployment/step3-owner-status.md) | Not independently attested by the running API. Git comparison of 2f8bc4b and d84f071 found no differences in backend main, frontend src or Dockerfile.production |
| Canonical source identity | [baseline hashes](../../test/evidence/sirapat-public-2026-10-09/baseline-source-hashes.json), [submission hashes](../../test/evidence/sirapat-public-2026-10-09/submission-source-hashes.json) | 352 Git blobs each; hashes use Git bytes rather than CRLF checkout bytes. New API runner has a separate tested hash in the evidence README |
| Release | Reviewer merge, reviewed deployed SHA and runtime/schema attestation | Pending; source equivalence does not prove a live revision |

## Executed results

| Layer | Source / environment | Passed | Failed/errors | Skipped | Evidence |
|---|---|---:|---:|---:|---|
| Backend local verify | d84f071, Java 21.0.6, Maven 3.9.16, isolated H2 | 322 of 350 discovered | 0 | 28 PostgreSQL/Testcontainers | BUILD SUCCESS at 09:14:07 ICT; Docker daemon unavailable |
| Integrated GitHub CI | d84f071, disposable PostgreSQL + Testcontainers | 352 backend / 121 frontend / 6 URL guards | 0 | 0 backend | [Run 37868211120](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37868211120), [saved summary](../../test/evidence/sirapat-public-2026-10-09/baseline-ci.json) |
| Changed frontend | production source at 11edeb0 | 125 in 14 files | 0 | 0 | Tests 09:24:12 ICT; lint 0 errors, 4 existing warnings; build passed |
| Public HTTPS API | real requests/cookies, no mocks | 12 scenario groups | 0 | — | [API results](../../test/evidence/sirapat-public-2026-10-09/api/results.json), 09:15:41–09:16:19 ICT |
| Public browser | manual CUA, real HTTP/cookies, no mocks | 12 scenario groups | 0 | — | [Browser results](../../test/evidence/sirapat-public-2026-10-09/browser/results.json), 09:17:29–09:29:47 ICT |
| Changed UI browser | local HTTP, isolated H2, real missing-session API | 1 | 0 | — | [Thai guidance](../../test/evidence/sirapat-public-2026-10-09/local-fix/results.json); not deployed |
| Screenshot inspection | 32 public screenshots + 1 local fix | 33 images inspected | 0 layout issues observed | — | [Manifest](../../test/evidence/sirapat-public-2026-10-09/browser/screenshot-manifest.json), [visual review](../../test/evidence/sirapat-public-2026-10-09/visual-qa.json) |

Scenario groups are not JUnit/Vitest case counts. Local backend skips do not erase the separate CI PostgreSQL result. The initial sandbox Java/Vitest launcher attempts were not acceptance runs; the completed reruns above provide the results. Four existing lint warnings are in StaffServingPage, KitchenBoardPage, StockPage and UsersPage; no new warning was introduced by the QR change.

## Public cases and traceability

| Requirement | Observation |
|---|---|
| Auth/roles | Manager and three user-authorized test accounts logged in; bad credentials 401; anonymous guards; 15 role-matrix requests with spoofed header (11 denied, 4 permitted); explicit logout revoked all four staff roles |
| Catalog | Real Manager UI created/edited category and menu, reloaded persisted values, changed sorting and opened/cancelled delete confirmation. API pagination/invalid-sort checks passed. Public permanent deletion was not executed; backend/component/CI tests supply deletion rule coverage |
| Session/QR | Two independent native HTTP cookie jars redeemed separate one-use QRs; reused QR rejected; no credential field in responses. Browser used one customer tab and verified fragment removal, reload restoration and fresh QR cart reset |
| Order/State | Quantity/category/cart/confirmation produced one RECEIVED order. API invalid items/quantities rejected without mutation; 12 rejected State transitions; Kitchen PREPARING/READY and Staff SERVED reflected in customer view |
| Bill/payment/close | Session 1 API paid 997.50; session 2 browser paid 1247.50 (two adults, one child at snapshotted package 499). Bill request blocked both API customers. Duplicate payment 409; explicit close set table AVAILABLE and revoked customers |
| Stock | Inactive writes 409 and wrong role 403 preserved quantity/history; browser disabled movement controls; Manager reactivation confirmation; Supervisor stock-in +1 and adjustment +1 persisted after reload. Final test stock quantity/target 5.125, shortfall 0, history 4 |
| Profile | API validation/role guards; Manager edited only the generated Staff profile; first/last required, maxlength 100, optional phone. UI persisted after reload. Screenshots crop the editor to exclude unrelated user details |
| Responsive/states | Customer 360; Staff/Kitchen 768; Manager 1280; Supervisor Stock and Profile editor 360/768/1280. Page scrollWidth never exceeded innerWidth in screenshot checks. Stock tables retain their intended internal horizontal scrolling. Empty Kitchen, missing/revoked QR and invalid login observed; other forced loading/error/race fixtures remain component or historical evidence |
| Thai recovery | Missing, invalid, expired session, inactive QR, closed session and bill-request errors map known server text to Thai; unknown server messages remain intact. Missing/expired/inactive recovery and explicit used-QR retry tested; missing-session local browser inspected |

The public browser tabs share a browser cookie context. Independent customer credentials were tested by the API runner, not two physical devices or two independent browser profiles. Desktop viewport emulation is not a real mobile-device claim. Controlled remount/StrictMode/late-response cases are covered by the 125-test suite and historical fixture reports, not a newly forced public delay.

## Test data and repeatability

Run label QA261009Sirapat. Generated Supervisor/Service Staff/Kitchen credentials live only in ignored local test/reports/sirapat-public-access.json. No password, cookie value, QR token or storage state belongs in the committed evidence. Sessions 1 and 2 are paid and explicitly closed; table 1 is AVAILABLE. Test masters/accounts/stock/history remain visibly labelled for audit; no unrelated account was changed and no delete/migration/deploy was performed.

[API runner instructions](../../test/README.md#public-https-api-regression) use explicit HTTPS origins, approved test-data flag, unique label and Manager environment credentials. The runner creates labelled records and accounts, so do not rerun it casually against shared public data. It never directly connects to the shared database. Human browser observations are recorded separately.

The executed runner at 11edeb0 has SHA256 eb2a474092bd7a21c7542966fa93e560cecd38de7a6d4d8e1151830d9f51a0b3. Its output key denialsWithSpoofedHeader was inaccurately named: 15 refers to all role requests, including 4 permitted. A subsequent metadata-only change renames the count and explicitly records 11 denied/4 allowed; it does not alter requests/assertions or retroactively rerun the public suite.

## Owner deliverables and remaining gates

Updated test plan, requirement traceability, regression checklist, canonical hashes, API/browser evidence, own SOLID/JPA rationale and [three QA slide notes](../slide/sirapat-public-quality-2026-10-09.md). The course's current minimum is 15 meaningful merged commits/person. Integrated d84f071 contains 30 non-merge author candidates for Sirapat; count alone does not certify meaningfulness, identity or review history. Team members' missing commits are their own gate; no artificial commits were created to meet a number.

- Reviewer ปวริศช์: integrated architecture/E2E/traceability and merge decision.
- Reviewer ศรัณย์: role/ErrorResponse/State API audit.
- Runtime owner ธีรเมธ / release owner ปวริศช์: merge/redeploy the Thai fix, attest live SHA/runtime/schema and rerun changed public recovery.
- Timed customer expiry after 8 hours and staff-session TTL: not executed. Secure/HttpOnly/SameSite and customer Max-Age 28800 are observed configuration/cookie evidence, not proof that a time elapsed.
- Real independent browser/mobile devices and release acceptance: pending owner validation if required by Final.
- Canonical Canva v02b remains a draft; own notes are ready for team merger. Shared deck reorder/review, Canva visual QA, export and five-person rehearsal remain pending.

Do not mark the whole Final/public task complete until these gates have evidence.
