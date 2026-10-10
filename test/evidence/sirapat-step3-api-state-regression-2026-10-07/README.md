# API/State and regression evidence — 7 October 2026

Owner: Sirapat. [Report](../../../doc/testing/sirapat-step3-api-state-regression-report.md). Integrated baseline `cb612d9`; browser source `f97ef42`. Local H2 only; public acceptance remains pending.

| Artifact | Meaning |
|---|---|
| `verification-summary.json` | Counts, source identity, timestamps, runtime versions and pending gates |
| `backend-summary.json`, `backend-result.txt` | Fresh H2 Maven verify: 334 discovered / 307 passed / 27 skipped / zero failures and errors |
| `frontend-test.txt`, `frontend-lint.txt`, `frontend-build.txt` | 116 Vitest passes, lint 0 errors / 4 existing warnings, successful build |
| `core-flow/results.json` and 17 PNGs | Actual HTTP/cookies/browser, 16 groups PASS including 12 denied State requests; no HTTP mocks |
| `ui-state-fixtures/results.json` and 12 PNGs | Controlled loading/empty/error states at 360/768/1280px; `httpMocks:true` |
| `concurrency-fixtures/browser-concurrency-results.json` and 3 PNGs | Three controlled UI races, delayed responses/injected 503; `httpMocks:true` |
| `runtime-summary.json`, `runtime-startup.txt` | In-memory H2, session/database providers, no env-file import or demo seed |
| `runtime-harness.cjs` | Exact local invocation/setup snapshot; passwords generated in memory and not included |
| `source-hashes.json` | SHA256 manifest for tested runtime/test sources |
| `canonical-source-hashes.json` | Reproducible SHA256 of all 335 unmodified Git blobs at the tested commit `f97ef42` |
| `tested-docs-diff.json` | Two documentation edits present while the browser ran; explains the dirty-tree marker |
| `visual-qa.json` | Inspection record for all 32 screenshots |

The runtime harness snapshot uses the original machine's Java/Maven/Playwright paths and is evidence, not a portable launcher. For a new run follow [the testing guide](../../../test/README.md), build the backend, run Vite and the backend against a fresh isolated database with real session/database providers, privately create four role accounts, and provide the `FINAL_*` environment variables to the three scripts under `test/browser/`. The H2 runtime and its generated accounts were discarded after this run. Never use shared Supabase for these tests.

State denials read persisted customer history after every request and validate ErrorResponse fields. The wrong-role permission guard can return 403 before evaluating a bad transition. Four-role 401 recovery uses server-side invalidation; this is distinct from timed TTL expiry.

## Canonical source identity added after PR #29 review

The original `source-hashes.json` hashes working-directory bytes from the old machine. Keep it as historical evidence: it is not a canonical Git manifest. The reviewer reproduced 333/335 raw hashes, reproduced `common.css` after CRLF conversion, and could not reproduce `MenuAdminPage.tsx` with ordinary LF/CRLF conversion. The exact historical working bytes of the latter were not archived, so this addition does not claim that mismatch has been explained retrospectively.

`canonical-source-hashes.json` instead hashes the unmodified blobs of **the recorded tested revision `f97ef42872b5d0fd585d475815166b42b021bc62`**. It includes Git blob IDs, SHA256 and file modes for all 335 sources. Production runtime/test sources did not change between that revision and submitted head `600013a`. Reproduce and verify without checkout transformations:

```text
node test/browser/evidence-source-hashes.cjs verify f97ef42872b5d0fd585d475815166b42b021bc62 test/evidence/sirapat-step3-api-state-regression-2026-10-07/canonical-source-hashes.json
```

The follow-up report dated 8 October records fresh tests/browser runs against explicitly identified revisions with canonical manifests; it is stronger evidence for the current source than interpreting old checkout bytes.

The screenshots mask the QR credential. Two customer contexts represent multi-device behavior in one browser, not physical phones. Controlled fixtures do not prove PostgreSQL locking, public HTTPS, Secure cookies or Final release acceptance. GitHub CI for the submitted PR is linked in its description and Notion separately from these local results.
