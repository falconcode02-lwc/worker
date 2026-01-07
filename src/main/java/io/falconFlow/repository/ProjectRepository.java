package io.falconFlow.repository;

import io.falconFlow.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Optional<ProjectEntity> findByCodeAndWorkspaceCode(String code, String workspaceCode);
    List<ProjectEntity> findByWorkspaceCode(String workspaceCode);
    long countByWorkspaceCode(String workspaceCode);
}
