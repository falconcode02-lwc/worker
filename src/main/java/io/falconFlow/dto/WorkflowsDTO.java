package io.falconFlow.dto;

import java.time.LocalDateTime;

public class    WorkflowsDTO {
    private Integer id;
    private String name;
    private Integer version;
    private String workflowJson;
    private String workflowJsonRaw;
    private String controller;
    private String description;
    private Boolean active;
    private String code;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String createdBy;
    private String modifiedBy;


    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getWorkflowJson() { return workflowJson; }
    public void setWorkflowJson(String workflowJson) { this.workflowJson = workflowJson; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(LocalDateTime modifiedTime) { this.modifiedTime = modifiedTime; }

    public String getWorkflowJsonRaw() {
        return workflowJsonRaw;
    }

    public void setWorkflowJsonRaw(String workflowJsonRaw) {
        this.workflowJsonRaw = workflowJsonRaw;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
