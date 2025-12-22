package io.falconFlow.dto.db;

import java.util.List;

public class IndexDto {
  private String name;
  private List<String> columns;
  private Boolean isUnique;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getColumns() {
    return columns;
  }

  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  public Boolean getIsUnique() {
    return isUnique;
  }

  public void setIsUnique(Boolean unique) {
    isUnique = unique;
  }
}
