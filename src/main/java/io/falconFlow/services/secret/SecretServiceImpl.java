package io.falconFlow.services.secret;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.configuration.CacheConfig;
import io.falconFlow.entity.SecretEntity;
import io.falconFlow.model.PluginSecretModel;
import io.falconFlow.repository.SecretRepository;
import io.falconFlow.services.isolateservices.PluginManagerService;
import io.falconFlow.services.secret.vault.VaultReader;
import io.falconFlow.services.secret.vault.VaultWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SecretServiceImpl implements SecretService {


    private final SecretRepository secretRepository;
    private final CryptoService cryptoService;
    private final Map<String, VaultWriter> vaultWriters;
    private final List<VaultReader> vaultReaders;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    PluginManagerService pluginManagerService;


    @Autowired
    public SecretServiceImpl(
            SecretRepository secretRepository,
            CryptoService cryptoService,
            Map<String, VaultWriter> vaultWriters,
            List<VaultReader> vaultReaders
    ) {
        this.secretRepository = secretRepository;
        this.cryptoService = cryptoService;
        this.vaultWriters = vaultWriters;
        this.vaultReaders = vaultReaders;
    }

    /**
     * Existing DB save logic extracted into a dedicated method.
     * IMPORTANT: keep this logic identical to the earlier DB-only create() implementation.
     */
    public SecretEntity createInDatabase(SecretEntity secret) {
        Instant now = Instant.now();
        secret.setCreatedAt(now);
        secret.setUpdatedAt(now);
        // encrypt value before saving
        secret.setValue(cryptoService.encrypt(secret.getValue()));
        return secretRepository.save(secret);
    }

    /**
     * New entry point used by controller when vaultType support is enabled.
     * Returns SecretEntity for consistent response format (no frontend change).
     */
    public SecretEntity storeByVaultType(SecretDto dto) {
        String vaultType = (dto == null) ? "DB" : dto.getVaultTypeOrDefault();
        VaultWriter writer = resolveWriter(vaultType);
        return writer.store(dto);
    }

    private VaultWriter resolveWriter(String vaultType) {
        String key = (vaultType == null) ? "DB" : vaultType.trim().toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(key)) key = "DB";

        // Map external API values to internal Spring bean names
        if ("DB".equals(key)) key = "VAULT_DB";
        else if ("AZURE".equals(key)) key = "VAULT_AZURE";
        else if ("GCP".equals(key)) key = "VAULT_GCP";

        if (vaultWriters == null) {
            throw new IllegalStateException("Vault writers are not configured");
        }

        VaultWriter writer = vaultWriters.get(key);
        if (writer == null) {
            throw new IllegalArgumentException("Invalid vaultType: " + vaultType);
        }
        return writer;
    }

    @CachePut(value = CacheConfig.SECRETS_CACHE, key = "#result.id")
    @Override
    public SecretEntity create(SecretEntity secret) {
    // Backward compatible: existing controller still calls this method.
    // Keep DB behavior unchanged.
    return createInDatabase(secret);
    }

    @CacheEvict(value = CacheConfig.SECRETS_CACHE, allEntries = true)
    @Override
    public SecretEntity update(Long id, SecretEntity secret) {
        SecretEntity existing = secretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secret not found: " + id));
        if (secret.getName() != null) existing.setName(secret.getName());
        if (secret.getType() != null) existing.setType(secret.getType());
        if (secret.getValue() != null) existing.setValue(cryptoService.encrypt(secret.getValue()));
        if (secret.getMetadata() != null) existing.setMetadata(secret.getMetadata());
        existing.setUpdatedAt(Instant.now());
        return secretRepository.save(existing);
    }

    @Cacheable(value = CacheConfig.SECRETS_CACHE, key = "#id")
    @Override
    public Optional<SecretEntity> get(Long id) {
        Optional<SecretEntity> opt = secretRepository.findById(id);
        if (opt.isPresent()) {
            SecretEntity e = opt.get();
            String vaultType = e.getVaultType();
            if (vaultType == null) vaultType = "DB"; // backward compatibility
            
            try {
                // Find appropriate reader and fetch actual value
                VaultReader reader = findReader(vaultType);
                String actualValue = reader.readSecret(e.getName());
                e.setValue(actualValue);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to read secret from vault: " + vaultType, ex);
            }
        }
        return opt;
    }

    /**
     * Find the appropriate VaultReader for the given vault type.
     */
    private VaultReader findReader(String vaultType) {
        for (VaultReader reader : vaultReaders) {
            if (reader.supports(vaultType)) {
                return reader;
            }
        }
        throw new IllegalArgumentException("No reader found for vault type: " + vaultType);
    }


    @Override
    public List<SecretEntity> list() {
        // Do not return decrypted values in lists — mask them for safety
        return secretRepository.findAll().stream().map(e -> {
            SecretEntity copy = new SecretEntity();
            copy.setId(e.getId());
            copy.setName(e.getName());
            copy.setType(e.getType());
            copy.setMetadata(e.getMetadata());
            copy.setCreatedAt(e.getCreatedAt());
            copy.setUpdatedAt(e.getUpdatedAt());
            // value intentionally omitted
            return copy;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SecretEntity> findByType(String type, String isDataKeys) {

        PluginSecretModel psm = pluginManagerService.getSecret(type);
        List<PluginSecretModel.Fields> s = psm.getFields().stream().filter(d->d.getType().equals("password")).toList();


        List<SecretEntity> ett =  secretRepository.findByType(type);
        for (SecretEntity et : ett) {
            String getDecreptedValues = this.getDecreptedValue(et.getValue());
            try {
                Map mp =  mapper.readValue(getDecreptedValues, new TypeReference<Map<String, Object>>() {});
                System.out.println(mp);
               for (PluginSecretModel.Fields fld : s){
                   System.out.println(fld);
                   mp.put(fld.getId(), "*********************");
               }
                et.setValue(mapper.writeValueAsString(mp));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }



        }

        System.out.println(ett);
        return ett;
    }

    @Override
    public Optional<SecretEntity> findByTypeAndName(String type, String name) {
        return secretRepository.findByTypeAndName(type, name);
    }

    @Override
    public String getDecreptedValue(String value){
        return  cryptoService.decrypt(value);
    }



    @CacheEvict(value = CacheConfig.SECRETS_CACHE, allEntries = true)
    @Override
    public void delete(Long id) {
        secretRepository.deleteById(id);
    }

    @Cacheable(value = CacheConfig.SECRETS_CACHE, key = "'name:' + #name")
    @Override
    public Optional<SecretEntity> findByName(String name) {
        Optional<SecretEntity> opt = secretRepository.findByName(name);
        if (opt.isPresent()) {
            SecretEntity e = opt.get();
            e.setValue(cryptoService.decrypt(e.getValue()));
        }
        return opt;
    }
}
