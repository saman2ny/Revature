package com.fleetai.agents.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin wrapper around the pgvector-backed VectorStore. Kept separate from
 * ConversationAgent so ingestion (KnowledgeIngestionService) and retrieval share
 * one place that knows how documents are chunked/stored.
 */
@Service
public class RagService {

    private final VectorStore vectorStore;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return vectorStore.similaritySearch(
            SearchRequest.builder().query(query).topK(topK).build()
        );
    }

    public void ingest(List<Document> documents) {
        vectorStore.add(documents);
    }
}
