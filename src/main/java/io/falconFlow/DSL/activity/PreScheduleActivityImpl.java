package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.model.WorkflowResponseWrapper;
import io.falconFlow.dto.WorkFlowManagerRequest;
import io.falconFlow.services.genservice.WorkflowsService;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl
public class PreScheduleActivityImpl implements IPreScheduleActivity{
    @Autowired
    WorkflowsService workflowsService;

    @Override
    public WorkflowResponseWrapper beforeWorkFlowStart(WorkFlowManagerRequest workFlowManagerRequest) {
        return workflowsService.runController(workFlowManagerRequest);
    }
}
