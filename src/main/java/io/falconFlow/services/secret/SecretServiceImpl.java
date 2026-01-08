package io.falconFlow.services.secret;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

@Service
public class SecretServiceImpl implements SecretService {

    private static final Logger log = LoggerFactory.getLogger(SecretServiceImpl.class);

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
        log.info("========== GET SECRET START ==========");
        log.info("Fetching secret with id: {}", id);
        
        Optional<SecretEntity> opt = secretRepository.findById(id);
        if (opt.isPresent()) {
            SecretEntity e = opt.get();
            String vaultType = e.getVaultType();
            if (vaultType == null) vaultType = "DB"; // backward compatibility
            
            log.info("Secret found in DB:");
            log.info("  - ID: {}", e.getId());
            log.info("  - Name: {}", e.getName());
            log.info("  - Type: {}", e.getType());
            log.info("  - VaultType: {}", vaultType);
            log.info("  - Value from DB (reference/encrypted): {}", e.getValue());
            log.info("  - Metadata: {}", e.getMetadata());
            log.info("  - CreatedAt: {}", e.getCreatedAt());
            log.info("  - UpdatedAt: {}", e.getUpdatedAt());
            
            try {
                // Find appropriate reader and fetch actual value
                log.info("Finding VaultReader for vaultType: {}", vaultType);
                VaultReader reader = findReader(vaultType);
                log.info("Using reader: {}", reader.getClass().getSimpleName());
                
                log.info("Calling readSecret('{}') from {} vault...", e.getName(), vaultType);
                String actualValue = reader.readSecret(e.getName());
                log.info("Actual value retrieved from {} vault: {}", vaultType, actualValue);
                
                e.setValue(actualValue);
                log.info("Final response value set to: {}", actualValue);
            } catch (Exception ex) {
                log.error("Failed to read secret from vault: {}", vaultType, ex);
                throw new RuntimeException("Failed to read secret from vault: " + vaultType, ex);
            }
        } else {
            log.info("Secret with id {} not found in DB", id);
        }
        
        log.info("========== GET SECRET END ==========");
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
        log.info("========== LIST SECRETS START ==========");
        
        List<SecretEntity> allSecrets = secretRepository.findAll();
        log.info("Found {} secrets in database", allSecrets.size());
        
        // Do not return decrypted values in lists — mask them for safety
        List<SecretEntity> result = allSecrets.stream().map(e -> {
            String vaultType = e.getVaultType();
            if (vaultType == null || vaultType.isEmpty()) vaultType = "DB";
            
            log.info("Secret [id={}, name='{}', type='{}', vaultType='{}']", 
                    e.getId(), e.getName(), e.getType(), vaultType);
            log.info("  - DB stored value (encrypted/reference): {}", e.getValue());

            // Do not attempt to fetch/resolve actual vault values during list.
            // This endpoint is called frequently by UI and should not trigger external calls.
            // (Also prevents noisy logs / auth failures when AZURE/GCP aren't configured locally.)
            
            SecretEntity copy = new SecretEntity();
            copy.setId(e.getId());
            copy.setName(e.getName());
            copy.setType(e.getType());
            copy.setVaultType(vaultType);
            copy.setMetadata(e.getMetadata());
            copy.setCreatedAt(e.getCreatedAt());
            copy.setUpdatedAt(e.getUpdatedAt());
            // value intentionally omitted from response
            return copy;
        }).collect(Collectors.toList());
        
