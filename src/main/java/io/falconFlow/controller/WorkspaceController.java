package io.falconFlow.controller;


import io.falconFlow.dto.WorkspaceCreateDTO;
import io.falconFlow.dto.WorkspaceListDTO;
import io.falconFlow.dto.WorkspaceResponseDTO;
import io.falconFlow.dto.WorkspaceUpdateDTO;
import io.falconFlow.services.workspace.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    @Autowired
    WorkspaceService workspaceService;

    // List all workspaces of particular organization
    @GetMapping("/list/{orgId}")
    public Page<WorkspaceListDTO> getWorkspaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @PathVariable String orgId) {

        return workspaceService.listWorkspaces(page, size, orgId);
    }

    // Create new workspace
    @PostMapping("/create")
    public ResponseEntity<WorkspaceResponseDTO> createWorkspace(
            @RequestBody WorkspaceCreateDTO workspaceCreateDTO) {

        WorkspaceResponseDTO workspaceResponseDTO = workspaceService.createWorkspace(workspaceCreateDTO);
        URI location = URI.create("/workspaces/" + workspaceResponseDTO.getId());
        return ResponseEntity.created(location).body(workspaceResponseDTO);
    }

    // Update existing workspace
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponseDTO> updateWorkspace(
            @PathVariable UUID id,
            @RequestBody WorkspaceUpdateDTO workspaceUpdateDTO) throws Exception {

        WorkspaceResponseDTO updated =
                workspaceService.updateWorkspace(id, workspaceUpdateDTO);

        return ResponseEntity.ok(updated);
    }

}
