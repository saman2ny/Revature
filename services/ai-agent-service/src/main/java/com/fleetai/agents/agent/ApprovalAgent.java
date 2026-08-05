package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Human-in-the-loop gatekeeper. Evaluates an action flagged
 * requiresApproval=true (a reroute, a policy exception, an escalation) against
 * stated thresholds and either auto-approves within policy or escalates to a
 * supervisor via notification-alert-service / the dashboard.
 *
 * This is intentionally conservative: the agent only self-approves low-risk,
 * clearly-bounded actions (context.autoApproveEligible=true, e.g. cost/time
 * deltas under a configured threshold checked upstream). Anything ambiguous is
 * escalated rather than guessed at — wrong auto-approvals are expensive, a
 * delayed decision usually is not.
 */
@Component
public class ApprovalAgent implements Agent {

    private static final String SYSTEM_PROMPT = """
        You are the Approval Agent for a commercial fleet platform. You evaluate
        a proposed action (route change, policy exception, cost override) against
        the stated policy thresholds in the request context.
        Only recommend auto-approval when the action is clearly within policy and
        low-risk. When in doubt, escalate to a human supervisor — do not guess.
        Explain your reasoning in 1-2 sentences, then on a final line output
        exactly one of: DECISION=AUTO_APPROVE or DECISION=ESCALATE_TO_SUPERVISOR.
        """;

    private final ChatClient chatClient;

    public ApprovalAgent(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public String name() {
        return "approval-agent";
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        String actionContext = String.valueOf(request.getContext());

        String modelOutput = chatClient.prompt()
                .user(u -> u.text("""
                    Driver: {driverId}
                    Vehicle: {vehicleId}
                    Proposed action + policy thresholds: {actionContext}
                    """)
                    .param("driverId", request.getDriverId())
                    .param("vehicleId", request.getVehicleId())
                    .param("actionContext", actionContext))
                .call()
                .content();

        boolean autoApproved = modelOutput != null && modelOutput.contains("DECISION=AUTO_APPROVE");

        return AgentResponse.builder()
                .agentName(name())
                .summary(modelOutput)
                .severity(autoApproved ? "INFO" : "WARNING")
                .requiresApproval(!autoApproved)
                .autoApproved(autoApproved)
                .data(Map.of("autoApproved", autoApproved))
                .build();
    }
}
