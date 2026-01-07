package io.falconFlow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "WorkspaceCreateDTO",
        description = "Payload used to create a new workspace"
)
public class WorkspaceCreateDTO {

    @NotBlank
    @Schema(
            description = "Organization ID under which the workspace is created",
            example = "org-123"
    )
    private String orgId;

    @NotBlank
    @Schema(
            description = "Unique workspace code",
            example = "FIN-OPS"
    )
    private String code;

    @NotBlank
    @Schema(
            description = "Display name of the workspace",
            example = "Finance Operations"
    )
    private String name;

    @Schema(
            description = "Icon identifier or icon URL for the workspace",
            example = "briefcase"
    )
    private String icon;

    @NotNull
    @Schema(
            description = "Workspace type",
            example = "PUBLIC",
            allowableValues = {"PUBLIC", "PRIVATE"}
    )
    private String type;

    @Schema(
            description = "Optional description providing additional details about the workspace",
            example = "Workspace used for finance automation workflows"
    )
    private String description;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
