-- Runs automatically on first Postgres container start (docker-entrypoint-initdb.d).
-- Enables pgvector so the same Postgres instance can serve as the RAG vector store
-- used by the Conversation Agent, alongside normal relational tables.

CREATE EXTENSION IF NOT EXISTS vector;

-- Core relational tables. Each service also manages its own tables via JPA/Hibernate
-- (ddl-auto=update in dev), but the shared ones that need to exist up-front are defined here.

CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(255) NOT NULL,
    license_number VARCHAR(64) UNIQUE,
    vehicle_id VARCHAR(64),
    status VARCHAR(32) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Vector store table used by Spring AI's PgVectorStore (ai-agent-service).
-- Holds embedded chunks of policy docs, incident reports, and driver history
-- that ground the Conversation Agent's RAG responses.
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT,
    metadata JSON,
    embedding VECTOR(1536)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);

INSERT INTO drivers (full_name, license_number, vehicle_id, status)
VALUES ('Demo Driver', 'DEMO-LIC-001', 'VEH-1001', 'ACTIVE')
ON CONFLICT DO NOTHING;
