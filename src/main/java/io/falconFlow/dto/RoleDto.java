package io.falconFlow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RoleDto {
    private UUID roleId;
    private String roleName;
    private String description;
    private String permissions;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String createdBy;
    private String modifiedBy;
}
