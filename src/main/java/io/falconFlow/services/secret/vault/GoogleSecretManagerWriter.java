package io.falconFlow.services.secret.vault;

import java.io.FileInputStream;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;

import io.falconFlow.entity.SecretEntity;
import io.falconFlow.repository.SecretRepository;
import io.falconFlow.services.secret.SecretDto;

/**
 * Stores and reads secrets from Google Secret Manager.
 * Returns SecretEntity for consistent API response (same format as DB).
 */
@Component("VAULT_GCP")
public class GoogleSecretManagerWriter implements VaultWriter, VaultReader {

    private static final Logger log = LoggerFactory.getLogger(GoogleSecretManagerWriter.class);

    private final GoogleSecretManagerProperties props;
    private final SecretRepository secretRepository;

    public GoogleSecretManagerWriter(GoogleSecretManagerProperties props, SecretRepository secretRepository) {
        this.props = props;
        this.secretRepository = secretRepository;
    }

    @Override
    public SecretEntity store(SecretDto request) {
        validate(request);

        try (SecretManagerServiceClient client = buildClient()) {
            String projectId = props.getProjectId();
            String secretId = request.getName();

            // Ensure secret exists (create if missing)
            SecretName secretName = SecretName.of(projectId, secretId);
            try {
                client.getSecret(secretName);
            } catch (Exception notFound) {
                Secret secret = Secret.newBuilder()
                        .setReplication(Replication.newBuilder()
                                .setAutomatic(Replication.Automatic.newBuilder().build())
                                .build())
                        .build();
                client.createSecret(ProjectName.of(projectId), secretId, secret);
                log.info("Created secret '{}' in Google Secret Manager", secretId);
            }

            SecretPayload payload = SecretPayload.newBuilder()
                    .setData(ByteString.copyFromUtf8(request.getValue()))
                    .build();

            SecretVersion version = client.addSecretVersion(
                    AddSecretVersionRequest.newBuilder()
                            .setParent(secretName.toString())
                            .setPayload(payload)
                            .build()
            );

            String versionName = version.getName();
            log.info("Stored secret '{}' in Google Secret Manager (version={})", secretId, versionName);
            
            // Also save reference in DB so we can track it
            return saveReferenceInDb(request, versionName);
        } catch (Exception ex) {
            log.error("Google Secret Manager store failed for secret '{}'", safeName(request), ex);
            throw new RuntimeException("Failed to store secret in Google Secret Manager", ex);
        }
    }

    /**
     * Save a reference in DB pointing to GCP Secret Manager.
     * Value stored is the version reference, not actual secret.
     */
    private SecretEntity saveReferenceInDb(SecretDto request, String versionName) {
        SecretEntity entity = new SecretEntity();
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setVaultType("GCP");
        entity.setValue("gcp-sm:" + versionName); // Reference, not actual value
        entity.setMetadata(request.getMetadata());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return secretRepository.save(entity);
    }

    @Override
    public String readSecret(String secretName) {
        try (SecretManagerServiceClient client = buildClient()) {
            String projectId = props.getProjectId();
            // Access the latest version
            SecretVersionName versionName = SecretVersionName.of(projectId, secretName, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(versionName);
            log.info("Retrieved secret '{}' from Google Secret Manager", secretName);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception ex) {
            log.error("Failed to read secret '{}' from Google Secret Manager", secretName, ex);
            throw new RuntimeException("Failed to read secret from Google Secret Manager: " + secretName, ex);
        }
    }

    @Override
    public boolean supports(String vaultType) {
        return "GCP".equalsIgnoreCase(vaultType);
    }

    private SecretManagerServiceClient buildClient() throws Exception {
        if (!StringUtils.hasText(props.getProjectId())) {
            throw new IllegalStateException("vault.gcp.project-id is not configured");
        }
        if (!StringUtils.hasText(props.getCredentialsPath())) {
            throw new IllegalStateException("vault.gcp.credentials-path is not configured");
        }

        GoogleCredentials credentials;
        try (FileInputStream fis = new FileInputStream(props.getCredentialsPath())) {
            credentials = GoogleCredentials.fromStream(fis);
        }

        SecretManagerServiceSettings settings = SecretManagerServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return SecretManagerServiceClient.create(settings);
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
