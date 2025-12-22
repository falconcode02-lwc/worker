package io.falconFlow.services.falconparser;

public class ObjectNewDslParser {

  static final String regex = "@new\\s+(\\w+)\\s+(\\w+);";

  public static String preprocess(String sourceCode) {
    return sourceCode.replaceAll(regex, "$1 $2 = new $1();");
  }
}
