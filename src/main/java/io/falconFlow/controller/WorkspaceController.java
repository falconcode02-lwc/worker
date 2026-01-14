package io.falconFlow.controller;

import io.falconFlow.dto.*;
import io.falconFlow.services.workspace.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(
        name = "Workspaces",
        description = "APIs for managing workspaces across organizations"
)
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    // List all workspaces of all organizations
    @Operation(
            summary = "List all workspaces",
            description = "Fetches a paginated list of all workspaces across all organizations"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workspaces fetched successfully")
    })
    @GetMapping("/list")
    public Page<WorkspaceListDTO> getAllWorkspaces(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return workspaceService.listWorkspaces(page, size);
    }

    // List all workspaces of a particular organization
    @Operation(
            summary = "List workspaces by organization",
            description = "Fetches a paginated list of workspaces for a given organization ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workspaces fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    @GetMapping("/list/{orgId}")
    public Page<WorkspaceListDTO> getWorkspaces(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Organization ID", example = "org-123")
            @PathVariable String orgId) {

        return workspaceService.listWorkspaces(page, size, orgId);
    }

    // Create new workspace
    @Operation(
            summary = "Create workspace",
            description = "Creates a new workspace under an organization"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workspace created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "409", description = "Workspace already exists", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<WorkspaceResponseDTO> createWorkspace(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Workspace creation payload",
                    required = true
            )
            @Valid @RequestBody WorkspaceCreateDTO workspaceCreateDTO) {

        WorkspaceResponseDTO response = workspaceService.createWorkspace(workspaceCreateDTO);
        URI location = URI.create("/workspaces/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    // Update existing workspace
    @Operation(
            summary = "Update workspace",
            description = "Updates an existing workspace using its unique ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workspace updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Workspace not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponseDTO> updateWorkspace(
            @Parameter(description = "Workspace ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Workspace update payload",
                    required = true
            )
            @Valid @RequestBody WorkspaceUpdateDTO workspaceUpdateDTO) throws Exception {

        WorkspaceResponseDTO updated =
                workspaceService.updateWorkspace(id, workspaceUpdateDTO);

        return ResponseEntity.ok(updated);
    }

    // Delete workspace
    @Operation(
            summary = "Delete workspace",
            description = "Deletes a workspace permanently using its unique ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workspace deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workspace not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkspace(
            @Parameter(description = "Workspace ID")
            @PathVariable UUID id) throws Exception {

        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }


    // Enable or disable workspace
    @Operation(
            summary = "Update workspace active status",
            description = "Activates or deactivates a workspace"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workspace status updated"),
            @ApiResponse(responseCode = "404", description = "Workspace not found", content = @Content)
    })
    @PutMapping("/{id}/active")
    public WorkspaceListDTO updateActive(
            @Parameter(description = "Workspace ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,

            @Parameter(description = "Active flag", example = "true")
            @RequestParam boolean active) {

        return workspaceService.updateActive(id, active);
    }
}
