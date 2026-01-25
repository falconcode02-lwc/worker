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

    @org.springframework.data.jpa.repository.Query(value = "SELECT p.* FROM ff_projects p " +
                   "JOIN user_projects up ON p.id = up.project_id " +
                   "WHERE up.user_id = :userId", 
           nativeQuery = true)
    List<ProjectEntity> findByUserId(@org.springframework.data.repository.query.Param("userId") UUID userId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT p.* FROM ff_projects p " +
                   "JOIN user_projects up ON p.id = up.project_id " +
                   "WHERE up.user_id = :userId AND p.workspace_code = :workspaceCode", 
           nativeQuery = true)
    List<ProjectEntity> findByWorkspaceCodeAndUserId(@org.springframework.data.repository.query.Param("workspaceCode") String workspaceCode, @org.springframework.data.repository.query.Param("userId") UUID userId);
}
