package io.falconFlow.dto;

import io.falconFlow.DSL.model.InputMap;

import java.util.Map;

public class WorkFlowManagerRequest {

    private String workflowId;
    private String workflowCode;
    private InputMap input;
    private InputMap userInput;

    private Map<String, Object> state;


    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public InputMap getInput() {
        return input;
    }

    public void setInput(InputMap input) {
        this.input = input;
    }

    public Map<String, Object> getState() {
        return state;
    }

    public void setState(Map<String, Object> state) {
        this.state = state;
    }

    public InputMap getUserInput() {
        return userInput;
    }

    public void setUserInput(InputMap userInput) {
        this.userInput = userInput;
    }
}
