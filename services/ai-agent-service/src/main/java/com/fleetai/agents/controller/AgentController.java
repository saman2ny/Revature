package com.fleetai.agents.controller;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;
import com.fleetai.agents.orchestrator.AgentOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for the AI Agent Layer. Routed to from the api-gateway at
 * /api/agents/**, and called directly (service-to-service) by monitoring-service,
 * route-optimization-service, and notification-alert-service.
 */
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestrator orchestrator;

    @PostMapping("/monitoring/analyze")
    public AgentResponse monitoring(@RequestBody AgentRequest request) {
        return orchestrator.runMonitoring(request);
    }

    @PostMapping("/monitoring/analyze-and-alert")
    public AgentResponse monitoringChained(@RequestBody AgentRequest request) {
        return orchestrator.chainMonitoringToAlert(request);
    }

    @PostMapping("/route/optimize")
    public AgentResponse route(@RequestBody AgentRequest request) {
        return orchestrator.runRoute(request);
    }

    @PostMapping("/alert/evaluate")
    public AgentResponse alert(@RequestBody AgentRequest request) {
        return orchestrator.runAlert(request);
    }

    @PostMapping("/approval/decide")
    public AgentResponse approval(@RequestBody AgentRequest request) {
        return orchestrator.runApproval(request);
    }

    @PostMapping("/conversation/chat")
    public AgentResponse conversation(@RequestBody AgentRequest request) {
        return orchestrator.runConversation(request);
    }
}
