package io.falconFlow.dao;

import java.util.List;

public class Query {
  public enum Type {
    SELECT,
    UPDATE,
    DELETE,
    INSERT
  }

  private final Type type;
  private final String entity;
  private final List<String> fields;
  private final String where;
  private final String updates;

  public Query(Type type, String entity, List<String> fields, String where, String updates) {
    this.type = type;
    this.entity = entity;
    this.fields = fields;
    this.where = where;
    this.updates = updates;
  }

  public Type getType() {
    return type;
  }

  public String getEntity() {
    return entity;
  }

  public List<String> getFields() {
    return fields;
  }

  public String getWhere() {
    return where;
  }

  public String getUpdates() {
    return updates;
  }
}
