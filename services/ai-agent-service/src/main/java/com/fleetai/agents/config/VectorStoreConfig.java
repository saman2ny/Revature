package com.fleetai.agents.config;

/**
 * The VectorStore bean itself (PgVectorStore, backed by the `vector_store` table
 * created in scripts/init-db.sql) is auto-configured by
 * spring-ai-pgvector-store-spring-boot-starter from the spring.datasource.* and
 * spring.ai.vectorstore.pgvector.* properties in application.yml.
 *
 * IMPORTANT: pgvector needs an EmbeddingModel to turn text into vectors, and
 * Anthropic does not currently expose an embeddings endpoint. This service is wired
 * to use Anthropic for chat/generation and expects a separate embeddings-capable
 * provider (e.g. spring-ai-openai-spring-boot-starter, used embeddings-only, or a
 * local model via spring-ai-ollama-spring-boot-starter) for the EmbeddingModel bean.
 * Add whichever embedding provider fits your environment and set
 * spring.ai.openai.chat.enabled=false (or equivalent) so it doesn't also register
 * a competing ChatModel bean alongside Anthropic's.
 */
class VectorStoreConfig {
    // Intentionally empty — see class javadoc. Kept as a documented placeholder so
    // the embedding-provider decision is visible in the codebase, not just the README.
}
