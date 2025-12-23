package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;


public class WorkflowResponseWrapper {

    private String workflowJson;
    private CreateResponse createResponse;
    private String workflowDefId;
    private String workflowCode;
    private String workSpaceId;
    private String projectId;

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

    public String getWorkflowDefId() {
        return workflowDefId;
    }

    public void setWorkflowDefId(String workflowDefId) {
        this.workflowDefId = workflowDefId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getWorkSpaceId() {
        return workSpaceId;
    }

    public void setWorkSpaceId(String workSpaceId) {
        this.workSpaceId = workSpaceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}