package io.falconFlow.controller;


import io.falconFlow.dto.WorkflowsDTO;
import io.falconFlow.dto.WorkflowsNameDTO;
import io.falconFlow.entity.WorkFlowsEntity;
import io.falconFlow.services.genservice.WorkflowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowsService workflowService;

    @PostMapping
    public WorkFlowsEntity createWorkflow(@RequestBody WorkFlowsEntity dto,
                                        @RequestParam(required = false) UUID projectId) {

        if(dto.getId() != null && dto.getId() >  0){
            return workflowService.update(dto.getId(), dto, projectId);
        }else{
            dto.setId(null);
        }
        return workflowService.create(dto, projectId);
    }

    @GetMapping
    public List<WorkflowsDTO> getAllWorkflows(@RequestParam(required = false) String workspaceId,
                                            @RequestParam(required = false) UUID projectId) {
        return workflowService.findAll(workspaceId, projectId);
    }

    @GetMapping("/{id}")
    public WorkflowsDTO getWorkflowById(@PathVariable Integer id) {
        return workflowService.findById(id);
    }

    @GetMapping("/{id}/active/{active}")
    public WorkflowsDTO updateActive(@PathVariable Integer id, @PathVariable boolean active) {
        return workflowService.updateActive(id, active);
    }

    @PutMapping("/{id}")
    public WorkFlowsEntity updateWorkflow(@PathVariable Integer id, @RequestBody WorkFlowsEntity dto) {
        return workflowService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkflow(@PathVariable Integer id) {
        workflowService.delete(id);
    }

    @GetMapping("/active")
    public List<WorkflowsNameDTO> getActiveWorkflows(@RequestParam(required = false) String workspaceId,
                                                   @RequestParam(required = false) UUID projectId) {
        return workflowService.findAllActive(workspaceId, projectId);
    }
}
