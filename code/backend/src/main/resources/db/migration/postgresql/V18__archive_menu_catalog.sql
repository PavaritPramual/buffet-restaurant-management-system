-- Team allocation: R01-A Sirapat V18 (PR46 reservation). No FK/RLS/grant changes.
ALTER TABLE menu_categories ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE menu_items ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE NULL;
CREATE INDEX idx_menu_categories_archived ON menu_categories(id) WHERE archived_at IS NOT NULL;
CREATE INDEX idx_menu_items_archived ON menu_items(id) WHERE archived_at IS NOT NULL;
