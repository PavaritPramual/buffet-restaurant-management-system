# Sirapat independent regression evidence — 8 October 2026

[Thai report](../../../doc/testing/sirapat-step3-followup-2026-10-08.md). Two runtime revisions are kept separate: integrated develop `6d83eac` with test runner `4f8bc5a`, and unmerged PR #28 candidate `cc72fa0`. Real browser results do not use HTTP mocks; controlled states and concurrency explicitly do. Public acceptance is pending.

| Folder/artifact | Scope |
|---|---|
| `develop/` | Local backend310 passed/27 skipped; frontend118; Core Flow16/16; UI states12; concurrency3/3; screenshots32 |
| `stock-profile-candidate/` | Local backend318 passed/28 skipped; frontend121; Core Flow16/16; real Stock/Profile8/8; controlled states10; screenshots38 |
| `*/backend-summary.json`, `backend-result.txt` | Surefire suite counts and sanitized Maven results |
| `*/frontend-*.txt` | Test/lint/build logs; 4 existing warnings in each revision |
| `*/canonical-source-hashes.json` | Exact Git blob IDs/modes/SHA256 for each runtime/test revision |
| `runner-source-hashes.json` | Final runner revision `b8084aa`; candidate scripts were copied outside tracked source while testing detached `cc72fa0` |
| `*runtime-startup.txt`, `*runtime-summary.json` | Fresh in-memory H2, migrations/provider setup, no env-file/demo seed; processes stopped after runs |
| `verification-summary.json`, `screenshot-index.json`, `visual-qa.json` | Scope/counts/time/source identity; all70 screenshots inspected |

The original raw PR #29 manifest remains historical. Its [canonical companion](../sirapat-step3-api-state-regression-2026-10-07/canonical-source-hashes.json) uses tested commit `f97ef42`, independent of checkout LF/CRLF/BOM behavior. Do not reinterpret an old unexplained checkout-byte mismatch as a passing canonical comparison.

## Reproduction

1. Checkout the runtime revision, install its frontend lockfile, and build the backend against isolated test settings. Never run against shared Supabase.
2. Set `PLAYWRIGHT_MODULE` and `FINAL_JAVA` to installed runtimes if required. `step3-local-runtime.cjs` starts fresh H2 with real session/database providers, generates private random accounts and waits for bootstrap login readiness. No password values are saved.
3. On the follow-up test branch run `node test/browser/step3-local-runtime.cjs`. The default runners are Core Flow, UI states and concurrency. `FINAL_RUN_LABEL` and `FINAL_LOCAL_OUTPUT` choose separate output paths.
4. To verify feature code before merge, copy the follow-up `test/browser/*.cjs` into an ignored local directory, then checkout the candidate runtime revision. Set `FINAL_LOCAL_SCRIPTS` to the copied Core Flow/StockProfile/StockProfileStates script paths. Record runtime revision and runner revision separately; the relative runtime-config helper must be copied too. Each invocation starts a separate H2 database.
5. Canonical verification: `node test/browser/evidence-source-hashes.cjs verify <recorded-commit> <canonical-source-hashes.json>`. The verifier compares every stored field and hash against raw Git blobs without checkout transformation.

The candidate Core Flow run passed before a Stock test locator failure. The Stock/UI fixture sections were repaired and rerun independently; successful component reports have their own timestamps. Preliminary harness failures are documented in the report and excluded from acceptance. Server-side invalidation is distinct from timed expiry. Two customer contexts are not physical-device testing. Local/CI/candidate results do not approve central migration, merge or public release.
