package com.fleetai.agents.rag;

import org.springframework.ai.document.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Seeds the vector store with a handful of example policy/compliance documents
 * on startup so the Conversation Agent has something to ground answers in out of
 * the box. Replace with a real ingestion pipeline (e.g. an Airflow/batch job that
 * chunks the client's actual driver handbooks, incident reports, and compliance
 * docs from S3/MinIO) before going to production — this CommandLineRunner is a
 * demo convenience, not the production ingestion path.
 */
@Component
public class KnowledgeIngestionService implements CommandLineRunner {

    private final RagService ragService;

    public KnowledgeIngestionService(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        List<Document> seedDocs = List.of(
            new Document(
                "Drivers must not exceed 11 hours of driving within a 14-hour on-duty window (Hours of Service policy).",
                Map.of("source", "policy/hours-of-service.md")
            ),
            new Document(
                "Route deviations that add more than 30 minutes or cross a scheduled delivery window require supervisor approval before the driver is redirected.",
                Map.of("source", "policy/route-deviation.md")
            ),
            new Document(
                "Harsh braking events are logged when deceleration exceeds 0.4g. Three or more events within a 15-minute window should be treated as a fatigue or road-condition signal, not dismissed as isolated incidents.",
                Map.of("source", "policy/safety-thresholds.md")
            )
        );
        ragService.ingest(seedDocs);
    }
}
