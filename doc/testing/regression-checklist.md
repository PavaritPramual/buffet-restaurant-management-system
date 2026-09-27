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
