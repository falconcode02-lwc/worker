//package io.falconFlow.repository;
//
//import io.falconFlow.entity.WorkSpaceEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface WorkspaceRepository extends JpaRepository<WorkSpaceEntity, UUID> {
//
//    Optional<WorkSpaceEntity> findByCode(String code);
//
//    boolean existsByCode(String code);
//}
//
