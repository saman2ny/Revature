package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reasons over a telemetry window (speed variance, harsh-braking/acceleration
 * counts, engine diagnostics, geofence status) that monitoring-service assembles
 * and passes in via AgentRequest.context. Produces a severity classification and
 * a human-readable insight — this is what lets the platform go beyond static
 * threshold rules ("speed > 120") to pattern-level judgment ("three harsh-braking
 * events in nine minutes on a wet-weather route suggests fatigue, not just traffic").
 */
@Component
public class MonitoringAgent implements Agent {

    private static final String SYSTEM_PROMPT = """
        You are the Monitoring Agent for a commercial fleet safety platform.
        Given a driver's recent telemetry window, decide whether it represents
        normal driving, a minor anomaly, or a safety-critical pattern.
        Consider speed relative to posted limits, harsh braking/acceleration
        frequency, engine diagnostics, and time-of-day/fatigue risk.
        Respond with a concise 2-3 sentence assessment, then on a final line
        output exactly one of: SEVERITY=INFO, SEVERITY=WARNING, SEVERITY=CRITICAL.
        """;

    private final ChatClient chatClient;

    public MonitoringAgent(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public String name() {
        return "monitoring-agent";
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        String telemetrySummary = String.valueOf(request.getContext());

        String modelOutput = chatClient.prompt()
                .user(u -> u.text("""
                    Driver: {driverId}
                    Vehicle: {vehicleId}
                    Recent telemetry window: {telemetry}
                    """)
                    .param("driverId", request.getDriverId())
                    .param("vehicleId", request.getVehicleId())
                    .param("telemetry", telemetrySummary))
                .call()
                .content();

        String severity = extractSeverity(modelOutput);

        return AgentResponse.builder()
                .agentName(name())
                .summary(modelOutput)
                .severity(severity)
                .requiresApproval(false)
                .data(Map.of("telemetryWindow", telemetrySummary))
                .build();
    }

    private String extractSeverity(String output) {
        if (output == null) return "INFO";
        if (output.contains("SEVERITY=CRITICAL")) return "CRITICAL";
        if (output.contains("SEVERITY=WARNING")) return "WARNING";
        return "INFO";
    }
}
