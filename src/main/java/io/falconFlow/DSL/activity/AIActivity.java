package io.falconFlow.DSL.activity;

import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface AIActivity {

  @ActivityMethod
  FunctionResponse callAI(FRequest req);

}
