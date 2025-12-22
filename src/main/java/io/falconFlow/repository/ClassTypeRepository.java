package io.falconFlow.repository;

import io.falconFlow.dto.ClassTypeProjection;
import io.falconFlow.entity.ClassTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassTypeRepository extends JpaRepository<ClassTypeEntity, Integer> {

  @Query(
      value =
          "select id,class_type,icon,seq,class_type_key from ff_class_types where is_active = true order by seq ",
      nativeQuery = true)
  List<ClassTypeProjection> findAllActive();

  @Query(
      value =
          "select k.id,k.class_type,k.icon,k.seq,k.class_type_key, ct.counts  from ff_class_types k left join (select count(1) counts, ff.class_type from ff_functions ff where ff.isdeleted = false group by ff.class_type ) ct on k.class_type_key  = ct.class_type where k.is_active = true order by seq",
      nativeQuery = true)
  List<ClassTypeProjection> findAllActiveWithCount();
}
