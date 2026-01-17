package io.falconFlow.services.isolateservices;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.falconFlow.services.secret.SecretService;
import io.falconFlow.entity.SecretEntity;
import io.falconFlow.services.secret.vault.VaultReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@Service
public class SecretManagerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Returns the decrypted value of a secret by type and name.
     */


	public Map<String, Object> get(String name, String type) {
		// Find secret by type and name.
		// NOTE: for non-DB vault types, ff_secrets.value is a reference (not encrypted JSON),
		// so we must resolve it via VaultReader instead of decrypting.
        Optional<SecretEntity> opt = secretService.findByTypeAndName(type, name);
        if (opt.isEmpty()) {
            return new HashMap<String, Object>();
        }

        SecretEntity entity = opt.get();
        String vaultType = entity.getVaultType();
        if (vaultType == null || vaultType.isBlank()) {
            vaultType = "DB"; // backward compatibility
        }

        String jsonPayload;
        if ("DB".equalsIgnoreCase(vaultType)) {
            String encryptedOrPlain = entity.getValue();
            if (encryptedOrPlain == null || encryptedOrPlain.isEmpty()) {
                return new HashMap<String, Object>();
            }
            try {
                jsonPayload = secretService.getDecreptedValue(encryptedOrPlain);
            } catch (RuntimeException ex) {
                // Fail-soft: don't break workflow execution due to one malformed secret.
                return new HashMap<String, Object>();
            }
        } else {
            try {
                VaultReader reader = findReader(vaultType);
                jsonPayload = reader.readSecret(entity.getName());
            } catch (Exception ex) {
                throw new RuntimeException("Failed to read secret from vault: " + vaultType, ex);
            }
        }

        if (jsonPayload == null || jsonPayload.isEmpty()) {
            return new HashMap<String, Object>();
        }

        try {
            return objectMapper.readValue(jsonPayload, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<String, Object>();
        }
	}

    private VaultReader findReader(String vaultType) {
        String normalized = (vaultType == null) ? "DB" : vaultType.trim().toUpperCase(Locale.ROOT);
        for (VaultReader reader : vaultReaders) {
            if (reader.supports(normalized)) {
                return reader;
            }
        }
        throw new IllegalArgumentException("No reader found for vault type: " + vaultType);
    }




    public void set(String name, String type, String value) {
        // Find secret by type and name

       //secretService.save(type, name);

    }

	private final SecretService secretService;
    private final List<VaultReader> vaultReaders;

    public SecretManagerService(SecretService secretService, List<VaultReader> vaultReaders) {
        this.secretService = secretService;
        this.vaultReaders = vaultReaders;
    }

	/**
	 * Returns all secrets as a HashMap where key is secret name and value is secret value.
	 */
	public HashMap<String, String> getAllSecretValues() {
		List<SecretEntity> secrets = secretService.list();
		HashMap<String, String> map = new HashMap<>();
		for (SecretEntity secret : secrets) {
			map.put(secret.getName(), secret.getValue());
		}
		return map;
	}

}
