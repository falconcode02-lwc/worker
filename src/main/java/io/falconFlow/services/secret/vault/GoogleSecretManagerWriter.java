package io.falconFlow.services.secret.vault;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.protobuf.ByteString;
import io.falconFlow.services.secret.SecretDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;

/**
 * Stores secrets in Google Secret Manager.
 *
 * NOTE: This bypasses DB persistence.
 */
@Component("VAULT_GCP")
public class GoogleSecretManagerWriter implements VaultWriter {

    private static final Logger log = LoggerFactory.getLogger(GoogleSecretManagerWriter.class);

    private final GoogleSecretManagerProperties props;

    public GoogleSecretManagerWriter(GoogleSecretManagerProperties props) {
        this.props = props;
    }

    @Override
    public void store(SecretDto request) {
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

            log.info("Stored secret '{}' in Google Secret Manager (version={})", secretId, version.getName());
        } catch (Exception ex) {
            log.error("Google Secret Manager store failed for secret '{}'", safeName(request), ex);
            throw new RuntimeException("Failed to store secret in Google Secret Manager", ex);
        }
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
