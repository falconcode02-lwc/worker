package io.falconFlow.model;

import io.falconFlow.dto.WorkFlowManagerRequest;

public class ScheduleRequest {

    private String scheduleId;
    private String cron;
    private boolean enable;
    private WorkFlowManagerRequest request;


    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }





    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public WorkFlowManagerRequest getRequest() {
        return request;
    }

    public void setRequest(WorkFlowManagerRequest request) {
        this.request = request;
    }
}
