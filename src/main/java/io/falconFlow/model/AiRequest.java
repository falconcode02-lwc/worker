package io.falconFlow.model;

public class AiRequest {
  private String code;
  private String prefix;
  private int cursor;

  // getters and setters
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public int getCursor() {
    return cursor;
  }

  public void setCursor(int cursor) {
    this.cursor = cursor;
  }
}
