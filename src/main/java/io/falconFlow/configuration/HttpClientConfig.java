package io.falconFlow.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2) // support HTTP/2
        .build();
  }
}
