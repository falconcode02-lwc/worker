package io.falconFlow.interfaces;

import io.falconFlow.DSL.model.*;

public interface IController {

  public CreateResponse invokeCreate(FCreateRequest fRequest);
  public InputResponse invokeInput(FInputRequest fRequest);

}
