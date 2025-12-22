package io.falconFlow.model.ai;

/**
 * Lightweight agent summary for dropdowns and lists.
 */
public class AgentSummary {
    private String agentId;
    private String model;
    private String provider;

    public AgentSummary() {}

    public AgentSummary(String agentId, String model, String provider) {
        this.agentId = agentId;
        this.model = model;
        this.provider = provider;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}