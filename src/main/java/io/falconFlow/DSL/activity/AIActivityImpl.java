package io.falconFlow.DSL.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import io.falconFlow.DSL.workflow.model.StateModel;
import io.falconFlow.DSL.model.*;
import io.falconFlow.services.ai.AIService;
import io.falconFlow.model.ai.AgentResponse;
import java.util.Map;

import io.falconFlow.services.isolateservices.StateManagerService;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@ActivityImpl(taskQueues = "MICROSERVICE_TASK_QUEUE_V2")
public class AIActivityImpl implements AIActivity {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Autowired private AIService aiService;
  @Autowired private io.falconFlow.services.secret.SecretManager secretManager;
@Autowired
Handlebars handlebars;
    @Override
  public FunctionResponse callAI(FRequest req) {
    try {
      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();
      req.setWorkflowActivityId(info.getActivityId());

        StateManagerService stateManagerService = new StateManagerService(req.getState());
        Map<String,Object> meta = (Map<String, Object>)req.getMetaData();
        String outputkey =  meta.get("outputstatekey")!= null?  meta.get("outputstatekey").toString() : "";
        String aitools = meta.get("aitools")!= null?  meta.get("aitools").toString() : "";
        String aimemo = meta.get("aimemo") != null? meta.get("aimemo").toString() : "";
        String aipromt = meta.get("aipromt")!= null? meta.get("aipromt").toString() : "";
        String aiagent = meta.get("aiagent")!= null? meta.get("aiagent").toString() : "";



        HashMap<String, Object> map =new HashMap<>();
        map.put("state", stateManagerService.getStateDecrypted().getStateValue());
        map.put("input", req.getInput());
        map.put("userInput", req.getUserInput());
        String output = handlebars.compileInline(aipromt).apply(map);
        System.out.println(output);
        if(aiagent.isEmpty()){
            throw Activity.wrap(new Exception("Ai agent should not be blank"));
        }

        if(aipromt.isEmpty()){
            throw Activity.wrap(new Exception("Ai agent prompt should not be blank"));
        }

//      String call = req.getCall(); // expected format: "ai" or "ai:agentId"
//      String agentId = "default";
//      if (call != null && call.contains(":")) {
//        agentId = call.split(":", 2)[1];
//      }
//
//      // Build a simple message from input and state for now
//      String message;
//      try {
//        HashMap<String, Object> m = new HashMap<>();
//        m.put("input", req.getInput());
//        m.put("state", req.getState());
//        message = mapper.writeValueAsString(m);
//      } catch (JsonProcessingException e) {
//        message = String.valueOf(req.getInput());
//      }
//
//      // If the agent has a configured secret name for provider keys, fetch it and pass it to the AI service
//      try {
//        Map<String, String> cfg = aiService.getAgentConfig(agentId);
//        if (cfg != null) {
//          String secretName = cfg.getOrDefault("ai.keySecretName", cfg.get("openai.secretName"));
//          if (secretName != null && !secretName.isEmpty()) {
//            java.util.Optional<String> secretVal = secretManager.getSecretValue(secretName);
//            if (secretVal.isPresent()) {
//              // attach the key into message metadata so provider adapters can pick it up
//              HashMap<String, Object> m = new HashMap<>();
//              m.put("input", req.getInput());
//              m.put("state", req.getState());
//              m.put("_providerKey", "***masked***"); // do not log real key
//              m.put("_providerKeyRaw", secretVal.get());
//              message = mapper.writeValueAsString(m);
//            }
//          }
//        }
//      } catch (Exception ex) {
//        // continue with message even if secret fetch fails
//      }

      AgentResponse ar = aiService.sendMessage(aiagent, output);
        stateManagerService.set(outputkey, ar.getMessage());
      FunctionResponse fr = new FunctionResponse();
      fr.setState(stateManagerService.get());
      fr.setStatus(FunctionStatus.SUCCESS);
      fr.setMessage(ar.getMessage() + " >>  Prompt was:" + output);
      return fr;
    } catch (Exception e) {
      FunctionResponse fr = new FunctionResponse();
      fr.setStatus(FunctionStatus.FAILED);
      fr.setErrorCode("AI_ACTIVITY_ERROR");
      fr.setMessage(e.getMessage());
      throw Activity.wrap(e);
    }
  }
}
