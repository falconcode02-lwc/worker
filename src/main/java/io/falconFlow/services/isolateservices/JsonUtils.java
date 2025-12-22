package io.falconFlow.services.isolateservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtils {
  private static final ObjectMapper mapper = new ObjectMapper();

  public static String jsonToStr(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  public static <T> T jsonToObj(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert JSON to object", e);
    }
  }
}
