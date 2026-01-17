package io.falconFlow.services.workspace;

import io.falconFlow.DSL.workflow.ScheduleWorkflowImpl;
import io.falconFlow.DSL.workflow.WorkFlowV2Impl;
import io.falconFlow.entity.WorkSpaceEntity;
import io.falconFlow.repository.WorkspaceRepository;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicWorkerManager {

    private final WorkflowServiceStubs serviceStubs;
    private final String taskQueue;
    private final Map<String, WorkerFactory> workerFactories = new ConcurrentHashMap<>();

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private io.falconFlow.DSL.activity.FunctionActivityImpl functionActivity;

    @Autowired
    private io.falconFlow.DSL.activity.ConditionActivityImpl conditionActivity;

    @Autowired
    private io.falconFlow.DSL.activity.AIActivityImpl aiActivity;

    @Autowired
    private io.falconFlow.DSL.activity.PreScheduleActivityImpl preScheduleActivity;

    @Autowired
    private io.falconFlow.DSL.activity.ConditionEntryActivityImpl conditionEntryActivity;

    public DynamicWorkerManager(@Value("${spring.workflowTaskQueue}") String taskQueue) {
        this.serviceStubs = WorkflowServiceStubs.newLocalServiceStubs();
        this.taskQueue = taskQueue;
    }

    /**
     * Register a worker for a specific namespace
     */
    public void registerWorkerForNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            namespace = "default";
        }

        if (workerFactories.containsKey(namespace)) {
            System.out.println("⚠️  Worker already registered for namespace: " + namespace);
            return; // Already registered
        }

        try {
            WorkflowClient client = WorkflowClient.newInstance(
                    serviceStubs,
                    WorkflowClientOptions.newBuilder()
                            .setNamespace(namespace)
                            .build()
            );

            // Use namespace as task queue name for workspace-specific isolation
            String taskQueueName = (namespace != null && !namespace.equals("default")) ? namespace : this.taskQueue;
            
            WorkerFactory factory = WorkerFactory.newInstance(client);
            Worker worker = factory.newWorker(taskQueueName);

            // Register workflow implementations
            worker.registerWorkflowImplementationTypes(
                    WorkFlowV2Impl.class,
                    ScheduleWorkflowImpl.class
            );

            // Register activity implementations
            worker.registerActivitiesImplementations(
                    functionActivity,
                    conditionActivity,
                    aiActivity,
                    preScheduleActivity,
                    conditionEntryActivity
            );

            factory.start();
            workerFactories.put(namespace, factory);

            System.out.println("✅ Worker registered for namespace: " + namespace + " on task queue: " + taskQueueName);
        } catch (Exception e) {
            System.err.println("❌ Failed to register worker for namespace: " + namespace + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register workers for all existing workspaces on startup
     */
    @PostConstruct
    public void registerExistingWorkspaces() {
        System.out.println("🚀 Starting dynamic worker registration...");

        // Register default namespace
        registerWorkerForNamespace("default");

        // Register all workspace namespaces
        try {
            List<WorkSpaceEntity> workspaces = workspaceRepository.findAll();
            System.out.println("📋 Found " + workspaces.size() + " workspaces to register");

            for (WorkSpaceEntity workspace : workspaces) {
                if (workspace.getTemporalNamespace() != null && !workspace.getTemporalNamespace().isEmpty()) {
                    registerWorkerForNamespace(workspace.getTemporalNamespace());
                }
            }

            System.out.println("✅ Registered workers for " + workerFactories.size() + " namespaces");
        } catch (Exception e) {
            System.err.println("❌ Error during worker registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shutdown all workers
     */
    @PreDestroy
    public void shutdown() {
        System.out.println("🛑 Shutting down all workers...");
        for (Map.Entry<String, WorkerFactory> entry : workerFactories.entrySet()) {
            try {
                entry.getValue().shutdown();
                System.out.println("✅ Shutdown worker for namespace: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("❌ Error shutting down worker for namespace: " + entry.getKey() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Get the number of registered workers
     */
    public int getRegisteredWorkerCount() {
        return workerFactories.size();
    }
}
