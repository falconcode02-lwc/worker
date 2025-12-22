package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;


public class WorkflowResponseWrapper {

    private String workflowJson;
    private CreateResponse createResponse;

    public String getWorkflowJson() {
        return workflowJson;
    }

    public void setWorkflowJson(String workflowJson) {
        this.workflowJson = workflowJson;
    }

    public CreateResponse getCreateResponse() {
        return createResponse;
    }

    public void setCreateResponse(CreateResponse createResponse) {
        this.createResponse = createResponse;
    }


}