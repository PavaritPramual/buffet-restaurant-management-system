# Regression checklist

## Current independent verification — 9 October 2026

See [public report](sirapat-public-regression-2026-10-09.md). These checks record executed owner evidence; overall Final remains in progress.

- [x] Clean sync to developd84f071; integrated CI backend352/352, frontend121/121, URL guards6/6, no backend skips.
- [x] Local backend322 passed/28 skipped of350; changed frontend125/125, lint0 errors/4 existing warnings, build passed.
- [x] Real public HTTPS API12 groups and browser12 groups; four roles, QR, State denials, bill/payment/close and Stock/Profile persistence.
- [x] Two independent customer cookie jars in API; one customer browser context; limitations explicitly recorded.
- [x] Public32 main +3 supplementary screenshots and local Thai-fix screenshot inspected; canonical Git source hashes and current traceability saved.
- [x] Added30 available foods/5categories; QR customer placed6menu lines/8pieces, Kitchen received and customer reload persisted order3; bill499/session4 closed.
- [x] Own SOLID/JPA rationale rechecked; separate editable3-slide QA PPTX/PDF with complete notes delivered per user's Canva-defer instruction; every slide/PDF page inspected.
- [ ] Review/merge/redeploy Thai QR recovery and recheck the changed public UI.
- [ ] Elapsed8-hour customer expiry / staffTTL, live deployed SHA/runtime/schema and release approval.
- [ ] Canonical Canva owner review, content/order merge, actual Canva export and team rehearsal.

## Current independent verification — 8 October 2026

See [current report](sirapat-step3-followup-2026-10-08.md). These checks record local/candidate evidence, not team release approval.

- [x] Integrated develop6d83eac Core Flow16/16; real State denials12 and Thai unpaid-close message/ACTIVE/OCCUPIED verified after #26/#27/#29 merge.
- [x] PR #28 candidatecc72fa0 Core Flow16/16, real Stock/Profile8/8 and responsive360/768/1280 checked before merge.
- [x] Candidate Stock/Profile controlled loading/empty/error9 + legacy fallback1 separately labelled; all70 screenshots inspected.
- [x] Canonical Git blob hashes and updated test-plan/traceability/report/slide-content draft ready for review.
- [ ] Stock/Profile reviewer/migration gates and regression on final integrated revision.
- [ ] Public same-origin HTTPS, Secure-cookie/timed TTL and final release/main/deployed SHA; team reviewed slides/export/rehearsal.

The following feature-owner lists retain historical verification and owner review status.

Run this list for changes to Menu, Ordering, Session contracts, API conventions, or frontend shared components. Record failures and the tested commit in the PR.

- [x] Backend tests pass; frontend tests, lint and build pass.
- [x] Valid active session shows only available items in its package.
- [x] Unavailable, out-of-package, duplicate, zero and negative quantity requests are rejected without creating an order.
- [x] Customer can add/remove quantities, submit once, and see a `RECEIVED` order.
- [x] Loading, empty, and API error states explain what happened without browser alerts.
- [x] Customer layout remains usable at roughly 360px width and at desktop width.
- [x] Category and item create/edit/delete work; deleting a category with items returns a clear error; an item with order history is retained and directs staff to mark it unavailable.
- [x] Pagination and sorting give stable results when there are more items than one page.
- [x] Late catalog responses cannot override the selected page/sort, show stale errors or dismiss a newer request's loading indicator.
- [x] A history refresh started before order acknowledgement cannot erase that order or report an obsolete error; a fresh refresh still updates status.
- [x] Supervisor cannot open Menu/Users editors through uppercase or trailing-slash URLs; Manager still can.
- [x] A-B-A QR scans execute in order and never render an obsolete session against another scan's cookie; latest duplicate subscriptions still share one POST.
- [x] PostgreSQL order/close tests wait for actual row-lock contention and separately verify revoked credentials after committed close.
- [x] History that observes a persisted order before POST acknowledgement retains one card and its newer kitchen status.
- [x] History refresh recovery clears only its own failure and preserves an order submission failure/cart.
- [x] Returning to Customer during QR exchange waits for the latest scan before reading cookie context; failed scans do not restore an older table and explicit retry works.
- [x] Optional menu image appears when set, while a menu without an image still renders cleanly.
- [x] Unknown or lowercase shared enum values still return 400 with `ErrorResponse`.
- [x] No automated test touches shared Supabase data or commits secrets.
- [ ] Full team integration: production Staff/Fulfillment Auth and Billing trigger/payment-close flow work with real adapters.

Checked Menu/Ordering items above refer to the 4 October 2026 verification in [Sirapat report](sirapat-step2-report.md): frontend 78 passes at that revision; earlier full backend 189 passes / 2 Stock-Docker skips, followed by a focused 38-case recheck. Backend code was unchanged by the second Customer-only corrections. They are historical checkmarks, not current release approval. Fresh integrated-develop verification on 7 October is recorded in the [API/State regression report](sirapat-step3-api-state-regression-report.md); Stock/Profile/public/TTL/release remain pending. The Fulfillment owner still records completion below; its auth criteria now reflect real session-cookie permissions.

## Order Fulfillment (Kitchen & Serving)

Run this section for changes to `service/state/*State.java`, `OrderFulfillmentService`,
`OrderFulfillmentAccessProvider` (and its fixture/disabled implementations),
`OrderFulfillmentController`, the Kitchen board, or the Staff serving page.

- [ ] Backend tests pass (`OrderStateTest`, `OrderFulfillmentServiceTest`, `FixtureOrderFulfillmentAccessProviderTest`, `OrderFulfillmentControllerTest`, `OrderFulfillmentIntegrationTest`); frontend tests, lint and build pass.
- [ ] Kitchen can move an order `RECEIVED -> PREPARING -> READY`; staff can move `READY -> SERVED`.
- [ ] Skipping a status (e.g. `RECEIVED -> READY`), reversing one (e.g. `PREPARING -> RECEIVED`), and changing a `SERVED` order are all rejected with a `400 Bad Request` `ErrorResponse`, and the order's stored status is unchanged.
- [ ] An unknown order id returns `404 Not Found`.
- [ ] A request to `/orders/incoming`, `/orders/ready`, or `PATCH /orders/{id}/status` without a valid employee session cookie returns `401 Unauthorized`; a spoofed `X-User-Role` header grants no access.
- [ ] `SERVICE_STAFF` cannot access `/orders/incoming` or start-preparing/mark-ready; `KITCHEN_STAFF` cannot access `/orders/ready` or mark-served. `SUPERVISOR`/`MANAGER` also cannot perform Kitchen/Serving operations. Wrong-role requests return `403 Forbidden` and leave the stored status unchanged; permission checks precede State-transition checks.
- [ ] Kitchen board shows table number, items, created time and status for every `RECEIVED`/`PREPARING` order, oldest first, with loading/empty/error states.
- [ ] Staff serving board shows only `READY` orders, shows each order's created time, and removes an order from the list once marked served.
- [ ] Both boards refresh automatically (poll) without a manual click — this must actually happen, not just be promised in the on-page copy — in addition to the manual "อัปเดต" button.
- [ ] Kitchen board's table/item-count line uses the `kitchen-sample` typography (20px, bold) documented in `design-system.css`'s Kitchen KDS sample.
- [ ] `doc/diagrams/order-fulfillment-state-diagram.md` still matches the transitions implemented in `service/state/*State.java`.
- [ ] No automated test touches shared Supabase data or commits secrets.
