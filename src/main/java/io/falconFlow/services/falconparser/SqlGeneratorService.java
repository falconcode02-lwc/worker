package io.falconFlow.services.falconparser;

import io.falconFlow.dto.db.ColumnDto;
import io.falconFlow.dto.db.TableDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SqlGeneratorService {

  private final SchemaService schemaService;

  public SqlGeneratorService(SchemaService schemaService) {
    this.schemaService = schemaService;
  }

  /** Generate SQL to create or alter a table */
  public String generateSql(TableDto newTable) {
    TableDto existing = schemaService.getTableSchema(newTable.getName());

    if (existing == null) {
      return generateCreate(newTable);
    } else {
      return generateAlter(existing, newTable);
    }
  }

  // ============================================================
  // CREATE TABLE
  // ============================================================
  private String generateCreate(TableDto table) {
    StringBuilder sb = new StringBuilder("CREATE TABLE ").append(table.getName()).append(" (\n");

    // Columns
    sb.append(
        table.getColumns().stream().map(this::formatColumn).collect(Collectors.joining(",\n")));

    // Primary keys
    List<String> pk =
        table.getColumns().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsPrimary()))
            .map(ColumnDto::getName)
            .toList();

    if (!pk.isEmpty()) {
      sb.append(",\nPRIMARY KEY(").append(String.join(",", pk)).append(")");
    }

    sb.append("\n);");

    // Indexes (if any)
    String indexSql = generateIndexes(table);
    if (!indexSql.isBlank()) {
      sb.append("\n").append(indexSql);
    }

    return sb.toString();
  }

  // ============================================================
  // ALTER TABLE
  // ============================================================
  private String generateAlter(TableDto existing, TableDto updated) {
    List<String> alters = new ArrayList<>();

    for (ColumnDto col : updated.getColumns()) {
      ColumnDto oldCol =
          existing.getColumns().stream()
              .filter(c -> c.getName().equalsIgnoreCase(col.getName()))
              .findFirst()
              .orElse(null);

      if (oldCol == null) {
        alters.add("ADD COLUMN " + formatColumn(col));
      } else if (!isSame(col, oldCol)) {
        alters.add("MODIFY COLUMN " + formatColumn(col));
      }
    }

    if (alters.isEmpty()) {
      return "-- No changes for " + updated.getName();
    }

    StringBuilder sb =
        new StringBuilder("ALTER TABLE ")
            .append(updated.getName())
            .append("\n")
            .append(String.join(",\n", alters))
            .append(";");

    // Index updates (new indexes if any)
    String indexSql = generateIndexes(updated);
    if (!indexSql.isBlank()) {
      sb.append("\n").append(indexSql);
    }

    return sb.toString();
  }

  // ============================================================
  // Helpers
  // ============================================================

  private String formatColumn(ColumnDto col) {
    StringBuilder sb = new StringBuilder(col.getName()).append(" ").append(col.getType());

    // Handle varchar length
    if ("VARCHAR".equalsIgnoreCase(col.getType()) && col.getLength() != null) {
      sb.append("(").append(col.getLength()).append(")");
    }

    // Handle nullability
    if (!Boolean.TRUE.equals(col.getIsNullable())) {
      sb.append(" NOT NULL");
    }

    // Handle auto-increment
    if (Boolean.TRUE.equals(col.getIsAutoIncrement())) {
      sb.append(" AUTO_INCREMENT");
    }

    return sb.toString();
  }

  private boolean isSame(ColumnDto a, ColumnDto b) {
    return a.getType().equalsIgnoreCase(b.getType())
        && Objects.equals(a.getLength(), b.getLength())
        && Objects.equals(a.getIsNullable(), b.getIsNullable())
        && Objects.equals(a.getIsAutoIncrement(), b.getIsAutoIncrement());
  }

  /** Generate SQL for indexes (normal and unique) */
  private String generateIndexes(TableDto table) {
    if (table.getIndexes() == null || table.getIndexes().isEmpty()) {
      return "";
    }

    return table.getIndexes().stream()
        .map(
            idx -> {
              String type = Boolean.TRUE.equals(idx.getIsUnique()) ? "UNIQUE " : "";
              return "CREATE "
                  + type
                  + "INDEX "
                  + idx.getName()
                  + " ON "
                  + table.getName()
                  + "("
                  + String.join(",", idx.getColumns())
                  + ");";
            })
        .collect(Collectors.joining("\n"));
  }

  // ============================================================
  // Passthrough to SchemaService
  // ============================================================

  public TableDto getColumns(String tableName) {
    return schemaService.getTableSchema(tableName);
  }

  public List<String> getObjects() {
    return schemaService.getObjects();
  }
}
