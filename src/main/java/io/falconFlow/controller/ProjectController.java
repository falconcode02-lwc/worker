package io.falconFlow.controller;

import io.falconFlow.entity.ProjectEntity;
import io.falconFlow.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    // CREATE - POSTMAN POST
    @PostMapping
    public ResponseEntity<ProjectEntity> create(@RequestBody ProjectEntity project) {
        project.setWorkspaceCode("DEV_WORKSPACE");
        ProjectEntity saved = projectRepository.save(project);
        return ResponseEntity.ok(saved);
    }

    // LIST ALL - POSTMAN GET
    @GetMapping("/workspace/{workspaceCode}")
    public ResponseEntity<List<ProjectEntity>> getByWorkspace(@PathVariable String workspaceCode) {
        List<ProjectEntity> projects = projectRepository.findByWorkspaceCode(workspaceCode);
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
    public ResponseEntity<List<ProjectEntity>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
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
