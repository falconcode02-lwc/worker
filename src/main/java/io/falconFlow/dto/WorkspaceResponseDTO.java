package io.falconFlow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
        name = "WorkspaceResponseDTO",
        description = "Response payload representing a workspace"
)
public class WorkspaceResponseDTO {
    @Schema(
            description = "Unique identifier of the workspace",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private UUID id;

    @Schema(
            description = "Display name of the workspace",
            example = "Finance Operations"
    )
    private String name;

    @Schema(
            description = "Organization ID to which the workspace belongs",
            example = "org-123"
    )
    private String orgId;

    @Schema(
            description = "Workspace type",
            example = "PUBLIC"
    )
    private String type;

    @Schema(
            description = "Unique workspace code",
            example = "FIN-OPS"
    )
    private String code;

    @Schema(
            description = "Icon identifier or icon URL for the workspace",
            example = "briefcase"
    )
    private String icon;

    @Schema(
            description = "Detailed description of the workspace",
            example = "Workspace used for finance automation workflows"
    )
    private String description;

    @Schema(
            description = "Timestamp when the workspace was created",
            example = "2024-06-01T10:15:30"
    )
    private LocalDateTime createdTime;

    @Schema(
            description = "Timestamp when the workspace was last modified",
            example = "2024-06-05T14:42:10"
    )
    private LocalDateTime modifiedTime;

    @Schema(
            description = "User who created the workspace",
            example = "admin"
    )
    private String createdBy;

    @Schema(
            description = "User who last modified the workspace",
            example = "admin"
    )
    private String modifiedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
