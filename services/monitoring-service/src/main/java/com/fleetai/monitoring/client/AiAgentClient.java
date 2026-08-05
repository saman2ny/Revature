package com.fleetai.monitoring.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * REST client into ai-agent-service. Kept as a small hand-rolled wrapper (rather
 * than a generated OpenFeign client) so the request/response shape is explicit
 * and easy to follow — swap for OpenFeign/WebClient with retries if the call
 * volume grows past what a synchronous RestClient comfortably handles.
 */
@Slf4j
@Component
public class AiAgentClient {

    private final RestClient restClient;

    public AiAgentClient(@Value("${fleetai.ai-agent-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Map<String, Object> analyzeAndAlert(String driverId, String vehicleId, Map<String, Object> telemetryContext) {
        try {
            return restClient.post()
                    .uri("/api/agents/monitoring/analyze-and-alert")
                    .body(Map.of(
                            "driverId", driverId,
                            "vehicleId", vehicleId,
                            "context", telemetryContext
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.warn("ai-agent-service call failed for driver={} vehicle={}, falling back to rule-only result",
                    driverId, vehicleId, ex);
            return Map.of("agentName", "monitoring-agent",
                    "summary", "AI agent unavailable — rule-based result only.",
                    "severity", "INFO");
        }
    }
}
