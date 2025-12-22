package io.falconFlow.dto;

import java.util.Map;

public class APIDto {

  private Integer id;

  /** Unique name or identifier for the API */
  private String name;

  /** Full endpoint URL */
  private String url;

  /** HTTP method (GET, POST, PUT, DELETE, PATCH, etc.) */
  private String method;

  /** Headers to include in the API call */
  private Map<String, String> headers;

  /** Payload to send in the API body (JSON string or raw text) */
  private String payload;

  /** Expected response model or mapping class */
  private String resultModel;

  /** Read timeout in milliseconds */
  private Integer readTimeout;

  /** Socket timeout in milliseconds */
  private Integer socketTimeout;

  /** Whether the call is synchronous or asynchronous */
  private String mode;

  private String classType;

  // 🧠 Getters and Setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public String getResultModel() {
    return resultModel;
  }

  public void setResultModel(String resultModel) {
    this.resultModel = resultModel;
  }

  public Integer getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(Integer socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public String getMode() {
    return mode;
  }

  public void setAsync(String mode) {
    this.mode = mode;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  // 🧩 Utility
  @Override
  public String toString() {
    return "ApiConfig{"
        + "name='"
        + name
        + '\''
        + ", url='"
        + url
        + '\''
        + ", method='"
        + method
        + '\''
        + ", headers="
        + headers
        + ", payload='"
        + payload
        + '\''
        + ", resultModel='"
        + resultModel
        + '\''
        + ", readTimeout="
        + readTimeout
        + ", socketTimeout="
        + socketTimeout
        + ", mode="
        + mode
        + '}';
  }
}
