package io.falconFlow.DSL.workflow;

import io.falconFlow.dto.WorkFlowManagerRequest;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface IScheduleWorkflow {

    @WorkflowMethod
    void start(WorkFlowManagerRequest workFlowManagerRequest);


}
