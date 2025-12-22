package io.falconFlow.model.ai;

import java.util.Map;

public class AgentRequest {
    private String agentId;
    private String model;
    private Map<String, String> config;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }
}
