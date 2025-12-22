package io.falconFlow.DSL.workflow.model;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public class WorkflowResultModel {

    private String workflowId;
    private String status;
    private List<NodeResult> nodeResultList;
    private Timestamp startTime;
    private Timestamp endTime;

    public static class NodeResult{

        private String id;
        private String status;
        private String name;
        private String type;
        private String error;
        private Timestamp startTime;
        private Timestamp endTime;
        private Integer runCounter = 0;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Timestamp getStartTime() {
            return startTime;
        }

        public void setStartTime(Timestamp startTime) {
            this.startTime = startTime;
        }

        public Timestamp getEndTime() {
            return endTime;
        }

        public void setEndTime(Timestamp endTime) {
            this.endTime = endTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getRunCounter() {
            return runCounter;
        }

        public void setRunCounter(Integer runCounter) {
            this.runCounter = runCounter;
        }
    }


    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<NodeResult> getNodeResultList() {
        return nodeResultList;
    }

    public void setNodeResultList(List<NodeResult> nodeResultList) {
        this.nodeResultList = nodeResultList;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}


