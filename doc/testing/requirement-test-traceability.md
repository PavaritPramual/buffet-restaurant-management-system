# Requirement to test traceability — Menu/Ordering

| Requirement | Current evidence | Integration evidence still needed |
|---|---|---|
| Active session sees only available items in its package | `OrderingServiceTest.menu_whenSessionIsActive_returnsOnlyPackageItems` | Real Session and package data |
| Valid order starts `RECEIVED` and publishes order fields | `OrderingServiceTest.place_whenValid_createsReceivedOrderWithContractFields`; `OrderingControllerTest.postOrder_whenValid_returns201AndReceivedOrder` | Persisted Order/OrderItem and Kitchen consumption |
| Item outside package is rejected without saving | `OrderingServiceTest.place_whenItemOutsidePackage_rejectsWithoutSaving` | Persistence rollback check |
| Inactive session cannot order | `OrderingServiceTest.place_whenSessionIsInactive_rejectsWithoutSaving` | Real Session status lookup |
| Quantity must be positive | Service and controller tests for zero quantity | Browser validation and negative quantity |
| Menu pagination and sorting | `OrderingServiceTest.items_whenPagedAndSorted_isStable` | Database pagination and ordering |
| Optional image URL survives into customer menu | `OrderingServiceTest.saveItem_whenImageUrlProvided_returnsItForCustomerMenu`; controller rejects unsupported scheme | Persistent image URL and browser preview |
| Category and menu item CRUD | API routes and development admin page | Persistence tests and staff authorization |
| Customer image, cart, submission, invalid session | `CustomerOrderingPage.test.tsx`; manual browser check in `test/reports/sirapat-module.md` | Integrated QR flow |
| Admin can submit image URL and package IDs | `MenuAdminPage.test.tsx`; manual browser CRUD check | Staff authorization and persistent data |
| Mobile customer layout | Manual 360px browser check in `test/reports/sirapat-module.md` | Integrated QR flow and additional devices |
| Shared error response | Existing `EnumErrorResponseTest` and `OrderingControllerTest` | All modules under integrated exception handling |

Update this table with PR links and actual run results during integration. A route existing in code is not evidence that its database or security boundary works.
