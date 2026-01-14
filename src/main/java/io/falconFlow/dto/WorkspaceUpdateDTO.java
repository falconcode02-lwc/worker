package io.falconFlow.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "WorkspaceUpdateDTO",
        description = "Payload used to update an existing workspace. Only provided fields will be updated."
)
public class WorkspaceUpdateDTO {
    @Schema(
            description = "Updated display name of the workspace",
            example = "Finance Operations"
    )
    private String name;

    @Schema(
            description = "Updated icon identifier or icon URL",
            example = "briefcase"
    )
    private String icon;

    @Schema(
            description = "Updated description of the workspace",
            example = "Updated workspace description for finance workflows"
    )
    private String description;

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
}
