package io.falconFlow.services.isolateservices;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.configuration.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import io.falconFlow.services.secret.SecretService;
import io.falconFlow.entity.SecretEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SecretManagerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Returns the decrypted value of a secret by type and name.
     */


	public Map<String, Object> get(String name, String type) {
		// Find secret by type and name
        String val = this.getEncryptedValue(name, type);
        if(val != null && !val.isEmpty()){
            try {
                String dscrVal = secretService.getDecreptedValue(val);
                return objectMapper.readValue(dscrVal, Map.class);
            } catch (JsonProcessingException e) {
                return new HashMap<String, Object>();
            }
        }
        return  new HashMap<String, Object>();
	}

    @Cacheable(value = CacheConfig.VaultCache, key = "#name + ':' + #type")
    private String getEncryptedValue(String name, String type) {
        // Find secret by type and name
        Optional<SecretEntity> f = secretService.findByTypeAndName(type, name);
        if(f.isPresent()){
            String val = f.get().getValue();
            if(val != null && !val.isEmpty()){
                return val;
            }
        }
        return null;
    }




    public void set(String name, String type, String value) {
        // Find secret by type and name

       //secretService.save(type, name);

    }

	// Stub for decryption logic. Replace with your actual decryption implementation.
	private String decrypt(String value) {
		// TODO: Implement real decryption
		return value;
	}

	private final SecretService secretService;

	@Autowired
	public SecretManagerService(SecretService secretService) {
		this.secretService = secretService;
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
