package io.falconFlow.repository;

import io.falconFlow.entity.PluginEntity;
import io.falconFlow.interfaces.enums.PluginType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PluginRepository extends JpaRepository<PluginEntity, Integer> {
    Optional<PluginEntity> findByPluginId(String pluginId);


    // search by plugin name or pluginId (case-insensitive) with pagination
    Page<PluginEntity> findByPluginNameContainingIgnoreCaseOrPluginIdContainingIgnoreCase(String name, String pluginId, Pageable pageable);

    Page<PluginEntity> findByPluginType(PluginType pluginType, Pageable pageable);

    @Query("SELECT p FROM PluginEntity p WHERE p.pluginType = :pluginType AND (LOWER(p.pluginName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.pluginId) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<PluginEntity> findByPluginTypeAndQuery(@Param("pluginType") PluginType pluginType, @Param("q") String q, Pageable pageable);
}
