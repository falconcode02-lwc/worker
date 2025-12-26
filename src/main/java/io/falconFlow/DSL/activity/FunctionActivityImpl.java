package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.*;
import io.falconFlow.interfaces.IFunction;
import io.falconFlow.services.cache.ICacheService;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@ActivityImpl(taskQueues = "MICROSERVICE_TASK_QUEUE_V2")
public class FunctionActivityImpl implements FunctionActivity {

  @Autowired private BeanFetcher serviceFetcher;
  @Autowired ICacheService cacheService;


  @Override
  public FunctionResponse callFunction(FRequest atRequestParser) {
    try {

      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();

       InputMap im = atRequestParser.getPluginProps();
      if(im.getBool("isAsync", false)){
          ctx.doNotCompleteOnReturn();
          atRequestParser.setAsyncToken(ctx.getTaskToken());
          //cacheService.setCache(info.getWorkflowId(), ctx.getTaskToken(), 3000);
      }

      atRequestParser.setWorkflowActivityId(info.getActivityId());

      IFunction function =
          (IFunction)
              serviceFetcher.getFunctionByName(Global.getBeanName(atRequestParser.getCall()));
      FunctionResponse atResponseParser = function.invoke(atRequestParser);

      if (!atResponseParser.getStatus().equals(FunctionStatus.SUCCESS)) {
        throw Activity.wrap(
            new Exception(atResponseParser.getErrorCode() + " - " + atResponseParser.getMessage()));
      }

      return atResponseParser;

    } catch (Exception e) {
      throw Activity.wrap(e);
    }
  }
}
