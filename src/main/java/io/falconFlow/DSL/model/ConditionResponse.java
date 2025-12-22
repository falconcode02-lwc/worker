package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

public class ConditionResponse {

  private ConditionStatus status;
  private String errorCode;
  private String message;
  private Object metaData;

  private StateModel state;

  public ConditionStatus getStatus() {
    return status;
  }

  public void setStatus(ConditionStatus status) {
    this.status = status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object getMetaData() {
    return metaData;
  }

  public void setMetaData(Object metaData) {
    this.metaData = metaData;
  }

  public StateModel getState() {
    return state;
  }

  public void setState(StateModel state) {
    this.state = state;
  }
}
