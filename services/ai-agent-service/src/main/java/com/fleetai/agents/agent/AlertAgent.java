package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Turns a Monitoring/Route Agent finding into an actual notification: decides
 * audience (driver, supervisor, or both), urgency framing, and message wording.
 * Also applies noise control — context.recentSimilarAlertCount lets
 * notification-alert-service tell this agent "we already sent 4 of these in the
 * last hour," and the agent can choose to suppress or downgrade rather than
 * paging a supervisor for every repeat event.
 */
@Component
public class AlertAgent implements Agent {

    private static final String SYSTEM_PROMPT = """
        You are the Alert Agent for a commercial fleet platform. You receive an
        upstream finding (from the Monitoring or Route Agent) and decide how — or
        whether — to notify people.
        Consider severity, how many similar alerts have fired recently (avoid
        alert fatigue: suppress or downgrade repetitive low-severity noise), and
        who needs to act (driver, supervisor, or both).
        Write the actual notification text (1-2 sentences, plain language, no
        jargon) a supervisor or driver would read on their phone. Then on a final
        line output AUDIENCE=DRIVER, AUDIENCE=SUPERVISOR, AUDIENCE=BOTH, or
        AUDIENCE=SUPPRESSED.
        """;

    private final ChatClient chatClient;

    public AlertAgent(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public String name() {
        return "alert-agent";
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        String findingContext = String.valueOf(request.getContext());

        String modelOutput = chatClient.prompt()
                .user(u -> u.text("""
                    Driver: {driverId}
                    Vehicle: {vehicleId}
                    Upstream finding + recent-alert context: {findingContext}
                    """)
                    .param("driverId", request.getDriverId())
                    .param("vehicleId", request.getVehicleId())
                    .param("findingContext", findingContext))
                .call()
                .content();

        String audience = extractAudience(modelOutput);
        boolean suppressed = "SUPPRESSED".equals(audience);

        return AgentResponse.builder()
                .agentName(name())
                .summary(modelOutput)
                .severity(suppressed ? "INFO" : "WARNING")
                .requiresApproval(false)
                .data(Map.of("audience", audience, "suppressed", suppressed))
                .build();
    }

    private String extractAudience(String output) {
        if (output == null) return "SUPERVISOR";
        if (output.contains("AUDIENCE=SUPPRESSED")) return "SUPPRESSED";
        if (output.contains("AUDIENCE=DRIVER")) return "DRIVER";
        if (output.contains("AUDIENCE=BOTH")) return "BOTH";
        return "SUPERVISOR";
    }
}
