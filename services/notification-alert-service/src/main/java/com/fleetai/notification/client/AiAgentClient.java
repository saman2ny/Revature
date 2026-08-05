package com.fleetai.notification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class AiAgentClient {

    private final RestClient restClient;

    public AiAgentClient(@Value("${fleetai.ai-agent-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Used for route.updates events that arrive already-classified but still need approval routing. */
    public Map<String, Object> decideApproval(String driverId, String vehicleId, Map<String, Object> actionContext) {
        try {
            return restClient.post()
                    .uri("/api/agents/approval/decide")
                    .body(Map.of("driverId", driverId, "vehicleId", vehicleId, "context", actionContext))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.warn("ai-agent-service approval call failed for driver={}", driverId, ex);
            return Map.of("autoApproved", false, "requiresApproval", true,
                    "summary", "AI agent unavailable — escalated to supervisor by default.");
        }
    }
}
