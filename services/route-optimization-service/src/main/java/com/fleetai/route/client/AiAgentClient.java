package com.fleetai.route.client;

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

    public Map<String, Object> optimizeRoute(String driverId, String vehicleId, Map<String, Object> routeContext) {
        try {
            return restClient.post()
                    .uri("/api/agents/route/optimize")
                    .body(Map.of(
                            "driverId", driverId,
                            "vehicleId", vehicleId,
                            "context", routeContext
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            log.warn("ai-agent-service route call failed for driver={} vehicle={}", driverId, vehicleId, ex);
            return Map.of("severity", "INFO", "requiresApproval", false,
                    "summary", "AI agent unavailable — keeping current route.",
                    "data", Map.of("reroute", false));
        }
    }
}
