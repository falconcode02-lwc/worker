package io.falconFlow.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ff_ai_agents")
public class AgentEntity {

    @Id
    @Column(name = "agent_id", nullable = false, length = 200)
    private String agentId;

    @Column(name = "model", length = 200)
    private String model;

    @Lob
    @Column(name = "config")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String config; // JSON string

    private Instant createdAt;
    private Instant updatedAt;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }
}
