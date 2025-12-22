package io.falconFlow.services.secret;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.services.secret.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecretManager {

    private final SecretService secretService;

    @Autowired
    public SecretManager(SecretService secretService) {
        this.secretService = secretService;
    }

    /**
     * Return plaintext secret value for the given secret name if present.
     */
    public Optional<String> getSecretValue(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        Optional<SecretEntity> s = secretService.findByName(name);
        return s.map(SecretEntity::getValue);
    }
}
