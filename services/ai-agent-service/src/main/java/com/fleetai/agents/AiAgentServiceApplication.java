package com.fleetai.agents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hosts the AI Agent Layer referenced in the architecture diagram:
 *   Monitoring Agent, Route Agent, Alert Agent, Approval Agent, Conversation Agent.
 *
 * Other services (monitoring-service, route-optimization-service,
 * notification-alert-service) call this service over REST rather than embedding
 * LLM logic themselves, so agent prompts/models/RAG grounding are versioned,
 * scaled, and evaluated in exactly one place.
 */
@SpringBootApplication
public class AiAgentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiAgentServiceApplication.class, args);
    }
}
