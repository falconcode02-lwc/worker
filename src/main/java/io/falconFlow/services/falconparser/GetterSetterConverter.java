package io.falconFlow.services.falconparser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetterSetterConverter {

  public static String addGettersSetters(String classCode) {
    // Match fields like: private Type name;
    Pattern fieldPattern = Pattern.compile("private\\s+([\\w<>]+)\\s+(\\w+)\\s*;");
    Matcher matcher = fieldPattern.matcher(classCode);

    StringBuilder methods = new StringBuilder();

    while (matcher.find()) {
      String type = matcher.group(1);
      String name = matcher.group(2);
      String camel = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);

      // Getter
      methods
          .append("\n    public ")
          .append(type)
          .append(" get")
          .append(camel)
          .append("() {\n")
          .append("        return ")
          .append(name)
          .append(";\n")
          .append("    }\n");

      // Setter
      methods
          .append("    public void set")
          .append(camel)
          .append("(")
          .append(type)
          .append(" ")
          .append(name)
          .append(") {\n")
          .append("        this.")
          .append(name)
          .append(" = ")
          .append(name)
          .append(";\n")
          .append("    }\n");
    }

    // Insert before last '}' in the class
    int lastBrace = classCode.lastIndexOf("}");
    if (lastBrace > 0) {
      return classCode.substring(0, lastBrace) + methods.toString() + "\n}";
    }
    return classCode + methods;
  }
}
