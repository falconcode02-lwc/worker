package io.falconFlow.services.falconparser;

import io.falconFlow.dto.db.ColumnDto;
import io.falconFlow.dto.db.TableDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SchemaService {

  private final JdbcTemplate jdbcTemplate;

  public SchemaService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Fetches column schema info for a given table. Includes primary key and auto-increment
   * detection.
   */
  public TableDto getTableSchema(String tableName) {
    String sql =
        "SELECT column_name, data_type, character_maximum_length, is_nullable, "
            + "column_key, extra "
            + "FROM information_schema.columns "
            + "WHERE table_schema = DATABASE() AND table_name = ? "
            + "ORDER BY ORDINAL_POSITION";

    List<ColumnDto> columns =
        jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
              ColumnDto col = new ColumnDto();
              col.setName(rs.getString("column_name"));
              col.setType(rs.getString("data_type").toUpperCase(Locale.ROOT));
              col.setLength(
                  rs.getObject("character_maximum_length") != null
                      ? rs.getInt("character_maximum_length")
                      : null);
              col.setIsNullable("YES".equalsIgnoreCase(rs.getString("is_nullable")));
              col.setIsPrimary("PRI".equalsIgnoreCase(rs.getString("column_key")));
              col.setIsAutoIncrement(
                  rs.getString("extra") != null
                      && rs.getString("extra").toUpperCase(Locale.ROOT).contains("AUTO_INCREMENT"));
              return col;
            },
            tableName);

    if (columns.isEmpty()) {
      return null; // table does not exist
    }

    TableDto table = new TableDto();
    table.setName(tableName);
    table.setColumns(columns);
    return table;
  }

  /** Lists all database tables excluding internal ones. */
  public List<String> getObjects() {
    String sql =
        "SELECT TABLE_NAME "
            + "FROM information_schema.TABLES "
            + "WHERE TABLE_SCHEMA = DATABASE() "
            + "AND TABLE_NAME NOT IN ('ff_functions', 'users');";

    return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("TABLE_NAME"));
  }

  /** Creates a table dynamically from TableDto (supports AUTO_INCREMENT) */
  public void createTable(TableDto tableDto) {
    String columnsSql =
        tableDto.getColumns().stream()
            .map(
                c -> {
                  StringBuilder sb = new StringBuilder();
                  sb.append(c.getName()).append(" ").append(mapSqlType(c));
                  if (Boolean.TRUE.equals(c.getIsPrimary())) {
                    sb.append(" PRIMARY KEY");
                  }
                  if (Boolean.TRUE.equals(c.getIsAutoIncrement())) {
                    sb.append(" AUTO_INCREMENT");
                  }
                  if (!Boolean.TRUE.equals(c.getIsNullable())) {
                    sb.append(" NOT NULL");
                  }
                  return sb.toString();
                })
            .collect(Collectors.joining(", "));

    String sql = "CREATE TABLE IF NOT EXISTS " + tableDto.getName() + " (" + columnsSql + ")";
    jdbcTemplate.execute(sql);
  }

  private String mapSqlType(ColumnDto col) {
    switch (col.getType().toUpperCase(Locale.ROOT)) {
      case "STRING":
      case "VARCHAR":
        return "VARCHAR(" + (col.getLength() != null ? col.getLength() : 255) + ")";
      case "INT":
      case "INTEGER":
        return "INT";
      case "LONG":
      case "BIGINT":
        return "BIGINT";
      case "DOUBLE":
      case "FLOAT":
        return "DOUBLE";
      case "DATE":
      case "DATETIME":
      case "TIMESTAMP":
        return "DATETIME";
      case "BOOLEAN":
      case "BIT":
        return "BOOLEAN";
      default:
        return col.getType();
    }
  }
}
