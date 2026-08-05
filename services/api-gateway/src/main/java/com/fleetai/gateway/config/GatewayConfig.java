package com.fleetai.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Static route table. Downstream services are addressed by their docker-compose /
 * Kubernetes service DNS names (see each service's application.yml for the port it binds).
 *
 * Circuit breaking (Resilience4j) wraps every downstream call so a slow/unavailable
 * service degrades gracefully instead of taking the gateway down with it.
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                                GatewayProperties props) {
        return builder.routes()
            .route("monitoring-service", r -> r.path("/api/monitoring/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("monitoringCB")
                        .setFallbackUri("forward:/fallback/monitoring")))
                .uri(props.getMonitoringServiceUrl()))

            .route("route-optimization-service", r -> r.path("/api/routes/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("routeCB")
                        .setFallbackUri("forward:/fallback/routes")))
                .uri(props.getRouteServiceUrl()))

            .route("notification-alert-service", r -> r.path("/api/notifications/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("notificationCB")
                        .setFallbackUri("forward:/fallback/notifications")))
                .uri(props.getNotificationServiceUrl()))

            .route("notification-websocket", r -> r.path("/ws/**")
                .uri(props.getNotificationServiceUrl()))

            .route("ai-agent-service", r -> r.path("/api/agents/**")
                .filters(f -> f.circuitBreaker(c -> c.setName("agentCB")
                        .setFallbackUri("forward:/fallback/agents")))
                .uri(props.getAiAgentServiceUrl()))

            .build();
    }
}
