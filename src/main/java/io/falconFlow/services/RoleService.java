package io.falconFlow.services;

import io.falconFlow.dto.RoleDto;
import io.falconFlow.entity.RoleEntity;
import io.falconFlow.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public RoleDto getRoleById(UUID id) {
        return roleRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public RoleDto createRole(RoleDto roleDto) {
        RoleEntity entity = new RoleEntity();
        updateEntity(entity, roleDto);
        return convertToDto(roleRepository.save(entity));
    }

    public RoleDto updateRole(UUID id, RoleDto roleDto) {
        return roleRepository.findById(id).map(entity -> {
            updateEntity(entity, roleDto);
            return convertToDto(roleRepository.save(entity));
        }).orElse(null);
    }

    public void deleteRole(UUID id) {
        roleRepository.deleteById(id);
    }

    private RoleDto convertToDto(RoleEntity entity) {
        RoleDto dto = new RoleDto();
        dto.setRoleId(entity.getRoleId());
        dto.setRoleName(entity.getRoleName());
        dto.setDescription(entity.getDescription());
        dto.setPermissions(entity.getPermissions());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private void updateEntity(RoleEntity entity, RoleDto dto) {
        entity.setRoleName(dto.getRoleName());
        entity.setDescription(dto.getDescription());
        entity.setPermissions(dto.getPermissions());
    }
}
