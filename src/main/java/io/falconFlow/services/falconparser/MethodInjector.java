package io.falconFlow.services.falconparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodInjector {
  // Pattern for @run method start — matches until opening brace {
  private static final Pattern RUN_METHOD_PATTERN =
      Pattern.compile("(@run\\s+public\\s+[^{]+\\{)", Pattern.MULTILINE);

  public static String preprocess(String source) {
    Matcher matcher = RUN_METHOD_PATTERN.matcher(source);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String methodStart = matcher.group(1);

      // Find indentation level after '{'
      String indent = "";
      Matcher indentMatch = Pattern.compile("^(\\s*)@run", Pattern.MULTILINE).matcher(methodStart);
      if (indentMatch.find()) {
        indent = indentMatch.group(1) + "    "; // one extra indent for inside the method
      }

      String replacement =
          methodStart
              + "\n"
              + indent
              + "this.initState(req.getState());";
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }

    matcher.appendTail(sb);
    return sb.toString();
  }
}
