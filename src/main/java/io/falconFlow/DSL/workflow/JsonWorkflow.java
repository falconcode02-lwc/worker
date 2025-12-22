package io.falconFlow.DSL.workflow;

import io.falconFlow.DSL.workflow.helpers.WorkFlowWrapper;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface JsonWorkflow {
  @WorkflowMethod
  void runWorkflow(WorkFlowWrapper workFlowWrapper);

  @SignalMethod
  void signal(String signal, Object metaData);
}
