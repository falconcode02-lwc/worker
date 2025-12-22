package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.model.ConditionResponse;
import io.falconFlow.DSL.model.FRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ConditionActivity {

  @ActivityMethod
  ConditionResponse callCondition(FRequest step);
}
