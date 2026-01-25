package io.falconFlow.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private String roleName;
    private Integer remainingAttempts;
    private boolean accountLocked;
}
