CREATE TABLE IF NOT EXISTS orders(
  id BIGSERIAL PRIMARY KEY,
  total_price NUMERIC(12, 2) NOT NULL CHECK (total_price > 0),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);