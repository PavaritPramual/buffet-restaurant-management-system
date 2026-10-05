-- Run only inside a newly created CI PostgreSQL service, never a shared server.
CREATE ROLE anon NOLOGIN;
CREATE ROLE authenticated NOLOGIN;
CREATE ROLE buffet_backend_test LOGIN PASSWORD 'ci-disposable-only';
CREATE DATABASE buffet_test_payment_ci OWNER buffet_backend_test;
COMMENT ON DATABASE buffet_test_payment_ci IS 'buffet-disposable-test-only';
CREATE DATABASE buffet_test_menu_ci;
COMMENT ON DATABASE buffet_test_menu_ci IS 'buffet-disposable-test-only';
CREATE DATABASE buffet_test_dining_ci;
COMMENT ON DATABASE buffet_test_dining_ci IS 'buffet-disposable-test-only';
