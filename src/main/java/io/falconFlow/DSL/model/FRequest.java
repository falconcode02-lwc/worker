package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

import java.util.Date;
import java.util.Map;

public class FRequest{

  private String workflowId;
  private Date timeStamp;
  private String activityId;
  private String condition;
  private String workflowActivityId;
  private InputMap input;
  private Object metaData;
  private String call;
  private StateModel state;
  private InputMap userInput;
  private InputMap pluginProps;
  private Map previous;

    public String getActivityId() {
    return activityId;
    }

    public void setActivityId(String activityId) {
    this.activityId = activityId;
    }

    public String getCondition() {
    return condition;
    }

    public void setCondition(String condition) {
    this.condition = condition;
    }

    public String getWorkflowActivityId() {
    return workflowActivityId;
    }

    public void setWorkflowActivityId(String workflowActivityId) {
    this.workflowActivityId = workflowActivityId;
    }

    public InputMap getInput() {
    return input;
    }

    public void setInput(InputMap input) {
    this.input = input;
    }

    public String getCall() {
    return call;
    }

    public void setCall(String call) {
    this.call = call;
    }

    public StateModel getState() {
    return state;
    }

    public void setState(StateModel state) {
    this.state = state;
    }


    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Object getMetaData() {
        return metaData;
    }

    public void setMetaData(Object metaData) {
        this.metaData = metaData;
    }

    public void setUserInput(InputMap userInput) {
        this.userInput = userInput;
    }

    public InputMap getUserInput() {
        return userInput;
    }

    public InputMap getPluginProps() {
        return pluginProps;
    }

    public void setPluginProps(InputMap pluginProps) {
        this.pluginProps = pluginProps;
    }

    public Map getPrevious() {
        return previous;
    }

    public void setPrevious(Map previous) {
        this.previous = previous;
    }
}
