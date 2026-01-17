package io.falconFlow.configuration;

import io.falconFlow.DSL.activity.ConditionActivityImpl;
import io.falconFlow.DSL.activity.ConditionEntryActivityImpl;
import io.falconFlow.DSL.activity.FunctionActivityImpl;
import io.falconFlow.DSL.activity.PreScheduleActivityImpl;
import io.falconFlow.DSL.workflow.ScheduleWorkflowImpl;
import io.falconFlow.DSL.workflow.WorkFlowV2Impl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class temporalConfig {

    @Value("${spring.workflowTaskQueue}")
    private String taskQueue;

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(WorkFlowV2Impl.class, ScheduleWorkflowImpl.class);


//        worker.registerActivitiesImplementations(
//                applicationContext.getBean(FunctionActivityImpl.class)
//                applicationContext.getBean(PreScheduleActivityImpl.class),
//                applicationContext.getBean(ConditionActivityImpl.class),
//                applicationContext.getBean(FunctionActivityImpl.class),
//                applicationContext.getBean(ConditionEntryActivityImpl.class)
     //   );


        factory.start();
        return factory;
    }

  //  @Bean
  //  @Primary
  //  public WorkflowServiceStubs workflowServiceStubs() {
  //    return WorkflowServiceStubs.newServiceStubs(
  //        WorkflowServiceStubsOptions.newBuilder()
  //            .setTarget("103.120.179.106:7233") // 👈 VPS IP + Temporal gRPC
  //            // .setUsePlaintext(true)              // 👈 no TLS in your Docker setup
  //            .build());
  //  }
  //
  //  @Bean
  //  public WorkflowClient workflowClient(WorkflowServiceStubs service) {
  //    return WorkflowClient.newInstance(service);
  //  }
  //
  //  @Bean
  //  public WorkerFactory workerFactory(WorkflowClient client) {
  //    return WorkerFactory.newInstance(client);
  //  }
}
