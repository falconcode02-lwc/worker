package io.falconFlow.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
public class DB {

  @Autowired private JdbcTemplate jdbcTemplate;

  // ---------- INSERT ----------
  public <T> T insert(String tableName, T entity) {
    String table = tableName;
    Map<String, Object> fields = extractFields(entity);

    String columns = String.join(", ", fields.keySet());
    String placeholders = String.join(", ", Collections.nCopies(fields.size(), "?"));

    String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
          int idx = 1;
          for (Object value : fields.values()) {
            ps.setObject(idx++, value);
          }
          return ps;
        },
        keyHolder);

    // If there's an auto-generated key (like `id`), set it back on entity
    if (keyHolder.getKey() != null) {
      try {
        Field idField = getDeclaredFieldIgnoreCase(entity, "id");
        idField.setAccessible(true);
        idField.set(entity, keyHolder.getKey().intValue());
      } catch (NoSuchFieldException | IllegalAccessException ignored) {
        // no id field or cannot set -> ignore
      }
    }

    return entity;
  }

  // ---------- UPDATE ----------
  // Default update (WHERE id = ?)
  public <T> T update(String tableName, T entity) {
    return update(tableName, entity, null);
  }

  // Update with custom WHERE clause
  public <T> T update(String tableName, T entity, String whereClause) {
    List<Object> values = new ArrayList<>();
    Map<String, Object> fields = extractFields(entity);

    Object idValue = fields.remove("id");

    List<String> columnsToUpdate = new ArrayList<>();
    for (Map.Entry<String, Object> ent : fields.entrySet()) {
      if (ent.getValue() != null) {
        columnsToUpdate.add(ent.getKey() + " = ?");
        values.add(ent.getValue());
      }
    }

    String setClause = String.join(", ", columnsToUpdate);

    //    String setClause =
    //        String.join(", ", fields.keySet().stream().map(col -> col + " = ?").toList());

    String sql = "UPDATE " + tableName + " SET " + setClause;

    if (whereClause != null && !whereClause.isBlank()) {
      sql += " WHERE " + whereClause;
    } else {
      if (idValue == null) {
        throw new IllegalArgumentException("Entity must have an id field or custom WHERE clause");
      }
      sql += " WHERE id = ?";
      values.add(idValue);
    }

    jdbcTemplate.update(sql, values.toArray());
    return entity;
  }

  // ---------- DELETE ----------
  public <T> void delete(Class<T> type, Object id) {
    String table = type.getSimpleName();
    String sql = "DELETE FROM " + table + " WHERE id = ?";
    jdbcTemplate.update(sql, id);
  }

  // ---------- SELECT ----------
  public List<Map<String, Object>> select(String sql) {
    return jdbcTemplate.queryForList(sql);
  }

  // Returns List<T> mapped to entity
  public <T> List<T> select(String sql, Class<T> type) {
    System.out.println(sql);
    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(type));
  }

  public List<Map<String, Object>> selectAsMap(String sql) {
    System.out.println(sql);
    return jdbcTemplate.queryForList(sql);
  }

  public <T> T selectOne(String sql, Class<T> type) {
    // Optionally enforce LIMIT 1 if not present
    String limitedSql = sql.trim().matches("(?i).*\\blimit\\s+\\d+\\b.*") ? sql : sql + " LIMIT 1";
    List<T> results = jdbcTemplate.query(limitedSql, new BeanPropertyRowMapper<>(type));
    return results.isEmpty() ? null : results.get(0);
  }

  // ---------- Helpers ----------
  private <T> Map<String, Object> extractFields(T entity) {
    Map<String, Object> row = new LinkedHashMap<>();
    for (Field f : entity.getClass().getDeclaredFields()) {
      f.setAccessible(true);
      try {
        row.put(f.getName().toLowerCase(Locale.ROOT), f.get(entity));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return row;
  }

  public static Field getDeclaredFieldIgnoreCase(Object entity, String fieldName)
      throws NoSuchFieldException {
    Class<?> clazz = entity.getClass();
    for (Field field : clazz.getDeclaredFields()) {
      if (field.getName().equalsIgnoreCase(fieldName)) {
        return field;
      }
    }
    // If not found in this class, try superclass recursively
    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null && superClass != Object.class) {
      try {
        return getDeclaredFieldIgnoreCase(superClass, fieldName);
      } catch (NoSuchFieldException e) {
        // continue searching up the hierarchy
      }
    }
    throw new NoSuchFieldException(
        "No field named '" + fieldName + "' (case-insensitive) found in " + clazz.getName());
  }
}
