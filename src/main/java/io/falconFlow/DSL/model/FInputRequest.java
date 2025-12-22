package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

// this is for controller handling
public class FInputRequest {

    private String workflowId;
    private InputMap userInput;
    private StateModel state;

    public String getWorkflowId() {
        return workflowId;
    }
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
    public InputMap getUserInput() {
        return userInput;
    }
    public void setUserInput(InputMap userInput) {
        this.userInput = userInput;
    }

    public StateModel getState() {
        return state;
    }

    public void setState(StateModel state) {
        this.state = state;
    }
}
