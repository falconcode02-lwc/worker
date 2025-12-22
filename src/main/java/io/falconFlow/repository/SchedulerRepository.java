package io.falconFlow.repository;

import io.falconFlow.entity.SchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerEntity, Long> {}
