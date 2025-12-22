package io.falconFlow.services.falconparser;

public class AnnotationParser {

  public static String preprocess(String sourceCode) {
    String convert = sourceCode;
    convert = convert.replace("@usedb", "@Autowired DB db;\n");
    convert = convert.replace("@useapi", "@Autowired APIService api;\n");
    convert = convert.replace("@usevault", "@Autowired SecretManagerService vault;\n");
    convert = convert.replace("@useplugin", "@Autowired PluginManagerService pluginService;\n");
    convert = convert.replace("@onload", "@PostConstruct");
    convert = convert.replace("@onunload", "@PreDestroy");
    convert = convert.replace("@run", "@Override");
    convert = convert.replace("@println", "System.out.println");
    convert = convert.replace("@print", "System.out.print");
    convert = convert.replace("@use", "@Autowired");
    convert = convert.replace("@vault", "vault.get");

    return convert;
  }
}