        log.info("========== LIST SECRETS END ==========");
        return result;
    }

    @Override
    public List<SecretEntity> findByType(String type, String isDataKeys) {

        PluginSecretModel psm = pluginManagerService.getSecret(type);
        List<PluginSecretModel.Fields> s = psm.getFields().stream().filter(d->d.getType().equals("password")).toList();


        List<SecretEntity> ett = secretRepository.findByType(type);
        for (SecretEntity et : ett) {
            String vaultType = et.getVaultType();
            if (!StringUtils.hasText(vaultType)) vaultType = "DB";

            // IMPORTANT:
            // - DB vault stores an AES-encrypted JSON blob in `value`.
            // - AZURE/GCP vault types may store a reference like `azure-kv:<id>`.
            // Trying to AES-decrypt such references causes Illegal base64 character errors.
            if (!"DB".equalsIgnoreCase(vaultType)) {
                // For non-DB vault types, return a masked placeholder (no external vault read).
                // UI only needs to know the credential exists; actual read happens via GET /api/secrets/{id}.
                et.setValue("*********************");
                continue;
            }

            String decrypted;
            try {
                decrypted = this.getDecreptedValue(et.getValue());
            } catch (RuntimeException ex) {
                // Fail-soft: one bad row should not break the whole list.
                log.warn("Failed to decrypt secret value for id={} (type={}, vaultType={}). Returning masked value.",
                        et.getId(), et.getType(), vaultType, ex);
                et.setValue("*********************");
                continue;
            }

            try {
                Map<String, Object> mp = mapper.readValue(decrypted, new TypeReference<Map<String, Object>>() {});
                for (PluginSecretModel.Fields fld : s) {
                    mp.put(fld.getId(), "*********************");
                }
                et.setValue(mapper.writeValueAsString(mp));
            } catch (JsonProcessingException e) {
                // Same idea: fail-soft and mask.
                log.warn("Failed to parse decrypted secret JSON for id={} (type={}, vaultType={}). Returning masked value.",
                        et.getId(), et.getType(), vaultType, e);
                et.setValue("*********************");
            }
        }

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
        log.info("========== DELETE SECRET START ==========");
        log.info("Deleting secret with id: {}", id);
        
        Optional<SecretEntity> opt = secretRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Secret with id {} not found for deletion", id);
            throw new RuntimeException("Secret not found: " + id);
        }
        
        SecretEntity entity = opt.get();
        String vaultType = entity.getVaultType();
        if (vaultType == null) vaultType = "DB";
        
        log.info("Secret found - Name: {}, VaultType: {}", entity.getName(), vaultType);
        
        // Find the appropriate writer and delete
        VaultWriter writer = findWriter(vaultType);
        log.info("Using writer: {} for deletion", writer.getClass().getSimpleName());
        
        writer.delete(entity.getName());
        
        log.info("========== DELETE SECRET END ==========");
    }

    /**
     * Find the appropriate VaultWriter for the given vault type.
     */
    private VaultWriter findWriter(String vaultType) {
        for (VaultWriter writer : vaultWriters.values()) {
            if (writer.supports(vaultType)) {
                return writer;
            }
        }
        throw new IllegalArgumentException("No writer found for vault type: " + vaultType);
    }

    @Cacheable(value = CacheConfig.SECRETS_CACHE, key = "'name:' + #name")
    @Override
    public Optional<SecretEntity> findByName(String name) {
        Optional<SecretEntity> opt = secretRepository.findByName(name);
        if (opt.isPresent()) {
            SecretEntity e = opt.get();
            String vaultType = e.getVaultType();
            if (!StringUtils.hasText(vaultType)) vaultType = "DB";

            // DB secrets are stored AES-encrypted in ff_secrets.value
            if ("DB".equalsIgnoreCase(vaultType)) {
                e.setValue(cryptoService.decrypt(e.getValue()));
                return opt;
            }

            // Non-DB (AZURE/GCP) secrets store a reference in ff_secrets.value.
            // For runtime use, resolve the actual secret from the configured vault.
            try {
                VaultReader reader = findReader(vaultType);
                String actualValue = reader.readSecret(e.getName());
                e.setValue(actualValue);
            } catch (Exception ex) {
                log.error("Failed to resolve secret '{}' from vaultType={}", name, vaultType, ex);
                throw new RuntimeException("Failed to read secret from vault: " + vaultType, ex);
            }
        }
        return opt;
    }
}
