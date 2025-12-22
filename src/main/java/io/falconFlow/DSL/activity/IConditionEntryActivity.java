package io.falconFlow.DSL.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface IConditionEntryActivity {

  @ActivityMethod
  Boolean callConditionEntry(String expression, boolean result);
}
