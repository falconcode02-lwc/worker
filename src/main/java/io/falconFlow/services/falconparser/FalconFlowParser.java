package io.falconFlow.services.falconparser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.falconFlow.services.constants.ConstantsPackagesnImports;
import org.springframework.amqp.rabbit.connection.Connection;

public class FalconFlowParser {
  public static final String FUNCTION = "function";
  public static final String CONDITION = "condition";
  public static final String CONTROLLER = "controller";
  public static final String PLUGIN = "plugin";

  public static final String MODEL = "model";

  public static String preprocess(String javaSource, String type) throws Exception {

    if (javaSource.contains("package")) {
      throw new Exception("Invalid Statement Package");
    }


//    if (javaSource.contains("import")) {
//      throw new Exception("Invalid Statement Import");
//    }

  String convert = javaSource;

  // Convert plugin.setProps({...}) calls to plugin.setProps("{...}") by
  // turning the object literal into an escaped JSON string so downstream
  // processors can treat it as a single string argument.
  if(type.equals(PLUGIN)) {
      convert = replacePluginSetPropsObjects(convert);
      convert = replacePluginSetSecretsObjects(convert);
      // Add second parameter to pluginService.register calls when missing
      convert = replacePluginServiceRegisterCalls(convert);


  }
    if (type.equals(FUNCTION) || type.equals(CONDITION) || type.equals(CONTROLLER) || type.equals(PLUGIN)) {
//      JavascriptObjTOMapParser jsObjectParser = new JavascriptObjTOMapParser();
//      convert = jsObjectParser.process(convert);
      convert = StateParser.preprocess(convert);
      if(!type.equals(FUNCTION)) {
          convert = MethodInjector.preprocess(convert);
      }
      convert = SelectDslParser.preprocess(convert); // parse all select querie
      convert = InsertDslParser.preprocess(convert);
      convert = UpdateDslParser.preprocess(convert);
      convert = ObjectNewDslParser.preprocess(convert);
      convert = AnnotationParser.preprocess(convert);
      convert = JsonDslParser.preprocess(convert);
      convert = APIDslParser.preprocessSimple(convert);
      convert = APIDslParser.preprocess(convert);
      convert = ReturnStateInjector.injectStateSetter(convert);
      convert = ConstantsPackagesnImports.functionsPackage + convert;
    }
    return convert;
  }

    /**
     * Find occurrences of pluginService.register(...) where only a single
     * top-level argument is provided, and append
     *   , this.getClass().getSimpleName()
     * as a second argument. This respects quoted strings and nested
     * parentheses/brackets/braces while scanning for the matching ')'.
     */
    private static String replacePluginServiceRegisterCalls(String src) {
      if (src == null || !src.contains("pluginService.register(")) return src;

      StringBuilder out = new StringBuilder();
      int idx = 0;
      String callToken = "pluginService.register(";
      while (true) {
        int callIdx = src.indexOf(callToken, idx);
        if (callIdx == -1) {
          out.append(src.substring(idx));
          break;
        }
        out.append(src, idx, callIdx);

        int parenStart = callIdx + callToken.length() - 1; // position of '('
        if (parenStart < 0 || parenStart >= src.length() || src.charAt(parenStart) != '(') {
          // shouldn't happen; append token and continue
          out.append(callToken);
          idx = callIdx + callToken.length();
          continue;
        }

        // scan to matching closing paren while tracking top-level commas
        int i = parenStart;
        int parenDepth = 0;
        boolean inString = false;
        boolean escape = false;
        boolean sawTopLevelComma = false;
        for (; i < src.length(); i++) {
          char c = src.charAt(i);
          if (inString) {
            if (escape) {
              escape = false;
            } else if (c == '\\') {
              escape = true;
            } else if (c == '"') {
              inString = false;
            }
          } else {
            if (c == '"') {
              inString = true;
            } else if (c == '(') {
              parenDepth++;
            } else if (c == ')') {
              parenDepth--;
              if (parenDepth == 0) {
                break; // i is closing paren for the call
              }
            } else if (c == ',' && parenDepth == 1) {
              // top-level comma separates arguments
              sawTopLevelComma = true;
            }
          }
        }

        if (i >= src.length()) {
          // unmatched, append remainder
          out.append(src.substring(callIdx));
          break;
        }

        // extract original arguments (between parenStart+1 and i)
        String args = src.substring(parenStart + 1, i).trim();

          // single arg — append second parameter
          out.append("pluginService.register(").append(args).append(", this.getClass().getSimpleName())");


        // advance past the closing paren
        idx = i + 1;
      }

      return out.toString();
    }

