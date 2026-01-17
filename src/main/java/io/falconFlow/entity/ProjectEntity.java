package io.falconFlow.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ff_projects")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "icon", length = 500)
    private String icon;

    @Lob
    @Column(name = "description")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "accessibility")
    private Accessibility accessibility;

    @Column(name = "workspace_code", nullable = false, length = 200)
    private String workspaceCode;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @JsonIgnoreProperties({"projects", "hibernateLazyInitializer", "handler"})
    private WorkSpaceEntity workspace;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnoreProperties({"project", "hibernateLazyInitializer", "handler"})
    private List<WorkFlowsEntity> workflows = new ArrayList<>();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Accessibility getAccessibility() { return accessibility; }
    public void setAccessibility(Accessibility accessibility) { this.accessibility = accessibility; }
    public String getWorkspaceCode() { return workspaceCode; }
    public void setWorkspaceCode(String workspaceCode) { this.workspaceCode = workspaceCode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.accessibility == null) this.accessibility = Accessibility.PRIVATE;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public WorkSpaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkSpaceEntity workspace) {
        this.workspace = workspace;
    }
}

enum Accessibility {
    PUBLIC, PRIVATE, WORKSPACE_ONLY
}
