package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.ConditionResponse;
import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;

public interface ICondition {

  public ConditionResponse invoke(FRequest conditionRequest);
}
