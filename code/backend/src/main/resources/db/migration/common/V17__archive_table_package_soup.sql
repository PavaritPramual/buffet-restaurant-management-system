-- R01-B forward migration. Team reservation: Methus V16, Pavarit V17, Sirapat V18.
-- V1-V15 are unchanged. No history is deleted. Apply only after approval.
ALTER TABLE restaurant_tables ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE buffet_packages ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE soups ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE restaurant_tables ADD CONSTRAINT chk_archived_table_available CHECK (NOT archived OR status = 'AVAILABLE');
ALTER TABLE buffet_packages ADD CONSTRAINT chk_archived_package_inactive CHECK (NOT archived OR NOT active);
ALTER TABLE soups ADD CONSTRAINT chk_archived_soup_inactive CHECK (NOT archived OR NOT active);
