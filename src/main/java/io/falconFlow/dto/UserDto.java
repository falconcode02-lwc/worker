package io.falconFlow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UserDto {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private String password; // Only used for create/update, not returned in responses
    private UUID roleId;
    private String roleName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private String createdBy;
    private String modifiedBy;
    private List<UUID> workspaceIds;
    private List<UUID> projectIds;
}
