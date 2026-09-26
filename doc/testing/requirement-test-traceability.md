# Requirement to test traceability — Menu/Ordering

| Requirement | Current evidence | Integration evidence still needed |
|---|---|---|
| Active session sees only available items in its package | `OrderingIntegrationTest.menuForSession_onlyReturnsAvailableItemsInPackage` | Real Session data |
| Valid order starts `RECEIVED` and publishes order fields | `OrderingIntegrationTest.placeOrder_whenValid_persistsReceivedOrderAndReturnsStatus` | Kitchen consumption |
| Item outside package is rejected without saving | `OrderingIntegrationTest.placeOrder_whenItemOutsidePackage_returns400` | Explicit persistence rollback assertion |
| Inactive session cannot order | `OrderingIntegrationTest.placeOrder_whenSessionInactive_returns400` | Real Session status lookup |
| Quantity must be positive | `OrderingIntegrationTest.placeOrder_whenQuantityIsZero_returns400WithoutSavingOrder` | Browser validation and negative quantity |
| Menu pagination and sorting | `OrderingIntegrationTest.menuItems_supportPaginationAndSorting`; `MenuAdminPage.test.tsx` final-page deletion case | Additional database sorting fields |
| Package references must exist | `OrderingIntegrationTest.createMenuItem_whenPackageDoesNotExist_returns400WithoutSavingItem`; `MenuAdminPage.test.tsx` package selection case | Inactive-package product rule |
| Optional image URL appears in customer menu | `CustomerOrderingPage.test.tsx` image case | Backend persistence round trip and browser preview |
| Category and menu item CRUD | Menu item create and read paths in `OrderingIntegrationTest` | Complete update/delete persistence tests and staff authorization |
| Customer image, cart, submission, invalid session | `CustomerOrderingPage.test.tsx` | Integrated QR flow |
| Admin selects packages from catalog | `MenuAdminPage.test.tsx` | Staff authorization and integrated browser CRUD |
| Mobile customer layout | No automated evidence yet | Manual 360px browser check and additional devices |
| Shared error response | `EnumErrorResponseTest`; error assertions in `OrderingIntegrationTest` | All modules under integrated exception handling |

Update this table with PR links and actual run results during integration. A route existing in code is not evidence that its database or security boundary works. Recorded suite results are in `doc/testing/sirapat-step2-report.md`.
