ALTER TABLE dining_sessions
    ADD COLUMN package_price_at_open DECIMAL(10, 2);

-- Existing test/local sessions predate the snapshot; use the current catalog price as a best-effort backfill.
UPDATE dining_sessions
SET package_price_at_open = (
    SELECT buffet_packages.price
    FROM buffet_packages
    WHERE buffet_packages.id = dining_sessions.package_id
);

ALTER TABLE dining_sessions
    ALTER COLUMN package_price_at_open SET NOT NULL;

ALTER TABLE dining_sessions
    ADD CONSTRAINT chk_dining_sessions_package_price_at_open
    CHECK (package_price_at_open >= 0);
