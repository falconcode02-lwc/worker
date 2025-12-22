package io.falconFlow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CompilerResponse {
  private String status;

  private Integer id;

  private Integer version;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  private String message;
  private StackTraceElement[] stakeStake;

  private LocalDateTime compiledTime;
  private LocalDateTime modifiedTime;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public StackTraceElement[] getStakeStake() {
    return stakeStake;
  }

  public void setStakeStake(StackTraceElement[] stakeStake) {
    this.stakeStake = stakeStake;
  }

  public List<String> getDiagnostic() {
    return diagnostic;
  }

  public void setDiagnostic(List<String> diagnostic) {
    this.diagnostic = diagnostic;
  }

  private List<String> diagnostic;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public LocalDateTime getCompiledTime() {
    return compiledTime;
  }

  public void setCompiledTime(LocalDateTime compiledTime) {
    this.compiledTime = compiledTime;
  }

  public LocalDateTime getModifiedTime() {
    return modifiedTime;
  }

  public void setModifiedTime(LocalDateTime modifiedTime) {
    this.modifiedTime = modifiedTime;
  }

  private String className;

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
