package io.falconFlow.dto;

public class CompilerRequest {
  public String getEncodedFile() {
    return encodedFile;
  }

  public void setEncodedFile(String encodedFile) {
    this.encodedFile = encodedFile;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  private String encodedFile;
  private String language;

  private String name;

  private String classType;

  private String version;

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private Integer id;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCompileType() {
    return compileType;
  }

  public void setCompileType(String compileType) {
    this.compileType = compileType;
  }

  private String compileType;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
