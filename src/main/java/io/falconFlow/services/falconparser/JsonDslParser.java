package io.falconFlow.services.falconparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDslParser {

  public static String preprocess(String input) {
    String output = input;

    // Replace @jsonToStr(object) -> JsonUtil.jsonToStr(object)
    Pattern strPattern = Pattern.compile("@jsonToStr\\((.*?)\\);");
    Matcher strMatcher = strPattern.matcher(output);
    StringBuffer sb1 = new StringBuffer();
    while (strMatcher.find()) {
      String arg = strMatcher.group(1).trim();
      strMatcher.appendReplacement(
          sb1, Matcher.quoteReplacement("JsonUtils.jsonToStr(" + arg + ");"));
    }
    strMatcher.appendTail(sb1);
    output = sb1.toString();

    // Replace @jsonToObj(str) when declared as <Type> var = @jsonToObj(str);
    Pattern objPattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*=\\s*@jsonToObj\\((.*?)\\);");
    Matcher objMatcher = objPattern.matcher(output);
    StringBuffer sb2 = new StringBuffer();
    while (objMatcher.find()) {
      String type = objMatcher.group(1); // e.g., Donut
      String var = objMatcher.group(2); // e.g., d
      String arg = objMatcher.group(3).trim(); // e.g., str
      String replacement =
          String.format("%s %s = JsonUtils.jsonToObj(%s, %s.class);", type, var, arg, type);
      objMatcher.appendReplacement(sb2, Matcher.quoteReplacement(replacement));
    }
    objMatcher.appendTail(sb2);

    return sb2.toString();
  }
}
