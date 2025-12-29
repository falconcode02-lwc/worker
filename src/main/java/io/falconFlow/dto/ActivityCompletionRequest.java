package io.falconFlow.dto;

import java.util.Map;

public class ActivityCompletionRequest {
    private String requestId;
    private Map<String, Object> result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}
