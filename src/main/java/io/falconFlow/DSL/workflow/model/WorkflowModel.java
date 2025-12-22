package io.falconFlow.DSL.workflow.model;

import io.falconFlow.DSL.workflow.helpers.ActivityType;
import io.falconFlow.DSL.model.InputMap;
import io.temporal.api.update.v1.Input;

import java.util.List;
import java.util.Map;

public class WorkflowModel {

  private String id;

  private List<Node> workflow;

  // Getters and Setters
  public List<Node> getWorkflow() {
    return workflow;
  }

  public void setWorkflow(List<Node> workflow) {
    this.workflow = workflow;
  }

  private InputMap input;

  public InputMap getInput() {
    return input;
  }

  public void setInput(InputMap input) {
    this.input = input;
  }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // 🔹 Inner Node class
  public static class Node {
    private String id;
    private ActivityType type;
    private String name;
    private String[] call;
    private String signalName;
    private String conditionCall;
    private String conditionInline;
    private Config config;
    private String next;
    private String nextFalse;
    private String status;
    private String error;
    private Switches[] switchval;
    private Map<String, Object> metaData;
    private InputMap pluginprop;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

        public Switches[] getSwitchval() {
            return switchval;
        }

        public void setSwitchval(Switches[] switchval) {
            this.switchval = switchval;
        }

        public Map<String, Object> getMetaData() {
            return metaData;
        }

        public void setMetaData(Map<String, Object> metaData) {
            this.metaData = metaData;
        }

        // Getters and Setters
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

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String[] getCall() {
      return call;
    }

    public String getConditionCall() {
      return conditionCall;
    }

    public void setConditionCall(String conditionCall) {
      this.conditionCall = conditionCall;
    }

    public String getConditionInline() {
      return conditionInline;
    }

    public void setConditionInline(String conditionInline) {
      this.conditionInline = conditionInline;
    }

    public void setCall(String[] call) {
      this.call = call;
    }

        public InputMap getPluginprop() {
            return pluginprop;
        }

        public void setData(InputMap pluginprop) {
            this.pluginprop = pluginprop;
        }

        public Config getConfig() {
      return config;
    }

    public void setConfig(Config config) {
      this.config = config;
    }

    public String getNext() {
      return next;
    }

    public void setNext(String next) {
      this.next = next;
    }

    public String getNextFalse() {
      return nextFalse;
    }



    public void setNextFalse(String nextFalse) {
      this.nextFalse = nextFalse;
    }

        public String getSignalName() {
            return signalName;
        }

        public void setSignalName(String signalName) {
            this.signalName = signalName;
        }
    }


public static  class Switches{
      private String id;
      private String key;
      private String value;
      private boolean visible;
      private Integer idx;
      private String[] next;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String[] getNext() {
        return next;
    }

    public void setNext(String[] next) {
        this.next = next;
    }

    public Integer getIdx() {
        return idx == null ? 1 : idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

  // 🔹 Inner Config class
  public static class Config {
    private Integer waitSeconds;
    private Integer timeoutSeconds;
    private Integer maximumAttempts;
    private Integer initialIntervalSeconds;
    private Integer maximumIntervalSeconds;
    private Double backoffCoefficient;

    // Getters and Setters

    public Integer getWaitSeconds() {
      return waitSeconds;
    }

    public void setWaitSeconds(Integer waitSeconds) {
      this.waitSeconds = waitSeconds;
    }

    public Integer getTimeoutSeconds() {
      return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getMaximumAttempts() {
      return maximumAttempts;
    }

    public void setMaximumAttempts(Integer maximumAttempts) {
      this.maximumAttempts = maximumAttempts;
    }

    public Integer getInitialIntervalSeconds() {
      return initialIntervalSeconds;
    }

    public void setInitialIntervalSeconds(Integer initialIntervalSeconds) {
      this.initialIntervalSeconds = initialIntervalSeconds;
    }

    public Integer getMaximumIntervalSeconds() {
      return maximumIntervalSeconds;
    }

    public void setMaximumIntervalSeconds(Integer maximumIntervalSeconds) {
      this.maximumIntervalSeconds = maximumIntervalSeconds;
    }

    public Double getBackoffCoefficient() {
      return backoffCoefficient;
    }

    public void setBackoffCoefficient(Double backoffCoefficient) {
      this.backoffCoefficient = backoffCoefficient;
    }



  }


}
