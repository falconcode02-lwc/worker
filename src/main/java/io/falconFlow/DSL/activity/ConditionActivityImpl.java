package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.Helpers.BeanFetcher;
import io.falconFlow.DSL.Helpers.Global;
import io.falconFlow.DSL.model.ConditionResponse;
import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.interfaces.ICondition;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.spring.boot.ActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl
public class ConditionActivityImpl implements ConditionActivity {

  @Autowired private BeanFetcher serviceFetcher;

  @Override
  public ConditionResponse callCondition(FRequest atRequestParser) {
    try {

      ActivityExecutionContext ctx = Activity.getExecutionContext();
      ActivityInfo info = ctx.getInfo();
      atRequestParser.setWorkflowActivityId(info.getActivityId());
      ICondition condition =
          (ICondition)
              serviceFetcher.getFunctionByName(Global.getBeanName(atRequestParser.getCall()));
      ConditionResponse atResponseParser = condition.invoke(atRequestParser);
      return atResponseParser;

    } catch (Exception e) {

      throw Activity.wrap(e);
      //      ConditionResponse atResponseParser = new ConditionResponse();
      //      atResponseParser.setErrorCode("CON-01");
      //      atResponseParser.setStatus(ConditionStatus.FAILED);
      //      atResponseParser.setMessage(e.getMessage());
      //      return atResponseParser;
    }
  }
}
