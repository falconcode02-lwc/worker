package io.falconFlow.repository;

import io.falconFlow.dto.FormsDTO;
import io.falconFlow.entity.FormsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormsRepository extends JpaRepository<FormsEntity, Integer> {
    boolean existsByCode(String code);

    @Transactional
    @Modifying
    @Query(value = "UPDATE ff_forms SET active = :active , modified_time= CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    Integer updateActive(@Param("id") Integer id, @Param("active") Boolean active);

    @Query(value = "SELECT * FROM ff_forms WHERE code = :code AND active = 1 LIMIT 1", nativeQuery = true)
    FormsEntity findByCode(@Param("code") String code);

    @Query(value = "SELECT * FROM ff_forms WHERE active = 1", nativeQuery = true)
    List<FormsEntity> findActiveForms();
}
