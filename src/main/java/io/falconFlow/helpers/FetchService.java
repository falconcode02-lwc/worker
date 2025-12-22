package io.falconFlow.helpers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class FetchService {
  private final WebClient webClient = WebClient.create();

  public String Post(String url, Parameter body) {
    return webClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }
}
