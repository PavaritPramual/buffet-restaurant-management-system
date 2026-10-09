# Sirapat R01-A evidence — 9 October 2026

Local production frontend + real backend, isolated in-memory H2, actual V18; not public Render acceptance.
Base `69fb7af` plus R01 working-tree source captured in `source-manifest.json`. No credentials or QR tokens included.

| File | Evidence |
|---|---|
| manager-30-menu-items-1280.png | Manager catalog, 30 items / 3 pages, real API |
| manager-removal-confirm-360.png | Actual Manager removal confirmation at narrow viewport |
| customer-order-confirm-360.png | Actual customer confirmation for quantity 2 in 360 × 800 iframe |
| customer-stale-cart-blocked-360.png | Actual archive conflict after Manager removed an item while customer confirmation was pending |
| native-api-results.json | Separate session: 30 real HTTP checks, restore prerequisites/retries, unchanged Order, 598 bill/Payment, normal COMPLETED close |

Screenshots were visually inspected. Outer iframe harness remains visible to identify the 360px test method.
Archive restore UI states use component tests; live browser restore acceptance remains pending.
See [full report](../../../doc/testing/sirapat-r01-a-2026-10-09.md) for counts, limitations and review/deployment gates.
