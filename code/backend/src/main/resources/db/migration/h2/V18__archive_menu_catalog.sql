-- Team allocation: R01-A Sirapat V18 (PR46 reservation). Never edit applied migrations.
ALTER TABLE menu_categories ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE menu_items ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_menu_categories_archive ON menu_categories(archived_at, id);
CREATE INDEX idx_menu_items_archive ON menu_items(archived_at, id);
