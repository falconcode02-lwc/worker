package io.falconFlow.services.isolateservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.utils.AESUtil;
import io.falconFlow.configuration.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class StateCacheService {

    private static final ObjectMapper mapper = new ObjectMapper();

   // @Cacheable(value = CacheConfig.STATE_DECRYPTED_CACHE, key = "#encryptedValue + 'State:' + #key")
    public Object decryptAndParse(String key, String encryptedValue) {
        try {
            String decrypted = AESUtil.decrypt(encryptedValue);
            return mapper.readValue(decrypted, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting state value", e);
        }
    }

   // @Cacheable(value = CacheConfig.STATE_DECRYPTED_CACHE, key = "'State:' + #key")
    public <T> T decryptAndParse(String key, String encryptedValue, Class<T> type) {
        try {
            String decrypted = AESUtil.decrypt(encryptedValue);
            return mapper.readValue(decrypted, type);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting state value", e);
        }
    }
}
