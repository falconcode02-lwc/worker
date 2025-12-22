package io.falconFlow.repository;

import io.falconFlow.entity.ProviderConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderConfigRepository extends JpaRepository<ProviderConfigEntity, Long> {
    Optional<ProviderConfigEntity> findByName(String name);
}
