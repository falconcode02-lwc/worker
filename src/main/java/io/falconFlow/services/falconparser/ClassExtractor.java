package io.falconFlow.services.falconparser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassExtractor {

  public static Map<String, String> extractClasses(String fileContent) {
    Map<String, String> classMap = new LinkedHashMap<>();

    // Regex to match: public class ClassName { ... }
    Pattern pattern =
        Pattern.compile("(public\\s+class\\s+(\\w+)\\s*\\{[\\s\\S]*?\\})", Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(fileContent);

    while (matcher.find()) {
      String fullClass = matcher.group(1); // entire class code
      String className = matcher.group(2); // class name
      classMap.put(className, fullClass.trim());
    }

    return classMap;
  }

  public static String getFirstClassName(String source) {
    // Regex to match "class ClassName"
    Pattern pattern = Pattern.compile("\\bclass\\s+(\\w+)");
    Matcher matcher = pattern.matcher(source);

    if (matcher.find()) {
      return matcher.group(1); // First class name
    }
    return null; // if no class found
  }
}
