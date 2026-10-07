# Menu/Ordering — SOLID and JPA contribution

Owner: ศิระพัทธ์. Source baseline: 54e3538, integrated develop, 6 October 2026. ปวริศช์ can integrate these notes into final docs. Recheck against release commit; this module does not implement all course patterns itself.

## SOLID examples

Controllers validate HTTP input and call services; services enforce rules in transactions, repositories persist, OrderingMapper produces DTOs. Entities are not the public JSON contract.

| Principle | Source | Reason and limit |
|---|---|---|
| S | [MenuCatalogServiceImpl](../../code/backend/src/main/java/com/buffetrestaurant/service/impl/MenuCatalogServiceImpl.java), [CustomerOrderingServiceImpl](../../code/backend/src/main/java/com/buffetrestaurant/service/impl/CustomerOrderingServiceImpl.java), [OrderingMapper](../../code/backend/src/main/java/com/buffetrestaurant/mapper/OrderingMapper.java) | Catalog maintenance, customer ordering and mapping have separate responsibilities. Customer page coordinates several UI states; do not claim perfect decomposition. |
| O | [MenuAdminAccessProvider](../../code/backend/src/main/java/com/buffetrestaurant/service/MenuAdminAccessProvider.java), session/fixture/disabled implementations | Services receive a selected authorization provider rather than Auth conditionals in each mutation. This seam does not make every business rule extension-free. |
| L | [SessionMenuAdminAccessProvider](../../code/backend/src/main/java/com/buffetrestaurant/service/SessionMenuAdminAccessProvider.java), [DisabledMenuAdminAccessProvider](../../code/backend/src/main/java/com/buffetrestaurant/service/fixture/DisabledMenuAdminAccessProvider.java) | requireMenuWriteAccess returns normally only for permitted writes; denial follows exception contract. Fixture is valid only in explicit isolated tests, never production. MenuCatalogIntegrationTest/MenuAdminDisabledIntegrationTest exercise allowed/denied contracts. |
| I | [CustomerOrderingService](../../code/backend/src/main/java/com/buffetrestaurant/service/CustomerOrderingService.java), [MenuCatalogService](../../code/backend/src/main/java/com/buffetrestaurant/service/MenuCatalogService.java), [SessionContextProvider](../../code/backend/src/main/java/com/buffetrestaurant/service/SessionContextProvider.java) | Customer interface excludes catalog mutations. Session interface supplies ID/package/table/status and order-time access without the whole Table/Auth implementation. |
| D | Constructor injection in catalog/ordering services | Rules depend on provider/repository abstractions. Database provider owns cookie/hash/state/locking details; disabled provider fails closed. |

These are reasoned examples, not an automatic SOLID score. Enterprise patterns: Service Layer, Spring Data Repository, request/response DTO and Mapper. State belongs to Ordering/Fulfillment (ศรัณย์); Strategy/Template Method belong to their owners. Do not relabel ordinary conditionals as those patterns.

## Menu and PackageMenuItem mapping

The design's **PackageMenuItem is a join-table concept, not a Java entity**. [MenuItem](../../code/backend/src/main/java/com/buffetrestaurant/domain/MenuItem.java) owns Set<Long> packageIds via LAZY ElementCollection and CollectionTable package_menu_items. It stores membership only, with no relation-specific price/quantity. An association with its own state would need an entity and reviewed forward migration.

| Relation | JPA choice | Database / business rationale |
|---|---|---|
| MenuItem → MenuCategory | ManyToOne LAZY, optional=false; no cascade | Shared category has independent lifecycle. V4 FK ON DELETE RESTRICT; service rejects deleting non-empty category. Deleting a menu must not delete its category. |
| MenuItem → package IDs | LAZY ElementCollection; no CascadeType entity relation | Values belong to menu; set updates modify join rows, not BuffetPackage entities. V4 FKs cascade membership rows when either parent is deleted. SQL cascade is distinct from JPA cascade. |
| OrderItem → historical menu | Service rejects menu deletion with order history | V4 menu FK ON DELETE RESTRICT. Mark unavailable; item_name/table_number snapshots retain historical meaning. |

LAZY avoids loading all associations for every operation. [MenuItemRepository.findAvailableForPackage](../../code/backend/src/main/java/com/buffetrestaurant/repository/MenuItemRepository.java) uses EntityGraph(category,packageIds) when Customer DTOs need them. Mapping occurs in transactional service methods with open-in-view=false, before JSON serialization.

Paginated Admin findAll(PageRequest) uses default mapping and may issue extra selects. No query-count benchmark was run, so do not claim every catalog query avoids N+1. Collection fetch joins require pagination review.

## Evidence and reviewer handoff

- MenuCatalogIntegrationTest: CRUD/roles/deletion history/non-empty categories/sorting/validation.
- OrderingIntegrationTest: package membership/unavailable items/snapshots/invalid input without saving.
- MenuOrderingMigrationTest and PostgresMenuOrderingMigrationTest: fresh/upgrade metadata/FK/grants; PG requires marked disposable database.
- Customer frontend: fragment removal, StrictMode, latest scan, consumed-QR retry, confirmation and duplicate-submit protection.

Review: ปวริศช์ architecture/integration, ศรัณย์ API/DTO, เมธัส mapping/schema. Final docs cite reviewed release commit, not baseline alone.

## Order and OrderItem cascade/fetch rationale

`CustomerOrder.items` is the aggregate-owned collection: `@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true, fetch = LAZY)`. Creating the order and its line snapshots is one service operation, so persisting the parent should persist only its dependent `OrderItem` rows. Removing an item from the collection deletes that owned row; it does not cascade to a `MenuItem`, session, or another order. The current API does not expose order deletion, so cascade is a persistence-lifecycle rule and not permission to erase historical orders.

`OrderItem.order` is `@ManyToOne(fetch = LAZY, optional = false)` with no cascade. The order is the shared lifecycle owner; saving or deleting one line must not create/delete the parent. The database FK uses `ON DELETE CASCADE` only when the parent order is deliberately deleted; the menu FK uses `ON DELETE RESTRICT` so menu history remains valid. `menuItemId` and `itemName` are scalar/snapshot fields rather than an eager JPA association.

Both collection and parent links are lazy to avoid loading order graphs for unrelated reads. The order response mapper reads line snapshots inside service transactions, and `spring.jpa.open-in-view=false` prevents serialization from triggering hidden database queries. `CustomerOrderingServiceImpl` builds the order and adds its items before saving; `OrderFulfillmentIntegrationTest` checks returned order line data. We have not run a query-count benchmark, so this is a lifecycle/fetch rationale, not a claim that every path is free of N+1 queries.
