package io.falconFlow.repository;

import io.falconFlow.entity.WorkSpaceEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<WorkSpaceEntity, UUID> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE ff_workspaces SET active = :active , modified_time= CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    Integer updateActive(@Param("id") UUID id, @Param("active") Boolean active);

    Page<WorkSpaceEntity> findByOrgId(String orgId, Pageable pageable);

    Optional<WorkSpaceEntity> findByCode(String code);

    boolean existsByCode(String code);

    Optional<WorkSpaceEntity> findById(UUID id);
}

