package io.falconFlow.DSL.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.activity.IPreScheduleActivity;
import io.falconFlow.DSL.model.WorkflowResponseWrapper;
import io.falconFlow.DSL.workflow.model.WorkflowModel;
import io.falconFlow.dto.WorkFlowManagerRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl
public class ScheduleWorkflowImpl implements IScheduleWorkflow {

    private IPreScheduleActivity hook = Workflow.newActivityStub(
            IPreScheduleActivity.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue(
                        (Workflow.getInfo().getNamespace() != null && !Workflow.getInfo().getNamespace().equals("default")) 
                        ? Workflow.getInfo().getNamespace() : "FalconFlow"
                    )
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .build()
    );

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void start(WorkFlowManagerRequest workFlowManagerRequest) {

        WorkflowResponseWrapper res = hook.beforeWorkFlowStart(workFlowManagerRequest);
        WorkflowModel workflowNode = null;
        try {
            workflowNode = mapper.readValue(res.getWorkflowJson(),  WorkflowModel.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        workflowNode.setId(res.getCreateResponse().getWorkflowId());
        workflowNode.setInput(res.getCreateResponse().getInput());
        IWorkFlowv2 wf = Workflow.newChildWorkflowStub(IWorkFlowv2.class);
        wf.runWorkflow(workflowNode);

    }
}
