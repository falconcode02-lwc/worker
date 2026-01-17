package io.falconFlow.DSL.activity;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl
public class ConditionEntryActivityImpl implements IConditionEntryActivity {

  @Override
  public Boolean callConditionEntry(String expression, boolean result) {
    return result;
  }
}
