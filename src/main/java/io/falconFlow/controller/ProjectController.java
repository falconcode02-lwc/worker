package io.falconFlow.controller;

import io.falconFlow.entity.ProjectEntity;
import io.falconFlow.entity.WorkSpaceEntity;
import io.falconFlow.repository.ProjectRepository;
import io.falconFlow.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private io.falconFlow.services.ProjectService projectService;

    // CREATE - POSTMAN POST
    @PostMapping
    public ResponseEntity<ProjectEntity> create(@RequestBody ProjectEntity project) {
        String workspaceCode = project.getWorkspaceCode();

        
        WorkSpaceEntity workSpace = workspaceRepository.findByCode(workspaceCode)
                .orElseThrow(() -> new RuntimeException("Workspace not found: " + workspaceCode));
        
        project.setWorkspace(workSpace);
        ProjectEntity saved = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // LIST ALL - POSTMAN GET
    @GetMapping("/workspace/{workspaceCode}")
    public ResponseEntity<List<ProjectEntity>> getByWorkspace(
            @PathVariable String workspaceCode,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        List<ProjectEntity> projects = projectService.getByWorkspace(workspaceCode, userId);
        return ResponseEntity.ok(projects);
    }

    // GET ONE - POSTMAN GET
    @GetMapping("/{id}")
    public ResponseEntity<ProjectEntity> getById(@PathVariable UUID id) {
        Optional<ProjectEntity> project = projectRepository.findById(id);
        return project.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping
    public ResponseEntity<List<ProjectEntity>> getAllProjects(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(projectService.getAllProjects(userId));
    }

    // ADD THIS: Start workflow for project (Temporal integration)
    @PostMapping("/{id}/start")
    public ResponseEntity<String> startWorkflow(@PathVariable UUID id) {
        // TODO: Trigger Temporal workflow
        return ResponseEntity.ok("Workflow started for project: " + id);
    }
    // UPDATE - POSTMAN PUT
    @PutMapping("/{id}")
    public ResponseEntity<ProjectEntity> update(@PathVariable UUID id, @RequestBody ProjectEntity projectDetails) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setName(projectDetails.getName());
                    project.setDescription(projectDetails.getDescription());
                    project.setIcon(projectDetails.getIcon());
                    project.setAccessibility(projectDetails.getAccessibility());
                    ProjectEntity updated = projectRepository.save(project);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - POSTMAN DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
