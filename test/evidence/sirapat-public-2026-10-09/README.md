# Sirapat public verification — 9 October 2026

See [full report](../../../doc/testing/sirapat-public-regression-2026-10-09.md). Real public URL: https://buffet-restaurant-management-system.onrender.com/.

- API: 12 passed scenario groups, separate cookie jars, no HTTP mocks, 09:15:41–09:16:19 ICT.
- Browser: 12 passed scenario groups, 32 unique JPEG screenshots, shared browser context, 09:17:29–09:29:47 ICT.
- Local changed UI: 1 missing-QR case, Thai guidance, isolated H2; 1 screenshot.
- Source manifests: d84f071 baseline and 11edeb0 submitted production tree, 352 canonical Git blobs each.
- Integrated CI: run 37868211120, backend 352/352 (0 skipped), frontend121/121, guards6/6.
- PR #36 CI at9b9eddf: [run37876116678](https://github.com/PavaritPramual/buffet-restaurant-management-system/actions/runs/37876116678) passed Backend/PostgreSQL352/352, changed Frontend125/125, guards6/6, lint/build; [summary](pr36-ci.json). Subsequent documentation-only evidence update records this result; it does not replace latest-head CI.
- Local backend: 322 passed + 28 skipped = 350 discovered. Changed frontend125/125; lint0 errors/4 existing warnings; build passed.
- Public revision 2f8bc4b is owner-reported, not live-attested. Thai source fix was not deployed during this run.
- No elapsed TTL test, physical mobile test, second browser profile test or public permanent-delete test.

Executed API runner Git blob at 11edeb0: SHA256 eb2a474092bd7a21c7542966fa93e560cecd38de7a6d4d8e1151830d9f51a0b3. sourceCommit in the original API result records the base HEAD before that runner was committed. The subsequent runner metadata correction only renames the 15-request role counter (11 denied/4 permitted). Original execution output is retained unchanged.

Raw local/CI logs and account access are ignored under test/reports. Committed JSON contains cookie attributes, never values. Screenshots omit Staff QR cards and unrelated user profiles. Table and test sessions are closed/AVAILABLE; labelled QA records remain for audit.

[Visual inspection](visual-qa.json), [screenshot integrity](browser/screenshot-manifest.json), [API](api/results.json), [browser](browser/results.json), [baseline CI](baseline-ci.json).
