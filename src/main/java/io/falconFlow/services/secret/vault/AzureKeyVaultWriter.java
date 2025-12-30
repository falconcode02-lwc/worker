package io.falconFlow.services.secret.vault;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.falconFlow.services.secret.SecretDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Stores secrets in Azure Key Vault.
 *
 * NOTE: This bypasses DB persistence.
 */
@Component("VAULT_AZURE")
public class AzureKeyVaultWriter implements VaultWriter {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultWriter.class);

    private final AzureKeyVaultProperties props;

    public AzureKeyVaultWriter(AzureKeyVaultProperties props) {
        this.props = props;
    }

    @Override
    public void store(SecretDto request) {
        validate(request);

        try {
            SecretClient client = buildClient();
            KeyVaultSecret saved = client.setSecret(request.getName(), request.getValue());
            log.info("Stored secret '{}' in Azure Key Vault (version={})", request.getName(), saved.getProperties().getVersion());
        } catch (Exception ex) {
            // Do not log secret value
            log.error("Azure Key Vault store failed for secret '{}'", safeName(request), ex);
            throw new RuntimeException("Failed to store secret in Azure Key Vault", ex);
        }
    }

    private SecretClient buildClient() {
        if (!StringUtils.hasText(props.getVaultUrl())) {
            throw new IllegalStateException("vault.azure.vault-url is not configured");
        }

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(props.getTenantId())
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .additionallyAllowedTenants("*")  // allow multi-tenant scenarios
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
