package io.falconFlow.services.falconparser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectDslParser {

  // Matches: List<TestClass> rows = [SELECT ...];
  // Matches: List<...> var = [SELECT ...];
  // Matches: List<...> var = [SELECT ...];
  // private static final Pattern QUERY_PATTERN =
  //  Pattern.compile(
  //    "List<(.+?)>\\s+([a-zA-Z0-9_]+)\\s*=\\s*\\[(SELECT[\\s\\S]*?)];", Pattern.MULTILINE);

  private static final Pattern QUERY_PATTERN =
      Pattern.compile(
              "(?:List<(.+?)>|([a-zA-Z_][a-zA-Z0-9_<>]*))\\s+([a-zA-Z0-9_]+)\\s*=\\s*\\[((?i)SELECT[\\s\\S]*?)];",
//          "(?:List<(.+?)>|([a-zA-Z_][a-zA-Z0-9_<>]*))\\s+([a-zA-Z0-9_]+)\\s*=\\s*\\[(SELECT[\\s\\S]*?)];",
          Pattern.MULTILINE);

  // Matches column := expression
  // Allow method calls, dots, (), arguments inside
  // allow optional single quotes around expression
  // column :operator expression
  private static final Pattern PLACEHOLDER =
      Pattern.compile(
          "([a-zA-Z0-9_]+)\\s*:(=|!=|in|notin|like|notlike)\\s*('([^']*)'|[a-zA-Z0-9_().]+)",
          Pattern.CASE_INSENSITIVE);

  public static String preprocess(String source) {
    Matcher matcher = QUERY_PATTERN.matcher(source);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String genericType = matcher.group(1);
      String singleType = matcher.group(2);
      String varName = matcher.group(3).trim();
      String query = matcher.group(4).trim();

      if (genericType != null) genericType = genericType.trim();
      if (singleType != null) singleType = singleType.trim();
      // replace placeholders
      query = replacePlaceholders(query);

      String replacement;
      if (genericType != null) {
        if (genericType.startsWith("Map")) {
          replacement =
              "List<" + genericType + "> " + varName + " = db.selectAsMap(\"" + query + "\");";
        } else {
          replacement =
              "List<"
                  + genericType
                  + "> "
                  + varName
                  + " = db.select(\""
                  + query
                  + "\", "
                  + genericType
                  + ".class);";
        }

      } else {
        // Handle single-object with LIMIT 1
        singleType = singleType.trim();
        replacement =
            singleType
                + " "
                + varName
                + " = db.selectOne(\""
                + query
                + "\", "
                + singleType
                + ".class);";
      }

      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static String replacePlaceholders(String query) {
    Matcher m = PLACEHOLDER.matcher(query);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      String column = m.group(1).trim();
      String operator = m.group(2).toLowerCase(Locale.ROOT).trim();
      String value = m.group(3).trim(); // either quoted literal or expression

      String replacement;
      if (value.startsWith("'") && value.endsWith("'")) {
        // ✅ literal like '%test%' → keep as-is
        replacement = column + " " + toSqlOperator(operator) + " " + value;
      } else {
        // ✅ Java expression → expand
        switch (operator) {
          case "=" -> replacement = column + " = \" + " + value + " + \"";
          case "!=" -> replacement = column + " != \" + " + value + " + \"";
          case "in" -> replacement = column + " IN (\" + " + value + " + \")";
          case "notin" -> replacement = column + " NOT IN (\" + " + value + " + \")";
          case "like" -> replacement = column + " LIKE '\" + " + value + " + \"'";
          case "notlike" -> replacement = column + " NOT LIKE '\" + " + value + " + \"'";
          default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
      }

      m.appendReplacement(sb, replacement);
    }
    m.appendTail(sb);
    return queryWrap(sb.toString());
  }

  private static String toSqlOperator(String op) {
    return switch (op) {
      case "=" -> "=";
      case "!=" -> "!=";
      case "in" -> "IN";
      case "notin" -> "NOT IN";
      case "like" -> "LIKE";
      case "notlike" -> "NOT LIKE";
      default -> op;
    };
  }

  private static String queryWrap(String q) {
    // collapse whitespace
    q = q.replaceAll("\\s+", " ").trim();
    if (q.endsWith("+ \"\"")) {
      q = q.substring(0, q.length() - 4);
    }
    return q;
  }
}
