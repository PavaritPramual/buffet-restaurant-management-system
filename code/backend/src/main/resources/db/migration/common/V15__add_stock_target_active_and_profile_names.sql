-- Forward-only Final migration (V15). Do not edit V1-V14.
-- Existing stock rows get target 0 and active TRUE; quantity/low_stock_threshold are untouched.
ALTER TABLE stock_items ADD COLUMN opening_target_stock DECIMAL(12, 3) NOT NULL DEFAULT 0;
ALTER TABLE stock_items ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE stock_items ADD CONSTRAINT chk_stock_opening_target_nonnegative CHECK (opening_target_stock >= 0);

-- Legacy profiles stay NULL: names are never guessed from display_name. The API requires
-- first/last name for new accounts, so old rows remain valid until a Manager fills them in.
ALTER TABLE user_profiles ADD COLUMN first_name VARCHAR(100);
ALTER TABLE user_profiles ADD COLUMN last_name VARCHAR(100);
ALTER TABLE user_profiles ADD COLUMN phone_number VARCHAR(20);
