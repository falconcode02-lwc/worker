package io.falconFlow.services.secret.vault;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.falconFlow.entity.SecretEntity;
import io.falconFlow.repository.SecretRepository;
import io.falconFlow.services.secret.SecretDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * Stores and reads secrets from Azure Key Vault.
 * Returns SecretEntity for consistent API response (same format as DB).
 */
@Component("VAULT_AZURE")
public class AzureKeyVaultWriter implements VaultWriter, VaultReader {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultWriter.class);

    private final AzureKeyVaultProperties props;
    private final SecretRepository secretRepository;

    public AzureKeyVaultWriter(AzureKeyVaultProperties props, SecretRepository secretRepository) {
        this.props = props;
        this.secretRepository = secretRepository;
    }

    @Override
    public SecretEntity store(SecretDto request) {
        validate(request);

        try {
            SecretClient client = buildClient();
            KeyVaultSecret saved = client.setSecret(request.getName(), request.getValue());
            String version = saved.getProperties().getVersion();
            log.info("Stored secret '{}' in Azure Key Vault (version={})", request.getName(), version);
            
            // Also save reference in DB so we can track it
            SecretEntity entity = saveReferenceInDb(request, version);
            return entity;
        } catch (Exception ex) {
            log.error("Azure Key Vault store failed for secret '{}'", safeName(request), ex);
            throw new RuntimeException("Failed to store secret in Azure Key Vault", ex);
        }
    }

    /**
     * Save a reference in DB pointing to Azure Key Vault.
     * Value stored is the version reference, not actual secret.
     */
    private SecretEntity saveReferenceInDb(SecretDto request, String version) {
        SecretEntity entity = new SecretEntity();
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setVaultType("AZURE");
        entity.setValue("azure-kv:" + version); // Reference, not actual value
        entity.setMetadata(request.getMetadata());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return secretRepository.save(entity);
    }

    @Override
    public String readSecret(String secretName) {
        try {
            SecretClient client = buildClient();
            KeyVaultSecret secret = client.getSecret(secretName);
            log.info("Retrieved secret '{}' from Azure Key Vault", secretName);
            return secret.getValue();
        } catch (Exception ex) {
            log.error("Failed to read secret '{}' from Azure Key Vault", secretName, ex);
            throw new RuntimeException("Failed to read secret from Azure Key Vault: " + secretName, ex);
        }
    }

    @Override
    public boolean supports(String vaultType) {
        return "AZURE".equalsIgnoreCase(vaultType);
    }

    private SecretClient buildClient() {
        if (!StringUtils.hasText(props.getVaultUrl())) {
            throw new IllegalStateException("vault.azure.vault-url is not configured");
        }

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(props.getTenantId())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .additionallyAllowedTenants("*")
                .build();

        return new SecretClientBuilder()
                .vaultUrl(props.getVaultUrl())
                .credential(credential)
                .buildClient();
    }

    private void validate(SecretDto request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        if (!StringUtils.hasText(request.getName())) throw new IllegalArgumentException("name is required");
        if (request.getValue() == null) throw new IllegalArgumentException("value is required");
    }

    private String safeName(SecretDto request) {
        if (request == null) return "<null>";
        return StringUtils.hasText(request.getName()) ? request.getName() : "<missing-name>";
    }
}
