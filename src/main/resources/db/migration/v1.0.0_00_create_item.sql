CREATE TABLE IF NOT EXISTS item(
  id BIGSERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  image_path TEXT,
  description TEXT NOT NULL,
  price NUMERIC(12, 2) NOT NULL CHECK (price > 0),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);