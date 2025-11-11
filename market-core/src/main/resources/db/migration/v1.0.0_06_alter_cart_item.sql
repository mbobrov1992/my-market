TRUNCATE TABLE cart_item CASCADE;

ALTER TABLE cart_item
    ADD COLUMN cart_id BIGINT NOT NULL REFERENCES cart (id) ON DELETE CASCADE,
    DROP CONSTRAINT unique_item_id,
    ADD CONSTRAINT unique_cart_id_item_id UNIQUE (cart_id, item_id);