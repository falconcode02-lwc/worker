package io.falconFlow.DSL.model;


import io.falconFlow.DSL.workflow.model.StateModel;

public  class CreateResponse {

    private ControllerStatus status;
    private String errorCode;
    private String message;
    private InputMap input;
    private StateModel state;
    private String workflowId;

    public ControllerStatus getStatus() {
        return status;
    }

    public void setStatus(ControllerStatus status) {

        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StateModel getState() {
        return state;
    }

    public void setState(StateModel state) {
        this.state = state;
    }

    public InputMap getInput() {
        return input;
    }

    public void setInput(InputMap input) {
        this.input = input;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}