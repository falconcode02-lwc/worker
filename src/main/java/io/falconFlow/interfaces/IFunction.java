package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.FRequest;
import io.falconFlow.DSL.model.FunctionResponse;

public interface IFunction {

  public FunctionResponse invoke(FRequest fRequest);
}
