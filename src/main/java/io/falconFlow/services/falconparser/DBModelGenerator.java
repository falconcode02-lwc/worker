package io.falconFlow.services.falconparser;

import io.falconFlow.dto.db.ColumnDto;
import io.falconFlow.dto.db.TableDto;

import java.util.Locale;
import java.util.Map;

public class DBModelGenerator {
  private static final Map<String, String> typeMap =
      Map.of(
          "VARCHAR",
          "String",
          "BIGINT",
          "Long",
          "INT",
          "Integer",
          "DOUBLE",
          "Double",
          "DATE",
          "LocalDate",
          "TINY INT",
          "Integer",
          "FLOAT",
          "Double",
          "BOOLEAN",
          "Boolean",
          "JSON",
          "String");

  public static String getClassFromJson(TableDto table) throws Exception {
    // Load JSON (you can also use a String or API input)

    StringBuilder classBuilder = new StringBuilder();
    // Set<String> imports = new HashSet<>();

    // Package and class declaration
    classBuilder.append("public class ").append(table.getName()).append(" {\n\n");

    // Fields
    for (ColumnDto col : table.getColumns()) {
      String colName = col.getName();
      String sqlType = col.getType();
      String javaType = typeMap.getOrDefault(sqlType, "String");

      //      if (javaType.equals("LocalDate")) {
      //        imports.add("import java.time.LocalDate;");
      //      }

      String fieldName = toCamelCase(colName);
      classBuilder
          .append("    private ")
          .append(javaType)
          .append(" ")
          .append(fieldName)
          .append(";\n");

      System.out.println(fieldName);
    }

    classBuilder.append("\n");

    // Getters and Setters
    //    for (ColumnDto col : table.getColumns()) {
    //
    //      String javaType = typeMap.getOrDefault(col.getType(), "String");
    //
    //      String fieldName = toCamelCase(col.getName());
    //      String methodName = toPascalCase(col.getName());
    //
    //      // Getter
    //      classBuilder
    //          .append("    public ")
    //          .append(javaType)
    //          .append(" get")
    //          .append(methodName)
    //          .append("() {\n")
    //          .append("        return ")
    //          .append(fieldName)
    //          .append(";\n")
    //          .append("    }\n\n");
    //
    //      // Setter
    //      classBuilder
    //          .append("    public void set")
    //          .append(methodName)
    //          .append("(")
    //          .append(javaType)
    //          .append(" ")
    //          .append(fieldName)
    //          .append(") {\n")
    //          .append("        this.")
    //          .append(fieldName)
    //          .append(" = ")
    //          .append(fieldName)
    //          .append(";\n")
    //          .append("    }\n\n");
    //    }
    //
    classBuilder.append("}");
    //
    //    // Print imports
    //    for (String imp : imports) {
    //      System.out.println(imp);
    //    }

    return classBuilder.toString();
  }

  private static String toCamelCase(String text) {
    // List<String> parts = Splitter.onPattern(Pattern.quote("_")).splitToList(text);
    StringBuilder sb = new StringBuilder();
    sb.append(text.substring(0, 1).toUpperCase(Locale.ROOT)).append(text.substring(1));
    return sb.toString();
  }

  //  private static String toPascalCase(String text) {
  //    List<String> parts = Splitter.onPattern(Pattern.quote("_")).splitToList(text);
  //    StringBuilder sb = new StringBuilder();
  //    for (String part : parts) {
  //      sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
  //    }
  //    return sb.toString();
  //  }
}
