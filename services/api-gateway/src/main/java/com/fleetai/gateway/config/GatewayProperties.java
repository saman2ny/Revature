package com.fleetai.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Binds the downstream service base URLs from environment variables
 * (see docker-compose.yml / infra/k8s configmaps) instead of hardcoding them,
 * so the same image runs unmodified across local, staging, and k8s.
 */
@Component("fleetaiGatewayProperties")
@ConfigurationProperties(prefix = "fleetai.services")
@Data
public class GatewayProperties {
    private String monitoringServiceUrl;
    private String routeServiceUrl;
    private String notificationServiceUrl;
    private String aiAgentServiceUrl;
}
