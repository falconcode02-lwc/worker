package io.falconFlow.DSL.workflow.helpers;

import io.falconFlow.DSL.model.InputMap;

public class WorkFlowWrapper {

  private String workflowName;
  private String taskQueue;
  private String workflowId;
  private String version;
  private WorkFlowItem[] steps;

  private InputMap input;

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  public String getTaskQueue() {
    return taskQueue;
  }

  public void setTaskQueue(String taskQueue) {
    this.taskQueue = taskQueue;
  }

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public WorkFlowItem[] getSteps() {
    return steps;
  }

  public void setSteps(WorkFlowItem[] steps) {
    this.steps = steps;
  }

  public InputMap getInput() {
    return input;
  }

  public void setInput(InputMap input) {
    this.input = input;
  }
}
