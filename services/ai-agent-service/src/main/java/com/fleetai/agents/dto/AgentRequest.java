package com.fleetai.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Generic envelope every agent accepts. `context` carries whatever structured
 * payload the calling service has (a telemetry window, a candidate route, a
 * pending alert) and `query` carries a free-text question for the Conversation
 * Agent. Keeping one shared shape avoids five near-identical DTOs and matches
 * how the /api/agents/{type} endpoints are structured below.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    private String requestId;
    private String driverId;
    private String vehicleId;
    private String query;
    private Map<String, Object> context;
}
