package io.falconFlow.DSL.workflow;

import io.falconFlow.DSL.model.InputMap;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.DSL.workflow.model.WorkflowModel;
import io.falconFlow.DSL.workflow.model.WorkflowResultModel;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Map;

@WorkflowInterface
public interface IWorkFlowv2 {
  @WorkflowMethod
  WorkflowResultModel runWorkflow(WorkflowModel workFlowWrapper);

  @SignalMethod
  void signal(InputMap userInput);

  @QueryMethod
  WorkflowResultModel getStatus();

}
