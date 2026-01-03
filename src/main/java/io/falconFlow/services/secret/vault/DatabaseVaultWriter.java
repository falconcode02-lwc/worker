package io.falconFlow.services.secret.vault;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.repository.SecretRepository;
import io.falconFlow.services.secret.CryptoService;
import io.falconFlow.services.secret.SecretDto;

/**
 * Wraps the existing DB flow (controller -> service -> repository -> entity).
 * This must reuse the existing service logic as-is.
 */
@Component("VAULT_DB")
public class DatabaseVaultWriter implements VaultWriter, VaultReader {

    private static final Logger log = LoggerFactory.getLogger(DatabaseVaultWriter.class);

    private final SecretRepository secretRepository;
    private final CryptoService cryptoService;

    public DatabaseVaultWriter(SecretRepository secretRepository, CryptoService cryptoService) {
        this.secretRepository = secretRepository;
        this.cryptoService = cryptoService;
    }

    @Override
    public SecretEntity store(SecretDto request) {
        // Check for duplicate
        if (secretRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Secret with name '" + request.getName() + "' already exists");
        }
        
        try {
            SecretEntity secret = request.toEntity();
            Instant now = Instant.now();
            secret.setCreatedAt(now);
            secret.setUpdatedAt(now);
            secret.setVaultType("DB");
            secret.setValue(cryptoService.encrypt(secret.getValue()));
            SecretEntity saved = secretRepository.save(secret);
            log.info("Stored secret '{}' in DB vault", request.getName());
            return saved;
        } catch (Exception ex) {
            log.error("Failed to store secret '{}' in DB vault", request.getName(), ex);
            throw new RuntimeException("Failed to store secret in DB", ex);
        }
    }

    @Override
    public String readSecret(String secretName) {
        Optional<SecretEntity> opt = secretRepository.findByName(secretName);
        if (opt.isPresent()) {
            String encryptedValue = opt.get().getValue();
            return cryptoService.decrypt(encryptedValue);
        }
        throw new RuntimeException("Secret not found in DB: " + secretName);
    }

    @Override
    public void delete(String secretName) {
        Optional<SecretEntity> opt = secretRepository.findByName(secretName);
        if (opt.isPresent()) {
            secretRepository.delete(opt.get());
            log.info("Deleted secret '{}' from DB vault", secretName);
        } else {
            log.warn("Secret '{}' not found in DB vault for deletion", secretName);
        }
    }

    @Override
    public boolean supports(String vaultType) {
        return "DB".equalsIgnoreCase(vaultType);
    }
}
