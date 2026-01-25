package io.falconFlow.entity;

import io.falconFlow.util.UserContextHolder;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * JPA Entity Listener for automatically populating audit fields.
 * This listener is applied to AuditableEntity to automatically set
 * createdBy, modifiedBy, createdTime, and modifiedTime fields.
 */
public class AuditEntityListener {
    
    @PrePersist
    public void setCreationAudit(AuditableEntity entity) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String currentUser = UserContextHolder.getCurrentUser();
        
        // Set timestamps using setters
        entity.setCreatedTime(now);
        entity.setModifiedTime(now);
        
        // Set user info
        entity.setCreatedBy(currentUser);
        entity.setModifiedBy(currentUser);
    }
    
    @PreUpdate
    public void setUpdateAudit(AuditableEntity entity) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String currentUser = UserContextHolder.getCurrentUser();
        
        // Update timestamp using setter
        entity.setModifiedTime(now);
        
        // Update user info
        entity.setModifiedBy(currentUser);
    }
}
