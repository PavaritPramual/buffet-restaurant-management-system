# Acceptance criteria template

## Task

- Owner:
- Feature and scope:
- Inputs and consumed contract:
- Outputs and published contract:
- Files or module owned:
- Dependencies needed for integration (not for fixture-based development):

## Given / When / Then criteria

1. Given **[initial state]**, when **[action]**, then **[observable result and status]**.
2. Given **[invalid or boundary state]**, when **[action]**, then **[error response and unchanged data]**.
3. Given **[cross-module fixture]**, when **[action]**, then **[published payload matches the shared contract]**.

## Evidence and done

- Unit, controller, and integration cases relevant to this change:
- Isolated test-data method:
- Backend test command/result:
- Frontend lint/build command/result:
- API example or Swagger endpoint:
- PR, reviewer, and demo evidence:

## Example: RestaurantTable

Given an available table, when staff changes its status to occupied, then the response contains `OCCUPIED` and the table remains occupied on a subsequent read. Given an unknown table ID, when staff requests it, then the API returns the agreed error shape and does not create a table. The table owner supplies the actual endpoint and status rules before implementation.
