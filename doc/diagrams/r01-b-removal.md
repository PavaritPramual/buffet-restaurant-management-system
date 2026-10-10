# R01-B — Table / Package / Soup removal delta

Source: [owner design attestation](../architecture/pavarit-r01-b-design-delta.md), baseline `69fb7af` plus R01-B implementation. Mermaid source is embedded so GitHub renders the delta beside its explanation. Existing PlantUML baseline diagrams remain historical; this delta does not certify other owners' V16/V18 or public deployment.

## Entity/FK delta (V17)

```mermaid
erDiagram
    RESTAURANT_TABLES ||--o{ DINING_SESSIONS : "table_id RESTRICT"
    BUFFET_PACKAGES ||--o{ DINING_SESSIONS : "package_id RESTRICT"
    SOUPS ||--o{ DINING_SESSIONS : "soup_id RESTRICT"
    BUFFET_PACKAGES ||--o{ PACKAGE_MENU_ITEMS : "existing CASCADE; service preserves membership"
    RESTAURANT_TABLES {
        bigint id PK
        varchar table_number UK
        varchar status
        boolean archived "V17 default false"
    }
    BUFFET_PACKAGES {
        bigint id PK
        decimal price
        boolean active
        boolean archived "V17 default false; archived implies inactive"
    }
    SOUPS {
        bigint id PK
        boolean active
        boolean archived "V17 default false; archived implies inactive"
    }
    DINING_SESSIONS {
        bigint id PK
        bigint table_id FK
        bigint package_id FK
        bigint soup_id FK
        decimal package_price_at_open "unchanged"
        varchar status "unchanged"
    }
    PACKAGE_MENU_ITEMS {
        bigint package_id PK,FK
        bigint menu_item_id PK,FK
    }
```

No JPA cascade is added. Session relationships remain LAZY; no name snapshot is added. V17 adds only three flags and safe-state checks, retaining applied migrations/FKs/grants. Archived table numbers remain unique/reserved.

## Removal and restore

```mermaid
sequenceDiagram
    actor Manager
    participant UI as MasterDataPage
    participant Boundary as MasterDataAccessConfig / Controller
    participant Service as Table or Catalog Service
    participant Guard as MasterDataRemovalAccessProvider
    participant DB as Repository / PostgreSQL
    Manager->>UI: Confirm removal
    UI->>Boundary: DELETE resource/id
    Boundary->>Boundary: Require Manager
    Boundary->>Service: Remove
    Service->>Guard: Require Manager (direct calls too)
    Service->>DB: Lock resource row in transaction
    alt Table ACTIVE or OCCUPIED
        Service-->>UI: 409 ErrorResponse, unchanged
    else Already archived
        Service-->>UI: 204, no mutation
    else Any session/history or package membership
        Service->>DB: Archive, retain references and snapshots
        Service-->>UI: 204
    else No references
        Service->>DB: DELETE + flush, FK authority remains
        Service-->>UI: 204 (FK conflict rolls back, 409)
    end
    Manager->>UI: Archived list / confirm restore
    UI->>Boundary: POST resource/id/restore
    Boundary->>Service: Restore (Manager checks at both layers)
    Service->>DB: Lock resource
    alt Archived to restored
        Service->>DB: archived=false, table AVAILABLE or catalog inactive
    else Already restored
        Service->>DB: No change, preserve active/OCCUPIED
    end
    Service-->>UI: 200 current DTO
```

Opening locks **Table → Package → Soup** and checks archive/availability after each lock. Table removal and open serialize on the same table row; catalog removal serializes on its package/soup row. Archive may succeed after an open commits, preserving that ACTIVE session and its price snapshot. Existing Billing/Payment/close read the session rather than requiring the catalog to remain active.
