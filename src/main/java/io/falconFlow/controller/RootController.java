package io.falconFlow.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.IWorkFlowv2;
import io.falconFlow.DSL.workflow.JsonWorkflow;
import io.falconFlow.DSL.workflow.helpers.WorkFlowWrapper;
import io.falconFlow.DSL.workflow.model.WorkflowModel;
import io.falconFlow.core.DynamicApiRegistry;
import io.falconFlow.dto.GetFileListProjection;
import io.falconFlow.entity.LoanApplication;
import io.falconFlow.services.genservice.FunctionService;
import io.falconFlow.services.genservice.LoanApplicationService;
import io.falconFlow.services.workflow.TemporalHistoryReader;
import io.falconFlow.services.workflow.WorkflowStatusCountService;
import io.falconFlow.entity.WorkSpaceEntity;
import io.falconFlow.repository.WorkspaceRepository;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.history.v1.History;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RootController {

  @Autowired WorkflowClient client;

  @Autowired LoanApplicationService loanApplicationService;

  @Autowired FunctionService functionService;

  @Autowired WorkspaceRepository workspaceRepository;

  WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

  @GetMapping("/hello")
  public String hello(Model model) {
    model.addAttribute("sample", "Say Hello");
    return "hello";
  }

  @GetMapping("/sample")
  public ResponseEntity sample(String data) {

    return new ResponseEntity<>("\" Message workflow completed\"", HttpStatus.OK);
  }

  @PostMapping(
      value = "/dynamic",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.TEXT_HTML_VALUE})
  ResponseEntity dynamic(@RequestBody String jsonFlow) throws JsonProcessingException {

    String id = String.valueOf(System.currentTimeMillis());
    LoanApplication l = new LoanApplication();
    // l.setId(Long.parseLong(id));
    l.setApplicantName("Pratik Naik");
    l.setStatus("PENDING");
    l.setLoanAmount(0.00);
    l = loanApplicationService.create(l);

    ObjectMapper mapper = new ObjectMapper();
    WorkFlowWrapper workflowJson = mapper.readValue(jsonFlow, WorkFlowWrapper.class);

    WorkflowClient client = WorkflowClient.newInstance(service);

    InputMap m = new InputMap();
    m.put("id", l.getId());
    workflowJson.setInput(m);
    JsonWorkflow workflow =
        client.newWorkflowStub(
            JsonWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(String.valueOf(l.getId()))
                .setTaskQueue(workflowJson.getTaskQueue())
                .build());

    // This starts the workflow asynchronously (does NOT block)
    WorkflowClient.start(workflow::runWorkflow, workflowJson);

    // You can continue immediately without waiting
    System.out.println("Workflow started: ");

    // bypass thymeleaf, don't return template name just result
    return new ResponseEntity<>("WorkFlowId - " + id, HttpStatus.OK);
  }

  @PostMapping(
      value = "/dynamicv2",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<LoanApplication> dynamicV2(@RequestBody String jsonFlow)
      throws JsonProcessingException {

    //    String id = String.valueOf(System.currentTimeMillis());
    LoanApplication l = new LoanApplication();
    // l.setId(Long.parseLong(id));
    l.setApplicantName("Pratik Naik");
    l.setStatus("PENDING");
    l.setLoanAmount(0.00);
    l = loanApplicationService.create(l);

    ObjectMapper mapper = new ObjectMapper();
    WorkflowModel workflowJson = mapper.readValue(jsonFlow, WorkflowModel.class);

    WorkflowClient client = WorkflowClient.newInstance(service);
    InputMap m = new InputMap();
    m.put("id", l.getId());
    workflowJson.setInput(m);
    IWorkFlowv2 workflow =
        client.newWorkflowStub(
            IWorkFlowv2.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(String.valueOf(l.getId()))
                .setTaskQueue("org_ORG-12345_ws_9cfa1f9a-7468-4f28-ab83-5162f073a9a8")
                .build());

    // This starts the workflow asynchronously (does NOT block)
    WorkflowClient.start(workflow::runWorkflow, workflowJson);

    // bypass thymeleaf, don't return template name just result
    return new ResponseEntity<>(l, HttpStatus.OK);
  }

  @GetMapping("/workflowSignal")
  public ResponseEntity workflowSignal(@Param("id") String id, @Param("signal") String signal) {

    // Create a stub to the existing workflow execution
    JsonWorkflow workflow = client.newWorkflowStub(JsonWorkflow.class, id);

    workflow.signal(signal, null);

    return new ResponseEntity<>("OK", HttpStatus.OK);
  }

  @GetMapping("/getAllFunctions")
  public ResponseEntity getAllFunctions() {

    List<GetFileListProjection> lst = functionService.getListOfFunctions();

    return new ResponseEntity<>(lst, HttpStatus.OK);
  }

  @GetMapping("/getAllFolders")
  public ResponseEntity getAllFolders() {

    List<GetFileListProjection> lst = functionService.getListOfFunctions();

    return new ResponseEntity<>(lst, HttpStatus.OK);
  }

  @PostMapping(
      value = "/activitCall",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<FunctionResponse> dynamic(@RequestBody FRequest reqParser) {

    FunctionResponse aRP = new FunctionResponse();
    aRP.setStatus(FunctionStatus.SUCCESS);
    aRP.setMessage("Successfull");
    aRP.setMetaData("meta Data");
    aRP.setErrorCode("");

    return new ResponseEntity<>(aRP, HttpStatus.OK);
  }

  @GetMapping("/getAllCounts")
  public ResponseEntity getAllCounts(@RequestParam(required = false) String workspaceId) {
    String resolvedNamespace = "default";
    if (workspaceId != null && !workspaceId.isEmpty()) {
      var ws = workspaceRepository.findByCode(workspaceId);
      if (ws.isPresent()) {
        resolvedNamespace = ws.get().getTemporalNamespace();
      }
    }
    WorkflowStatusCountService metrics = new WorkflowStatusCountService(resolvedNamespace);
    HashMap<String, Integer> d = new HashMap<>();
    metrics
        .getWorkflowStatusCounts()
        .forEach(
            (status, count) -> {
              d.put(status.name().replace("WORKFLOW_EXECUTION_STATUS_", ""), count);
            });

    return new ResponseEntity<>(d, HttpStatus.OK);
  }

  @GetMapping("/getHistory")
  public ResponseEntity<String> getHistory(
      @Param("workflowId") String workflowId,
      @RequestParam(required = false) String workspaceId,
      @RequestParam(required = false) String namespace) {

    History data = null;

    String resolvedNamespace = "default";
    if (namespace != null && !namespace.isEmpty()) {
      resolvedNamespace = namespace;
    } else if (workspaceId != null && !workspaceId.isEmpty()) {
      var ws = workspaceRepository.findByCode(workspaceId);
      if (ws.isPresent()) {
        resolvedNamespace = ws.get().getTemporalNamespace();
      }
    }

    WorkflowClient client = WorkflowClient.newInstance(service);
    WorkflowStub stub = client.newUntypedWorkflowStub(workflowId);

    try {
      WorkflowExecutionInfo info = stub.describe().getWorkflowExecutionInfo();
      WorkflowExecutionStatus status = info.getStatus();
      System.out.println(status.name().replace("WORKFLOW_EXECUTION_STATUS_", ""));

    } catch (Exception e) {
      if (e.getCause().getMessage().contains("NOT_FOUND")) {

      } else {

      }
    }

    try {
      TemporalHistoryReader temporalHistoryReader = new TemporalHistoryReader(service, workspaceRepository);
      data = temporalHistoryReader.getFullHistory(resolvedNamespace, workflowId, "");

      String json = JsonFormat.printer().includingDefaultValueFields().print(data);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping()
  public String get() {
    return "WORKED";
  }



    @GetMapping("/api/code/metadata")
    public ResponseEntity<Map<String, Map<String, String>>> getMetadata() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(DynamicApiRegistry.getAll());
    }
}
