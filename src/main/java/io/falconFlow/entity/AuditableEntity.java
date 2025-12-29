package io.falconFlow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@MappedSuperclass
public abstract class AuditableEntity {
    @Column(
            name = "created_time",
            nullable = false,
            updatable = false)
    protected LocalDateTime createdTime;

    @Column(name = "modified_time")
    protected LocalDateTime modifiedTime;

    @PrePersist
    protected void prePersist() {
        this.createdTime = LocalDateTime.now(ZoneId.systemDefault());
    }
    @PreUpdate
    protected void preUpdate() {
        this.modifiedTime = LocalDateTime.now(ZoneId.systemDefault());
    }
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }
}
