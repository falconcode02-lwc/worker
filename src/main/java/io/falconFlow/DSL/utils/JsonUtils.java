package io.falconFlow.DSL.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JsonUtils {
  static Scope rootScope;
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  public static boolean evaluate(String json, String jqExpression) throws Exception {
    JsonNode in = mapper.readTree(json);
    Scope childScope1 = Scope.newChildScope(JsonUtils.rootScope);
    JsonQuery query = JsonQuery.compile(jqExpression, Versions.JQ_1_6);
    final List<JsonNode> out = new ArrayList<>();
    query.apply(childScope1, in, out::add);
    System.out.println(out); // => [84]

    if (out.isEmpty()) {
      return false;
    }
    // Only interested in boolean truthiness
    JsonNode result = out.get(0);
    return result.isBoolean() ? result.booleanValue() : false;
  }
}
