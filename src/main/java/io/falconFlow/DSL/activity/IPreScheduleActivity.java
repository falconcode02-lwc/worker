package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;
import io.falconFlow.DSL.model.WorkflowResponseWrapper;
import io.falconFlow.dto.WorkFlowManagerRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;


@ActivityInterface
public interface IPreScheduleActivity {

    @ActivityMethod
    WorkflowResponseWrapper beforeWorkFlowStart(WorkFlowManagerRequest workFlowManagerRequest);

}
