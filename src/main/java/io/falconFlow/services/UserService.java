package io.falconFlow.services;

import io.falconFlow.dto.UserDto;
import io.falconFlow.entity.UserEntity;
import io.falconFlow.repository.UserRepository;
import io.falconFlow.repository.RoleRepository;
import io.falconFlow.entity.RoleEntity;
import io.falconFlow.entity.WorkSpaceEntity;
import io.falconFlow.entity.ProjectEntity;
import io.falconFlow.repository.WorkspaceRepository;
import io.falconFlow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

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
        dto.setRoleId(entity.getRoleId());
        
        if (entity.getRoleId() != null) {
            roleRepository.findById(entity.getRoleId()).ifPresent(role -> {
                dto.setRoleName(role.getRoleName());
            });
        }
        
        if (entity.getWorkspaces() != null) {
            dto.setWorkspaceIds(entity.getWorkspaces().stream()
                    .map(WorkSpaceEntity::getId)
                    .collect(Collectors.toList()));
        }

        if (entity.getProjects() != null) {
            dto.setProjectIds(entity.getProjects().stream()
                    .map(ProjectEntity::getId)
                    .collect(Collectors.toList()));
        }

        dto.setCreatedTime(entity.getCreatedTime());
        dto.setModifiedTime(entity.getModifiedTime());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        return dto;
    }

    private void updateEntity(UserEntity entity, UserDto dto) {
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setFullName(dto.getFullName());
        entity.setStatus(dto.getStatus());
        entity.setRoleId(dto.getRoleId());
        
        // Hash password if provided
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else if (entity.getPassword() == null) {
            // Set default hashed password for new users
            entity.setPassword(passwordEncoder.encode("password123")); // Change this in production
        }
        
        // Initialize security fields for new users
        if (entity.getUserId() == null) {
            entity.setFailedLoginAttempts(0);
            entity.setAccountLocked(false);
        }

        if (dto.getWorkspaceIds() != null) {
            entity.setWorkspaces(workspaceRepository.findAllById(dto.getWorkspaceIds()));
        }

        if (dto.getProjectIds() != null) {
            entity.setProjects(projectRepository.findAllById(dto.getProjectIds()));
        }
    }
}
