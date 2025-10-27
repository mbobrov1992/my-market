CREATE TABLE IF NOT EXISTS order_item
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    item_id    BIGINT         NOT NULL REFERENCES item (id) ON DELETE CASCADE,
    count      INTEGER        NOT NULL CHECK (count > 0),
    price      NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at TIMESTAMP      NOT NULL DEFAULT now()
);

ALTER TABLE order_item
    ADD CONSTRAINT unique_order_id_item_id UNIQUE (order_id, item_id);