package io.falconFlow.model.ai;

public class AgentResponse {
    private String agentId;
    private String message;

    public AgentResponse() {}

    public AgentResponse(String agentId, String message) {
        this.agentId = agentId;
        this.message = message;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
