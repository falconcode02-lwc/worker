package io.falconFlow.services.genservice;


import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.dto.GetWorkFlowsProjection;
import io.falconFlow.dto.WorkFlowManagerRequest;
import io.falconFlow.dto.WorkflowsDTO;
import io.falconFlow.dto.WorkflowsNameDTO;
import io.falconFlow.entity.WorkFlowsEntity;
import io.falconFlow.interfaces.IController;
import io.falconFlow.repository.ProjectRepository;
import io.falconFlow.repository.WorkflowsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowsService {

    @Autowired
    private WorkflowsRepository workflowRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired private BeanFetcher serviceFetcher;

    public WorkFlowsEntity create(WorkFlowsEntity entity) {
       return workflowRepository.save(entity);
    }

    public WorkFlowsEntity create(WorkFlowsEntity entity, UUID projectId) {
        if (projectId != null) {
            io.falconFlow.entity.ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
            entity.setProject(project);
        }
        return workflowRepository.save(entity);
    }

    public List<WorkflowsDTO> findAll(String workspaceCode, UUID projectId) {
        return workflowRepository.findAllFiltered(workspaceCode, projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<WorkflowsDTO> findAll() {
        return workflowRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<WorkflowsNameDTO> findAllActive(String workspaceCode, UUID projectId) {
        return workflowRepository.findActiveWorkflowsFiltered(workspaceCode, projectId);
    }

    public List<WorkflowsNameDTO> findAllActive() {
        return workflowRepository.findActiveWorkflows();
    }

    public WorkflowsDTO findById(Integer id) {
        WorkFlowsEntity entity = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
        return mapToDTO(entity);
    }

    public GetWorkFlowsProjection findByCode(String workflowCode) {
        return workflowRepository.findByCode(workflowCode);

    }

    public WorkFlowsEntity update(Integer id, WorkFlowsEntity entity, UUID projectId) {
        WorkFlowsEntity entity1 = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));

        if (projectId != null) {
            io.falconFlow.entity.ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId));
            entity.setProject(project);
        }
        return workflowRepository.save(entity);
    }

    public WorkFlowsEntity update(Integer id, WorkFlowsEntity entity) {
        WorkFlowsEntity entity1 = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));

        return workflowRepository.save(entity);
    }

    public void delete(Integer id) {
       Integer id1 = workflowRepository.updateActive(id, false);
       System.out.println(id1);
    }

    private WorkflowsDTO mapToDTO(WorkFlowsEntity entity) {
        WorkflowsDTO dto = new WorkflowsDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setVersion(entity.getVersion());
        dto.setWorkflowJson(entity.getWorkflowJson());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setController(entity.getController());
        dto.setCode(entity.getCode());
        dto.setWorkflowJsonRaw(entity.getWorkflowJsonRaw());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setModifiedTime(entity.getModifiedTime());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setModifiedBy(entity.getModifiedBy());
        return dto;
    }


    public WorkflowsDTO updateActive(Integer id, boolean active) {
        Integer id1 = workflowRepository.updateActive(id, active);
        System.out.println(id1);
        return new WorkflowsDTO();
    }


    public WorkflowResponseWrapper runController(WorkFlowManagerRequest workFlowManagerRequest){
        WorkflowResponseWrapper responseWrapper  = new WorkflowResponseWrapper();
        CreateResponse res = new CreateResponse();
        String workflowId = workFlowManagerRequest.getWorkflowId() != null && !workFlowManagerRequest.getWorkflowId().isEmpty() ? workFlowManagerRequest.getWorkflowId() : UUID.randomUUID().toString();
        GetWorkFlowsProjection proj =  findByCode(workFlowManagerRequest.getWorkflowCode());

        res.setWorkflowId(workflowId);
        res.setStatus(ControllerStatus.SUCCESS);
        if(proj == null || !proj.getActive()){
            res.setStatus(ControllerStatus.FAILED)  ;
            res.setMessage("Workflow not found!");
            responseWrapper.setCreateResponse(res);
            return responseWrapper;
        }

        // Retrieve workspace namespace from workflow entity
        String workspaceNamespace = null;
        try {
            WorkFlowsEntity entity = workflowRepository.findEntityByCode(proj.getCode())
                    .orElse(null);
            if (entity != null && entity.getProject() != null && entity.getProject().getWorkspace() != null) {
                workspaceNamespace = entity.getProject().getWorkspace().getTemporalNamespace();
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to retrieve workspace namespace: " + e.getMessage());
            // Continue with null namespace (will fall back to default)
        }

        if(proj.getController() != null && !proj.getController().isEmpty()){
            var request = new FCreateRequest();
            request.setWorkflowId(workflowId);
            if(workFlowManagerRequest.getInput() == null){
                InputMap mp = new InputMap();
                request.setInput(mp);
            }else {
                request.setInput(workFlowManagerRequest.getInput());
            }
            System.out.println(proj.getController() + " Bean finding......");
            IController controller =
                    (IController)
                            serviceFetcher.getFunctionByName(Global.getBeanName(proj.getController()));
            res = controller.invokeCreate(request);

            if(res.getInput() == null){
                res.setInput(request.getInput());
            }

            if(res.getWorkflowId() == null){
                res.setWorkflowId(workflowId);
            }

            if (!res.getStatus().equals(ControllerStatus.SUCCESS)) {
                responseWrapper.setCreateResponse(res);
                return responseWrapper;
            }
        }else{
            res.setInput(workFlowManagerRequest.getInput());
            if(workFlowManagerRequest.getState() != null && !workFlowManagerRequest.getState().isEmpty()){
                StateModel m = new StateModel();
                m.setStateValue(workFlowManagerRequest.getState());
                res.setState(m);
            }

            res.setWorkflowId(workflowId);
        }

        responseWrapper.setWorkflowCode(proj.getCode());
        responseWrapper.setWorkflowDefId(proj.getId().toString());
        responseWrapper.setWorkflowJson(proj.getWorkflowJson());
        responseWrapper.setWorkspaceNamespace(workspaceNamespace);
        responseWrapper.setCreateResponse(res);
        return responseWrapper;

    }



}
