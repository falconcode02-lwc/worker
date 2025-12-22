package io.falconFlow.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class HttpClientComponent {

  private final HttpClient client;
  private final ObjectMapper mapper;

  public HttpClientComponent(HttpClient client) {
    this.client = client;
    this.mapper = new ObjectMapper();

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // allow case-insensitive matching (Data -> data)
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
  }

  /**
   * Call an API with GET or POST.
   *
   * @param method "GET" or "POST"
   * @param url API endpoint
   * @param body Request body (for POST); null for GET
   * @param headers Optional headers (can be empty map)
   * @return Response body as String
   */
  public String call(String method, String url, String body, Map<String, String> headers) {
    try {
        if("GET".equalsIgnoreCase(method)){
            url = appendQueryStr(url , body);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(url));

        // Add headers if provided
        if (headers != null) {
            headers.forEach(builder::header);
        }


        // Choose method
        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(
                    body != null
                            ? HttpRequest.BodyPublishers.ofString(body)
                            : HttpRequest.BodyPublishers.noBody());
        } else if("PUT".equalsIgnoreCase(method)){
            builder.PUT(
                    body != null
                            ? HttpRequest.BodyPublishers.ofString(body)
                            : HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }

      HttpRequest request = builder.build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return response.body();

    } catch (Exception e) {
      throw new RuntimeException("API call failed: " + e.getMessage(), e);
    }
  }

  public <T> T call(
      String method, String url, String body, Map<String, String> headers, Class<T> clazz) {
    try {
    if("GET".equalsIgnoreCase(method)){
        url = appendQueryStr(url , body);
    }


    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(url));

      // Add headers if provided
      if (headers != null) {
        headers.forEach(builder::header);
      }


      // Choose method
      if ("POST".equalsIgnoreCase(method)) {
        builder.POST(
            body != null
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody());
      } else if("PUT".equalsIgnoreCase(method)){
          builder.PUT(
                  body != null
                          ? HttpRequest.BodyPublishers.ofString(body)
                          : HttpRequest.BodyPublishers.noBody());
      } else {
        builder.GET();
      }

      HttpRequest request = builder.build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // Deserialize JSON into the provided class
      return mapper.readValue(response.body(), clazz);

    } catch (Exception e) {
      throw new RuntimeException("API call failed: " + e.getMessage(), e);
    }
  }

  public <T> T call(
      String method,
      String url,
      String body,
      Map<String, String> headers,
      TypeReference<T> typeRef) {
    try {

        if("GET".equalsIgnoreCase(method)){
            url = appendQueryStr(url , body);
        }

      HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(url));

      // Add headers if provided
      if (headers != null) {
        headers.forEach(builder::header);
      }

      // Choose method
        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(
                    body != null
                            ? HttpRequest.BodyPublishers.ofString(body)
                            : HttpRequest.BodyPublishers.noBody());
        } else if("PUT".equalsIgnoreCase(method)){
            builder.PUT(
                    body != null
                            ? HttpRequest.BodyPublishers.ofString(body)
                            : HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }

      HttpRequest request = builder.build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // Deserialize JSON into the provided class
      return mapper.readValue(response.body(), typeRef);

    } catch (Exception e) {
      throw new RuntimeException("API call failed: " + e.getMessage(), e);
    }
  }

    private String appendQueryStr(String url, String params){
        if (url.contains("?")) {
            // If URL ends with '?' or '&', don't add extra '&'
            return  url + "&" + params;
        } else {
            return url + "?" + params;
        }
    }

}
