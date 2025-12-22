package io.falconFlow.repository;

import io.falconFlow.dto.GetFileListProjection;
import io.falconFlow.entity.FunctionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionsEntity, Integer> {

  // find by fully qualified class name
    Optional<FunctionsEntity> findByClassTypeAndFqcn(String classType, String fqcn);

  // delete by fqcn
  void deleteByFqcn(String fqcn);

  // find all
  @Query(
      value =
          "select id,class_name,compiled_time,created_time,fqcn,isdeleted,modified_time,class_type from ff_functions where isdeleted = false and class_type = :classType",
      nativeQuery = true)
  List<GetFileListProjection> findAllFileList(@Param("classType") String classType);

  @Query(
      value =
          "select class_type, count(1) as counts from ff_functions where isdeleted = false group by class_type",
      nativeQuery = true)
  List<GetFileListProjection> findAllListOfFolders();

  @Query(
      value =
          "select id,class_name,compiled_time,created_time,fqcn,isdeleted,modified_time,class_type from ff_functions where isdeleted = false and class_type in('function','condition','controller', 'plugin')",
      nativeQuery = true)
  List<GetFileListProjection> findAllListOfFunctions();
}
