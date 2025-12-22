package io.falconFlow.services.falconparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReturnStateInjector {
  // Match either FunctionResponse or ConditionResponse declaration
  private static final Pattern RESPONSE_DECL_PATTERN =
      Pattern.compile(
          "(FunctionResponse|ConditionResponse)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*new\\s+\\1\\s*\\(\\s*\\)\\s*;",
          Pattern.MULTILINE);

  // Match "return varName;"
  private static final Pattern RETURN_PATTERN =
      Pattern.compile("(?m)^\\s*return\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;");

  public static String injectStateSetter(String source) {
    // Collect all variables that are FunctionResponse or ConditionResponse
    Matcher declMatcher = RESPONSE_DECL_PATTERN.matcher(source);
    StringBuffer sb = new StringBuffer();
    String intermediate;
    StringBuilder vars = new StringBuilder();

    while (declMatcher.find()) {
      // String type = declMatcher.group(1);
      String varName = declMatcher.group(2);
      vars.append(varName).append(","); // collect variable names
      declMatcher.appendReplacement(sb, declMatcher.group(0)); // keep as is
    }
    declMatcher.appendTail(sb);
    intermediate = sb.toString();
    sb.setLength(0);

    // Split variable list
    String[] responseVars = vars.toString().split(",");
    Matcher returnMatcher = RETURN_PATTERN.matcher(intermediate);

    while (returnMatcher.find()) {
      String varName = returnMatcher.group(1);

      boolean isTarget =
          java.util.Arrays.stream(responseVars)
              .anyMatch(v -> !v.isEmpty() && v.trim().equals(varName));

      if (isTarget) {
        // Preserve indentation
        String fullLine = returnMatcher.group(0);
        String indent = fullLine.substring(0, fullLine.indexOf("return"));
        String replacement =
            indent
                + varName
                + ".setState(this.getAllState());\n"
                + indent
                + "return "
                + varName
                + ";";
        returnMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
      } else {
        returnMatcher.appendReplacement(sb, Matcher.quoteReplacement(returnMatcher.group(0)));
      }
    }

    returnMatcher.appendTail(sb);
    return sb.toString();
  }
}
