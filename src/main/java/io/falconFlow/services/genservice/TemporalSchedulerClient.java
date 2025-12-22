package io.falconFlow.services.genservice;

import io.falconFlow.DSL.model.FCreateRequest;
import io.falconFlow.DSL.model.WorkflowResponseWrapper;
import io.falconFlow.DSL.workflow.IScheduleWorkflow;
import io.falconFlow.DSL.workflow.JsonWorkflow;
import io.falconFlow.DSL.workflow.ScheduleWorkflowImpl;
import io.falconFlow.model.ScheduleRequest;
import io.temporal.client.WorkflowException;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TemporalSchedulerClient {

    @Value("${spring.workflowTaskQueue}")
    private String taskQueue;


    @Autowired
    WorkflowsService workflowsService;


  private final WorkflowServiceStubs service;
  // private final WorkflowClient workflowClient;
  private final ScheduleClient scheduleClient;

  public TemporalSchedulerClient() {
    this.service = WorkflowServiceStubs.newLocalServiceStubs();
    // this.workflowClient = WorkflowClient.newInstance(service);
    this.scheduleClient = ScheduleClient.newInstance(service);
  }

  public String createSchedule(ScheduleRequest request) {
    try {

      // 1️⃣ Workflow action to start
      ScheduleActionStartWorkflow action =
          ScheduleActionStartWorkflow.newBuilder()
              .setWorkflowType(IScheduleWorkflow.class)
              .setOptions(
                  WorkflowOptions.newBuilder()
                          .setWorkflowId(request.getScheduleId().toString())
                      .setTaskQueue(taskQueue)
                      .build())
              .setArguments(request.getRequest())
              .build();

      // 2️⃣ Build the spec (either cron or interval)
      ScheduleSpec.Builder specBuilder = ScheduleSpec.newBuilder();

      if (request.getCron() != null && !request.getCron().isBlank()) {
        List<String> ls = new ArrayList<>();
        ls.add(request.getCron());
        specBuilder.setCronExpressions(ls);
      }

      ScheduleSpec spec = specBuilder.build();

      // 3️⃣ Build the schedule
      Schedule schedule = Schedule.newBuilder().setAction(action).setSpec(spec).build();

      // 4️⃣ Create schedule in Temporal
      String scheduleId = request.getScheduleId().toString();
      scheduleClient.createSchedule(scheduleId, schedule, ScheduleOptions.newBuilder().build());

      // Optional: pause immediately if not enabled
      if (!request.isEnable()) {
        scheduleClient.getHandle(scheduleId).pause("Disabled by user");
      }

      return scheduleId;

    } catch (WorkflowException e) {
      throw new RuntimeException("Failed to create Temporal schedule: " + e.getMessage(), e);
    }
  }

  public void deleteSchedule(String scheduleId) {
    try {
      scheduleClient.getHandle(scheduleId).delete();
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete schedule " + scheduleId, e);
    }
  }

  public void toggleSchedule(String scheduleId, boolean isEnabled) {
    ScheduleHandle handle = scheduleClient.getHandle(scheduleId);

    if (isEnabled) handle.unpause();
    else handle.pause("Paused via API");
  }

  public void  updateCron(ScheduleRequest request) {

      ScheduleHandle handle = scheduleClient.getHandle(request.getScheduleId());
      handle.update(input -> {
          ScheduleDescription current = input.getDescription();
          // Build new schedule object
         // Schedule schedule1 = Schedule.newBuilder(current.getSchedule()).build();
          ScheduleSpec.Builder specBuilder = ScheduleSpec.newBuilder();

          if (request.getCron() != null && !request.getCron().isBlank()) {
              List<String> ls = new ArrayList<>();
              ls.add(request.getCron());
              specBuilder.setCronExpressions(ls);
          }
          ScheduleSpec spec = specBuilder.build();
          // 3️⃣ Build the schedule

          ScheduleState state = ScheduleState.newBuilder().setNote("Action from falcon").setPaused(!request.isEnable()).build();
          Schedule schedule = Schedule.newBuilder()
                  .setAction(current.getSchedule().getAction())
                  .setSpec(spec)
                  .setState(state).build();
          // ✅ MUST wrap inside ScheduleUpdate
          return  new ScheduleUpdate(schedule);
      });
  }

}
