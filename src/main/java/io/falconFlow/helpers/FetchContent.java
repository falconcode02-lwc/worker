package io.falconFlow.helpers;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FetchContent {

  public static String asString(String path) throws IOException {
    ClassPathResource resource = new ClassPathResource(path);
    return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
  }
}
