package io.falconFlow.repository;

import io.falconFlow.entity.SecretEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecretRepository extends JpaRepository<SecretEntity, Long> {
    Optional<SecretEntity> findByName(String name);
    List<SecretEntity> findByType(String type);
    Optional<SecretEntity> findByTypeAndName(String type, String name);
}
