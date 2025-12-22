package io.falconFlow.dto;


import java.util.Date;
import java.util.List;

public record WorkflowItem(
        String workflowId,
        String runId,
        String type,
        String status,
        Date startTime,
        Date closeTime
) {}

