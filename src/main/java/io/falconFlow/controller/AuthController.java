package io.falconFlow.controller;

import io.falconFlow.dto.LoginRequest;
import io.falconFlow.dto.LoginResponse;
import io.falconFlow.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/unlock/{username}")
    public ResponseEntity<String> unlockAccount(@PathVariable String username) {
        boolean unlocked = authService.unlockAccount(username);
        if (unlocked) {
            return ResponseEntity.ok("Account unlocked successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
