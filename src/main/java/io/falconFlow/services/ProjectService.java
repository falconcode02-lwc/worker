package io.falconFlow.services;

import io.falconFlow.entity.ProjectEntity;
import io.falconFlow.entity.RoleEntity;
import io.falconFlow.repository.ProjectRepository;
import io.falconFlow.repository.UserRepository;
import io.falconFlow.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<ProjectEntity> getAllProjects(UUID userId) {
        if (userId != null) {
            return userRepository.findById(userId).map(user -> {
                RoleEntity role = roleRepository.findById(user.getRoleId()).orElse(null);
                if (role != null && (role.getRoleName().equalsIgnoreCase("ADMIN") || role.getRoleName().equalsIgnoreCase("SUPER_ADMIN"))) {
                    return projectRepository.findAll();
                }
                return projectRepository.findByUserId(userId);
            }).orElse(projectRepository.findAll());
        }
        return projectRepository.findAll();
    }

    public List<ProjectEntity> getByWorkspace(String workspaceCode, UUID userId) {
        if (userId != null) {
            return userRepository.findById(userId).map(user -> {
                RoleEntity role = roleRepository.findById(user.getRoleId()).orElse(null);
                if (role != null && (role.getRoleName().equalsIgnoreCase("ADMIN") || role.getRoleName().equalsIgnoreCase("SUPER_ADMIN"))) {
                    return projectRepository.findByWorkspaceCode(workspaceCode);
                }
                return projectRepository.findByWorkspaceCodeAndUserId(workspaceCode, userId);
            }).orElse(projectRepository.findByWorkspaceCode(workspaceCode));
        }
        return projectRepository.findByWorkspaceCode(workspaceCode);
    }
}
