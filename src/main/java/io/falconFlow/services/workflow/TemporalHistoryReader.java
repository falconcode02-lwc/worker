package io.falconFlow.services.workflow;

import com.google.protobuf.ByteString;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.HistoryEventFilterType;
import io.temporal.api.failure.v1.Failure;
import io.temporal.api.history.v1.History;
import io.temporal.api.workflow.v1.PendingActivityInfo;
import io.temporal.api.workflowservice.v1.*;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemporalHistoryReader {

  private final WorkflowServiceStubs service;

  public TemporalHistoryReader(WorkflowServiceStubs service) {
    this.service = service;
  }

  /**
   * Print full history for the given workflowId (and optional runId). Handles pagination
   * (nextPageToken) automatically.
   */
  public History getFullHistory(String namespace, String workflowId, String runId)
      throws Exception {



    ByteString nextPageToken = ByteString.EMPTY;
    History history;

    // JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace();

//    workflowId = "5703";
//    runId = "019a827f-7e3e-7098-9954-5b80e7da8e58";


      do {
      GetWorkflowExecutionHistoryReverseRequest.Builder reqBuilder =
              GetWorkflowExecutionHistoryReverseRequest.newBuilder()
              .setNamespace(namespace)
                  .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build());

      if (runId != null && !runId.isEmpty()) {
        reqBuilder.getExecutionBuilder().setRunId(runId);
      }
      if (!nextPageToken.isEmpty()) {
        reqBuilder.setNextPageToken(nextPageToken);
      }
          // GetWorkflowExecutionHistoryRequest
      GetWorkflowExecutionHistoryReverseResponse response =
          service.blockingStub().getWorkflowExecutionHistoryReverse(reqBuilder.build());

      history = response.getHistory();

      //  System.out.println(jsonPrinter.print(history));
      ;
      //      for (HistoryEvent event : history.getEventsList()) {
      //        // simple human-readable print: eventId + eventType
      //        System.out.println("EventId=" + event.getEventId() + " type=" +
      // event.getEventType());
      //
      //        // For full JSON of the protobuf event (very verbose but useful for inspection)
      //        String json = jsonPrinter.print(event);
      //        System.out.println(json);
      //      }

      nextPageToken = response.getNextPageToken();
    } while (nextPageToken != null && !nextPageToken.isEmpty());

    return history;
  }

  //  public static void main(String[] args) throws Exception {
  //    String namespace = "default";
  //    String workflowId = "30";
  //    String runId = ""; // optional, can be empty to use latest run
  //
  //    TemporalHistoryReader reader = new TemporalHistoryReader();
  //    reader.getFullHistory(namespace, workflowId, runId);
  //  }
}
