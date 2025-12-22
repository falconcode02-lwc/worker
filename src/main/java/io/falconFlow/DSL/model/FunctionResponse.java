package io.falconFlow.DSL.model;

import io.falconFlow.DSL.workflow.model.StateModel;

import java.util.Map;

public class FunctionResponse {

  private FunctionStatus status;
  private String errorCode;
  private String message;
  private Object metaData;
  private Map next;

  private StateModel state;

  public FunctionStatus getStatus() {
    return status;
  }

  public void setStatus(FunctionStatus status) {
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

    public Map getNext() {
        return next;
    }

    public void setNext(Map next) {
        this.next = next;
    }
}
