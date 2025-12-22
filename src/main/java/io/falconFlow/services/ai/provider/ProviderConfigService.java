package io.falconFlow.services.ai.provider;

import io.falconFlow.entity.ProviderConfigEntity;
import io.falconFlow.repository.ProviderConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProviderConfigService {

    private final ProviderConfigRepository repo;

    @Autowired
    public ProviderConfigService(ProviderConfigRepository repo) {
        this.repo = repo;
    }

    public Optional<ProviderConfigEntity> getConfig(String providerName) {
        if (providerName == null) return Optional.empty();
        return repo.findByName(providerName);
    }

    public String getApiUrl(String providerName, String fallback) {
        return getConfig(providerName).map(ProviderConfigEntity::getApiUrl).orElse(fallback);
    }

    public String getDefaultModel(String providerName, String fallback) {
        return getConfig(providerName).map(ProviderConfigEntity::getDefaultModel).orElse(fallback);
    }

    /**
     * Read a single property from the provider's `properties` JSON blob.
     */
    public java.util.Optional<String> getProperty(String providerName, String key) {
        return getConfig(providerName)
                .map(ProviderConfigEntity::getProperties)
                .flatMap(props -> {
                    if (props == null || props.isEmpty()) return java.util.Optional.empty();
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<String,Object> map = m.readValue(props, java.util.Map.class);
                        Object v = map.get(key);
                        return v == null ? java.util.Optional.empty() : java.util.Optional.of(String.valueOf(v));
                    } catch (Exception e) {
                        return java.util.Optional.empty();
                    }
                });
    }
}
