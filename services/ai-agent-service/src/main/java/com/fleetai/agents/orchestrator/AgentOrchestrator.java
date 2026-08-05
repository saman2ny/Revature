package com.fleetai.agents.orchestrator;

import com.fleetai.agents.agent.AlertAgent;
import com.fleetai.agents.agent.ApprovalAgent;
import com.fleetai.agents.agent.ConversationAgent;
import com.fleetai.agents.agent.MonitoringAgent;
import com.fleetai.agents.agent.RouteAgent;
import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point AgentController delegates to. Exposes one method per agent for the
 * simple case (a service calls exactly one agent), plus chain() for the common
 * multi-agent pipeline: Monitoring finding -> Alert framing -> (if flagged)
 * Approval decision, in one round trip instead of three.
 */
@Component
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final MonitoringAgent monitoringAgent;
    private final RouteAgent routeAgent;
    private final AlertAgent alertAgent;
    private final ApprovalAgent approvalAgent;
    private final ConversationAgent conversationAgent;

    public AgentResponse runMonitoring(AgentRequest request) {
        return monitoringAgent.execute(request);
    }

    public AgentResponse runRoute(AgentRequest request) {
        return routeAgent.execute(request);
    }

    public AgentResponse runAlert(AgentRequest request) {
        return alertAgent.execute(request);
    }

    public AgentResponse runApproval(AgentRequest request) {
        return approvalAgent.execute(request);
    }

    public AgentResponse runConversation(AgentRequest request) {
        return conversationAgent.execute(request);
    }

    /**
     * Convenience pipeline: Monitoring -> Alert -> (conditionally) Approval.
     * Used by monitoring-service when it wants one call instead of orchestrating
     * three itself. Returns the Alert Agent's response (what actually gets sent),
     * annotated with the approval outcome if one was required.
     */
    public AgentResponse chainMonitoringToAlert(AgentRequest request) {
        AgentResponse monitoringResult = runMonitoring(request);

        Map<String, Object> alertContext = new HashMap<>();
        alertContext.put("monitoringFinding", monitoringResult.getSummary());
        alertContext.put("severity", monitoringResult.getSeverity());
        alertContext.put("recentSimilarAlertCount", request.getContext() == null
                ? 0 : request.getContext().getOrDefault("recentSimilarAlertCount", 0));

        AgentResponse alertResult = runAlert(AgentRequest.builder()
                .requestId(request.getRequestId())
                .driverId(request.getDriverId())
                .vehicleId(request.getVehicleId())
                .context(alertContext)
                .build());

        if ("CRITICAL".equals(monitoringResult.getSeverity())) {
            Map<String, Object> approvalContext = new HashMap<>();
            approvalContext.put("action", "critical-safety-escalation");
            approvalContext.put("finding", monitoringResult.getSummary());

            AgentResponse approvalResult = runApproval(AgentRequest.builder()
                    .requestId(request.getRequestId())
                    .driverId(request.getDriverId())
                    .vehicleId(request.getVehicleId())
                    .context(approvalContext)
                    .build());

            alertResult.setRequiresApproval(approvalResult.isRequiresApproval());
            alertResult.setAutoApproved(approvalResult.isAutoApproved());
        }

        return alertResult;
    }
}
