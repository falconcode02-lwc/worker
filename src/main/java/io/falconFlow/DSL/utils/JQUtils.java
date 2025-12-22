package io.falconFlow.DSL.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class JQUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Scope SCOPE = Scope.newEmptyScope();

  static {
    try {
      // Load built-in jq functions (for 1.6.0)
      BuiltinFunctionLoader.getInstance().loadFunctions(Version.LATEST, SCOPE);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load jq functions", e);
    }
  }

  public static boolean evaluateJq(String json, String jqExpression) {
    try {
      JsonNode input = MAPPER.readTree(json);
      JsonQuery query = getCachedQuery(jqExpression);
      List<JsonNode> results = new ArrayList<>();
      query.apply(SCOPE, input, results::add);
      for (JsonNode node : results) {
        if (isTruthy(node)) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      throw new RuntimeException("Failed to evaluate JQ expression: " + jqExpression, e);
    }
  }

  private static final ConcurrentHashMap<String, JsonQuery> JQ_CACHE = new ConcurrentHashMap<>();

  public static JsonQuery getCachedQuery(String expr) {
    try {
      return JQ_CACHE.computeIfAbsent(
          expr,
          expr1 -> {
            try {
              return JsonQuery.compile(expr1, Version.LATEST);
            } catch (Exception e) {
              throw new RuntimeException("Invalid jq expression: " + expr, e);
            }
          });
    } catch (Exception ex) {
      return null;
    }
  }

  private static boolean isTruthy(JsonNode node) {
    if (node == null || node.isNull()) return false;
    if (node.isBoolean()) return node.booleanValue();
    if (node.isNumber()) return node.doubleValue() != 0;
    if (node.isTextual()) return !node.textValue().isEmpty();
    if (node.isArray() || node.isObject()) return node.size() > 0;
    return false;
  }
}
