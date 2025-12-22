package io.falconFlow.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.workflow.IWorkFlowv2;
import io.falconFlow.DSL.workflow.model.WorkflowModel;
import io.falconFlow.DSL.workflow.model.WorkflowResultModel;
import io.falconFlow.dto.*;
import io.falconFlow.interfaces.IController;
import io.falconFlow.services.genservice.WorkflowsService;
import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.failure.v1.Failure;
import io.temporal.api.workflow.v1.PendingActivityInfo;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionResponse;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1/workflowManager")
public class WorkflowManagerController {

    @Value("${spring.workflowTaskQueue}")
    private String taskQueue;

    @Autowired
    WorkflowClient client;

    @Autowired
    WorkflowsService workflowsService;

    ObjectMapper mapper = new ObjectMapper();


    @Autowired private BeanFetcher serviceFetcher;

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

    @PostMapping(
            value = "/createWorkflow",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<CreateResponse> createWorkflow(@RequestBody WorkFlowManagerRequest workFlowManagerRequest)
            throws JsonProcessingException {

        WorkflowResponseWrapper responseWrapper = workflowsService.runController(workFlowManagerRequest);
        CreateResponse res = responseWrapper.getCreateResponse();

        if(res.getStatus() != null && res.getStatus().equals(ControllerStatus.FAILED)){
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        WorkflowClient client = WorkflowClient.newInstance(service);
        IWorkFlowv2 workflow =
                client.newWorkflowStub(
                        IWorkFlowv2.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(res.getWorkflowId())
                                .setTaskQueue(taskQueue)
                                .build());


        WorkflowModel workflowNode = mapper.readValue(responseWrapper.getWorkflowJson(),  WorkflowModel.class);
        workflowNode.setId(res.getWorkflowId());
        workflowNode.setInput(res.getInput());
        WorkflowClient.start(workflow::runWorkflow, workflowNode);

                return new ResponseEntity<>(res, HttpStatus.OK);
            }


    @PostMapping(
            value = "/updateWorkflow",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<InputResponse> updateWorkflow(@RequestBody WorkFlowManagerRequest workFlowManagerRequest)
            throws JsonProcessingException {
        InputResponse res = new InputResponse();

        if(workFlowManagerRequest.getWorkflowId()== null || workFlowManagerRequest.getWorkflowId().isEmpty() ){
            res.setStatus(ControllerStatus.FAILED)  ;
            res.setMessage("Workflow id if required!");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkflowStub stub = client.newUntypedWorkflowStub(workFlowManagerRequest.getWorkflowId());

        try {
            WorkflowExecutionInfo info = stub.describe().getWorkflowExecutionInfo();
            WorkflowExecutionStatus status = info.getStatus();
            if(!status.name().equalsIgnoreCase("WORKFLOW_EXECUTION_STATUS_RUNNING")){
                res.setErrorCode("401");
                res.setStatus(ControllerStatus.FAILED)  ;
                res.setMessage("Not allowed to perform action! Workflow status is " + status.name());
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            if (e.getCause().getMessage().contains("NOT_FOUND")) {
                res.setErrorCode("404");
                res.setStatus(ControllerStatus.FAILED)  ;
                res.setMessage("Workflow id not found!");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            } else {
                res.setErrorCode("500");
                res.setStatus(ControllerStatus.FAILED)  ;
                res.setMessage(e.getMessage());
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        }

//        GetWorkFlowsProjection proj =  workflowsService.findByCode(workFlowManagerRequest.getWorkflowCode());
//        WorkflowModel workflowNode = mapper.readValue(proj.getWorkflowJson(),  WorkflowModel.class);
//        Optional<WorkflowModel.Node> nd = workflowNode.getWorkflow().stream().filter(a -> a.getSignalName() != null && a.getSignalName().equals(workFlowManagerRequest.getSignalName())).findFirst();
//        if(nd.isEmpty()){
//            res.setErrorCode("404");
//            res.setStatus(ControllerStatus.FAILED)  ;
//            res.setMessage("No signal name is exists");
//            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//        }
        GetWorkFlowsProjection proj =  workflowsService.findByCode(workFlowManagerRequest.getWorkflowCode());
        if(proj.getController() != null && !proj.getController().isEmpty()){
            var request = new FInputRequest();
            request.setWorkflowId(workFlowManagerRequest.getWorkflowId());
            if(workFlowManagerRequest.getUserInput() == null){
                InputMap mp = new InputMap();
                request.setUserInput(mp);
            }else {
                request.setUserInput(workFlowManagerRequest.getUserInput());
            }

            // calling controller
            IController controller =
                    (IController)
                            serviceFetcher.getFunctionByName(Global.getBeanName(proj.getController()));
            res = controller.invokeInput(request);

            if(res.getUserInput() == null){
                res.setUserInput(request.getUserInput());
            }
            if (!res.getStatus().equals(ControllerStatus.SUCCESS)) {
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        }

        stub.signal("signal", res.getUserInput());

//
//
//
//        WorkflowModel workflowNode = mapper.readValue(proj.getWorkflowJson(),  WorkflowModel.class);
//        workflowNode.setId(res.getWorkflowId());
//        workflowNode.setInput(res.getInput());
//        WorkflowClient.start(workflow::runWorkflow, workflowNode);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }





    @GetMapping("/list")
    public ResponseEntity<PaginatedResponse> getWorkflows(
            @RequestParam(defaultValue = "default") String namespace,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String nextPageToken
    ) {


        ListWorkflowExecutionsRequest.Builder reqBuilder = ListWorkflowExecutionsRequest.newBuilder()
                .setNamespace(namespace)
                .setPageSize(pageSize)
                .setQuery(query);

        if (nextPageToken != null && !nextPageToken.isEmpty()) {
            reqBuilder.setNextPageToken(com.google.protobuf.ByteString.copyFromUtf8(nextPageToken));
        }

        ListWorkflowExecutionsResponse response = service.blockingStub().listWorkflowExecutions(reqBuilder.build());

        List<WorkflowItem> items = response.getExecutionsList().stream()
                .map(info -> new WorkflowItem(
                        info.getExecution().getWorkflowId(),
                        info.getExecution().getRunId(),
                        info.getType().getName(),
                        info.getStatus().name(),
                        info.hasStartTime() ? new Date(info.getStartTime().getSeconds() * 1000) : null,
                        info.hasCloseTime() ? new Date(info.getCloseTime().getSeconds() * 1000) : null
                ))
                .collect(Collectors.toList());

        String nextToken = response.getNextPageToken().toStringUtf8();

        return new ResponseEntity<>(new PaginatedResponse(items, nextToken), HttpStatus.OK);
    }


    @GetMapping("/terminate")
    public ResponseEntity<ActionStatus> terminateWorkflow(
            @RequestParam(defaultValue = "default") String workflowId,
                @RequestParam(defaultValue = "") String reason
    ) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);


        // Hard terminate the workflow
        WorkflowStub stb = client.newUntypedWorkflowStub(workflowId);
        ActionStatus a = new ActionStatus();
        try {
            stb.terminate(reason);
            a.setStatus("SUCCESS");
        }catch (Exception ex){
            a.setStatus("FAILED");
            a.setErrorCode("101");
            a.setErrorMessage(ex.getMessage());
        }

        System.out.println("Workflow terminated.");


        return new ResponseEntity<>(a, HttpStatus.OK);
    }


    @GetMapping("/getStepsStatus")
    public ResponseEntity<ActionStatus> getStepsStatus(
            @RequestParam(defaultValue = "default") String workflowId
    ) {
        ActionStatus res = new ActionStatus();
        if(workflowId== null || workflowId.isEmpty() ){
            res.setStatus("FAILED")  ;
            res.setErrorMessage("Workflow id if required!");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkflowStub stub = client.newUntypedWorkflowStub(workflowId);

        try {
            WorkflowExecutionInfo info = stub.describe().getWorkflowExecutionInfo();
            WorkflowExecutionStatus status = info.getStatus();
            System.out.println(status.name());
//            if(!status.name().equalsIgnoreCase("WORKFLOW_EXECUTION_STATUS_RUNNING")){
//                res.setErrorCode("401");
//                res.setStatus(ControllerStatus.FAILED.name())  ;
//                res.setErrorMessage("Not allowed to perform action! Workflow status is " + status.name());
//                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
//            }




        } catch (Exception e) {
            if (e.getCause().getMessage().contains("NOT_FOUND")) {
                res.setErrorCode("404");
                res.setStatus("FAILED")  ;
                res.setErrorMessage("Workflow id not found!");
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            } else {
                res.setErrorCode("500");
                res.setStatus("FAILED")  ;
                res.setErrorMessage(e.getMessage());
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        }
        WorkflowResultModel result =  stub.query("getStatus", WorkflowResultModel.class);
        List<WorkflowResultModel.NodeResult> nodeResultList = result.getNodeResultList();
        DescribeWorkflowExecutionRequest req =  DescribeWorkflowExecutionRequest.newBuilder()
                .setNamespace("default").setExecution(
                        WorkflowExecution.newBuilder().setWorkflowId(workflowId))
                .build();

        DescribeWorkflowExecutionResponse resp =
                service.blockingStub().describeWorkflowExecution(req);

        List<PendingActivityInfo> pending = resp.getPendingActivitiesList();

        PendingActivityInfo failed = pending.stream()
                .filter(PendingActivityInfo::hasLastFailure)
                .findFirst()
                .orElse(null);

        if (failed != null) {
            Failure lastFailure = failed.getLastFailure();
            WorkflowResultModel.NodeResult lastNode =  nodeResultList.get(nodeResultList.size()-1);

            lastNode.setStatus("FAILED");
            lastNode.setError( lastFailure.getMessage() +"\n"+ lastFailure.getCause().getMessage() + "\n" + lastFailure.getCause().getStackTrace());
            lastNode.setRunCounter(failed.getAttempt());
//            Map.of(
//                    "activityType", failed.getActivityType().getName(),
//                    "attempt", failed.getAttempt(),
//                    "message", lastFailure.getMessage(),
//                    "stack", lastFailure.getStackTrace()
//            );
        }

        res.setStatus("SUCCESS");
        res.setResult(result);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


}
