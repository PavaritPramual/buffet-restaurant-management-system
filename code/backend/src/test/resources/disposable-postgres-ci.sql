-- Run only inside a newly created CI PostgreSQL service, never a shared server.
CREATE ROLE anon NOLOGIN;
CREATE ROLE authenticated NOLOGIN;
CREATE DATABASE buffet_test_menu_ci;
COMMENT ON DATABASE buffet_test_menu_ci IS 'buffet-disposable-test-only';
CREATE DATABASE buffet_test_dining_ci;
COMMENT ON DATABASE buffet_test_dining_ci IS 'buffet-disposable-test-only';
