package io.falconFlow.services.secret;

import io.falconFlow.configuration.CacheConfig;
import io.falconFlow.entity.SecretEntity;
import io.falconFlow.repository.SecretRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SecretServiceImpl implements SecretService {


    private final SecretRepository secretRepository;
    private final CryptoService cryptoService;

    @Autowired
    public SecretServiceImpl(SecretRepository secretRepository, CryptoService cryptoService) {
        this.secretRepository = secretRepository;
        this.cryptoService = cryptoService;
    }

    @CachePut(value = CacheConfig.SECRETS_CACHE, key = "#result.id")
    @Override
    public SecretEntity create(SecretEntity secret) {
        Instant now = Instant.now();
        secret.setCreatedAt(now);
        secret.setUpdatedAt(now);
        // encrypt value before saving
        secret.setValue(cryptoService.encrypt(secret.getValue()));
        return secretRepository.save(secret);
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
            // decrypt value before returning
            try {
                e.setValue(cryptoService.decrypt(e.getValue()));
            } catch (Exception ex) {
                // if decryption fails, throw a runtime exception to surface the error
                throw new RuntimeException("Failed to decrypt secret value", ex);
            }
        }
        return opt;
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
    public List<SecretEntity> findByType(String type) {
        return secretRepository.findByType(type);
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
