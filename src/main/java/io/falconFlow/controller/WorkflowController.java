package io.falconFlow.controller;


import io.falconFlow.dto.WorkflowsDTO;
import io.falconFlow.dto.WorkflowsNameDTO;
import io.falconFlow.entity.WorkFlowsEntity;
import io.falconFlow.services.genservice.WorkflowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowsService workflowService;

    @PostMapping
    public WorkFlowsEntity createWorkflow(@RequestBody WorkFlowsEntity dto) {

        if(dto.getId() >  0){
            return workflowService.update(dto.getId(), dto);
        }else{
            dto.setId(null);
        }
        return workflowService.create(dto);
    }

    @GetMapping
    public List<WorkflowsDTO> getAllWorkflows() {
        return workflowService.findAll();
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
    public List<WorkflowsNameDTO> getActiveWorkflows() {
        return workflowService.findAllActive();
    }
}
