package io.falconFlow.repository;

import io.falconFlow.entity.PluginEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PluginRepository extends JpaRepository<PluginEntity, Integer> {
    Optional<PluginEntity> findByPluginId(String pluginId);

    // search by plugin name or pluginId (case-insensitive) with pagination
    Page<PluginEntity> findByPluginNameContainingIgnoreCaseOrPluginIdContainingIgnoreCase(String name, String pluginId, Pageable pageable);
}