    /**
     * Find occurrences of plugin.setProps({ ... }) and replace the object literal
     * argument with a quoted string: plugin.setProps("{ ... }"). Internal
     * double-quotes are escaped. This is a best-effort parser that handles
     * strings and escaped quotes inside the JSON-like object.
     */
    private static String replacePluginSetPropsObjects(String src) {
        if (src == null || !src.contains("plugin.setProps(")) return src;

        StringBuilder out = new StringBuilder();
        int idx = 0;
        while (true) {
            int callIdx = src.indexOf("plugin.setProps(", idx);
            if (callIdx == -1) {
                out.append(src.substring(idx));
                break;
            }
            // append up to the call
            out.append(src, idx, callIdx);

            // find the first '{' after the call start
            int braceStart = src.indexOf('{', callIdx);
            if (braceStart == -1) {
                // nothing to do, append rest and break
                out.append(src.substring(callIdx));
                break;
            }

            // scan to matching closing brace, respecting quoted strings
            int i = braceStart;
            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            for (; i < src.length(); i++) {
                char c = src.charAt(i);
                if (inString) {
                    if (escape) {
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        inString = false;
                    }
                } else {
                    if (c == '"') {
                        inString = true;
                    } else if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            break; // i points to closing brace
                        }
                    }
                }
            }

            if (i >= src.length()) {
                // unmatched braces: append remainder and exit
                out.append(src.substring(callIdx));
                break;
            }

            // extract object literal including braces
            String obj = src.substring(braceStart, i + 1);

            // First collapse the object literal to a single line by removing
            // actual newlines and tabs (replace them with a single space to avoid
            // token concatenation). Then escape backslashes and quotes so the
            // resulting Java string literal contains no literal line breaks and
            // has proper escaping.
            String oneLine = obj.replace("\r", "").replace("\n", " ").replace("\t", " ");
            // collapse multiple spaces to single space for tidiness
            oneLine = oneLine.replaceAll("[ ]+", " ").trim();

            // escape backslashes and double quotes for embedded Java string
            String escaped = oneLine
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");

            // append plugin.setProps("<escaped JSON>")
            out.append("plugin.setProps(\"").append(escaped).append("\")");

            // advance idx to position after the closing brace
            idx = i + 1;
            // skip any whitespace following the brace
            while (idx < src.length() && Character.isWhitespace(src.charAt(idx))) idx++;
            // If the original source had the closing ')' for the call (e.g. '});'),
            // we should skip it because our replacement already includes a closing
            // parenthesis. Skip exactly one ')' if present to avoid producing
            // duplicate closing parens.
            if (idx < src.length() && src.charAt(idx) == ')') {
                idx++;
            }
        }

        return out.toString();
    }


    /**
     * Find occurrences of plugin.setProps({ ... }) and replace the object literal
     * argument with a quoted string: plugin.setProps("{ ... }"). Internal
     * double-quotes are escaped. This is a best-effort parser that handles
     * strings and escaped quotes inside the JSON-like object.
     */
    private static String replacePluginSetSecretsObjects(String src) {
        if (src == null || !src.contains("plugin.setSecrets(")) return src;

        StringBuilder out = new StringBuilder();
        int idx = 0;
        while (true) {
            int callIdx = src.indexOf("plugin.setSecrets(", idx);
            if (callIdx == -1) {
                out.append(src.substring(idx));
                break;
            }
            // append up to the call
            out.append(src, idx, callIdx);

            // find the first '{' after the call start
            int braceStart = src.indexOf('{', callIdx);
            if (braceStart == -1) {
                // nothing to do, append rest and break
                out.append(src.substring(callIdx));
                break;
            }

            // scan to matching closing brace, respecting quoted strings
            int i = braceStart;
            int depth = 0;
            boolean inString = false;
            boolean escape = false;
            for (; i < src.length(); i++) {
                char c = src.charAt(i);
                if (inString) {
                    if (escape) {
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        inString = false;
                    }
                } else {
                    if (c == '"') {
                        inString = true;
                    } else if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            break; // i points to closing brace
                        }
                    }
                }
            }

            if (i >= src.length()) {
                // unmatched braces: append remainder and exit
                out.append(src.substring(callIdx));
                break;
            }

            // extract object literal including braces
            String obj = src.substring(braceStart, i + 1);

            // First collapse the object literal to a single line by removing
            // actual newlines and tabs (replace them with a single space to avoid
            // token concatenation). Then escape backslashes and quotes so the
            // resulting Java string literal contains no literal line breaks and
            // has proper escaping.
            String oneLine = obj.replace("\r", "").replace("\n", " ").replace("\t", " ");
            // collapse multiple spaces to single space for tidiness
            oneLine = oneLine.replaceAll("[ ]+", " ").trim();

            // escape backslashes and double quotes for embedded Java string
            String escaped = oneLine
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");

            // append plugin.setProps("<escaped JSON>")
            out.append("plugin.setSecrets(\"").append(escaped).append("\")");

            // advance idx to position after the closing brace
            idx = i + 1;
            // skip any whitespace following the brace
            while (idx < src.length() && Character.isWhitespace(src.charAt(idx))) idx++;
            // If the original source had the closing ')' for the call (e.g. '});'),
            // we should skip it because our replacement already includes a closing
            // parenthesis. Skip exactly one ')' if present to avoid producing
            // duplicate closing parens.
            if (idx < src.length() && src.charAt(idx) == ')') {
                idx++;
            }
        }

        return out.toString();
    }
}
