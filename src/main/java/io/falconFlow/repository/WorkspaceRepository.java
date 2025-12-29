package io.falconFlow.repository;

import io.falconFlow.entity.WorkSpaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<WorkSpaceEntity, UUID> {

    Page<WorkSpaceEntity> findByOrgId(String orgId, Pageable pageable);

    Optional<WorkSpaceEntity> findByCode(String code);

    boolean existsByCode(String code);
}

