package io.falconFlow.services.models;

import java.util.Map;

public class APIModel {

  private int id;
  private String name;
  private String url;
  private String method;
  private Map<String, String> headers;
  private String payload;
  private String resultModel;
  private int readTimeout;
  private int socketTimeout;
  private String mode;
  private String classType;
  private String raw_process_class;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

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

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public int getSocketTimeout() {
    return socketTimeout;
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  public String getRaw_process_class() {
    return raw_process_class;
  }

  public void setRaw_process_class(String raw_process_class) {
    this.raw_process_class = raw_process_class;
  }
}
