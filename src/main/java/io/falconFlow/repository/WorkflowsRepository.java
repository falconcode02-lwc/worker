package io.falconFlow.repository;



import io.falconFlow.dto.GetWorkFlowsProjection;
import io.falconFlow.dto.WorkflowsNameDTO;
import io.falconFlow.entity.WorkFlowsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowsRepository extends JpaRepository<WorkFlowsEntity, Integer> {
    boolean existsByCode(String code);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ff_workflows SET active = :active , modified_time= CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    Integer updateActive(@Param("id") Integer id, @Param("active") Boolean active);

    @Query(value = "select id,name,version,workflow_json,description,code, controller,active from ff_workflows where code = :workFlowCode and active = true limit 1", nativeQuery = true)
    GetWorkFlowsProjection findByCode(@Param("workFlowCode") String workFlowCode);

    @Query(value = "select id,name,code from ff_workflows where active = true", nativeQuery = true)
    List<WorkflowsNameDTO> findActiveWorkflows();

    @Query("SELECT w.id as id, w.name as name, w.code as code FROM WorkFlowsEntity w " +
           "LEFT JOIN w.project p " +
           "LEFT JOIN p.workspace ws " +
           "WHERE w.active = true " +
           "AND (:workspaceCode IS NULL OR ws.code = :workspaceCode) " +
           "AND (:projectId IS NULL OR p.id = :projectId)")
    List<WorkflowsNameDTO> findActiveWorkflowsFiltered(@Param("workspaceCode") String workspaceCode, @Param("projectId") java.util.UUID projectId);

    @Query("SELECT w FROM WorkFlowsEntity w " +
           "LEFT JOIN FETCH w.project p " +
           "LEFT JOIN FETCH p.workspace ws " +
           "WHERE w.code = :code AND w.active = true")
    java.util.Optional<WorkFlowsEntity> findEntityByCode(@Param("code") String code);

    @Query("SELECT w FROM WorkFlowsEntity w " +
           "LEFT JOIN w.project p " +
           "LEFT JOIN p.workspace ws " +
           "WHERE (:workspaceCode IS NULL OR ws.code = :workspaceCode) " +
           "AND (:projectId IS NULL OR p.id = :projectId)")
    List<WorkFlowsEntity> findAllFiltered(@Param("workspaceCode") String workspaceCode, @Param("projectId") java.util.UUID projectId);
}


