package io.falconFlow.services.ai;

import io.falconFlow.model.ai.AgentResponse;
import io.falconFlow.model.ai.AgentSummary;

import java.util.List;
import java.util.Map;

public interface AIService {
    void createAgent(String agentId, String model, Map<String, String> config);
    AgentResponse sendMessage(String agentId, String message);
    Object getMemory(String agentId);
    // removed external n8n tool support; providers handle external calls
    Map<String, String> getAgentConfig(String agentId);
    
    /**
     * List all agents with their model and provider info.
     */
    List<AgentSummary> listAgents();
}
