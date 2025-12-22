package io.falconFlow.services.falconparser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateDslParser {
  // Detects @new loan ln;
  private static final Pattern NEW_PATTERN =
      Pattern.compile("@new\\s+([a-zA-Z0-9_]+)\\s+([a-zA-Z0-9_]+);");

  // Detects Java-style declarations like: loan ln = ...
  private static final Pattern DECL_PATTERN =
      Pattern.compile("([a-zA-Z0-9_]+)\\s+([a-zA-Z0-9_]+)\\s*=.*;");

  // Detects DSL update statements
  private static final Pattern UPDATE_PATTERN =
      Pattern.compile(
          "update\\s*(?:\\[\\s*([a-zA-Z0-9_]+)\\s*,\\s*([a-zA-Z0-9_]+)(?:\\s*,\\s*\"([^\"]*)\")?\\s*]|([a-zA-Z0-9_]+)(?:\\s*,\\s*\"([^\"]*)\")?)\\s*;",
          Pattern.CASE_INSENSITIVE);

  public static String preprocess(String javaSource) {
    Map<String, String> varToType = new HashMap<>();

    // 1️⃣ Collect from @new loan ln;
    Matcher newMatcher = NEW_PATTERN.matcher(javaSource);
    while (newMatcher.find()) {
      varToType.put(newMatcher.group(2).trim(), newMatcher.group(1).trim());
    }

    // 2️⃣ Collect from Java declarations like: loan ln = ...
    Matcher declMatcher = DECL_PATTERN.matcher(javaSource);
    while (declMatcher.find()) {
      String type = declMatcher.group(1).trim();
      String var = declMatcher.group(2).trim();
      varToType.put(var, type);
    }

    // 3️⃣ Replace update statements
    Matcher matcher = UPDATE_PATTERN.matcher(javaSource);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String table = matcher.group(1);
      String var = matcher.group(2);
      String whereClause = matcher.group(3);
      String singleVar = matcher.group(4);
      String singleWhere = matcher.group(5);

      String replacement;
      if (singleVar != null) {
        String tableName = varToType.getOrDefault(singleVar, singleVar.toLowerCase(Locale.ROOT));
        if (singleWhere != null)
          replacement =
              "db.update(\"" + tableName + "\", " + singleVar + ", \"" + singleWhere + "\");";
        else replacement = "db.update(\"" + tableName + "\", " + singleVar + ");";
      } else {
        if (whereClause != null)
          replacement = "db.update(\"" + table + "\", " + var + ", \"" + whereClause + "\");";
        else replacement = "db.update(\"" + table + "\", " + var + ");";
      }

      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  public static void main(String[] args) {
    String code =
        """
            loan ln = db.selectOne("SELECT * from loan id = " + 2 + "", loan.class);
            ln.setName("Delta");
            update ln;
            update ln, "id=2";
        """;

    System.out.println(preprocess(code));
  }
}
