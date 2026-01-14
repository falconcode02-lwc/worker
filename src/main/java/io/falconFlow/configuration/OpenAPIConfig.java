package io.falconFlow.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenAPIConfig {
    @Bean
    public OpenAPI openAPI(
            @Value("${app.swagger.server-url}") String serverUrl) {

        return new OpenAPI()
                .info(new Info().title("FalconFlow API").version("v1"))
                .addServersItem(
                        new Server()
                                .url(serverUrl)
                                .description("Current environment")
                );
    }
}
