package io.falconFlow.dto.db;

// Column DTO
public class ColumnDto {
  private String name;
  private String type;
  private Integer length;
  private Boolean isPrimary;
  private Boolean isNullable;
  private Boolean isAutoIncrement;

  // getters and setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean primary) {
    isPrimary = primary;
  }

  public Boolean getIsNullable() {
    return isNullable;
  }

  public void setIsNullable(Boolean nullable) {
    isNullable = nullable;
  }

  public Boolean getIsAutoIncrement() {
    return isAutoIncrement;
  }

  public void setIsAutoIncrement(Boolean isAutoIncrement) {
    this.isAutoIncrement = isAutoIncrement;
  }
}
