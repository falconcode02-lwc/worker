package io.falconFlow.DSL.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.activity.*;
import io.falconFlow.DSL.model.*;
import io.falconFlow.DSL.utils.HandlebarsUtil;
import io.falconFlow.DSL.utils.JQUtils;
import io.falconFlow.DSL.utils.JsonUtils;
import io.falconFlow.DSL.workflow.helpers.ActivityType;
import io.falconFlow.DSL.model.InputMap;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.DSL.workflow.model.WorkflowModel;
import io.falconFlow.DSL.workflow.model.WorkflowResultModel;
import io.falconFlow.model.ai.Parameters;
import io.falconFlow.model.ai.Property;
import io.falconFlow.model.ai.ToolDefinition;
import io.falconFlow.services.isolateservices.StateManagerService;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import org.thymeleaf.util.StringUtils;

@WorkflowImpl
// @WorkflowImpl(taskQueues = "MICROSERVICE_TASK_QUEUE_V2")
public class WorkFlowV2Impl implements IWorkFlowv2 {
  private static final ObjectMapper mapper = new ObjectMapper();

  WorkflowResultModel workflowResultModel = new WorkflowResultModel();
  private final InputMap userInput = new InputMap();
  //  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public WorkflowResultModel runWorkflow(WorkflowModel workFlowWrapper) {

      workflowResultModel.setWorkflowId(Workflow.getInfo().getWorkflowId());
      List<WorkflowResultModel.NodeResult> nodeResultList = new ArrayList<>();

      workflowResultModel.setStatus("Running");
      workflowResultModel.setStartTime(Timestamp.valueOf(LocalDateTime.now()));

    List<WorkflowModel.Node> nodes = workFlowWrapper.getWorkflow();
    final StateModel state = new StateModel();
    // Map for quick lookup
    Map<String, WorkflowModel.Node> nodeMap =
        nodes.stream().collect(Collectors.toMap(WorkflowModel.Node::getId, n -> n));
    String nextId = nodes.get(0).getId(); // start node
      InputMap previousResult = new InputMap();

