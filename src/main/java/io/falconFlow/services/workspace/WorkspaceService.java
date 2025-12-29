package io.falconFlow.services.workspace;

import io.falconFlow.dto.WorkspaceCreateDTO;
import io.falconFlow.dto.WorkspaceListDTO;
import io.falconFlow.dto.WorkspaceResponseDTO;
import io.falconFlow.dto.WorkspaceUpdateDTO;
import io.falconFlow.entity.WorkSpaceEntity;
import io.falconFlow.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    public Page<WorkspaceListDTO> listWorkspaces(int page, int size, String orgId) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdTime").descending()
        );

        return workspaceRepository.findByOrgId(orgId,pageable).map(this::toWorkspaceDto);
    }

    @Transactional
    public WorkspaceResponseDTO createWorkspace(WorkspaceCreateDTO workspaceCreateDTO){
        WorkSpaceEntity workSpaceEntity = new WorkSpaceEntity();
        workSpaceEntity.setName(workspaceCreateDTO.getName());
        workSpaceEntity.setOrgId(workspaceCreateDTO.getOrgId());
        workSpaceEntity.setIcon(workspaceCreateDTO.getIcon());
        workSpaceEntity.setType(workspaceCreateDTO.getType());
        workSpaceEntity.setCode(workspaceCreateDTO.getCode());
        workSpaceEntity.setDescription(workspaceCreateDTO.getDescription());

        WorkSpaceEntity saved = workspaceRepository.save(workSpaceEntity);
        return toResponseDto(saved);
    }

    @Transactional
    public WorkspaceResponseDTO updateWorkspace(
            UUID id,
            WorkspaceUpdateDTO dto) throws Exception {

        WorkSpaceEntity entity = workspaceRepository.findById(id)
                .orElseThrow(() -> new Exception("Workspace not found"));

        entity.setName(dto.getName());
        entity.setIcon(dto.getIcon());
        entity.setDescription(dto.getDescription());

        WorkSpaceEntity saved = workspaceRepository.save(entity);
        return toResponseDto(saved);
    }

    private WorkspaceListDTO toWorkspaceDto(WorkSpaceEntity entity) {
        WorkspaceListDTO dto = new WorkspaceListDTO();
        dto.setOrgId(entity.getOrgId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setModifiedTime(entity.getModifiedTime());
        return dto;
    }

    private WorkspaceResponseDTO toResponseDto(WorkSpaceEntity entity) {
        WorkspaceResponseDTO dto = new WorkspaceResponseDTO();
        dto.setId(entity.getUuid());
        dto.setName(entity.getName());
        dto.setOrgId(entity.getOrgId());
        dto.setType(entity.getType());
        dto.setCode(entity.getCode());
        dto.setIcon(entity.getIcon());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedTime(entity.getCreatedTime());
        return dto;
    }
}
