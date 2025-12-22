package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

public class FCreateRequest {

    private String workflowId;
    private InputMap input;
    private StateModel state;

    public InputMap getInput() {
    return input;
    }
    public void setInput(InputMap input) {
    this.input = input;
    }
    public StateModel getState() {
    return state;
    }
    public void setState(StateModel state) {
    this.state = state;
    }
    public String getWorkflowId() {
        return workflowId;
    }
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
