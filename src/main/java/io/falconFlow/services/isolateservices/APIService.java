package io.falconFlow.services.isolateservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.component.HttpClientComponent;
import io.falconFlow.dao.DB;
import io.falconFlow.services.models.APIModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class APIService {

  @Autowired DB db;

  ObjectMapper mapper = new ObjectMapper();

  @Autowired HttpClientComponent httpCli;

  public <T> T callByKey(
      String API_NAME, Object params, Map<String, String> headers, Class<T> clazz) {

    APIModel apiModel1 =
        db.selectOne(
            "select raw_process_class from ff_functions where class_type='api' and class_name='"
                + API_NAME
                + "' limit 1",
            APIModel.class);
    APIModel apiModel = null;
    try {
      apiModel = mapper.readValue(apiModel1.getRaw_process_class(), APIModel.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    Map<String, String> newHeaders = new HashMap<>();
    if(apiModel.getHeaders()!=null){
        newHeaders.putAll(apiModel.getHeaders());
    }
    if(headers != null){

        newHeaders.putAll(headers);
    }



    return call(apiModel.getUrl(), apiModel.getMethod(), params, newHeaders, clazz);
  }

  public <T> T callByKey(
      String API_NAME, Object params, Map<String, String> headers, TypeReference<T> typeRef) {
    APIModel apiModel1 =
        db.selectOne(
            "select raw_process_class from ff_functions where class_type='api' and class_name='"
                + API_NAME
                + "' limit 1",
            APIModel.class);
    APIModel apiModel = null;
    try {
      apiModel = mapper.readValue(apiModel1.getRaw_process_class(), APIModel.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

      Map<String, String> newHeaders = new HashMap<>();
      if(apiModel.getHeaders()!=null){
          newHeaders.putAll(apiModel.getHeaders());
      }
      if(headers != null){

          newHeaders.putAll(headers);
      }

    return call(apiModel.getUrl(), apiModel.getMethod(), params, newHeaders, typeRef);
  }

//  public String call(String API_NAME, Object params, Map<String, String> headers) {
//    String body = null;
//    try {
//      body = mapper.writeValueAsString(params);
//    } catch (JsonProcessingException e) {
//      return e.getMessage();
//    }
//    return httpCli.call("POST", API_NAME, body, headers);
//  }

  public String call(String API_NAME, String method, Object params, Map<String, String> headers) {
    String body = null;
    try {
      body = mapper.writeValueAsString(params);
    } catch (JsonProcessingException e) {
      return e.getMessage();
    }
    return httpCli.call(method, API_NAME, body, headers);
  }

  public <T> T call(
      String API_NAME,
      String method,
      Object params,
      Map<String, String> headers,
      Class<T> clazz) {
    String body = null;
    try {
        if("GET".equalsIgnoreCase(method) && params!=null){
            body =  toQueryString((Map<String, Object>)params);
        }else {
            body = mapper.writeValueAsString(params);
        }
    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
    }
    return httpCli.call(method, API_NAME, body, headers, clazz);
  }

  public <T> T call(
      String API_NAME,
      String method,
      Object params,
      Map<String, String> headers,
      TypeReference<T> typeRef) {
    String body = null;
    try {
        if("GET".equalsIgnoreCase(method) && params!=null){
            body = toQueryString((Map<String, Object>)params);
        }else {
            body = mapper.writeValueAsString(params);
        }
    } catch (JsonProcessingException e) {
      System.err.println(e.getMessage());
    }
    return httpCli.call(method, API_NAME, body, headers, typeRef);
  }

    public static String toQueryString(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .flatMap(entry -> flatten(entry.getKey(), entry.getValue()).entrySet().stream())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static Map<String, Object> flatten(String prefix, Object value) {
        Map<String, Object> flat = new LinkedHashMap<>();

        if (value instanceof Map<?, ?> m) {
            m.forEach((k, v) -> flat.putAll(flatten(prefix + "[" + k + "]", v)));
        } else if (value instanceof List<?> l) {
            for (int i = 0; i < l.size(); i++) {
                flat.putAll(flatten(prefix + "[" + i + "]", l.get(i)));
            }
        } else {
            flat.put(prefix, value);
        }
        return flat;
    }




}



