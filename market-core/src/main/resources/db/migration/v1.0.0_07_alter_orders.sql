TRUNCATE TABLE orders CASCADE;

ALTER TABLE orders
    ADD COLUMN user_id BIGINT NOT NULL REFERENCES users (id),
    ADD COLUMN user_order_id BIGINT NOT NULL,
    ADD CONSTRAINT unique_user_id_user_order_id UNIQUE (user_id, user_order_id);