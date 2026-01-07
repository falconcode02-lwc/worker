package io.falconFlow.services.workspace;

import com.google.protobuf.util.Durations;
import io.temporal.api.workflowservice.v1.RegisterNamespaceRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.stereotype.Service;

@Service
public class TemporalNamespaceService {

    private final WorkflowServiceStubs serviceStubs;

    public TemporalNamespaceService() {
        // In real systems, this should come from config
        this.serviceStubs = WorkflowServiceStubs.newLocalServiceStubs();
    }

    public void createNamespace(String namespaceName, int retentionDays) {

        RegisterNamespaceRequest request =
                RegisterNamespaceRequest.newBuilder()
                        .setNamespace(namespaceName)
                        .setWorkflowExecutionRetentionPeriod(
                                Durations.fromDays(retentionDays)
                        )
                        .setDescription("FalconFlow workspace namespace")
                        .build();

        try {
            serviceStubs
                    .blockingStub()
                    .registerNamespace(request);
        } catch (Exception e) {
            // 🚨 MUST FAIL HARD
            throw new IllegalStateException(
                    "Failed to create Temporal namespace: " + namespaceName,
                    e
            );
        }
    }
}