    while (nextId != null) {
        WorkflowResultModel.NodeResult nodeResult = new WorkflowResultModel.NodeResult();
        nodeResult.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
      WorkflowModel.Node step = nodeMap.get(nextId);

      if (step == null) {
        Workflow.getLogger(getClass()).warn("No step found for id {}", nextId);
        break;
      }

    nodeResult.setId(step.getId());
    nodeResult.setStatus("Running");
    nodeResult.setName(step.getName());
    nodeResult.setType(step.getType().name());
    Stream<WorkflowResultModel.NodeResult> existsNodeResult = nodeResultList.stream().filter(a->a.getId().equals(step.getId()));
    Optional<WorkflowResultModel.NodeResult> optionalResult =    existsNodeResult.findAny();
    if(optionalResult.isPresent()){
        nodeResult = optionalResult.get();
        nodeResult.setStatus("Running");
    }else {
        nodeResultList.add(nodeResult);
    }

      if (step.getType().equals(ActivityType.function) || step.getType().equals(ActivityType.plugin)) {

              List<FunctionResponse> res =
                      (List<FunctionResponse>) executeNode(nodeResultList, nodeMap, step, workFlowWrapper.getInput(), state, previousResult);
              previousResult = new InputMap();
              if (res != null && res.size() > 0) {
                  for (int i = 0; i < res.size(); i++) {
                      FunctionResponse res1 = res.get(i);
                      if (res1.getState() != null) {
                          state.setStateValue(res1.getState().getStateValue());
                      }
                      if(res1.getNext() != null){
                          previousResult.putAll(res1.getNext());
                      }
                  }
              }
              nextId = step.getNext();
              nodeResult.setStatus("Success");

      }
      else if (step.getType().equals(ActivityType.condition)) {
        ConditionResponse res =
            (ConditionResponse) executeNode(nodeResultList, nodeMap, step, workFlowWrapper.getInput(), state, previousResult);
        previousResult = new InputMap();
        if(res !=null) {
            if (res.getStatus() != null && res.getStatus().equals(ConditionStatus.TRUE)) {
                nextId = step.getNext();
                nodeResult.setStatus("Success");
            } else if (res.getStatus() != null && res.getStatus().equals(ConditionStatus.FALSE)) {
                nextId = step.getNextFalse();

                nodeResult.setStatus("Success");
            }
            if (res.getState() != null) {
                if(res.getState().getStateValue()!=null) {
                    state.setStateValue(res.getState().getStateValue());
                }
            }
        }else{
            nextId = step.getNextFalse();
            nodeResult.setStatus("Success");
        }
      }
      else if (step.getType().equals(ActivityType.switches)) {
          ConditionResponse res =
                  (ConditionResponse) executeNode(nodeResultList, nodeMap, step, workFlowWrapper.getInput(), state, previousResult);
          previousResult = new InputMap();
          if(res !=null && res.getMetaData() != null) {
              nextId = res.getMetaData().toString();
              nodeResult.setStatus("Success");
          }
      }else  if (step.getType().equals(ActivityType.aiagent)) {



          FunctionResponse res = (FunctionResponse)executeNode(nodeResultList, nodeMap, step, workFlowWrapper.getInput(), state, previousResult);
          previousResult = new InputMap();



          state.setStateValue(res.getState().getStateValue());
          nodeResult.setStatus("Success");
          nextId = step.getNext();
      }
      else if (step.getType().equals(ActivityType.signal)) {
              final Supplier<Boolean> func =
                      () -> {
                          try {
                              String conditionInline = step.getConditionInline();
                              ConditionResponse result = inlineConditionExecute(state, workFlowWrapper.getInput(),null,conditionInline);
                              Workflow.getLogger(getClass()).info("✅ Condition [{}] result: {}", conditionInline, result.getStatus());
                              return result.getStatus().equals(ConditionStatus.TRUE);
                          } catch (Exception e) {
                              Workflow.getLogger(getClass()).error("Error evaluating condition", e);
                              return false;
                          }
                      };
              Workflow.await(func);
              nextId = step.getNext();
          nodeResult.setStatus("Success");
      }
      else if (step.getType().equals(ActivityType.wait)) {
          long timeout = step.getConfig().getWaitSeconds().longValue();
          Workflow.sleep(Duration.ofSeconds(timeout));
          nextId = step.getNext();
          nodeResult.setStatus("Success");
          Workflow.getLogger(JsonWorkflowImpl.class).info("Waited {} seconds (timer)", timeout);
      }else if (step.getType().equals(ActivityType.childwf)) {

          List<FunctionResponse> res =
                  (List<FunctionResponse>) executeNode(nodeResultList, nodeMap, step, workFlowWrapper.getInput(), state, previousResult);
          previousResult = new InputMap();
          if (res != null && res.size() > 0) {
              for (int i = 0; i < res.size(); i++) {
                  FunctionResponse res1 = res.get(i);
                  if (res1.getState() != null) {
                      state.setStateValue(res1.getState().getStateValue());
                  }
              }
          }
          nextId = step.getNext();
          nodeResult.setStatus("Success");

          nextId = step.getNext();
        }
      else{
          nextId = step.getNext();
          nodeResult.setStatus("Success");
      }

      nodeResult.setEndTime(Timestamp.valueOf(LocalDateTime.now()));

      workflowResultModel.setNodeResultList(nodeResultList);
        System.out.println(JsonUtils.toJson(nodeResultList));
      // 🧠 Give Temporal a deterministic yield
      Workflow.sleep(Duration.ZERO);
        nodeResult.setRunCounter(nodeResult.getRunCounter()+1);

    }
    Workflow.getLogger(getClass()).info("✅ Workflow execution completed successfully.");
      workflowResultModel.setEndTime(Timestamp.valueOf(LocalDateTime.now()));
      workflowResultModel.setStatus("Completed");
     return workflowResultModel;
  }

  private Object executeNode(List<WorkflowResultModel.NodeResult> nodeResultList, Map<String, WorkflowModel.Node> nodeMap, WorkflowModel.Node step, InputMap input, StateModel state, Map previous) {
      ActivityType type = step.getType();
      String conditionalCall = step.getConditionCall();
      String conditionInline = step.getConditionInline();
      boolean isConditionInline = false;

      if (conditionInline != null && !conditionInline.isEmpty()) {
          conditionalCall = null;
          isConditionInline = true;
      }

      Workflow.getLogger(getClass())
              .info(
                      "▶️ Executing Node [{}] type={} call={} callInline={}",
                      step.getId(),
                      type,
                      conditionalCall,
                      conditionInline);

      if ((conditionalCall == null || conditionalCall.isEmpty()) &&
              !isConditionInline && !type.equals(ActivityType.function)
              && !type.equals(ActivityType.plugin)
              && !type.equals(ActivityType.switches) && !type.equals(ActivityType.aiagent)) {
          Workflow.getLogger(getClass()).warn("Skipping node {} — no call defined.", step.getId());
          return null;
      }

      // Retry/timeout setup
      WorkflowModel.Config cfg = step.getConfig();
      Duration timeout =
              Duration.ofSeconds(
                      cfg != null && cfg.getTimeoutSeconds() != null ? cfg.getTimeoutSeconds() : 60);

      RetryOptions retryOptions =
              RetryOptions.newBuilder()
                      .setMaximumAttempts(cfg != null ? cfg.getMaximumAttempts() : 3)
                      .setInitialInterval(
                              Duration.ofSeconds(cfg != null ? cfg.getInitialIntervalSeconds() : 5))
                      .setMaximumInterval(
                              Duration.ofSeconds(cfg != null ? cfg.getMaximumIntervalSeconds() : 10))
                      .setBackoffCoefficient(cfg != null ? cfg.getBackoffCoefficient() : 2.0)
                      .build();

      ActivityOptions options =
              ActivityOptions.newBuilder()
                      .setSummary(type + "::" + (isConditionInline ? "Inline Condition" : conditionalCall))
                      .setStartToCloseTimeout(timeout)
                      .setRetryOptions(retryOptions)
                      .build();

      // Prepare request object
      FRequest req = new FRequest();
      req.setWorkflowId(Workflow.getInfo().getWorkflowId());
      req.setActivityId(step.getId());
      req.setInput(input);
      req.setPrevious(previous);
      req.setUserInput(userInput);
      if( type.equals(ActivityType.plugin)){


          try {
              req.setPluginProps( (InputMap) HandlebarsUtil.applyTemplateToMap(
                      step.getPluginprop(),
                      getContext(state, input, req.getPrevious())
              ));
          } catch (Exception e) {
              throw new RuntimeException(e);
          }

      }else{
          req.setPluginProps(step.getPluginprop());
      }

      req.setState(state);
      req.setCall(conditionalCall);
      req.setTimeStamp(Date.from(Instant.now()));
      req.setMetaData(step.getMetaData());

      // Select and call the correct Activity
      if (type.equals(ActivityType.function) || type.equals(ActivityType.plugin)) {
          // FunctionActivity activity = Workflow.newActivityStub(FunctionActivity.class, options);
          // Workflow.getLogger(getClass()).info("✅ Function [{}] initiate: ", conditionalCall);
          //FunctionResponse result = activity.callFunction(req);
          // Workflow.getLogger(getClass()).info("✅ Function [{}] result: {}", conditionalCall, result.getMessage());
          return executeCallNode(nodeMap, step, req, retryOptions, timeout);

      } else if(type.equals(ActivityType.aiagent)){
            // ReAct Loop for Tool Chaining
            int maxIterations = 1; // Prevent infinite loops
            StringBuilder conversationHistory = new StringBuilder();
            FunctionResponse finalResponse = null;

            String currentPrompt = req.getPluginProps().get("prompt").toString();
            WorkflowModel.Node model = nodeMap.get(step.getAi().getAimodel());

            // Setup AI model node result tracking
            WorkflowResultModel.NodeResult nodeResultAI = new WorkflowResultModel.NodeResult();
            Stream<WorkflowResultModel.NodeResult> existsNodeResult = nodeResultList.stream().filter(a -> a.getId().equals(model.getId()));
            Optional<WorkflowResultModel.NodeResult> optionalResult = existsNodeResult.findAny();
            if (optionalResult.isPresent()) {
                nodeResultAI = optionalResult.get();
                nodeResultAI.setStatus("Running");
            } else {
                nodeResultList.add(nodeResultAI);
            }
            nodeResultAI.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
            nodeResultAI.setId(model.getId());
            nodeResultAI.setStatus("Running");
            nodeResultAI.setName(model.getName());
            nodeResultAI.setType(model.getType().name());

            // Build tool definitions
            List<String> aitools = step.getAi().getAitool();
            List<Map> toolsR = new ArrayList<>();
            Map<String, String> toolMap = new HashMap<>();
            for (String tool : aitools) {
                WorkflowModel.Node tl = getStepById(tool, nodeMap);
                tl.getResource().getProperties().remove("arg0");
                String d = "{\"name\": \"\",\"description\": \"\",\"parameters\": {\"type\": \"object\",\"properties\": {},\"required\": []}}";
                Map toolDef = io.falconFlow.services.isolateservices.JsonUtils.jsonToObj(d, Map.class);
                toolDef.put("name", tl.getResource().getName());
                toolDef.put("description", tl.getResource().getDescription());
                Map<String, Map<String, Map<String, String>>> props =
                        (Map<String, Map<String, Map<String, String>>>) toolDef.get("parameters");
                List<String> required = new ArrayList<>();
                for (Map.Entry<String, Map<String, String>> entr : tl.getResource().getProperties().entrySet()){
                        if(entr.getValue().containsKey("required")){
                            if(Boolean.valueOf(entr.getValue().get("required"))){
                                required.add(entr.getKey());
                            }
                            entr.getValue().remove("required");
                        }
                }
                props.put("properties", tl.getResource().getProperties());
                ((Map<String, Object>)toolDef.get("parameters")).put("required", required);

                toolsR.add(toolDef);
                toolMap.put(tl.getResource().getName(), tl.getId());
            }

            String toolStringR = JsonUtils.toJson(toolsR);
            toolStringR = "<TOOLS>" + toolStringR + "</TOOLS>";
            
            // Initialize conversation with goal
            conversationHistory.append("Goal: ").append(currentPrompt).append("\n\n");

            // ReAct Loop
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                Workflow.getLogger(getClass()).info("🔄 AI Agent Iteration {}/{}", iteration + 1, maxIterations);

                // Prepare AI request with conversation history
                String aimodel = model.getPluginprop().get("model").toString();
                HashMap<String, Object> aiparams = new HashMap<>();
                aiparams.put("model", aimodel);
                aiparams.put("prompt", conversationHistory.toString());
                aiparams.put("systemprompt", req.getPluginProps().get("systemprompt"));
                
                req.setCall(model.getCall()[0]);
                req.setMetaData(aiparams);
                req.setPluginProps(model.getPluginprop());
                req.setAiToolDef(toolStringR);

                // Call AI model
                options = ActivityOptions.newBuilder(options).setSummary(type + "::" + model.getName() + " (iter " + (iteration + 1) + ")").build();
                AIActivity activity = Workflow.newActivityStub(AIActivity.class, options);
                Workflow.getLogger(getClass()).info("✅ AI Function [{}] iteration {}: ", req.getCall(), iteration + 1);
                FunctionResponse aiResponse = activity.callAI(req);
                
                // Parse tool calls from AI response
                java.util.List<AIActivityImpl.ToolCall> toolCalls = AIActivityImpl.parseToolCalls(aiResponse.getNext().get("response").toString());
                
                // Check if AI provided final answer (no tool calls)
                if (toolCalls == null || toolCalls.isEmpty()) {
                    Workflow.getLogger(getClass()).info("✅ AI provided final answer at iteration {}", iteration + 1);
                    finalResponse = aiResponse;
                    break; // Exit loop - we have final answer
                }

                Workflow.getLogger(getClass()).info("🔧 Executing {} tool(s) at iteration {}", toolCalls.size(), iteration + 1);

                // Execute all tool calls and collect results
                StringBuilder toolResults = new StringBuilder();
                toolResults.append("Tool execution results:\n");

                for (AIActivityImpl.ToolCall toolCall : toolCalls) {
                    String toolID = toolMap.get(toolCall.getTool());

                    if (toolID == null) {
                        Workflow.getLogger(getClass()).warn("⚠️ Tool '{}' not found in tool map", toolCall.getTool());
                        toolResults.append("- Tool '").append(toolCall.getTool()).append("' not found\n");
                        continue;
                    }

                    Workflow.getLogger(getClass()).info("🔨 Executing tool: {} with args: {}", toolCall.getTool(), toolCall.getArgs());

                    // Setup tool node
                    WorkflowModel.Node toolNode = nodeMap.get(toolID);
                    
                    // IMPORTANT: Create a copy of pluginProps to avoid modifying the shared node
                    InputMap toolPluginProps = new InputMap();
                    toolPluginProps.putAll(toolNode.getPluginprop());
                    toolPluginProps.putAll(toolCall.getArgs());

                    // Track tool execution in results
                    WorkflowResultModel.NodeResult nodeResultTool = new WorkflowResultModel.NodeResult();
                    Stream<WorkflowResultModel.NodeResult> existsNodeResultTool = nodeResultList.stream().filter(a -> a.getId().equals(toolID));
                    Optional<WorkflowResultModel.NodeResult> optionalToolResult = existsNodeResultTool.findAny();
                    if (optionalToolResult.isPresent()) {
                        nodeResultTool = optionalToolResult.get();
                        nodeResultTool.setStatus("Running");
                    } else {
                        nodeResultList.add(nodeResultTool);
                    }
                    nodeResultTool.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
                    nodeResultTool.setId(toolID);
                    nodeResultTool.setStatus("Running");
                    nodeResultTool.setName(toolNode.getName());
                    nodeResultTool.setType(toolNode.getType().name());

                    // Create tool request
                    FRequest toolReq = new FRequest();
                    toolReq.setWorkflowId(Workflow.getInfo().getWorkflowId());
                    toolReq.setActivityId(toolNode.getId());
                    toolReq.setInput(input);
                    toolReq.setPrevious(previous);
                    toolReq.setUserInput(userInput);
                    toolReq.setPluginProps(toolPluginProps);  // Use the copy instead of modified node
                    toolReq.setState(state);
                    toolReq.setCall(conditionalCall);
                    toolReq.setTimeStamp(Date.from(Instant.now()));
                    toolReq.setMetaData(toolNode.getMetaData());

                    // Execute tool
                    try {
                        List<FunctionResponse> toolResponse = (List<FunctionResponse>) executeCallNode(nodeMap, toolNode, toolReq, retryOptions, timeout);

                        // Collect tool result
                        if (toolResponse != null && !toolResponse.isEmpty()) {
                            FunctionResponse tr = toolResponse.get(0);
                            String resultMessage = tr.getMessage() != null ? tr.getMessage() : "Success";
                            toolResults.append("- ").append(toolCall.getTool()).append(": ").append(resultMessage).append("\n");

                            // Update state if needed
                            if (tr.getState() != null && tr.getState().getStateValue() != null) {
                                state.setStateValue(tr.getState().getStateValue());
                            }
                            
                            nodeResultTool.setStatus("Success");
                        } else {
                            toolResults.append("- ").append(toolCall.getTool()).append(": No response\n");
                            nodeResultTool.setStatus("Success");
                        }
                    } catch (Exception e) {
                        Workflow.getLogger(getClass()).error("❌ Tool execution failed: {}", e.getMessage());
                        toolResults.append("- ").append(toolCall.getTool()).append(": Error - ").append(e.getMessage()).append("\n");
                        nodeResultTool.setStatus("Failed");
                    }

                    nodeResultTool.setEndTime(Timestamp.valueOf(LocalDateTime.now()));
                }

                // Add tool results to conversation history for next iteration
                conversationHistory.append(toolResults.toString()).append("\n");
                Workflow.getLogger(getClass()).info("📝 Tool results added to conversation history");
            }

            // Handle max iterations reached without final answer
            if (finalResponse == null) {
                Workflow.getLogger(getClass()).warn("⚠️ Max iterations ({}) reached without final answer", maxIterations);
                finalResponse = new FunctionResponse();
                finalResponse.setMessage("Max iterations reached without final answer. Last conversation:\n" + conversationHistory.toString());
                finalResponse.setState(state);
                finalResponse.setStatus(FunctionStatus.SUCCESS);
                InputMap nextMap = new InputMap();
                nextMap.put("response", "Max iterations reached");
                finalResponse.setNext(nextMap);
            }

            nodeResultAI.setStatus("Success");
            nodeResultAI.setEndTime(Timestamp.valueOf(LocalDateTime.now()));

            return finalResponse;
        // here we need to write response to the main activity

      }
      else if (type.equals(ActivityType.condition)) {

          ConditionResponse result = null;
          if (isConditionInline) {

              Workflow.getLogger(getClass()).info("✅ Condition [{}] initiate: ", conditionInline);

              try {


                  result = inlineConditionExecute(req.getState(), req.getInput(),previous, conditionInline);


                  boolean status = result.getStatus().equals(ConditionStatus.TRUE);
                  options = ActivityOptions.newBuilder(options).setSummary(type + "::" + (status? "TRUE" : "FALSE")).build();
                  IConditionEntryActivity activity =
                          Workflow.newActivityStub(IConditionEntryActivity.class, options);
                  activity.callConditionEntry(
                          conditionInline, status);
                  Workflow.getLogger(getClass()).info("✅ Condition [{}] result: {}", conditionInline, result.getStatus());

              } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
              }

          } else {
              ConditionActivity activity = Workflow.newActivityStub(ConditionActivity.class, options);
              Workflow.getLogger(getClass()).info("✅ Condition [{}] initiate: ", conditionalCall);
              result = activity.callCondition(req);
              Workflow.getLogger(getClass()).info("✅ Condition [{}] result: {}", conditionalCall, result.getMessage());

          }
          return result;


      }
      else if (type.equals(ActivityType.switches)) {
          ConditionResponse result = null;
          try {

              WorkflowModel.Switches[] list = step.getSwitchval();
//              System.out.println(list);
              WorkflowModel.Switches _sw = null;
              WorkflowModel.Switches _defaultSW = null;

              boolean status = false;
              for (WorkflowModel.Switches sw : list) {

                  if(sw.getIdx() == -1){
                      _defaultSW = sw;
                      continue;
                  }
                  _sw = sw;
                  result = inlineConditionExecute(req.getState(), req.getInput(), previous, sw.getValue());
                  status = result.getStatus().equals(ConditionStatus.TRUE);
                  if (status) {
                        break;
                  }
              }

              if(!status){
                  _sw = _defaultSW;
                  status = true;

              }
              conditionInline = _sw.getValue();

              options = ActivityOptions.newBuilder(options).setSummary(type + "::" + _sw.getKey()).build();
              result.setMetaData(_sw.getNext().length > 0 ? _sw.getNext()[0] : "");
              IConditionEntryActivity activity =
                      Workflow.newActivityStub(IConditionEntryActivity.class, options);
              activity.callConditionEntry(conditionInline, status);
              Workflow.getLogger(getClass()).info("✅ Switch [{}] result: {}", _sw.getValue(), result.getStatus());
              return result;
          }
          catch (JsonProcessingException e) {
              throw new RuntimeException(e);
          }

      }

      Workflow.getLogger(getClass()).warn("⚠️ Unknown type '{}' for step {}", type, step.getId());
      return null;

  }

  private Object executeCallNode(Map<String, WorkflowModel.Node> nodeMap, WorkflowModel.Node step, FRequest request, RetryOptions retryOptions, Duration timeout){
      ActivityType type = step.getType();
      String[] call = step.getCall();

      if(call == null && call.length == 0){
          return null;
      }

      List<Promise<FunctionResponse>> promises = new ArrayList<>();
      for (String func : call) {
          request.setCall(func);
          ActivityOptions options =
                  ActivityOptions.newBuilder()
                          .setSummary(type + "::" + func)
                          .setStartToCloseTimeout(timeout)
                          .setRetryOptions(retryOptions)
                          .build();
              FunctionActivity activity = Workflow.newActivityStub(FunctionActivity.class, options);
              Promise<FunctionResponse> fn = Async.function(activity::callFunction, request);
              Workflow.getLogger(getClass()).info("✅ Function [{}] adding in promises: ", func);
              promises.add(fn);
      }
      Promise.allOf(promises).get();
      List<FunctionResponse> results = new ArrayList<>();
      for (Promise<FunctionResponse> p : promises) {
          results.add(p.get());
      }

      Workflow.getLogger(getClass())
              .info(
                      "▶️ Executing Node [{}] type={} call={}",
                      step.getId(),
                      type,
                      StringUtils.join(call,",")
                      );
      return  results;
  }

  private ConditionResponse inlineConditionExecute(StateModel stateModel,InputMap inputMap,Map previous, String inlineCondition)
          throws JsonProcessingException  {
      ConditionResponse c = new ConditionResponse();

    if (runExpression(stateModel, inputMap, previous, inlineCondition)) {
      c.setStatus(ConditionStatus.TRUE);
    } else {
      c.setStatus(ConditionStatus.FALSE);
    }
    c.setMessage("Inline Expression");

    return c;
  }

  private boolean runExpression(StateModel stateModel,InputMap inputMap,Map previous, String inlineCondition)throws JsonProcessingException {

      String inputJSON =
              mapper.writeValueAsString(getContext(stateModel, inputMap, previous));
      System.out.println(inputJSON );
      return JQUtils.evaluateJq(inputJSON, inlineCondition);
  }

  private Map<String, Object> getContext(StateModel stateModel,InputMap inputMap,Map previous){
      StateManagerService stateManagerService = new StateManagerService(stateModel);
      HashMap<String, Object> map =new HashMap<>();
      map.put("state", stateManagerService.getStateDecrypted().getStateValue());
      map.put("input", inputMap);
      map.put("userInput", userInput);
      map.put("previous", previous);

      return map;
  }

  private WorkflowModel.Node getStepById(String id, Map<String, WorkflowModel.Node> nodeMap){
      return nodeMap.get(id);
  }

  private String getTool(List<String> nodeIds, Map<String, WorkflowModel.Node> nodeMap){



      return "";
  }


  @Override
  public void signal(InputMap userInputD) {
      if(userInputD!=null) {
          userInput.putAll(userInputD);
      }
      Workflow.getLogger(getClass()).info("Signal received with data: {}", userInputD);
  }

    @Override
    public WorkflowResultModel getStatus() {
        return workflowResultModel;
    }
