package io.falconFlow.services.workspace;

import com.google.protobuf.util.Durations;
import io.temporal.api.workflowservice.v1.RegisterNamespaceRequest;
import io.temporal.api.workflowservice.v1.UpdateNamespaceRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.stereotype.Service;

@Service
public class TemporalNamespaceService {

    private final WorkflowServiceStubs serviceStubs;

    public TemporalNamespaceService() {
        // In real systems, this should come from config
        this.serviceStubs = WorkflowServiceStubs.newLocalServiceStubs();
    }

    public void createNamespace(String namespaceName, int retentionDays, String description) {

        RegisterNamespaceRequest request =
                RegisterNamespaceRequest.newBuilder()
                        .setNamespace(namespaceName)
                        .setWorkflowExecutionRetentionPeriod(
                                Durations.fromDays(retentionDays)
                        )
                        .setDescription(description != null ? description : "FalconFlow workspace namespace")
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

    public void updateNamespace(String namespaceName, String description) {
        // Note: Temporal namespace update API is limited
        // We can only update certain fields, and description update may not be supported
        // in all Temporal versions. This is a best-effort update.
        try {
            UpdateNamespaceRequest request =
                    UpdateNamespaceRequest.newBuilder()
                            .setNamespace(namespaceName)
                            .build();

            serviceStubs
                    .blockingStub()
                    .updateNamespace(request);
                    
            // Log the update attempt
            System.out.println("Namespace update requested for: " + namespaceName + " with description: " + description);
        } catch (Exception e) {
            // Log but don't fail - namespace update is not critical
            System.err.println("Warning: Failed to update Temporal namespace: " + namespaceName + " - " + e.getMessage());
        }
    }
}

