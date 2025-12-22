package io.falconFlow.services.falconparser;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIDslParser {

  /**
   * Handles syntax like: Todo t = @api[TODO_API, null, null]; → Todo t = api.call("TODO_API", null,
   * null, Todo.class);
   */
  public static String preprocessSimple(String dslString) {
    StringBuffer result = new StringBuffer();

    // Match pattern: <Type> <var> = @api[API_NAME, arg1, arg2];
    String regex = "([\\w<>\\s,]+)\\s+(\\w+)\\s*=\\s*@api\\s*\\[\\s*(\\w+)\\s*,\\s*(.*?)\\s*];";
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(dslString);

    while (matcher.find()) {
      String type = matcher.group(1).trim(); // e.g. Todo, Map<String,Object>
      String var = matcher.group(2).trim(); // variable name
      String apiName = matcher.group(3).trim(); // TODO_API
      String inside = matcher.group(4).trim(); // arguments inside brackets after API name

      // Split args safely
      List<String> args =
          Splitter.onPattern("\\s*,\\s*").trimResults().omitEmptyStrings().splitToList(inside);

      // pad to 2 args (payload, headers)
      while (args.size() < 2) args.add("null");

      String payload = args.get(0);
      String headers = args.get(1);

      String replacement;

      if (type.startsWith("List<")) {
        replacement =
            String.format(
                "\n%s %s = api.callByKey(\"%s\", %s, %s, new TypeReference<%s>() {});",
                type, var, apiName, payload, headers, type);
      } else if (type.startsWith("Map")) {
        replacement =
            String.format(
                "\n%s %s = api.callByKey(\"%s\", %s, %s, Map.class);",
                type, var, apiName, payload, headers);
      } else {
        replacement =
            String.format(
                "\n%s %s = api.callByKey(\"%s\", %s, %s, %s.class);",
                type, var, apiName, payload, headers, type);
      }

      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }

    matcher.appendTail(result);
    return result.toString();
  }

  /** Original variant for parsing API DSL with dynamic URL, method, etc. */
  public static String preprocess(String dslString) {
    StringBuffer result = new StringBuffer();

    String regex = "([\\w<>\\s,]+)\\s+(\\w+)\\s*=\\s*@api\\s*\\[\\s*(.*?)\\s*];";
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(dslString);

    while (matcher.find()) {
      String type = matcher.group(1).trim();
      String var = matcher.group(2).trim();
      String inside = matcher.group(3).trim();

      List<String> args =
          Splitter.onPattern("\\s*,\\s*").trimResults().omitEmptyStrings().splitToList(inside);

      while (args.size() < 4) args.add("null");

      String method = args.get(1);
      String url = args.get(0);
      String body = args.get(2);
      String headers = args.get(3);

      String replacement;

      if (type.startsWith("List<")) {
        replacement =
            String.format(
                "\n%s %s = api.call(%s, %s, %s, %s, new TypeReference<%s>() {});",
                type, var, url, method, body, headers, type);
      } else if (type.startsWith("Map")) {
        replacement =
            String.format(
                "\n%s %s = api.call(%s, %s, %s, %s, Map.class);",
                type, var, url, method, body, headers);
      } else {
        replacement =
            String.format(
                "\n%s %s = api.call(%s, %s, %s, %s, %s.class);",
                type, var, url, method, body, headers, type);
      }

      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }

    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Variant for quoted string based API syntax: e.g. User u = @api["GET","/api/users", payload,
   * headers];
   */
  public static String preprocess1(String dslString) {
    String regex =
        "([\\w<>\\s,]+)\\s+(\\w+)\\s*=\\s*@api\\s*\\[\\s*\"(.*?)\"\\s*,\\s*\"(.*?)\"\\s*(?:,\\s*(.*?))?\\];";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(dslString);

    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String type = matcher.group(1).trim();
      String var = matcher.group(2);
      String method = matcher.group(3);
      String url = matcher.group(4);
      String restArgs = matcher.group(5);

      String finalType = type;
      String runtimeClass;

      if (type.startsWith("Map<") || type.equals("Map")) {
        finalType = "Map<String,Object>";
        runtimeClass = "Map.class";
      } else {
        String simple = type.contains("<") ? type.substring(0, type.indexOf("<")) : type;
        runtimeClass = simple + ".class";
      }

      String body = "null";
      String headers = "null";

      if (restArgs != null && !restArgs.isBlank()) {
        List<String> parts =
            Splitter.onPattern("\\s*,\\s*").trimResults().omitEmptyStrings().splitToList(restArgs);
        if (parts.size() > 0) body = parts.get(0);
        if (parts.size() > 1) headers = parts.get(1);
      }

      String replacement =
          String.format(
              "%s %s = api.call(\"%s\", \"%s\", %s, %s, %s);",
              finalType, var, method, url, body, headers, runtimeClass);

      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);

    return result.toString();
  }
}
