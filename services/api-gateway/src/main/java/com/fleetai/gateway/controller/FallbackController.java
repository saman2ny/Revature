package com.fleetai.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Resilience4j circuit-breaker fallback targets referenced from GatewayConfig.
 * No method restriction on the mappings - the circuit breaker forwards using the
 * original request's HTTP method (e.g. POST for the agents chat route).
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/monitoring")
    public ResponseEntity<Map<String, String>> monitoring() {
        return degraded("monitoring-service");
    }

    @RequestMapping("/routes")
    public ResponseEntity<Map<String, String>> routes() {
        return degraded("route-optimization-service");
    }

    @RequestMapping("/notifications")
    public ResponseEntity<Map<String, String>> notifications() {
        return degraded("notification-alert-service");
    }

    @RequestMapping("/agents")
    public ResponseEntity<Map<String, String>> agents() {
        return degraded("ai-agent-service");
    }

    private ResponseEntity<Map<String, String>> degraded(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("status", "degraded", "service", service,
                        "message", "Service is temporarily unavailable, please retry shortly."));
    }
}
