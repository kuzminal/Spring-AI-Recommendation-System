-- Таблица товаров/контента с эмбеддингами
CREATE TABLE items
(
    id          uuid PRIMARY KEY default uuid_generate_v4(),
    title       VARCHAR(500) NOT NULL,
    description TEXT,
    category    VARCHAR(100),
    price       DECIMAL(10, 2),
    embedding   VECTOR(1024),
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Индекс HNSW для быстрого векторного поиска
CREATE INDEX IF NOT EXISTS vector_store_hnsw_index
    ON items USING hnsw (embedding vector_cosine_ops);