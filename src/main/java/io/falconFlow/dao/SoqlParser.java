package io.falconFlow.dao;

import com.google.common.base.Splitter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SoqlParser {

  public static Query parse(String soql) {
    soql = soql.trim();
    String upper = soql.toUpperCase(Locale.ROOT);

    if (upper.startsWith("SELECT")) {
      return parseSelect(soql);
    } else if (upper.startsWith("UPDATE")) {
      return parseUpdate(soql);
    } else if (upper.startsWith("DELETE")) {
      return parseDelete(soql);
    } else if (upper.startsWith("INSERT")) {
      return parseInsert(soql);
    }

    throw new IllegalArgumentException("Unsupported SOQL: " + soql);
  }

  // ---------- SELECT ----------
  private static Query parseSelect(String soql) {
    // Example: SELECT id, name FROM User WHERE status = 'ACTIVE'
    List<String> parts = Splitter.onPattern("(?i)FROM").splitToList(soql);

    String fieldsPart = parts.get(0).replaceFirst("(?i)SELECT", "").trim();
    List<String> fields = Arrays.stream(fieldsPart.split(",")).map(String::trim).toList();

    String rest = parts.get(1).trim();
    String entity;
    String where = null;

    if (rest.matches("(?i).*WHERE.*")) {
      String[] sub = rest.split("(?i)WHERE", 2);
      entity = sub[0].trim();
      where = sub[1].trim();
    } else {
      entity = rest;
    }

    return new Query(Query.Type.SELECT, entity, fields, where, null);
  }

  // ---------- UPDATE ----------
  private static Query parseUpdate(String soql) {
    // Example: UPDATE User SET status = 'INACTIVE' WHERE id = 1
    List<String> parts = Splitter.onPattern("(?i)SET").splitToList(soql);
    String entity = parts.get(0).replaceFirst("(?i)UPDATE", "").trim();
    String rest = parts.get(0).trim();

    String updates;
    String where = null;

    if (rest.matches("(?i).*WHERE.*")) {
      String[] sub = rest.split("(?i)WHERE", 2);
      updates = sub[0].trim();
      where = sub[1].trim();
    } else {
      updates = rest;
    }

    return new Query(Query.Type.UPDATE, entity, List.of(), where, updates);
  }

  // ---------- DELETE ----------
  private static Query parseDelete(String soql) {
    // Example: DELETE FROM User WHERE id = 1
    String afterDelete = soql.replaceFirst("(?i)DELETE FROM", "").trim();
    String entity;
    String where = null;

    if (afterDelete.matches("(?i).*WHERE.*")) {
      String[] sub = afterDelete.split("(?i)WHERE", 2);
      entity = sub[0].trim();
      where = sub[1].trim();
    } else {
      entity = afterDelete;
    }

    return new Query(Query.Type.DELETE, entity, List.of(), where, null);
  }

  // ---------- INSERT ----------
  private static Query parseInsert(String soql) {
    // Example: INSERT INTO User (id, name) VALUES (1, 'Alice')
    String afterInsert = soql.replaceFirst("(?i)INSERT INTO", "").trim();

    String entity;
    String fieldsPart = null;
    String valuesPart = null;

    if (afterInsert.matches("(?i).+VALUES.+")) {
      String[] parts = afterInsert.split("(?i)VALUES", 2);
      String beforeValues = parts[0].trim();
      valuesPart = parts[1].trim();

      int openIdx = beforeValues.indexOf("(");
      int closeIdx = beforeValues.indexOf(")");
      if (openIdx > 0 && closeIdx > openIdx) {
        entity = beforeValues.substring(0, openIdx).trim();
        fieldsPart = beforeValues.substring(openIdx + 1, closeIdx).trim();
      } else {
        entity = beforeValues;
      }
    } else {
      throw new IllegalArgumentException("Invalid INSERT syntax: " + soql);
    }

    List<String> fields =
        fieldsPart != null
            ? Arrays.stream(fieldsPart.split(",")).map(String::trim).toList()
            : List.of();

    String updates = valuesPart; // raw VALUES part stored as "updates"

    return new Query(Query.Type.INSERT, entity, fields, null, updates);
  }
}
