package io.falconFlow.services.secret;

import java.time.Instant;

/**
 * Unified response for all vault operations (DB, Azure, GCP, etc.)
 * Ensures consistent API response structure regardless of vault type.
 */
public class VaultResponse {
    private boolean success;
    private String message;
    private String vaultType;
    private String secretName;
    private String secretId;
    private Instant timestamp;

    public VaultResponse() {
        this.timestamp = Instant.now();
    }

    public static VaultResponse success(String vaultType, String secretName, String secretId) {
        VaultResponse r = new VaultResponse();
        r.success = true;
        r.message = "Secret stored successfully";
        r.vaultType = vaultType;
        r.secretName = secretName;
        r.secretId = secretId;
        return r;
    }

    public static VaultResponse success(String vaultType, String secretName) {
        return success(vaultType, secretName, null);
    }

    public static VaultResponse failure(String vaultType, String secretName, String errorMessage) {
        VaultResponse r = new VaultResponse();
        r.success = false;
        r.message = errorMessage;
        r.vaultType = vaultType;
        r.secretName = secretName;
        return r;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getVaultType() { return vaultType; }
    public void setVaultType(String vaultType) { this.vaultType = vaultType; }

    public String getSecretName() { return secretName; }
    public void setSecretName(String secretName) { this.secretName = secretName; }

    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
