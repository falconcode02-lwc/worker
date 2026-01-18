package io.falconFlow.services;

import io.falconFlow.dto.UserDto;
import io.falconFlow.entity.UserEntity;
import io.falconFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public UserDto createUser(UserDto userDto) {
        UserEntity entity = new UserEntity();
        updateEntity(entity, userDto);
        return convertToDto(userRepository.save(entity));
    }

    public UserDto updateUser(UUID id, UserDto userDto) {
        return userRepository.findById(id).map(entity -> {
            updateEntity(entity, userDto);
            return convertToDto(userRepository.save(entity));
        }).orElse(null);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFullName(entity.getFullName());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private void updateEntity(UserEntity entity, UserDto dto) {
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setStatus(dto.getStatus());
        
        // Set password if provided (in production, hash the password)
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPassword(dto.getPassword());
        } else if (entity.getPassword() == null) {
            // Set default password for new users
            entity.setPassword("password123"); // Change this in production
        }
        
        // Initialize security fields for new users
        if (entity.getUserId() == null) {
            entity.setFailedLoginAttempts(0);
            entity.setAccountLocked(false);
        }
    }
}
