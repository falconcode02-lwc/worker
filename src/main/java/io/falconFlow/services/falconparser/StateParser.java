package io.falconFlow.services.falconparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StateParser {

    private static final Pattern SET_STATE_PATTERN =
            Pattern.compile("@setState\\s*\\[\\s*(.+?)\\s*,\\s*(.+?)\\s*\\]", Pattern.MULTILINE);

    private static final Pattern ADD_STATE_PATTERN =
            Pattern.compile("@addState\\s*\\[\\s*(.+?)\\s*,\\s*(.+?)\\s*\\]", Pattern.MULTILINE);

    private static final Pattern GET_STATE_TYPED_PATTERN =
            Pattern.compile("@getState\\s*\\[\\s*(.+?)\\s*,\\s*(.+?)\\s*\\]", Pattern.MULTILINE);

    // Detect variable declarations before @getState["key"]
    // Example: String cr = @getState["current"];
    private static final Pattern GET_STATE_WITH_TYPE_INFERRED_PATTERN =
            Pattern.compile("(\\b[A-Z][A-Za-z0-9_<>?]+)\\s+[A-Za-z0-9_]+\\s*=\\s*@getState\\s*\\[\\s*(.+?)\\s*\\]",
                    Pattern.MULTILINE);

    // Simple untyped @getState["key"]
    private static final Pattern GET_STATE_PATTERN =
            Pattern.compile("@getState\\s*\\[\\s*(.+?)\\s*\\]", Pattern.MULTILINE);

    private static final Pattern REMOVE_STATE_PATTERN =
            Pattern.compile("@removeState\\s*\\[\\s*(.+?)\\s*\\]", Pattern.MULTILINE);

    private static final Pattern CLEAR_STATE_PATTERN =
            Pattern.compile("@clearState\\s*\\(\\s*\\)", Pattern.MULTILINE);

    public static String preprocess(String source) {
        StringBuffer sb = new StringBuffer();

        // --- @setState[key, value] ---
        Matcher matcher = SET_STATE_PATTERN.matcher(source);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            String replacement = "stateManager.setState(" + key + ", " + value + ");";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @addState[key, value] ---
        String intermediate = sb.toString();
        sb.setLength(0);

        matcher = ADD_STATE_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            String replacement = "stateManager.addState(" + key + ", " + value + ");";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @removeState[key] ---
        intermediate = sb.toString();
        sb.setLength(0);

        matcher = REMOVE_STATE_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String replacement = "stateManager.removeState(" + key + ");";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @clearState() ---
        intermediate = sb.toString();
        sb.setLength(0);

        matcher = CLEAR_STATE_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String replacement = "stateManager.clearState();";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @getState["key", Type] (explicit typed) ---
        intermediate = sb.toString();
        sb.setLength(0);

        matcher = GET_STATE_TYPED_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String type = matcher.group(2).trim();
            String replacement = "stateManager.getState(" + key + ", " + type + ".class)";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @getState["key"] (type inferred from variable declaration) ---
        intermediate = sb.toString();
        sb.setLength(0);

        matcher = GET_STATE_WITH_TYPE_INFERRED_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String inferredType = matcher.group(1).trim();
            String key = matcher.group(2).trim();

            // Replace only the @getState part, preserving declaration
            String replacement = matcher.group(0)
                    .replaceAll("@getState\\s*\\[\\s*" + Pattern.quote(key) + "\\s*\\]",
                            "stateManager.getState(" + key + ", " + inferredType + ".class)");

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        // --- @getState["key"] (untyped fallback) ---
        intermediate = sb.toString();
        sb.setLength(0);

        matcher = GET_STATE_PATTERN.matcher(intermediate);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String replacement = "stateManager.getState(" + key + ")";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    // For quick local test
    public static void main(String[] args) {
        String input = """
        @setState["a", 10]
        @addState["b", 20]
        String cr = @getState["current"];
        Integer x = @getState["count"];
        Double rate = @getState["interest"];
        Object raw = @getState["blob"];
        @removeState["count"]
        @clearState()
        """;

        System.out.println(preprocess(input));
    }
}
