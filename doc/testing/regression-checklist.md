# Regression checklist

Run this list for changes to Menu, Ordering, Session contracts, API conventions, or frontend shared components. Record failures and the tested commit in the PR.

- [ ] Backend tests pass; frontend tests, lint and build pass.
- [ ] Valid active session shows only available items in its package.
- [ ] Unavailable, out-of-package, duplicate, zero and negative quantity requests are rejected without creating an order.
- [ ] Customer can add/remove quantities, submit once, and see a `RECEIVED` order.
- [ ] Loading, empty, and API error states explain what happened without browser alerts.
- [ ] Customer layout remains usable at roughly 360px width and at desktop width.
- [ ] Category and item create/edit/delete work; deleting a category with items returns a clear error; an item with order history is retained and directs staff to mark it unavailable.
- [ ] Pagination and sorting give stable results when there are more items than one page.
- [ ] Optional menu image appears when set, while a menu without an image still renders cleanly.
- [ ] Unknown or lowercase shared enum values still return 400 with `ErrorResponse`.
- [ ] No automated test touches shared Supabase data or commits secrets.
- [ ] After integration: session token, authorization, Kitchen order flow and Billing trigger work with real adapters.

## Order Fulfillment (Kitchen & Serving)

Run this section for changes to `service/state/*State.java`, `OrderFulfillmentService`,
`OrderFulfillmentAccessProvider` (and its fixture/disabled implementations),
`OrderFulfillmentController`, the Kitchen board, or the Staff serving page.

- [ ] Backend tests pass (`OrderStateTest`, `OrderFulfillmentServiceTest`, `FixtureOrderFulfillmentAccessProviderTest`, `OrderFulfillmentControllerTest`, `OrderFulfillmentIntegrationTest`); frontend tests, lint and build pass.
- [ ] Kitchen can move an order `RECEIVED -> PREPARING -> READY`; staff can move `READY -> SERVED`.
- [ ] Skipping a status (e.g. `RECEIVED -> READY`), reversing one (e.g. `PREPARING -> RECEIVED`), and changing a `SERVED` order are all rejected with a `400 Bad Request` `ErrorResponse`, and the order's stored status is unchanged.
- [ ] An unknown order id returns `404 Not Found`.
- [ ] A request to `/orders/incoming`, `/orders/ready`, or `PATCH /orders/{id}/status` with no (or an unrecognized) `X-User-Role` header returns `401 Unauthorized`.
- [ ] `SERVICE_STAFF` cannot access `/orders/incoming` or start-preparing/mark-ready; `KITCHEN_STAFF` cannot access `/orders/ready` or mark-served. Both cases return `403 Forbidden` and leave the order's status unchanged. `SUPERVISOR`/`MANAGER` can do both.
- [ ] Kitchen board shows table number, items, created time and status for every `RECEIVED`/`PREPARING` order, oldest first, with loading/empty/error states.
- [ ] Staff serving board shows only `READY` orders, shows each order's created time, and removes an order from the list once marked served.
- [ ] Both boards refresh automatically (poll) without a manual click — this must actually happen, not just be promised in the on-page copy — in addition to the manual "อัปเดต" button.
- [ ] Kitchen board's table/item-count line uses the `kitchen-sample` typography (20px, bold) documented in `design-system.css`'s Kitchen KDS sample.
- [ ] `doc/diagrams/order-fulfillment-state-diagram.md` still matches the transitions implemented in `service/state/*State.java`.
- [ ] No automated test touches shared Supabase data or commits secrets.
