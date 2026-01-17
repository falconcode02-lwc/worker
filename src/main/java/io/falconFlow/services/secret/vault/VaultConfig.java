package io.falconFlow.services.secret.vault;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AzureKeyVaultProperties.class,
        GoogleSecretManagerProperties.class
})
public class VaultConfig {
}
