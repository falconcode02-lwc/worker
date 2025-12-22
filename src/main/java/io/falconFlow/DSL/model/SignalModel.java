package io.falconFlow.DSL.model;

import java.util.Date;

public class SignalModel {
  private String signalName;

  public String getSignalName() {
    return signalName;
  }

  public void setSignalName(String signalName) {
    this.signalName = signalName;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }

  public void setReceivedDate(Date receivedDate) {
    this.receivedDate = receivedDate;
  }

  public Date getExecutedDate() {
    return executedDate;
  }

  public void setExecutedDate(Date executedDate) {
    this.executedDate = executedDate;
  }

  public Object getMetaData() {
    return metaData;
  }

  public void setMetaData(Object metaData) {
    this.metaData = metaData;
  }

  private Date receivedDate;
  private Date executedDate;
  private Object metaData;
}
