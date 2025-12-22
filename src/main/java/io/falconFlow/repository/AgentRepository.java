package io.falconFlow.repository;

import io.falconFlow.entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, String> {
}
