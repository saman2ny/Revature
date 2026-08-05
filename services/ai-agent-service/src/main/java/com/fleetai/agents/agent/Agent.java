package com.fleetai.agents.agent;

import com.fleetai.agents.dto.AgentRequest;
import com.fleetai.agents.dto.AgentResponse;

/** Common contract every specialized agent implements. */
public interface Agent {
    String name();
    AgentResponse execute(AgentRequest request);
}
