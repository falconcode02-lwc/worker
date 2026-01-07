package io.falconFlow.services.workspace;

import io.falconFlow.dto.*;
import io.falconFlow.entity.FormsEntity;
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

    @Autowired
    private TemporalNamespaceService temporalNamespaceService;

    public Page<WorkspaceListDTO> listWorkspaces(int page, int size, String orgId) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdTime").descending()
        );

        return workspaceRepository.findByOrgId(orgId,pageable).map(this::toWorkspaceDto);
    }
    public Page<WorkspaceListDTO> listWorkspaces(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdTime").descending()
        );

        return workspaceRepository.findAll(pageable).map(this::toWorkspaceDto);
    }

    @Transactional
    public WorkspaceResponseDTO createWorkspace(WorkspaceCreateDTO workspaceCreateDTO){

        if (workspaceRepository.existsByCode(workspaceCreateDTO.getCode())) {
            throw new IllegalArgumentException(
                    "Workspace with code already exists: " + workspaceCreateDTO.getCode()
            );
        }

        UUID workspaceId = UUID.randomUUID();
        String namespace = generateTemporalNamespace(workspaceCreateDTO.getOrgId(),workspaceId);

        WorkSpaceEntity workSpaceEntity = getWorkSpaceEntity(workspaceCreateDTO, workspaceId, namespace);

        temporalNamespaceService.createNamespace(namespace, 30);

        WorkSpaceEntity saved = workspaceRepository.save(workSpaceEntity);
        return toResponseDto(saved);
    }

    private static WorkSpaceEntity getWorkSpaceEntity(WorkspaceCreateDTO workspaceCreateDTO, UUID workspaceId, String namespace) {
        WorkSpaceEntity workSpaceEntity = new WorkSpaceEntity();
        workSpaceEntity.setName(workspaceCreateDTO.getName());
        workSpaceEntity.setOrgId(workspaceCreateDTO.getOrgId());
        workSpaceEntity.setIcon(workspaceCreateDTO.getIcon());
        workSpaceEntity.setType(workspaceCreateDTO.getType());
        workSpaceEntity.setCode(workspaceCreateDTO.getCode());
        workSpaceEntity.setDescription(workspaceCreateDTO.getDescription());
        workSpaceEntity.setId(workspaceId);
        workSpaceEntity.setTemporalNamespace(namespace);
        return workSpaceEntity;
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

    @Transactional
    public void deleteWorkspace(UUID id) throws Exception{

        WorkSpaceEntity entity = workspaceRepository.findById(id)
                .orElseThrow(() ->
                        new Exception("Workspace not found"));

        workspaceRepository.delete(entity);
    }

    private WorkspaceListDTO toWorkspaceDto(WorkSpaceEntity entity) {
        WorkspaceListDTO dto = new WorkspaceListDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setIcon(entity.getIcon());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setModifiedTime(entity.getModifiedTime());
        return dto;
    }

    private WorkspaceResponseDTO toResponseDto(WorkSpaceEntity entity) {
        WorkspaceResponseDTO dto = new WorkspaceResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOrgId(entity.getOrgId());
        dto.setType(entity.getType());
        dto.setCode(entity.getCode());
        dto.setIcon(entity.getIcon());
        dto.setDescription(entity.getDescription());
        dto.setCreatedTime(entity.getCreatedTime());
        return dto;
    }

    private String generateTemporalNamespace(String orgId, UUID workspaceId) {
        return "org_" + orgId + "_ws_" + workspaceId;
    }

    public WorkspaceListDTO updateActive(UUID id, boolean active) {
        Integer result = workspaceRepository.updateActive(id, active);
        System.out.println("Form active status updated. Result: " + result);
        return findById(id);
    }

    public WorkspaceListDTO findById(UUID id) {
        WorkSpaceEntity entity = workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with ID: " + id));
        return toWorkspaceDto(entity);
    }
}
