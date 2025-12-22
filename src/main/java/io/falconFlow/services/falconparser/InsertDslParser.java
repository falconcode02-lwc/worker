package io.falconFlow.services.falconparser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertDslParser {
  // Matches insert[user, v];
  // Pattern for @new loan ln;
  private static final Pattern NEW_PATTERN =
      Pattern.compile("@new\\s+([a-zA-Z0-9_]+)\\s+([a-zA-Z0-9_]+);");

  // Pattern for Java declarations like: loan ln = ...
  private static final Pattern DECL_PATTERN =
      Pattern.compile("([a-zA-Z0-9_]+)\\s+([a-zA-Z0-9_]+)\\s*=.*;");

  // Pattern for DSL insert statements
  private static final Pattern INSERT_PATTERN =
      Pattern.compile(
          "insert\\s*(?:\\[\\s*([a-zA-Z0-9_]+)\\s*,\\s*([a-zA-Z0-9_]+)\\s*]|([a-zA-Z0-9_]+))\\s*;",
          Pattern.CASE_INSENSITIVE);

  public static String preprocess(String javaSource) {
    Map<String, String> varToType = new HashMap<>();

    // 1️⃣ Collect variable-type mappings from @new statements
    Matcher newMatcher = NEW_PATTERN.matcher(javaSource);
    while (newMatcher.find()) {
      String type = newMatcher.group(1).trim();
      String var = newMatcher.group(2).trim();
      varToType.put(var, type);
    }

    // 2️⃣ Collect from Java declarations like: loan ln = ...
    Matcher declMatcher = DECL_PATTERN.matcher(javaSource);
    while (declMatcher.find()) {
      String type = declMatcher.group(1).trim();
      String var = declMatcher.group(2).trim();
      varToType.put(var, type);
    }

    // 3️⃣ Replace insert statements
    Matcher matcher = INSERT_PATTERN.matcher(javaSource);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String table = matcher.group(1);
      String var = matcher.group(2);
      String singleVar = matcher.group(3);

      String replacement;
      if (singleVar != null) {
        // infer table name using @new or declaration
        String tableName = varToType.getOrDefault(singleVar, singleVar.toLowerCase(Locale.ROOT));
        replacement = "db.insert(\"" + tableName + "\", " + singleVar + ");";
      } else {
        replacement = "db.insert(\"" + table + "\", " + var + ");";
      }

      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }

  public static void main(String[] args) {
    String code =
        """
            @new loan ln;
            ln.setId(1);
            ln.setName("Alpha");
            insert ln;

            loan ln2 = db.selectOne("SELECT * FROM loan WHERE id=2", loan.class);
            ln2.setName("Beta");
            insert ln2;

            insert[loan, ln2];
        """;

    System.out.println(preprocess(code));
  }
}
