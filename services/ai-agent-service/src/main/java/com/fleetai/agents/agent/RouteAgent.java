package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reasons about whether route-optimization-service's candidate route change
 * should actually be recommended, weighing ETA savings against driver hours-of-
 * service, delivery-window constraints, and the cost/disruption of a mid-route
 * change. Large deviations are flagged requiresApproval=true so the Approval
 * Agent (and ultimately a human on the supervisor dashboard) signs off before
 * the driver is redirected.
 */
@Component
public class RouteAgent implements Agent {

    private static final String SYSTEM_PROMPT = """
        You are the Route Optimization Agent for a commercial fleet platform.
        Given the driver's current route, live telemetry, and a candidate
        alternate route, recommend whether to reroute the driver.
        Weigh estimated time savings against hours-of-service limits, delivery
        windows, and rider/cargo disruption. Explain your reasoning in 2-3
        sentences, then on a final line output exactly one of:
        DECISION=REROUTE or DECISION=KEEP_CURRENT, followed by
        APPROVAL_REQUIRED=true or APPROVAL_REQUIRED=false.
        Require approval for any reroute that adds material distance/cost or
        crosses a delivery-window risk.
        """;

    private final ChatClient chatClient;

    public RouteAgent(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public String name() {
        return "route-agent";
    }

    @Override
    public AgentResponse execute(AgentRequest request) {
        String routeContext = String.valueOf(request.getContext());

        String modelOutput = chatClient.prompt()
                .user(u -> u.text("""
                    Driver: {driverId}
                    Vehicle: {vehicleId}
                    Current route / candidate route / telemetry context: {routeContext}
                    """)
                    .param("driverId", request.getDriverId())
                    .param("vehicleId", request.getVehicleId())
                    .param("routeContext", routeContext))
                .call()
                .content();

        boolean reroute = modelOutput != null && modelOutput.contains("DECISION=REROUTE");
        boolean approvalRequired = modelOutput != null && modelOutput.contains("APPROVAL_REQUIRED=true");

        return AgentResponse.builder()
                .agentName(name())
                .summary(modelOutput)
                .severity(reroute ? "WARNING" : "INFO")
                .requiresApproval(approvalRequired)
                .data(Map.of("reroute", reroute))
                .build();
    }
}
