package io.falconFlow.services.secret.vault;

/**
 * Interface for reading secrets from different vault backends.
 * Each implementation reads from a specific vault (DB, Azure, GCP, etc.)
 */
public interface VaultReader {
    
    /**
     * Retrieve the actual secret value from the vault.
     * 
     * @param secretName The name/key of the secret
     * @return The actual secret value (decrypted for DB, fetched from vault for Azure/GCP)
     */
    String readSecret(String secretName);
    
    /**
     * Check if this reader can handle the given vault type.
     * 
     * @param vaultType The vault type (DB, AZURE, GCP)
     * @return true if this reader handles this vault type
     */
    boolean supports(String vaultType);
}
