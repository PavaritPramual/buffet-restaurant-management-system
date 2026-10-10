# Pavarit integrated validation — 8 October 2026

[Thai report](../../../doc/testing/pavarit-integrated-validation-2026-10-08.md). Runtime/runner revision `adc5798` is merged develop after PR28/31/32; owner documentation/diagrams revision is `8efe5b5`. Documentation was dirty during browser execution, with an empty runtime/runner diff. No runtime bugs required fixes in this run.

| Artifact | Scope |
|---|---|
| backend-summary.json | Fresh H2 and marked PostgreSQL18 test databases; 348 tests, no fail/error/skip, every suite recorded |
| frontend-*.txt / validate.txt | 121 frontend tests, six guards, lint0errors/4existingwarnings, build and Mavenvalidate |
| browser/core-flow | Real HTTP/session/database-provider browser16/16, including12State denials |
| browser/stock-profile | Real merged Stock/Profile UI/HTTP8/8 |
| browser/ui-state-fixtures | Controlled12loading/empty/error cases, not real endpoint responses |
| browser/concurrency-fixtures | Three controlled response-race cases, not PostgreSQL locking proof |
| browser/stock-profile-fixtures | Ten controlled states including legacyfallback; real legacy migration covered by automated tests |
| screenshots.json | 53 images with hashes/sizes; all inspected in contact sheets, QRmasked |
| canonical-source-hashes.json | 348 canonical Git blobs/modes/SHA256 at tested revision |
| validation.json / verify.py | Owner source anchors, seven diagram hashes, backend/browser counts and source/reference validation |
| verification-result.json | Passed source/links/diagram/evidence verification, separate from public acceptance |

`python test/evidence/pavarit-integrated-2026-10-08/verify.py` verifies the snapshot without connecting to any database. Original runtime runner hashes are checkout bytes; canonical hashes are Git blob bytes. CRLF differences are not treated as different production code.

Passwords are generated in memory by the existing isolated launcher, never stored. Env imports/demo seed disabled. Runtime and disposable database stopped after use. No Supabase/migration/public acceptance in this evidence; release/public tests and review/merge remain separate gates.
