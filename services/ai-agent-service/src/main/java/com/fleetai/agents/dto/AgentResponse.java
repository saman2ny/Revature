package com.fleetai.agents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {
    private String agentName;
    private String summary;
    /** INFO | WARNING | CRITICAL — consumed by Alert Agent / notification-alert-service. */
    private String severity;
    private boolean requiresApproval;
    private boolean autoApproved;
    private Map<String, Object> data;
    /** RAG source citations, populated by the Conversation Agent. */
    private List<String> citations;
}
