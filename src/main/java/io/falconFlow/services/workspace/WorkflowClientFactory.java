package io.falconFlow.services.workspace;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.stereotype.Service;

@Service
public class WorkflowClientFactory {

    private final WorkflowServiceStubs serviceStubs;

    public WorkflowClientFactory() {
        this.serviceStubs = WorkflowServiceStubs.newLocalServiceStubs();
    }

    /**
     * Creates a WorkflowClient for the specified namespace.
     * Falls back to "default" if namespace is null or empty.
     *
     * @param namespace The Temporal namespace to use
     * @return WorkflowClient configured for the specified namespace
     */
    public WorkflowClient createClient(String namespace) {
        String targetNamespace = (namespace != null && !namespace.isEmpty())
                ? namespace
                : "default";

        return WorkflowClient.newInstance(
                serviceStubs,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(targetNamespace)
                        .build()
        );
    }

    /**
     * Gets the underlying service stubs (for backward compatibility)
     */
    public WorkflowServiceStubs getServiceStubs() {
        return serviceStubs;
    }
}
