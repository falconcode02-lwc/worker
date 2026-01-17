package io.falconFlow.services.secret.vault;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vault.gcp")
public class GoogleSecretManagerProperties {
    private String projectId;
    /** Absolute/relative path to a service account JSON key file. */
    private String credentialsPath;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }
}
