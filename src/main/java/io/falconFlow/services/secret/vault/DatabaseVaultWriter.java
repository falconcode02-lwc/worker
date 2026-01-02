package io.falconFlow.services.secret.vault;

import io.falconFlow.services.secret.SecretDto;
import io.falconFlow.entity.SecretEntity;
import io.falconFlow.repository.SecretRepository;
import io.falconFlow.services.secret.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Wraps the existing DB flow (controller -> service -> repository -> entity).
 * This must reuse the existing service logic as-is.
 */
@Component("VAULT_DB")
public class DatabaseVaultWriter implements VaultWriter {

    private static final Logger log = LoggerFactory.getLogger(DatabaseVaultWriter.class);

    private final SecretRepository secretRepository;
    private final CryptoService cryptoService;

    public DatabaseVaultWriter(SecretRepository secretRepository, CryptoService cryptoService) {
        this.secretRepository = secretRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public SecretEntity store(SecretDto request) {
        try {
            SecretEntity secret = request.toEntity();
            Instant now = Instant.now();
            secret.setCreatedAt(now);
            secret.setUpdatedAt(now);
            secret.setValue(cryptoService.encrypt(secret.getValue()));
            SecretEntity saved = secretRepository.save(secret);
            log.info("Stored secret '{}' in DB vault", request.getName());
            return saved;
        } catch (Exception ex) {
            log.error("Failed to store secret '{}' in DB vault", request.getName(), ex);
            throw new RuntimeException("Failed to store secret in DB", ex);
        }
    }
}
