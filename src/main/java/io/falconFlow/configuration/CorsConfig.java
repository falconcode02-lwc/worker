package io.falconFlow.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @PostConstruct
  public void bridgeJulToLog4j() {
    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
