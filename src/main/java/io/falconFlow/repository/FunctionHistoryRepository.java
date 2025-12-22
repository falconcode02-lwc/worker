package io.falconFlow.repository;

import io.falconFlow.entity.FunctionsHitoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FunctionHistoryRepository extends JpaRepository<FunctionsHitoryEntity, Integer> {

  // find by fully qualified class name
  Optional<FunctionsHitoryEntity> findByFqcn(String fqcn);
}
