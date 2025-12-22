package io.falconFlow.DSL.workflow.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StateModel implements Serializable {

  public StateModel() {
    stateValue = new HashMap<>();
  }

  private Map<String, Object> stateValue;

  public Map<String, Object> getStateValue() {
    return stateValue;
  }

  public void setStateValue(Map<String, Object> stateValue) {
    this.stateValue = stateValue;
  }
}
