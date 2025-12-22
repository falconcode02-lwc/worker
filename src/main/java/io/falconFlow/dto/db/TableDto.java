package io.falconFlow.dto.db;

import java.util.List;

// Table DTO
public class TableDto {

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  private Integer id;

  private String classType;

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  private String name;
  private List<ColumnDto> columns;
  private List<IndexDto> indexes;

  // getters and setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ColumnDto> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnDto> columns) {
    this.columns = columns;
  }

  public List<IndexDto> getIndexes() {
    return indexes;
  }

  public void setIndexes(List<IndexDto> indexes) {
    this.indexes = indexes;
  }
}
