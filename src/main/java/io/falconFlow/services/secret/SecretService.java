package io.falconFlow.services.secret;

import io.falconFlow.entity.SecretEntity;

import java.util.List;
import java.util.Optional;

public interface SecretService {
    SecretEntity create(SecretEntity secret);
    SecretEntity update(Long id, SecretEntity secret);
    Optional<SecretEntity> get(Long id);
    List<SecretEntity> list();
    List<SecretEntity> findByType(String type, String isDataKeys);
    Optional<SecretEntity> findByTypeAndName(String type, String name);
    void delete(Long id);
    Optional<SecretEntity> findByName(String name);
    String getDecreptedValue(String value);
}
