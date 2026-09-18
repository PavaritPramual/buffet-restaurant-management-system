# Shared test conventions

## Ownership and naming

The feature owner writes tests for the feature. The SQA owner maintains this convention, reviews coverage, and helps make acceptance criteria testable. Name tests `action_whenCondition_expectedResult`, so a failing test states the affected rule.

Use Given / When / Then in each test: establish a small fixture, perform one action, assert externally observable behavior. Keep test data inside the test or a clearly named builder. Avoid order dependence and time-sensitive assertions; inject a clock when timestamps matter.

## Test levels

| Level | Purpose | Example |
|---|---|---|
| Unit | Business rules without Spring or a database | `placeOrder_whenQuantityIsZero_rejectsRequest` |
| Controller | HTTP validation, status, JSON and error contract with mocked service | `postOrder_whenBodyIsInvalid_returns400` |
| Integration | Real API/persistence boundary in an isolated environment | `placeOrder_whenValid_persistsItemsAndReceivedStatus` |

Cover success, invalid input, unavailable menu item, item outside package, inactive session, and repeated submission where relevant. A test should assert a rule or boundary, not duplicate implementation details. Use canonical JSON fixtures for cross-module payload shape, then create isolated local test records for behavior.

## Data isolation and review

Never run automated tests against the team's shared Supabase data. Integration tests need a dedicated disposable database/schema; migrations must run against that isolated target. Do not commit credentials. Before a PR, run the backend test suite and frontend lint/build, report the results, and link the acceptance criteria. The reviewer checks both happy and failure paths.

The feature owner performs module tests. Pairwise API/database tests belong at the integration checkpoint after both sides have working implementations. The SQA owner tracks requirements against tests and reviews the final test report; they do not write every module's tests.