//
//    private void handleWait(WorkflowModel.Node step,  StateModel stateModel, InputMap input) {
//
//           if(!step.getConditionInline().isEmpty()){
//               String condition = step.getConditionInline().isEmpty() ? "." : step.getConditionInline();
//               final Supplier<Boolean> func =
//                       () -> {
//                           try {
//                               return runExpression(stateModel, input, condition); // re-evaluate dynamically
//                           } catch (Exception e) {
//                               Workflow.getLogger(getClass()).error("Error evaluating condition", e);
//                               return false;
//                           }
//                       };
//               Workflow.await(func);
//
//
//           }else if(step.getCall() != null && !step.getCall().isEmpty()) {
//               FRequest req = new FRequest();
//               req.setWorkflowId(Workflow.getInfo().getWorkflowId());
//               req.setActivityId(step.getId());
//               req.setInput(input);
//               req.setState(stateModel);
//               req.setCall(step.getCall());
//               req.setTimeStamp(Date.from(Instant.now()));
//
//
//               ICondition condition1 =
//                       (ICondition)
//                               serviceFetcher.getFunctionByName(Global.getBeanName(step.getCall()));
//               ConditionResponse atResponseParser = condition1.invoke(req);
//
//               final Supplier<Boolean> func =
//                       () -> {
//                           try {
//                               return atResponseParser.getStatus().equals(ConditionStatus.TRUE); // re-evaluate dynamically
//                           } catch (Exception e) {
//                               Workflow.getLogger(getClass()).error("Error evaluating condition", e);
//                               return false;
//                           }
//                       };
//               Workflow.await(func);
//
//           }






//            Workflow.getLogger(getClass())
//                    .info("Signal {} satisfied condition: {}", step.getSignalName(), condition);

   // }



}
