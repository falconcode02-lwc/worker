package io.falconFlow.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "ff_workspaces",
        uniqueConstraints = {@UniqueConstraint(name = "ff_workspaces_code_UN", columnNames = "code")}
)
public class WorkSpaceEntity extends AuditableEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", length = 150, nullable = false)
    private String orgId;

    @Column(name = "code", length = 150, nullable = false)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "icon_url", length = 200, nullable = true)
    private String icon;

    @Column(name = "workflow_type", nullable = false)
    private String type;

    @Column(name = "description", length = 2000, nullable = true)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}