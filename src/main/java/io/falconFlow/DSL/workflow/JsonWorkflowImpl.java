package io.falconFlow.DSL.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.falconFlow.DSL.activity.FunctionActivity;
import io.falconFlow.DSL.activity.MicroserviceActivity;
import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;
import io.falconFlow.DSL.model.SignalModel;
import io.falconFlow.DSL.utils.JsonUtils;
import io.falconFlow.DSL.workflow.helpers.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// @WorkflowImpl(taskQueues = "MICROSERVICE_TASK_QUEUE_1")
public class JsonWorkflowImpl implements JsonWorkflow {
  ObjectMapper mapper = new ObjectMapper();

  public void setSignal(SignalModel signal) {
    signalList.put(signal.getSignalName(), signal);
  }

  private final Map<String, SignalModel> signalList = new HashMap<>();

  @PostConstruct
  void init() {
    System.out.println("test");
  }

  @Override
  public void runWorkflow(WorkFlowWrapper workFlowWrapper) {

    WorkFlowItem[] workFlowItems = workFlowWrapper.getSteps();

    for (WorkFlowItem step : workFlowItems) {
      ActivityType type = step.getType();

      if (type.equals(ActivityType.activity) || type.equals(ActivityType.function)) {

        RetryPolicy retryPolicy = step.getRetryPolicy();
        if (retryPolicy == null) {
          ///
        }

        RetryOptions retryOptions =
            RetryOptions.newBuilder()
                .setMaximumAttempts(retryPolicy.getMaximumAttempts())
                .setInitialInterval(Duration.ofSeconds(retryPolicy.getInitialIntervalSeconds()))
                .setMaximumInterval(Duration.ofSeconds(retryPolicy.getMaximumIntervalSeconds()))
                .setBackoffCoefficient(retryPolicy.getBackoffCoefficient())
                .build();

        //        long timeoutSeconds = ((Number) step.getOrDefault("timeoutSeconds",
        // 60)).longValue();

        // Fill up all in request
        String stepId = step.getId();
        String condition = step.getConditions().length > 0 ? step.getConditions()[0] : "";
        String call = step.getCall();
        Object metaData = step.getMetaData();
        //        Map<String, Object> input = step.getInput();
        /// Pass payload to activity
        FRequest requestParser = new FRequest();
        requestParser.setWorkflowId(Workflow.getInfo().getWorkflowId());
        requestParser.setCondition(condition);
        requestParser.setActivityId(stepId);
        requestParser.setCondition(condition);
        requestParser.setTimeStamp(Date.from(Instant.now()));
        requestParser.setInput(workFlowWrapper.getInput());
        requestParser.setCall(call);
        requestParser.setMetaData(metaData);

        ActivityOptions options =
            ActivityOptions.newBuilder()
                .setSummary(type + "::" + requestParser.getCall())
                .setStartToCloseTimeout(
                    Duration.ofSeconds(step.getTimeoutSeconds())) // ✅ dynamic timeout
                .setRetryOptions(retryOptions) // ✅ dynamic retry
                .build();

        if (type.equals(ActivityType.activity)) {
          MicroserviceActivity activity =
              Workflow.newActivityStub(MicroserviceActivity.class, options);
          FunctionResponse result = activity.callMicroservice(requestParser);
          Workflow.getLogger(JsonWorkflowImpl.class)
              .info("Activity {} result: {}", requestParser.getActivityId(), result.getStatus());
        } else {
          FunctionActivity functionActivity =
              Workflow.newActivityStub(FunctionActivity.class, options);
          FunctionResponse result = functionActivity.callFunction(requestParser);
          Workflow.getLogger(JsonWorkflowImpl.class)
              .info("Activity {} result: {}", requestParser.getActivityId(), result.getStatus());
        }

      } else if (type.equals(ActivityType.wait)) {
        handleWait(step);
      }
    }
  }

  @Override
  public void signal(String signalName, Object metaData) {
    SignalModel m = new SignalModel();
    m.setSignalName(signalName);
    m.setReceivedDate(Date.from(Instant.now()));
    m.setMetaData(metaData);
    setSignal(m);
    Workflow.getLogger(getClass()).info("Signal received: {}", m);
  }

  private void handleWait(WorkFlowItem step) {
    WaitFor waitFor = step.getWaitFor();

    if (WaitFor.TIMER.equals(waitFor)) {
      long timeout = ((Number) step.getTimeoutSeconds()).longValue();
      Workflow.sleep(Duration.ofSeconds(timeout));
      Workflow.getLogger(JsonWorkflowImpl.class).info("Waited {} seconds (timer)", timeout);

    } else if (WaitFor.SIGNAL.equals(waitFor)) {
      String condition = step.getConditions().length > 0 ? "." : step.getConditions()[0].toString();
      final Supplier<Boolean> func =
          () -> {
            try {
              String json = mapper.writeValueAsString(signalList);
              System.out.println(json);
              return JsonUtils.evaluate(json, condition); // re-evaluate dynamically
            } catch (Exception e) {
              Workflow.getLogger(getClass()).error("Error evaluating condition", e);
              return false;
            }
          };
      Workflow.await(func);

      Workflow.getLogger(getClass())
          .info("Signal {} satisfied condition: {}", step.getSignalName(), condition);
    }
  }
}
