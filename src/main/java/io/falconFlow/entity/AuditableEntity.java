package io.falconFlow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class AuditableEntity {
    @Column(
            name = "created_time",
            updatable = false)
    protected LocalDateTime createdTime;

    @Column(name = "modified_time")
    protected LocalDateTime modifiedTime;

    @Column(name = "created_by", length = 100)
    protected String createdBy;

    @Column(name = "modified_by", length = 100)
    protected String modifiedBy;

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
