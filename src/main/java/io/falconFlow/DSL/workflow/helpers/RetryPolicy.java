package io.falconFlow.DSL.workflow.helpers;

public class RetryPolicy {

  private int maximumAttempts;
  private int initialIntervalSeconds;
  private int maximumIntervalSeconds;
  private double backoffCoefficient;

  public int getMaximumAttempts() {
    return maximumAttempts;
  }

  public void setMaximumAttempts(int maximumAttempts) {
    this.maximumAttempts = maximumAttempts;
  }

  public int getInitialIntervalSeconds() {
    return initialIntervalSeconds;
  }

  public void setInitialIntervalSeconds(int initialIntervalSeconds) {
    this.initialIntervalSeconds = initialIntervalSeconds;
  }

  public int getMaximumIntervalSeconds() {
    return maximumIntervalSeconds;
  }

  public void setMaximumIntervalSeconds(int maximumIntervalSeconds) {
    this.maximumIntervalSeconds = maximumIntervalSeconds;
  }

  public double getBackoffCoefficient() {
    return backoffCoefficient;
  }

  public void setBackoffCoefficient(double backoffCoefficient) {
    this.backoffCoefficient = backoffCoefficient;
  }
}
