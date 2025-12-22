package io.falconFlow.DSL.workflow.helpers;

import java.util.Map;

public class WorkFlowItem {

  private String id;
  private ActivityType type;
  private String call;

  private String conditionCall;
  private String conditionInline;
  private long timeoutSeconds;
  private RetryPolicy retryPolicy;
  private Map<String, Object> input;
  private String[] conditions;
  private String signalName;
  private String nextId;

  private Object metaData;

  private WaitFor waitFor;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ActivityType getType() {
    return type;
  }

  public void setType(ActivityType type) {
    this.type = type;
  }

  public String getCall() {
    return call;
  }

  public void setCall(String call) {
    this.call = call;
  }

  public long getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setTimeoutSeconds(long timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }

  public RetryPolicy getRetryPolicy() {
    return retryPolicy;
  }

  public void setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
  }

  public Map<String, Object> getInput() {
    return input;
  }

  public void setInput(Map<String, Object> input) {
    this.input = input;
  }

  public String[] getConditions() {
    return conditions;
  }

  public void setConditions(String[] conditions) {
    this.conditions = conditions;
  }

  public String getNextId() {
    return nextId;
  }

  public void setNextId(String nextId) {
    this.nextId = nextId;
  }

  public WaitFor getWaitFor() {
    return waitFor;
  }

  public void setWaitFor(WaitFor waitFor) {
    this.waitFor = waitFor;
  }

  public String getSignalName() {
    return signalName;
  }

  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }

  public Object getMetaData() {
    return metaData;
  }

  public void setMetaData(Object metaData) {
    this.metaData = metaData;
  }

  public String getConditionInline() {
    return conditionInline;
  }

  public void setConditionInline(String conditionInline) {
    this.conditionInline = conditionInline;
  }

  public String getConditionCall() {
    return conditionCall;
  }

  public void setConditionCall(String conditionCall) {
    this.conditionCall = conditionCall;
  }
}
