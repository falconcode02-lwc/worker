package io.falconFlow.services.falconparser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.regex.*;

import java.util.*;
import java.util.regex.*;

public class JavascriptObjTOMapParser {

    // Match both JS-style let and Java-style typed declarations
    private static final Pattern VAR_PATTERN =
            Pattern.compile("(?:let)\\s+(\\w+)\\s*=\\s*([^;]+);?");
    private static final Pattern ASSIGN_PATTERN =
            Pattern.compile("(\\w+)\\.(\\w+)\\s*=\\s*([^;]+);?");
    private static final Pattern ACCESS_PATTERN =
            Pattern.compile("(?:([A-Za-z0-9_<>]+)\\s+)?(\\w+)\\s*=\\s*(\\w+)\\.(\\w+)\\s*;");

    private final Map<String, String> symbolTable = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> objectPropertyTypes = new HashMap<>();

    public String process(String jsText) {
        Matcher matcher = VAR_PATTERN.matcher(jsText);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);
            String valuePart = matcher.group(2).trim();
            String javaCode = translate(varName, valuePart);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(javaCode));
        }
        matcher.appendTail(sb);

        String afterAssign = processAssignments(sb.toString());
        String finalCode = processAccesses(afterAssign);

        return addImportsIfNeeded(finalCode);
    }

    private String translate(String varName, String value) {
        value = value.trim();

        if (value.startsWith("{") && value.endsWith("}")) {
            symbolTable.put(varName, "Map");
            return translateObject(varName, value);
        }

        if (value.startsWith("[") && value.endsWith("]")) {
            symbolTable.put(varName, "List");
            return translateArray(varName, value);
        }

        return translatePrimitiveOrReference(varName, value);
    }

    private String translateObject(String varName, String value) {
        String inner = value.substring(1, value.length() - 1).trim();
        List<String> pairs = smartSplit(inner, ',');

        StringBuilder sb = new StringBuilder();
        sb.append("Map<String, Object> ").append(varName)
                .append(" = new HashMap<>();\n");

        Map<String, String> fieldTypes = new HashMap<>();
        for (String pair : pairs) {
            if (pair.isBlank()) continue;
            List<String> kv = smartSplit(pair, ':');
            if (kv.size() == 2) {
                String key = kv.get(0).trim().replaceAll("\"", "");
                String val = kv.get(1).trim();
                String parsedVal = parseValue(val);
                sb.append(varName).append(".put(\"")
                        .append(key).append("\", ")
                        .append(parsedVal).append(");\n");
                fieldTypes.put(key, inferTypeFromValue(val));
            }
        }
        objectPropertyTypes.put(varName, fieldTypes);
        return sb.toString();
    }

    private String translateArray(String varName, String value) {
        String inner = value.substring(1, value.length() - 1).trim();
        List<String> elements = smartSplit(inner, ',');

        StringBuilder sb = new StringBuilder();
        sb.append("List<Object> ").append(varName)
                .append(" = new ArrayList<>();\n");

        for (String el : elements) {
            el = el.trim();
            if (!el.isEmpty()) {
                sb.append(varName).append(".add(")
                        .append(parseValue(el)).append(");\n");
            }
        }
        return sb.toString();
    }

    private String translatePrimitiveOrReference(String varName, String value) {
        String type = inferTypeFromValue(value);
        symbolTable.put(varName, type);
        return type + " " + varName + " = " + parseValue(value) + ";\n";
    }

    private String parseValue(String val) {
        val = val.trim();

        // literal string
        if (val.matches("\"[^\"]*\"")) return val;
        // number or boolean
        if (val.matches("\\d+(\\.\\d+)?") || val.equals("true") || val.equals("false")) return val;
        // known variable
        if (symbolTable.containsKey(val)) return val;
        // method calls or expressions
        if (val.matches(".*[+().].*")) return val;
        // fallback
        return "\"" + val + "\"";
    }

    private String inferTypeFromValue(String val) {
        if (val.matches("\"[^\"]*\"")) return "String";
        if (val.matches("\\d+")) return "int";
        if (val.matches("\\d+\\.\\d+")) return "double";
        if (val.equals("true") || val.equals("false")) return "boolean";
        if (symbolTable.containsKey(val)) return symbolTable.get(val);
        if (val.matches(".*[+().].*")) return "Object";
        return "Object";
    }

    private String processAssignments(String code) {
        Matcher matcher = ASSIGN_PATTERN.matcher(code);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String object = matcher.group(1);
            String key = matcher.group(2);
            String val = matcher.group(3).trim();

            String replacement;
            if ("Map".equals(symbolTable.get(object))) {
                replacement = object + ".put(\"" + key + "\", " + parseValue(val) + ");\n";
                objectPropertyTypes
                        .computeIfAbsent(object, k -> new HashMap<>())
                        .put(key, inferTypeFromValue(val));
            } else {
                replacement = object + "." + key + " = " + parseValue(val) + ";\n";
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String processAccesses(String code) {
        Matcher matcher = ACCESS_PATTERN.matcher(code);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String declaredType = matcher.group(1);
            String targetVar = matcher.group(2);
            String object = matcher.group(3);
            String key = matcher.group(4);

            String objectType = symbolTable.getOrDefault(object, "Object");
            String inferredType = "Object";

            if ("Map".equals(objectType)) {
                inferredType = objectPropertyTypes
                        .getOrDefault(object, Collections.emptyMap())
                        .getOrDefault(key, "Object");
            }

            boolean alreadyDeclared = symbolTable.containsKey(targetVar);
            String typeToUse = declaredType != null ? declaredType : inferredType;

            StringBuilder replacement = new StringBuilder();
            if (!alreadyDeclared && declaredType == null) {
                replacement.append(typeToUse).append(" ");
            }

            replacement.append(targetVar)
                    .append(" = (").append(typeToUse).append(") ")
                    .append(object).append(".get(\"").append(key).append("\");\n");

            symbolTable.put(targetVar, typeToUse);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private List<String> smartSplit(String input, char delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int depth = 0;

        for (char c : input.toCharArray()) {
            if (c == '"' && (current.length() == 0 || current.charAt(current.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{' || c == '[' || c == '(') depth++;
                if (c == '}' || c == ']' || c == ')') depth--;
                if (c == delimiter && depth == 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    private String addImportsIfNeeded(String code) {
        boolean needsUtil = code.contains("Map<") || code.contains("List<");
        if (needsUtil) {
            return "import java.util.*;\n\n" + code;
        }
        return code;
    }

    public static void main(String[] args) {
        String input = """
                Current cr = stateManager.getState("current", Current.class);
                Map<String, Object> info = new HashMap<>();

                info.put("chat_id", "-4792945578");
                String d = "Hello world >> Todays temprature >> " + cr.getTemperature_2m();
                info.put("text", d);
                """;

        JavascriptObjTOMapParser parser = new JavascriptObjTOMapParser();
        System.out.println(parser.process(input));
    }
}


