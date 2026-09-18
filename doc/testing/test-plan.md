# Test plan — Buffet Restaurant Management System

Owner of this plan and test process: ศิระพัทธ์. Each feature owner writes tests for their own module. The plan is updated when the integrated system and deployment target become available.

## Objectives and scope

Prove that the customer can use a valid session to see allowed menu items, place one valid order, and see its status; that invalid requests leave order data unchanged; and that staff can maintain categories and menu items. Confirm the shared JSON contract, isolated test data, and the basic frontend states (loading, empty, error, narrow screen).

Unit tests cover business rules without network or database. Controller tests cover validation, HTTP status, and ErrorResponse. Integration tests will cover the real Supabase/JPA adapter using a dedicated disposable database/schema. Browser checks cover the customer path and menu admin path. Pairwise tests with Session, Kitchen, Billing, and Authentication occur after those owners publish their implementations.

## Environments and data

- Local independent development: `DemoOrderingStore` with process-local session 1 and package 1. Data resets when backend restarts.
- Automated backend tests: isolated in-memory fakes and Spring MVC test context. No shared Supabase project.
- Future persistence tests: dedicated disposable database/schema with migrations applied from scratch; never use the team's shared project.
- Cross-module payload examples: `test/fixtures/` and `doc/contracts/shared-contracts.md`.

## Entry and exit criteria

Entry: the module contract and fixture are agreed; the branch is based on current `develop`. Exit for a module PR: backend tests, frontend lint/build, and relevant browser checks pass; acceptance criteria and limits are recorded; another team member reviews. Integration is complete only after real Session and persistence adapters, authorization, Kitchen/Billing handoff, and integration tests pass.

## Responsibilities

ศิระพัทธ์ maintains this plan, traceability, regression checklist, Menu/Ordering tests, and frontend quality review. Other feature owners test their own rules. ศรัณย์ reviews API/JSON changes, ปวริศช์ reviews shared architecture/session integration, เมธัส reviews migrations and authentication boundary, and ธีรเมธ reviews deployment/browser connectivity.
