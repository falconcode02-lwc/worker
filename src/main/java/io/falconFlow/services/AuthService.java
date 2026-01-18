package io.falconFlow.services;

import io.falconFlow.dto.LoginRequest;
import io.falconFlow.dto.LoginResponse;
import io.falconFlow.entity.UserEntity;
import io.falconFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public LoginResponse login(LoginRequest request) {
        LoginResponse response = new LoginResponse();
        
        UserEntity user = userRepository.findByUsername(request.getUsername());
        
        if (user == null) {
            response.setSuccess(false);
            response.setMessage("Invalid username or password");
            return response;
        }

        // Check if account is locked
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            response.setSuccess(false);
            response.setMessage("Account is locked due to multiple failed login attempts. Please contact administrator.");
            response.setAccountLocked(true);
            return response;
        }

        // Check if account is inactive
        if ("INACTIVE".equals(user.getStatus())) {
            response.setSuccess(false);
            response.setMessage("Account is inactive. Please contact administrator.");
            return response;
        }

        // Verify password (in production, use BCrypt or similar)
        if (!user.getPassword().equals(request.getPassword())) {
            handleFailedLogin(user);
            int remainingAttempts = MAX_LOGIN_ATTEMPTS - user.getFailedLoginAttempts();
            
            response.setSuccess(false);
            response.setMessage("Invalid username or password");
            response.setRemainingAttempts(Math.max(0, remainingAttempts));
            
            if (user.getAccountLocked()) {
                response.setMessage("Account locked due to multiple failed login attempts");
                response.setAccountLocked(true);
            }
            
            return response;
        }

        // Successful login
        handleSuccessfulLogin(user);
        
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRemainingAttempts(MAX_LOGIN_ATTEMPTS);
        
        return response;
    }

    private void handleFailedLogin(UserEntity user) {
        int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        attempts++;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedAt(LocalDateTime.now());
            user.setStatus("LOCKED");
        }
        
        userRepository.save(user);
    }

    private void handleSuccessfulLogin(UserEntity user) {
        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public boolean unlockAccount(String username) {
        UserEntity user = userRepository.findByUsername(username);
        if (user != null && Boolean.TRUE.equals(user.getAccountLocked())) {
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockedAt(null);
            user.setStatus("ACTIVE");
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
