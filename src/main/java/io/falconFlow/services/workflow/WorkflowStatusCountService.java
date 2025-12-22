package io.falconFlow.services.workflow;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.EnumMap;
import java.util.Map;

public class WorkflowStatusCountService {

  private final WorkflowServiceStubs service;
  private final String namespace;

  public WorkflowStatusCountService(String namespace) {
    this.service = WorkflowServiceStubs.newLocalServiceStubs();
    this.namespace = namespace;
  }

  public Map<WorkflowExecutionStatus, Integer> getWorkflowStatusCounts() {
    Map<WorkflowExecutionStatus, Integer> statusCounts =
        new EnumMap<>(WorkflowExecutionStatus.class);

    for (WorkflowExecutionStatus status : WorkflowExecutionStatus.values()) {
      if (status == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_UNSPECIFIED) continue;

      // ✅ Correct mapping (Temporal expects specific casing)
      String queryValue =
          switch (status) {
            case WORKFLOW_EXECUTION_STATUS_RUNNING -> "Running";
            case WORKFLOW_EXECUTION_STATUS_COMPLETED -> "Completed";
            case WORKFLOW_EXECUTION_STATUS_FAILED -> "Failed";
            case WORKFLOW_EXECUTION_STATUS_CANCELED -> "Canceled";
            case WORKFLOW_EXECUTION_STATUS_TERMINATED -> "Terminated";
            case WORKFLOW_EXECUTION_STATUS_CONTINUED_AS_NEW -> "ContinuedAsNew";
            case WORKFLOW_EXECUTION_STATUS_TIMED_OUT -> "TimedOut";
            default -> null;
          };

      if (queryValue == null) continue;

      String query = "ExecutionStatus='" + queryValue + "'";
      int count = countByQuery(query);
      statusCounts.put(status, count);
    }

    return statusCounts;
  }

  private int countByQuery(String query) {
    byte[] nextPage = new byte[0];
    int total = 0;

    do {
      ListWorkflowExecutionsRequest request =
          ListWorkflowExecutionsRequest.newBuilder()
              .setNamespace(namespace)
              .setQuery(query)
              .setNextPageToken(com.google.protobuf.ByteString.copyFrom(nextPage))
              .build();

      ListWorkflowExecutionsResponse response =
          service.blockingStub().listWorkflowExecutions(request);
      total += response.getExecutionsCount();
      nextPage = response.getNextPageToken().toByteArray();

    } while (nextPage.length > 0);

    return total;
  }

  public static void main(String[] args) {
    WorkflowStatusCountService metrics = new WorkflowStatusCountService("default");
    metrics
        .getWorkflowStatusCounts()
        .forEach(
            (status, count) ->
                System.out.println(
                    status.name().replace("WORKFLOW_EXECUTION_STATUS_", "") + " → " + count));
  }
}
