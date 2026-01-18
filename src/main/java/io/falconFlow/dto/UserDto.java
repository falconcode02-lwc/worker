package io.falconFlow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDto {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private String password; // Only used for create/update, not returned in responses
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
